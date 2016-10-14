package balint.lenart.dao.mongo;

import balint.lenart.model.Device;
import balint.lenart.model.observations.Anamnesis;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
import balint.lenart.services.Migrator;
import balint.lenart.utils.ObservationUtils;
import com.google.common.collect.Lists;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class MongoObservationDAO {

    private final MongoCollection<Document> observationCollection;

    public MongoObservationDAO() {
        this.observationCollection = MongoConnection.getInstance().getDatabase().getCollection("Observation");
    }

    public Long count() {
        return observationCollection.count();
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

    public List<Observation> getObservationsByDevice(Device device) {
        Document filter = new Document("device", new DBRef("Device", new ObjectId(device.getMongoId())));
        List<Observation> observations = Lists.newArrayList();
        FindIterable<Document> documents = observationCollection.find(filter);
        for (Document document : documents) {
            if(Migrator.PASSED_TYPES.contains(ObservationUtils.getObservationTypeByDocument(document))) {
                observations.add(ObservationUtils.fillByDocument(document));
            }
        }
        observations.forEach(observation -> observation.setSourceDevice(device));
        return observations;
    }

    public Anamnesis getAnamnesisByDevice(Device device) {
        Anamnesis result = null;
        Document filter = new Document("device", new DBRef("Device", new ObjectId(device.getMongoId())));
        FindIterable<Document> documents = observationCollection.find(filter);
        for(Document doc : documents) {
            result = (Anamnesis) ObservationUtils.fillByDocument(doc);
        }
        return result;
    }

    public List<Observation> getObservationsByDeviceAndType(Device device, ObservationType type) {
        Document filter = new Document()
                .append("device", new DBRef("Device", new ObjectId(device.getMongoId())))
                .append("type", type.getClassName());
        FindIterable<Document> documents = observationCollection.find(filter);
        List<Observation> observations = Lists.newArrayList();
        for(Document doc : documents) {
            observations.add(ObservationUtils.fillByDocument(doc));
        }
        return observations;
    }
}
