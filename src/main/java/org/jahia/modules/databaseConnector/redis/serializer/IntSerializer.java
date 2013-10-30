package org.jahia.modules.databaseConnector.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public enum IntSerializer implements RedisSerializer<Integer> {
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
