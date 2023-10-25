package com.bcd.base.support_notify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify")
@Getter
@Setter
public class NotifyProp {
    /**
     * 当前服务的id
     * 用于设置topic的消费组id、确保topic发布的消息能达到广播的效果(即每个服务都能消费到全量的)
     *
     * 即使是同一个服务的集群、id也应该不一样
     * 例如有两个业务平台做集群
     * 应分别为 bus1、bus2
     */
    public String id;

    /**
     * kafka地址
     * 每一个业务类型、例如实时推送
     * 都会产生2个topic
     * 一个用于订阅、取消订阅
     * 一个用于消息通知
     */
    public String bootstrapServers;
}
