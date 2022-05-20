package com.bcd.base.support_kafka.nospring;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;

@Getter
@Setter
public class ConsumerProp {
    @NotEmpty
    public String bootstrapServers;
    public boolean enableAutoCommit = true;
    public Duration autoCommitInterval = Duration.ofSeconds(1);
    public String autoOffsetReset = "latest";
    public Duration heartbeatInterval = Duration.ofSeconds(15);
    public Duration sessionTimeout = Duration.ofSeconds(30);
    public Duration requestTimeout = Duration.ofSeconds(30);
    public int maxPartitionFetchBytes = 3027200;
}
