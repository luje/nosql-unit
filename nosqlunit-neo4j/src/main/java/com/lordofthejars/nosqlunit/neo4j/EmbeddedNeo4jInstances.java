package com.lordofthejars.nosqlunit.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.HashMap;
import java.util.Map;

public final class EmbeddedNeo4jInstances {

	private static EmbeddedNeo4jInstances embeddedInstances;
	
	private Map<String, GraphDatabaseService> instances = new HashMap<String, GraphDatabaseService>();
	
	private EmbeddedNeo4jInstances() {
		super();
	}

	public static synchronized EmbeddedNeo4jInstances getInstance() {
		if (embeddedInstances == null) {
			embeddedInstances = new EmbeddedNeo4jInstances();
		}
		return embeddedInstances;
	}
	
	public void addGraphDatabaseService(GraphDatabaseService graphDatabaseService, String targetPath) {
		this.instances.put(targetPath, graphDatabaseService);
	}
	
	public void removeGraphDatabaseService(String targetPath) {
		this.instances.remove(targetPath);
	}
	
	public GraphDatabaseService getGraphDatabaseServiceByTargetPath(String targetPath) {
		return this.instances.get(targetPath);
	}
	
	public GraphDatabaseService getDefaultGraphDatabaseService() {
		return this.instances.values().stream().findFirst().orElse(null);
	}
	
}
