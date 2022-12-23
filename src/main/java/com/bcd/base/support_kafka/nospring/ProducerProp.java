package com.bcd.base.support_kafka.nospring;

import javax.validation.constraints.NotBlank;

public class ProducerProp {
    @NotBlank
    public String bootstrapServers;
    public int acks = 0;
    public int batchSize = 16384;
    public long bufferMemory = 33554432L;
    public String compressionType = "gzip";
}
