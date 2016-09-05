package balint.lenart.dao.mongo;

import com.mongodb.client.MongoCollection;

public class MongoObservationDAO {

    private final MongoCollection observationCollection;

    public MongoObservationDAO() {
        this.observationCollection = MongoConnection.getInstance().getDatabase().getCollection("Observation");
    }

    public Long count() {
        return observationCollection.count();
    }

}
