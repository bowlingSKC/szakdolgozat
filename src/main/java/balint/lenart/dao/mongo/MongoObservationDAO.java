package balint.lenart.dao.mongo;

import balint.lenart.model.Device;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;

public class MongoObservationDAO {

    private final MongoCollection observationCollection;

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
}
