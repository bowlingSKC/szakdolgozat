package balint.lenart.dao.mongo;

import balint.lenart.Configuration;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    private static MongoConnection instance;

    private final MongoClient client;
    private final MongoDatabase database;

    protected MongoConnection() {
        MongoClient mongoClient = new MongoClient(
                Configuration.get("mongo.connection.host"),
                Integer.valueOf(Configuration.get("mongo.connection.port"))
        );
        client = mongoClient;
        database = mongoClient.getDatabase(Configuration.get("mongo.connection.database"));
    }

    public static MongoConnection getInstance() {
        if( instance == null ) {
            instance = new MongoConnection();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        client.close();
    }
}
