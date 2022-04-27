package com.lordofthejars.nosqlunit.mongodb;

import com.lordofthejars.nosqlunit.core.PropertyGetter;
import com.lordofthejars.nosqlunit.util.SpringUtils;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.context.ApplicationContext;

public class SpringMongoDbExtension extends MongoDbExtension {

    private PropertyGetter<ApplicationContext> propertyGetter = new PropertyGetter<ApplicationContext>();

    private MongoDbConfiguration mongoDbConfiguration;

    public SpringMongoDbExtension(MongoDbConfiguration mongoDbConfiguration) {
        super(mongoDbConfiguration);
        this.mongoDbConfiguration = mongoDbConfiguration;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        this.databaseOperation = new MongoOperation(definedMongo(context.getTestInstance()), this.mongoDbConfiguration);
        super.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        this.databaseOperation = new MongoOperation(definedMongo(context.getTestInstance()), this.mongoDbConfiguration);
        super.afterEach(context);
    }

    @Override
	public void close() {
		// DO NOT CLOSE the connection (Spring will do it when destroying the context)
	}
    
    private MongoClient definedMongo(Object testObject) {
        ApplicationContext applicationContext = propertyGetter.propertyByType(testObject, ApplicationContext.class);

        MongoClient mongo = SpringUtils.getBeanOfType(applicationContext, MongoClient.class);

        if (mongo == null) {
            throw new IllegalArgumentException(
                    "At least one Mongo instance should be defined into Spring Application Context.");
        }
        return mongo;
    }

}
