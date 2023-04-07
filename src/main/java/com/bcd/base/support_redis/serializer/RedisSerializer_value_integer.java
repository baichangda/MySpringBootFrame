package com.bcd.base.support_redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RedisSerializer_value_integer implements RedisSerializer<Integer> {
    @Override
    public byte[] serialize(Integer i) throws SerializationException {
        if (i == null) {
            return null;
        } else {
            return new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i.intValue()};
        }
    }

    @Override
    public Integer deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        } else {
            if (bytes.length == 4) {
                return ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[2] & 0xff);
            } else {
                throw new SerializationException("deserialize error,bytes length[" + bytes.length + "] not 4");
            }
        }
    }
}
