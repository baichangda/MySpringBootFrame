package com.config.redis.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(RedisClusterProperties.class)
public class RedisClusterConfig {
    private final int DEFAULT_TIMEOUT = 2000;
    private final int DEFAULT_MAX_REDIRECTIONS = 5;
    @Autowired
    private RedisClusterProperties redisClusterProperties;

    @Bean
    public JedisCluster redisCluster(){
        //1、生成集群节点
        Set<HostAndPort> nodes=redisClusterProperties.getNodes().stream().map(node->{
            String[] parts= StringUtils.split(node,":");
            return new HostAndPort(parts[0], Integer.valueOf(parts[1]));
        }).collect(Collectors.toSet());
        //2、构造集群簇
        JedisCluster jedisCluster=new JedisCluster(
                nodes,
                redisClusterProperties.getConnectionTimeout()==null?DEFAULT_TIMEOUT:redisClusterProperties.getConnectionTimeout(),
                redisClusterProperties.getSoTimeout()==null?DEFAULT_TIMEOUT:redisClusterProperties.getSoTimeout(),
                redisClusterProperties.getMaxAttempts()==null?DEFAULT_MAX_REDIRECTIONS:redisClusterProperties.getMaxAttempts(),
                redisClusterProperties.getPassword(),
                redisClusterProperties
        );
        return jedisCluster;
    }

}