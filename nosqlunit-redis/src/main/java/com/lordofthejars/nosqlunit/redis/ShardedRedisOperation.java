package com.lordofthejars.nosqlunit.redis;

import com.lordofthejars.nosqlunit.core.AbstractCustomizableDatabaseOperation;
import com.lordofthejars.nosqlunit.core.NoSqlAssertionError;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

import java.io.InputStream;
import java.util.Collection;

public class ShardedRedisOperation extends AbstractCustomizableDatabaseOperation<RedisConnectionCallback, ShardedJedis> {

	private ShardedJedis shardedJedis;
	
	public ShardedRedisOperation(ShardedJedis shardedJedis) {
		this.shardedJedis = shardedJedis;
		setInsertionStrategy(new DefaultRedisInsertionStrategy());
		setComparisonStrategy(new DefaultRedisComparisonStrategy());
	}
	
	@Override
	public void insert(InputStream dataScript) {
		insertData(dataScript);
	}

	private void insertData(InputStream dataScript) {
		try {
			executeInsertion(new RedisConnectionCallback() {
				
				@Override
				public Collection<Jedis> getAllJedis() {
					return shardedJedis.getAllShards();
				}
				
				@Override
				public Jedis getActiveJedis(byte[] key) {
					return shardedJedis.getShard(key);
				}

				@Override
				public BinaryJedisCommands insertionJedis() {
					return shardedJedis;
				}
			}, dataScript);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void deleteAll() {
        shardedJedis.getAllShards()
                .forEach(BinaryJedis::flushAll);
	}

	@Override
	public boolean databaseIs(InputStream expectedData) {
		return compareData(expectedData);
	}

	private boolean compareData(InputStream expectedData) throws NoSqlAssertionError {
		try {
			return executeComparison(new RedisConnectionCallback() {
				
				@Override
				public Collection<Jedis> getAllJedis() {
					return shardedJedis.getAllShards();
				}
				
				@Override
				public Jedis getActiveJedis(byte[] key) {
					return shardedJedis.getShard(key);
				}

				@Override
				public BinaryJedisCommands insertionJedis() {
					return shardedJedis;
				}
			}, expectedData);
		} catch (NoSqlAssertionError e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ShardedJedis connectionManager() {
		return shardedJedis;
	}

}
