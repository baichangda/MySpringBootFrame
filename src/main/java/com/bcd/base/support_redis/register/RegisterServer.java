package com.bcd.base.support_redis.register;


public enum RegisterServer {
    test1(5),
    test2(10);

    /**
     * 最大感知超时时间
     * 即服务提供者、如果宕机、在此时间内会被消费者感知到
     * <p>
     * 此时间会产生如下3个重要时间
     * {@link #provider_heartbeat_s} 服务提供者心跳频率
     * {@link #consumer_localCacheExpired_ms} 消费者本地缓存过期时间
     * {@link #consumer_providerInfoExpired_ms} 消费者从redis获取信息、检查过期时间
     * <p>
     * 例如
     * maxTimeout_s=5s
     * provider_heartbeat_s=2s
     * consumer_localCacheExpired_ms=2000ms
     * consumer_providerInfoExpired_ms=3000ms
     * <p>
     * 极端场景下(最大过期信息场景)
     * 1、服务提供者发送完心跳立即宕机(从现在开始计算5s内消费者需要感知到)
     * 2、接着在3s内最后一刻、消费者获取到了服务提供者的信息(因为获取服务信息后、如果时间在3s内、则视为可用)
     * 逻辑分析:
     * 此时消费者本地缓存检查此信息在3s内、合法、并在本地缓存、缓存时间为2s、则在本地缓存失效前一刻、信息达到最大过期时间即接近5s
     * 此后本地缓存失效、再次从redis中获取并检查、剔除掉过期信息
     */
    public final int maxTimeout_s;
    public final int provider_heartbeat_s;
    public final long consumer_localCacheExpired_ms;
    public final long consumer_providerInfoExpired_ms;
    RegisterServer(int maxTimeout_s) {
        this.maxTimeout_s = maxTimeout_s;
        int consumer_localCacheExpired = (maxTimeout_s - 1) / 2;
        consumer_localCacheExpired_ms = consumer_localCacheExpired * 1000L;
        provider_heartbeat_s = (maxTimeout_s - 1) - consumer_localCacheExpired;
        consumer_providerInfoExpired_ms = (provider_heartbeat_s + 1) * 1000L;
    }
}

