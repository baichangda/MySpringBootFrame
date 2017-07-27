package com.config.redis.distributed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/22.
 */
//@Configuration
//@ConfigurationProperties(prefix = "spring.redis.distributed")
public class RedisDistributedProperties  extends JedisPoolConfig {
    //分布式节点
    private List<NodeProperties> nodes=new ArrayList<>();

    public List<NodeProperties> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeProperties> nodes) {
        this.nodes = nodes;
    }
}
