package com.lordofthejars.nosqlunit.mongodb;

import com.lordofthejars.nosqlunit.annotation.CustomComparisonStrategy;
import com.lordofthejars.nosqlunit.annotation.IgnorePropertyValue;
import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.lordofthejars.nosqlunit.mongodb.InMemoryMongoDb.InMemoryMongoRuleBuilder.newInMemoryMongoDbRule;
import static com.lordofthejars.nosqlunit.mongodb.MongoDbExtension.MongoDbExtensionBuilder.newMongoDbExtension;

@CustomComparisonStrategy(comparisonStrategy = MongoFlexibleComparisonStrategy.class)
public class MongoFlexibleComparisonStrategyJUnit5Test {

    @RegisterExtension
    static InMemoryMongoDb.InMemoryMongoDbExtension mongoDbExtension = new InMemoryMongoDb.InMemoryMongoDbExtension(newInMemoryMongoDbRule().build());

    @RegisterExtension
    MongoDbExtension mongoDbRule = newMongoDbExtension().defaultEmbeddedMongoDb("test");

    @Test
    @UsingDataSet(locations = "MongoFlexibleComparisonStrategyTest#thatShowWarnings.json")
    @ShouldMatchDataSet(location = "MongoFlexibleComparisonStrategyTest#thatShowWarnings-expected.json")
    @IgnorePropertyValue(properties = {"2", "collection.3"})
    public void shouldIgnorePropertiesInFlexibleStrategy() {
    }
}
