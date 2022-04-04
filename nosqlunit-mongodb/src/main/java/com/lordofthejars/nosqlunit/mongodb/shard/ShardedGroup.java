package com.lordofthejars.nosqlunit.mongodb.shard;

import com.lordofthejars.nosqlunit.core.AbstractLifecycleManager;
import com.lordofthejars.nosqlunit.mongodb.ManagedMongoDbLifecycleManager;
import com.lordofthejars.nosqlunit.mongodb.replicaset.ReplicaSetManagedMongoDb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShardedGroup {

    private List<ManagedMongoDbLifecycleManager> shards = new ArrayList<ManagedMongoDbLifecycleManager>();
    private List<ManagedMongoDbLifecycleManager> configs = new ArrayList<ManagedMongoDbLifecycleManager>();
    private List<ManagedMongosLifecycleManager> mongos = new ArrayList<ManagedMongosLifecycleManager>();

    private List<ReplicaSetManagedMongoDb> replicaSets = new ArrayList<ReplicaSetManagedMongoDb>();

    private String username;
    private String password;

    public void addShard(ManagedMongoDbLifecycleManager managedMongoDbLifecycleManager) {
        this.shards.add(managedMongoDbLifecycleManager);
    }

    public void addConfig(ManagedMongoDbLifecycleManager managedMongoDbLifecycleManager) {
        this.configs.add(managedMongoDbLifecycleManager);
    }

    public void addMongos(ManagedMongosLifecycleManager managedMongosLifecycleManager) {
        this.mongos.add(managedMongosLifecycleManager);
    }

    public void addReplicaSet(ReplicaSetManagedMongoDb replicaSetManagedMongoDb) {
        this.replicaSets.add(replicaSetManagedMongoDb);
    }

    public ManagedMongosLifecycleManager getFirstMongosServer() {

        if (this.mongos.size() > 0) {
            return this.mongos.get(0);
        } else {
            throw new IllegalArgumentException("At least one Mongos server is required for sharding");
        }

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthenticationSet() {
        return this.username != null && this.password != null;
    }

    public AbstractLifecycleManager getStoppingServer(int port) {
        return getServerByPortAndState(port, true);
    }

    public AbstractLifecycleManager getStartingServer(int port) {
        return getServerByPortAndState(port, false);
    }

    public boolean isOnlyShards() {
        return this.getShards().size() > 0 && this.getReplicaSets().size() == 0;
    }

    public boolean isOnlyReplicaSetShards() {
        return this.getShards().size() == 0 && this.getReplicaSets().size() > 0;
    }

    public boolean isShardsAndReplicSetShardsMixed() {
        return this.getShards().size() != 0 && this.getReplicaSets().size() != 0;
    }

    private AbstractLifecycleManager getServerByPortAndState(int port, boolean state) {
        AbstractLifecycleManager abstractLifecycleManager =
                this.shards.stream()
                        .filter(it -> Objects.equals(port, it.getPort()))
                        .filter(it -> Objects.equals(state, it.isReady()))
                        .findFirst()
                        .orElse(null);

        if (abstractLifecycleManager == null) {
            abstractLifecycleManager =
                    this.configs.stream()
                            .filter(it -> Objects.equals(port, it.getPort()))
                            .filter(it -> Objects.equals(state, it.isReady()))
                            .findFirst()
                            .orElse(null);

            if (abstractLifecycleManager == null) {
                abstractLifecycleManager =
                        this.mongos.stream()
                                .filter(it -> Objects.equals(port, it.getPort()))
                                .filter(it -> Objects.equals(state, it.isReady()))
                                .findFirst()
                                .orElse(null);

                if (abstractLifecycleManager == null) {
                    for (ReplicaSetManagedMongoDb replicaSetManagedMongoDb : this.replicaSets) {
                        ManagedMongoDbLifecycleManager serverByPortAndState = replicaSetManagedMongoDb.getServerByPortAndState(port, state);

                        if (serverByPortAndState != null) {
                            abstractLifecycleManager = serverByPortAndState;
                            break;
                        }

                    }
                }
            }

        }

        return abstractLifecycleManager;
    }

    public List<ManagedMongoDbLifecycleManager> getShards() {
        return shards;
    }

    public List<ManagedMongoDbLifecycleManager> getConfigs() {
        return configs;
    }

    public List<ManagedMongosLifecycleManager> getMongos() {
        return mongos;
    }

    public List<ReplicaSetManagedMongoDb> getReplicaSets() {
        return replicaSets;
    }
}
