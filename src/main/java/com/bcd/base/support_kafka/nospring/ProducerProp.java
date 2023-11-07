package com.bcd.base.support_kafka.nospring;

import jakarta.validation.constraints.NotBlank;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ProducerProp {
    @NotBlank
    public String bootstrapServers;
    public String acks = "0";
    public int batchSize = 16384;
    public long bufferMemory = 33554432L;
    public String compressionType = "gzip";
    public Map<String, String> properties = new HashMap<>();

    public ProducerProp(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public ProducerProp() {
    }

    public final Properties toProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.putAll(properties);
        return props;
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getAcks() {
        return this.acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getBufferMemory() {
        return this.bufferMemory;
    }

    public void setBufferMemory(long bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public String getCompressionType() {
        return this.compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
