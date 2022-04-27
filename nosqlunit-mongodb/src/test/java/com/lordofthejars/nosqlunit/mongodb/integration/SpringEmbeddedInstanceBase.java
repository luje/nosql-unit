package com.lordofthejars.nosqlunit.mongodb.integration;

import com.lordofthejars.nosqlunit.core.DatabaseOperation;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public abstract class SpringEmbeddedInstanceBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoClient mongo;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

    protected void validateMongoConnection() {
        DatabaseOperation<MongoClient> databaseOperation = mongoDbRule.getDatabaseOperation();
        MongoClient connectionManager = databaseOperation.connectionManager();

        assertThat(connectionManager, is(mongo));
    }

    @Configuration
    public static class EmbeddedMongoConfiguration {

        @Bean
        public MongoClient mongo() {
            final MongoServer server = new MongoServer(new MemoryBackend());

            final InetSocketAddress inetSocketAddress = server.bind();

            MongoClientSettings clientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://" + new ServerAddress(inetSocketAddress)))
                    .build();

            return MongoClients.create(clientSettings);
        }
    }
}
