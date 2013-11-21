package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.redis.serializer.IntSerializer;
import org.jahia.modules.databaseConnector.redis.serializer.LongSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisDatabaseConnectionImpl extends AbstractDatabaseConnection implements RedisDatabaseConnection {

    public static final String NODE_TYPE = "dc:redisConnection";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.REDIS;

    private final Integer timeout;

    private final Integer weight;

    private final RedisConnectionFactory connectionFactory;

    private StringRedisTemplate stringRedisTemplate;

    private RedisTemplate<String, Long> longRedisTemplate;

    private RedisTemplate<String, Integer> integerRedisTemplate;

    public RedisDatabaseConnectionImpl(String id, String host, Integer port) {
        this(id, host, port, null, null, null);
    }

    public RedisDatabaseConnectionImpl(String id, String host, Integer port, String password) {
        this(id, host, port, password, null, null);
    }

    public RedisDatabaseConnectionImpl(String id, String host, Integer port, String password, Integer timeout, Integer weight) {
        super(id, host, port, null, null, password);
        this.timeout = timeout;
        this.weight = weight;
        JedisConnectionFactory cf = new JedisConnectionFactory(makeJedisShardInfo());
        cf.afterPropertiesSet();
        connectionFactory = cf;
        initStringRedisTemplate();
        initLongRedisTemplate();
        initIntegerRedisTemplate();
    }

    private JedisShardInfo makeJedisShardInfo() {
        Assert.hasText(host, "Host must be defined");
        Assert.notNull(port, "Port must be defined");
        JedisShardInfo shardInfo;
        if (timeout != null || weight != null) {
            if (timeout == null) {
                shardInfo = new JedisShardInfo(host, port, Protocol.DEFAULT_TIMEOUT, weight);
            }
            else if (weight != null) {
                shardInfo = new JedisShardInfo(host, port, timeout, weight);
            }
            else {
                shardInfo = new JedisShardInfo(host, port, timeout);
            }
        }
        else {
            shardInfo = new JedisShardInfo(host, port);
        }
        if (password != null && !password.isEmpty()) {
            shardInfo.setPassword(password);
        }
        return shardInfo;
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

    @Override
    protected boolean registerAsService() {
        boolean b = registerAsService(connectionFactory, true);
        boolean b1 = registerAsService(stringRedisTemplate);
        boolean b2 = registerAsService(longRedisTemplate);
        boolean b3 = registerAsService(integerRedisTemplate);
        return b && b1 && b2 && b3;
    }

    @Override
    public ConnectionData makeConnectionData() {
        return new RedisConnectionDataImpl(id, host, port, dbName, uri, user, password, getDatabaseType(), timeout, weight);
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
        return DATABASE_TYPE;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public Integer getWeight() {
        return weight;
    }
}
