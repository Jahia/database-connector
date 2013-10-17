package org.jahia.modules.databaseConnector.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

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
        cf.setPort( 6379 );
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
        template.setKeySerializer(StringSerializer.INSTANCE);
        template.setValueSerializer(LongSerializer.INSTANCE);
        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> integerRedisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<String, Integer>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(StringSerializer.INSTANCE);
        template.setValueSerializer(IntSerializer.INSTANCE);
        return template;
    }

    public static enum StringSerializer implements RedisSerializer<String> {
        INSTANCE;

        @Override public byte[] serialize( String s ) throws SerializationException {
            return (null != s ? s.getBytes() : new byte[0]);
        }

        @Override public String deserialize( byte[] bytes ) throws SerializationException {
            if ( bytes.length > 0 ) {
                return new String( bytes );
            } else {
                return null;
            }
        }
    }

    public static enum LongSerializer implements RedisSerializer<Long> {
        INSTANCE;

        @Override public byte[] serialize( Long aLong ) throws SerializationException {
            if ( null != aLong ) {
                return aLong.toString().getBytes();
            } else {
                return new byte[0];
            }
        }

        @Override public Long deserialize( byte[] bytes ) throws SerializationException {
            if ( bytes.length > 0 ) {
                return Long.parseLong( new String( bytes ) );
            } else {
                return null;
            }
        }
    }

    public static enum IntSerializer implements RedisSerializer<Integer> {
        INSTANCE;

        @Override public byte[] serialize( Integer i ) throws SerializationException {
            if ( null != i ) {
                return i.toString().getBytes();
            } else {
                return new byte[0];
            }
        }

        @Override public Integer deserialize( byte[] bytes ) throws SerializationException {
            if ( bytes.length > 0 ) {
                return Integer.parseInt( new String( bytes ) );
            } else {
                return null;
            }
        }
    }

}
