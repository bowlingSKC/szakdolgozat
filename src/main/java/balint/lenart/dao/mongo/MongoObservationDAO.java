package balint.lenart.dao.mongo;

import balint.lenart.model.Device;
import balint.lenart.model.observations.Anamnesis;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
import balint.lenart.utils.ObservationUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.DBRef;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoObservationDAO {

    private final MongoCollection<Document> observationCollection;

    public MongoObservationDAO() {
        this.observationCollection = MongoConnection.getInstance().getDatabase().getCollection("Observation");
    }

    public Long count() {
        return observationCollection.count();
    }

    public long countByTypes(ObservationType observationType) {
        Document filter = new Document("type", observationType.getClassName());
        if( ObservationType.DIETLOG_ANAM_RECORD.equals(observationType) ) {
            return countAnamnesisDocuments(filter);
        }
        return observationCollection.count(filter);
    }

    private long countAnamnesisDocuments(Document filter) {
        DistinctIterable<ObjectId> ids = observationCollection.distinct("device.$id", filter, ObjectId.class);
        long count = 0;
        for (ObjectId id : ids) {
            count++;
        }
        return count;
    }

    public Date getLatestObservationDateByDevice(Device device) {
        Document filter = new Document("device", new DBRef("Device", new ObjectId(device.getMongoId())));
        Document sort = new Document("timestampIn", 1);
        FindIterable<Document> documents = observationCollection.find(filter).sort(sort).limit(1);
        for(Document document : documents) {
            return document.getDate("timestampIn");
        }
        return null;
    }

    public List<Observation> getObservationsByDeviceAndType(Device device, ObservationType type) {
        Document filter = new Document()
                .append("device", new DBRef("Device", new ObjectId(device.getMongoId())))
                .append("type", type.getClassName());
        FindIterable<Document> documents = observationCollection.find(filter);
        if( ObservationType.MEAL_LOG_RECORD.equals(type) ) {
            return ObservationUtils.groupMealItems(documents);
        } else if( ObservationType.DIETLOG_ANAM_RECORD.equals(type) ) {
            Anamnesis latestAnamnesis = getLatestAnamnesisByDevice(device);
            return latestAnamnesis == null ? Lists.newArrayList() : Lists.newArrayList(latestAnamnesis);
        } else {
            List<Observation> observations = Lists.newArrayList();
            for(Document doc : documents) {
                observations.add(ObservationUtils.fillByDocument(doc));
            }
            return observations;
        }
    }

    public Anamnesis getLatestAnamnesisByDevice(Device device) {
        Document filter = new Document("type", ObservationType.DIETLOG_ANAM_RECORD.getClassName())
                .append("device", new DBRef("Device", new ObjectId(device.getMongoId())));
        Document sort = new Document("timestampIn", -1);
        FindIterable<Document> documents = observationCollection.find(filter).sort(sort).limit(1);
        for( Document document : documents ) {
            Anamnesis result = (Anamnesis) ObservationUtils.fillByDocument(document);
            Document latestLabRecord = getLatestLabRecordDocumentByDevice(device);
            if( latestLabRecord != null ) {
                result = ObservationUtils.fillAnamnesisWithLabRecord(result, latestLabRecord);
            }
            return result;
        }
        return null;
    }

    public Document getLatestLabRecordDocumentByDevice(Device device) {
        Document filter = new Document("type", ObservationType.LAB_RECORD.getClassName())
                .append("device", new DBRef("Device", new ObjectId(device.getMongoId())));
        Document sort = new Document("timestampIn", -1);
        FindIterable<Document> documents = observationCollection.find(filter).sort(sort).limit(1);
        for(Document document : documents) {
            return document;
        }
        return null;
    }


}
