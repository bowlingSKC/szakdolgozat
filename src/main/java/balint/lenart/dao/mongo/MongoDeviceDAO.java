package balint.lenart.dao.mongo;

import balint.lenart.model.Device;
import balint.lenart.model.User;
import com.google.common.collect.Lists;
import com.mongodb.Block;
import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public class MongoDeviceDAO {

    private final MongoCollection deviceCollection;

    public MongoDeviceDAO() {
        this.deviceCollection = MongoConnection.getInstance().getDatabase().getCollection("Device");
    }

    public Long count() {
        return deviceCollection.count();
    }

    public List<Device> getAllDevice() {
        List<Device> devices = Lists.newArrayList();
        deviceCollection.find().forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                devices.add( createDeviceFromDocument(document) );
            }
        });
        return devices;
    }

    public List<Device> getDeviceByUser(User user) {
        List<Device> devices = Lists.newArrayList();
        deviceCollection.find(new Document("owner", new DBRef("User", new ObjectId(user.getMongoId())))).forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                devices.add( createDeviceFromDocument(document) );
            }
        });
        return devices;
    }

    private Device createDeviceFromDocument(Document document) {
        return new Device(
                document.getObjectId("_id").toString(),
                document.getString("name"),
                document.getString("description"),
                document.getString("hwId"),
                0                                       // FIXME: 2016.09.10. Change constant code
        );
    }

}
