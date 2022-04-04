package com.lordofthejars.nosqlunit.hbase;

import org.apache.hadoop.conf.Configuration;

import java.util.HashMap;
import java.util.Map;

public class EmbeddedHBaseInstances {

	private static EmbeddedHBaseInstances embeddedInstances;
	
	private Map<String, Configuration> instances = new HashMap<String, Configuration>();
	
	private EmbeddedHBaseInstances() {
		super();
	}

	public static synchronized EmbeddedHBaseInstances getInstance() {
		if (embeddedInstances == null) {
			embeddedInstances = new EmbeddedHBaseInstances();
		}
		return embeddedInstances;
	}
	
	public void addHBaseConfiguration(Configuration hbase, String targetPath) {
		this.instances.put(targetPath, hbase);
	}
	
	public void removeHBaseConfiguration(String targetPath) {
		this.instances.remove(targetPath);
	}
	
	public Configuration getConfigurationByTargetPath(String targetPath) {
		return this.instances.get(targetPath);
	}
	
	public Configuration getDefaultConfiguration() {
        return this.instances.values().stream()
                .findFirst()
                .orElse(null);
	}
	
}
