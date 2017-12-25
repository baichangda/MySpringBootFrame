package com.bcd.config.plugins.redis.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/22.
 * 此为redis3.0之前,客户端使用一致性hash实现的分布式方案
 */
//@Configuration
//@EnableConfigurationProperties(RedisDistributedProperties.class)
public class RedisDistributedConfig {
    @Autowired
    private RedisDistributedProperties redisDistributedProperties;

    @Bean
    public ShardedJedisPool shardedJedisPool(){
        List<JedisShardInfo> jedisShardInfoList= redisDistributedProperties.getNodes().stream().map(node->node.toJedisShardInfo()).collect(Collectors.toList());
        //1、构造分布式pool
        ShardedJedisPool shardedJedisPool=new ShardedJedisPool(redisDistributedProperties,jedisShardInfoList);
        return shardedJedisPool;
    }
}
