package com.bcd.base.support_kafka.nospring;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ConsumerProp {
    @NotEmpty
    public String bootstrapServers;
    @NotEmpty
    public String groupId;
    public boolean enableAutoCommit = true;
    public Duration autoCommitInterval = Duration.ofSeconds(1);
    public String autoOffsetReset = "latest";
    public Duration heartbeatInterval = Duration.ofSeconds(15);
    public Duration sessionTimeout = Duration.ofSeconds(30);
    public Duration requestTimeout = Duration.ofSeconds(30);
    public int maxPartitionFetchBytes = 3027200;
    public Map<String,String> properties=new HashMap<>();
}
