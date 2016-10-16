package balint.lenart.dao.mongo;

import balint.lenart.model.Device;
import balint.lenart.model.observations.Observation;
import balint.lenart.model.observations.ObservationType;
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

    public List<Observation> getObservationsByDeviceAndType(Device device, ObservationType type) {
        Document filter = new Document()
                .append("device", new DBRef("Device", new ObjectId(device.getMongoId())))
                .append("type", type.getClassName());
        FindIterable<Document> documents = observationCollection.find(filter);
        if( ObservationType.MEAL_LOG_RECORD.equals(type) ) {
            return ObservationUtils.groupMealItems(documents);
        } else {
            List<Observation> observations = Lists.newArrayList();
            for(Document doc : documents) {
                observations.add(ObservationUtils.fillByDocument(doc));
            }
            return observations;
        }
    }
}
