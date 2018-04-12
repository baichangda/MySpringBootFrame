package com.bcd.config.redis.cluster;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

//@Configuration
//@ConfigurationProperties(prefix = "spring.redis.cluster")
public class RedisClusterProperties extends JedisPoolConfig {

    //集群节点
    private List<String> nodes=new ArrayList<>();

    //连接超时时间
    private Integer connectionTimeout;

    //返回结果超时时间
    private Integer soTimeout;

    //最大尝试次数
    private Integer maxAttempts;

    //密码
    private String password;

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
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

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}