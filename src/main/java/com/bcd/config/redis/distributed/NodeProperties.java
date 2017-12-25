package com.bcd.config.plugins.redis.distributed;

import redis.clients.jedis.JedisShardInfo;

/**
 * Created by Administrator on 2017/7/22.
 */
public class NodeProperties {
    private final int DEFAULT_TIMEOUT=2000;
    private final int DEFAULT_WEIGHT=1;
    private final boolean DEFAULT_SSL=false;

    private Integer connectionTimeout;
    private Integer soTimeout;
    private String host;
    private Integer port;
    private Boolean ssl;
    private Integer weight;

    public NodeProperties(){
        this.connectionTimeout=DEFAULT_TIMEOUT;
        this.soTimeout=DEFAULT_TIMEOUT;
        this.weight=DEFAULT_WEIGHT;
        this.ssl=DEFAULT_SSL;
    }

    public int getDEFAULT_TIMEOUT() {
        return DEFAULT_TIMEOUT;
    }

    public int getDEFAULT_WEIGHT() {
        return DEFAULT_WEIGHT;
    }

    public boolean isDEFAULT_SSL() {
        return DEFAULT_SSL;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(Integer soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public JedisShardInfo toJedisShardInfo(){
        return new JedisShardInfo(host,port,connectionTimeout,soTimeout,weight,ssl);
    }
}
