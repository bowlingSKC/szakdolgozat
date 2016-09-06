package balint.lenart.dao.mongo;

import balint.lenart.Configuration;
import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.BooleanUtils;

public class MongoConnection {

    private static MongoConnection instance;

    private final MongoClient client;
    private final MongoDatabase database;

    protected MongoConnection() {
        MongoClient mongoClient;
        if(BooleanUtils.toBoolean(Configuration.get("mongo.connection.usepassword"))) {
            MongoCredential credential = MongoCredential.createCredential(
                    Configuration.get("mongo.connection.username"),
                    Configuration.get("mongo.connection.database"),
                    Configuration.get("mongo.connection.password").toCharArray()
            );
            ServerAddress serverAddress = new ServerAddress(
                    Configuration.get("mongo.connection.host"),
                    Integer.valueOf(Configuration.get("mongo.connection.port"))
            );
            mongoClient = new MongoClient(serverAddress, Lists.newArrayList(credential));
        } else {
            mongoClient = new MongoClient(
                    Configuration.get("mongo.connection.host"),
                    Integer.valueOf(Configuration.get("mongo.connection.port"))
            );
        }
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
