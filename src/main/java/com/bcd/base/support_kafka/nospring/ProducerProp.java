package com.bcd.base.support_kafka.nospring;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public class ProducerProp {
    @NotBlank
    public String bootstrapServers;
    public String acks = "0";
    public int batchSize = 16384;
    public long bufferMemory = 33554432L;
    public String compressionType = "gzip";
    public Map<String,String> properties=new HashMap<>();

    public ProducerProp(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public ProducerProp() {
    }
}
