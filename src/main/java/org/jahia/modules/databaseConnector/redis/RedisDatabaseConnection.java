package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.redis.serializer.IntSerializer;
import org.jahia.modules.databaseConnector.redis.serializer.LongSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisDatabaseConnection extends AbstractDatabaseConnection {

    private static final DatabaseTypes databaseType = DatabaseTypes.REDIS;

    private RedisConnectionFactory connectionFactory;

    private StringRedisTemplate stringRedisTemplate;

    private RedisTemplate<String, Long> longRedisTemplate;

    private RedisTemplate<String, Integer> integerRedisTemplate;

    public RedisDatabaseConnection(String id, String host, Integer port) {
        super(id, host, port, null);
        JedisConnectionFactory cf = new JedisConnectionFactory();
        cf.setHostName(host);
        cf.setPort(port);
        cf.afterPropertiesSet();
        connectionFactory = cf;
        initStringRedisTemplate();
        initLongRedisTemplate();
        initIntegerRedisTemplate();
    }

    private void initStringRedisTemplate() {
        stringRedisTemplate = new StringRedisTemplate(connectionFactory);
    }

    private void initLongRedisTemplate() {
        RedisTemplate<String, Long> template = new RedisTemplate<String, Long>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(LongSerializer.INSTANCE);
        longRedisTemplate = template;
    }

    private void initIntegerRedisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<String, Integer>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(IntSerializer.INSTANCE);
        integerRedisTemplate = template;
    }

    public RedisConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public RedisTemplate<String, Long> getLongRedisTemplate() {
        return longRedisTemplate;
    }

    public RedisTemplate<String, Integer> getIntegerRedisTemplate() {
        return integerRedisTemplate;
    }

    public <K,V> RedisTemplate<K,V> getRedisTemplate(RedisSerializer<K> keySerializer, RedisSerializer<V> valueSerializer) {
        RedisTemplate<K,V> template = new RedisTemplate<K, V>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        return template;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }
}
