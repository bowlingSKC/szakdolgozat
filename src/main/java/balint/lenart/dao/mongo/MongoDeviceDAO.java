package balint.lenart.dao.mongo;

import com.mongodb.client.MongoCollection;

public class MongoDeviceDAO {

    private final MongoCollection deviceCollection;

    public MongoDeviceDAO() {
        this.deviceCollection = MongoConnection.getInstance().getDatabase().getCollection("Device");
    }

    public Long count() {
        return deviceCollection.count();
    }

}
