package balint.lenart.dao.mongo;

import balint.lenart.model.User;
import com.google.common.collect.Lists;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;

public class MongoUserDAO extends MongoConnection {

    private final MongoCollection userCollection;

    public MongoUserDAO() {
        userCollection = MongoConnection.getInstance().getDatabase().getCollection("User");
    }

    public Long count() {
        return userCollection.count();
    }

    public List<User> getAllUser() {
        List<User> users = Lists.newArrayList();
        userCollection.find().forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                users.add(new User(
                        document.getObjectId("_id").toString(),
                        document.getBoolean("active"),
                        document.getString("comment"),
                        document.getString("email"),
                        document.getString("fullname"),
                        document.getString("password"),
                        document.getString("type")
                ));
            }
        });
        return users;
    }
}
