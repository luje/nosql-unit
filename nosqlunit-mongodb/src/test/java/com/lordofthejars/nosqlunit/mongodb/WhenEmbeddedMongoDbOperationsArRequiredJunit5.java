package com.lordofthejars.nosqlunit.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;

import static com.lordofthejars.nosqlunit.mongodb.InMemoryMongoDb.InMemoryMongoRuleBuilder.newInMemoryMongoDbRule;
import static com.lordofthejars.nosqlunit.mongodb.InMemoryMongoDbConfigurationBuilder.inMemoryMongoDb;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class WhenEmbeddedMongoDbOperationsArRequiredJunit5 {

    private static final String DATA = "" +
            "{" +
            "\"collection1\": " +
            "	[" +
            "		{\"id\":1,\"code\":\"JSON dataset\",}" +
            "	]" +
            "}";

    @RegisterExtension
    static InMemoryMongoDb.InMemoryMongoDbExtension mongoDbExtension = new InMemoryMongoDb.InMemoryMongoDbExtension(newInMemoryMongoDbRule().build());

    @AfterEach
    public void tearDown() {
        MongoClient defaultEmbeddedInstance = EmbeddedMongoInstancesFactory.getInstance().getDefaultEmbeddedInstance();
        defaultEmbeddedInstance.getDatabase("test").getCollection("collection1").drop();
    }

    @Test
    public void data_should_be_inserted_into_mongodb() {

        MongoOperation mongoOperation = new MongoOperation(inMemoryMongoDb().databaseName("test").build());
        mongoOperation.insert(new ByteArrayInputStream(DATA.getBytes()));

        MongoClient mongo = mongoOperation.connectionManager();
        MongoCollection<Document> collection = mongo.getDatabase("test").getCollection("collection1");
        Document object = collection.find().first();

        assertThat((Integer) object.get("id"), is(new Integer(1)));
        assertThat((String) object.get("code"), is("JSON dataset"));
    }

    @Test
    public void data_should_be_removed_from_mongo() {

        MongoOperation mongoOperation = new MongoOperation(inMemoryMongoDb().databaseName("test").build());
        mongoOperation.insert(new ByteArrayInputStream(DATA.getBytes()));
        mongoOperation.deleteAll();

        MongoClient mongo = mongoOperation.connectionManager();
        MongoCollection<Document> collection = mongo.getDatabase("test").getCollection("collection1");
        Document object = collection.find().first();

        assertThat(object, is(nullValue()));
    }

    @Test
    public void data_should_be_compared_between_expected_and_current_data() {

        MongoOperation mongoOperation = new MongoOperation(inMemoryMongoDb().databaseName("test").build());
        mongoOperation.insert(new ByteArrayInputStream(DATA.getBytes()));

        boolean result = mongoOperation.databaseIs(new ByteArrayInputStream(DATA.getBytes()));

        assertThat(result, is(true));
    }


}
