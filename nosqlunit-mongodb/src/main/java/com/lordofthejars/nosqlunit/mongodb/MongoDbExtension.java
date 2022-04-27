package com.lordofthejars.nosqlunit.mongodb;

import com.lordofthejars.nosqlunit.core.AbstractNoSqlTestExtension;
import com.lordofthejars.nosqlunit.core.DatabaseOperation;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;

import static com.lordofthejars.nosqlunit.mongodb.InMemoryMongoDbConfigurationBuilder.inMemoryMongoDb;
import static com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder.mongoDb;


public class MongoDbExtension extends AbstractNoSqlTestExtension {

	private static final String EXTENSION = "json";

	protected DatabaseOperation<MongoClient> databaseOperation;

	public static class MongoDbExtensionBuilder {

		private MongoDbConfiguration mongoDbConfiguration;

		private MongoDbExtensionBuilder() {
		}

		public static MongoDbExtensionBuilder newMongoDbExtension() {
			return new MongoDbExtensionBuilder();
		}

		public MongoDbExtensionBuilder configure(MongoDbConfiguration mongoDbConfiguration) {
			this.mongoDbConfiguration = mongoDbConfiguration;
			return this;
		}

		public MongoDbExtension defaultEmbeddedMongoDb(String databaseName) {
			return new MongoDbExtension(inMemoryMongoDb().databaseName(databaseName).build());
		}

		public MongoDbExtension defaultManagedMongoDb(String databaseName) {
			return new MongoDbExtension(mongoDb().databaseName(databaseName).build());
		}

		public MongoDbExtension defaultManagedMongoDb(String databaseName, int port) {
			return new MongoDbExtension(mongoDb().databaseName(databaseName).port(port).build());
		}

		public MongoDbExtension defaultSpringMongoDb(String databaseName) {
			return new SpringMongoDbExtension(mongoDb().databaseName(databaseName).build());
		}

		public MongoDbExtension build() {

			if(this.mongoDbConfiguration == null) {
				throw new IllegalArgumentException("Configuration object should be provided.");
			}

			return new MongoDbExtension(mongoDbConfiguration);
		}

	}

	public MongoDbExtension(MongoDbConfiguration mongoDbConfiguration) {
		super(mongoDbConfiguration.getConnectionIdentifier());
		try {
			databaseOperation = new MongoOperation(mongoDbConfiguration);
		} catch (MongoException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public DatabaseOperation<MongoClient> getDatabaseOperation() {
		return this.databaseOperation;
	}

	@Override
	public String getWorkingExtension() {
		return EXTENSION;
	}

	@Override
	public void close() {
		this.databaseOperation.connectionManager().close();
	}

}
