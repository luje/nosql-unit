package com.lordofthejars.nosqlunit.mongodb.replicaset;

import com.lordofthejars.nosqlunit.mongodb.ManagedMongoDbLifecycleManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReplicaSetGroup {

	private static final int DEFAULT_DEFAULT_CONNECTION_INDEX = 0; 
	
	
	private List<ManagedMongoDbLifecycleManager> servers = new LinkedList<ManagedMongoDbLifecycleManager>();
	private ConfigurationDocument configurationDocument;
	
	private String username;
	private String password;
	
	private int connectionIndex = DEFAULT_DEFAULT_CONNECTION_INDEX;
	
	public void setConfigurationDocument(ConfigurationDocument configurationDocument) {
		this.configurationDocument = configurationDocument;
	}
	
	public void addServer(ManagedMongoDbLifecycleManager server) {
		this.servers.add(server);
	}
	
	public ConfigurationDocument getConfiguration() {
		return configurationDocument;
	}
	
	public List<ManagedMongoDbLifecycleManager> getServers() {
		return servers;
	}
	
	public ManagedMongoDbLifecycleManager getDefaultConnection() {
		return servers.get(connectionIndex);
	}
	
	public void setConnectionIndex(int connectionIndex) {
		this.connectionIndex = connectionIndex;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean isAuthenticationSet() {
		return this.username != null && this.password != null;
	}
	
	public ManagedMongoDbLifecycleManager getStoppedServer(int port) {
        return this.servers.stream()
                .filter(it -> Objects.equals(port, it.getPort()))
                .filter(it -> Objects.equals(false, it.isReady()))
                .findFirst()
                .orElse(null);
	}

	public ManagedMongoDbLifecycleManager getStartedServer(int port) {
        return     this.servers.stream()
                .filter(it -> Objects.equals(port, it.getPort()))
                .filter(it -> Objects.equals(true, it.isReady()))
                .findFirst()
                .orElse(null);
	}
	
	public int numberOfStartedServers() {
		return this.servers.size() - numberOfStoppedServers();
	}
	
	public int numberOfStoppedServers() {
		int numberOfStoppedServers = 0;
		
		for (ManagedMongoDbLifecycleManager managedMongoDbLifecycleManager : this.servers) {
			if(!managedMongoDbLifecycleManager.isReady()) {
				numberOfStoppedServers++;
			}
		}
		
		return numberOfStoppedServers;
	}

	public String getReplicaSetName() {
		return this.configurationDocument.getReplicaSetName();
	}

}
