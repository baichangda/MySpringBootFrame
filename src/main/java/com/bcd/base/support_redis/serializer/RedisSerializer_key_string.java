package com.bcd.base.support_redis.serializer;

import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;

public class RedisSerializer_key_string extends StringRedisSerializer {
    public final String keyPrefix;
    public final int keyPrefixLen;

    public RedisSerializer_key_string(Charset charset, String keyPrefix) {
        super(charset);
        this.keyPrefix = keyPrefix;
        this.keyPrefixLen = keyPrefix.length();
    }

    @Override
    public String deserialize(byte[] bytes) {
        final String deserialize = super.deserialize(bytes);
        if (deserialize == null) {
            return null;
        }
        if (deserialize.startsWith(keyPrefix)) {
            return deserialize.substring(keyPrefixLen);
        } else {
            return deserialize;
        }
    }

    @Override
    public byte[] serialize(String str) {
        return super.serialize(keyPrefix + str);
    }
}
