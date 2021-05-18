package com.br.marketing.api.client;

import com.br.redisengin.RedisClusterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.Properties;

/**
 * The type Jedis cluster util.
 */
public class JedisClusterUtil {


    private static  JedisClusterUtil instance = new JedisClusterUtil();


    private JedisCluster jedisCluster = null;

    private JedisClusterUtil() {
        initJedisCluster();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static JedisClusterUtil getInstance() {
        return instance;
    }

    private void initJedisCluster() {
         jedisCluster = RedisClusterUtil.createJedisCluster();
    }


    /**
     * Gets jedis cluster.
     *
     * @return the jedis cluster
     */
    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

}
