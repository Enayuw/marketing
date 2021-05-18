package com.br.marketing.api.client;

import com.br.redisengin.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Redis config.
 */
@Configuration
@Slf4j
public class RedisConfig {


    /**
     * Jedis pool config jedis pool config.
     *
     * @return the jedis pool config
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(PropertiesUtil.getIntegerValue("redis.max.total"));
        jedisPoolConfig.setMaxIdle(PropertiesUtil.getIntegerValue("redis.max.idle"));
        jedisPoolConfig.setMinIdle(PropertiesUtil.getIntegerValue("redis.min.idle"));
        jedisPoolConfig.setMaxWaitMillis(Long.parseLong(PropertiesUtil.getStringValue("redis.max.wait.time")));
        // 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }

    /**
     * Redis cluster configuration redis cluster configuration.
     *
     * @return the redis cluster configuration
     */
    @Bean
    public RedisClusterConfiguration redisClusterConfiguration(){
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        //String clusterNodes = (String) redisProperties.get("redis.address");
        String clusterNodes = PropertiesUtil.getStringValue("redis.clusterIp");
        String[] serverArray = clusterNodes.split(",");
        Set<RedisNode> nodes = new HashSet<RedisNode>();
        for(String ipPort:serverArray){
            String[] ipAndPort = ipPort.split(":");
            nodes.add(new RedisNode(ipAndPort[0].trim(), Integer.parseInt(ipAndPort[1])));
        }
        redisClusterConfiguration.setClusterNodes(nodes);
        return redisClusterConfiguration;
    }

    /**
     * Jedis connection factory jedis connection factory.
     *
     * @param jedisPoolConfig           the jedis pool config
     * @param redisClusterConfiguration the redis cluster configuration
     * @return the jedis connection factory
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig,RedisClusterConfiguration redisClusterConfiguration) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration,jedisPoolConfig);
        return jedisConnectionFactory;
    }

    /**
     * Redis template redis template.
     *
     * @param redisConnectionFactory the redis connection factory
     * @return the redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }
}
