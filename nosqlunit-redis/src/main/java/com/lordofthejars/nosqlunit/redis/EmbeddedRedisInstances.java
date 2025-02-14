package com.lordofthejars.nosqlunit.redis;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;


public class EmbeddedRedisInstances {

	private static EmbeddedRedisInstances embeddedInstances;
	
	private Map<String, Jedis> instances = new HashMap<String, Jedis>();
	
	private EmbeddedRedisInstances() {
		super();
	}

	public static synchronized EmbeddedRedisInstances getInstance() {
		if (embeddedInstances == null) {
			embeddedInstances = new EmbeddedRedisInstances();
		}
		return embeddedInstances;
	}
	
	public void addJedis(Jedis jedis, String targetPath) {
		this.instances.put(targetPath, jedis);
	}
	
	public void removeJedis(String targetPath) {
		this.instances.remove(targetPath);
	}
	
	public Jedis getJedisByTargetPath(String targetPath) {
		return this.instances.get(targetPath);
	}
	
	public Jedis getDefaultJedis() {
		return this.instances.values().stream().findFirst().get();
	}
	
}
