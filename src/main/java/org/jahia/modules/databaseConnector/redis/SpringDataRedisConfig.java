package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.redis.serializer.IntSerializer;
import org.jahia.modules.databaseConnector.redis.serializer.LongSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
@Configuration
public class SpringDataRedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory cf = new JedisConnectionFactory();
        cf.setHostName( "localhost" );
        cf.setPort(6379);
        cf.afterPropertiesSet();
        return cf;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }

    @Bean
    public RedisTemplate<String, Long> longRedisTemplate() {
        RedisTemplate<String, Long> template = new RedisTemplate<String, Long>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(LongSerializer.INSTANCE);
        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> integerRedisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<String, Integer>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(IntSerializer.INSTANCE);
        return template;
    }


}
