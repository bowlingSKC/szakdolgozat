package balint.lenart.dao.mongo;

import com.mongodb.client.MongoCollection;

public class MongoUserDAO extends MongoConnection {

    private final MongoCollection userCollection;

    public MongoUserDAO() {
        userCollection = MongoConnection.getInstance().getDatabase().getCollection("User");
    }

    public Long count() {
        return userCollection.count();
    }

}
