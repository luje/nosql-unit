package com.lordofthejars.nosqlunit.mongodb.integration;

import com.lordofthejars.nosqlunit.mongodb.MongoDbCommands;
import com.lordofthejars.nosqlunit.mongodb.shard.ShardedManagedMongoDb;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import static com.lordofthejars.nosqlunit.mongodb.ManagedMongoDbLifecycleManagerBuilder.newManagedMongoDbLifecycle;
import static com.lordofthejars.nosqlunit.mongodb.replicaset.ReplicaSetBuilder.replicaSet;
import static com.lordofthejars.nosqlunit.mongodb.shard.ManagedMongosLifecycleManagerBuilder.newManagedMongosLifecycle;
import static com.lordofthejars.nosqlunit.mongodb.shard.ShardedGroupBuilder.shardedGroup;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WhenReplicaSetShardingIsRequired {

	static {
		System.setProperty("MONGO_HOME", "d:\\opt\\mongo");
	}

	@ClassRule
	public static ShardedManagedMongoDb shardedManagedMongoDb = shardedGroup()
																	.replicaSet(replicaSet("rs-test-1")
																		.eligible(
																					newManagedMongoDbLifecycle().port(27007).dbRelativePath("rs-0").logRelativePath("log-0").get()
																				 )
																	  .get())
																	 .replicaSet(replicaSet("rs-test-2")
																		.eligible(
																					newManagedMongoDbLifecycle().port(27009).dbRelativePath("rs-0").logRelativePath("log-0").get()
																				 )
																	  .get())
																	.config(newManagedMongoDbLifecycle().port(27020).dbRelativePath("rs-3").logRelativePath("log-3").get())
																	.mongos(newManagedMongosLifecycle().configServer(27020).get())
																	.get();
	
	@AfterClass
	public static void tearDown() {
		System.clearProperty("MONGO_HOME");
	}
	
	@Test
	public void sharded_replica_set_should_be_started() throws UnknownHostException {

		MongoClient mongoClient = MongoClients.create(MongoClientSettings
				.builder()
				.applyToClusterSettings(b -> b.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
				.build());

		Document listShards = MongoDbCommands.listShards(mongoClient);
		
		assertThat((String)listShards.get("serverUsed"), is("localhost/127.0.0.1:27017"));
		BasicDBList shards = (BasicDBList) listShards.get("shards");

        //selectFirst(shards, having(on(DBObject.class).get("_id"), is("rs-test-2")))
		DBObject replicaSet1 = shards.stream().map(it -> (DBObject) it).filter(it -> Objects.equals(it.get("_id"), "rs-test-2")).findFirst().get();
		
		assertThat(replicaSet1, is(createShardDbObject("rs-test-2", "rs-test-2/localhost:27009")));

        //selectFirst(shards, having(on(DBObject.class).get("_id"), is("rs-test-1")));
		DBObject replicaSet2 = shards.stream().map(it -> (DBObject) it).filter(it -> Objects.equals(it.get("_id"), "rs-test-1")).findFirst().get();
		
		assertThat(replicaSet2, is(createShardDbObject("rs-test-1", "rs-test-1/localhost:27007")));
		
	}
	
	private DBObject createShardDbObject(String id, String host) {
		BasicDBObjectBuilder basicDBObjectBuilder = new BasicDBObjectBuilder();
		return basicDBObjectBuilder.append("_id", id).append("host", host).get();
	}
	
}
