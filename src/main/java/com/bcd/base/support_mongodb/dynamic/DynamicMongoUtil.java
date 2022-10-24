package com.bcd.base.support_mongodb.dynamic;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DynamicMongoUtil {

    /**
     * datasource闲置过期时间
     */
    private final static int EXPIRE_IN_SECOND = 5;

    static Logger logger = LoggerFactory.getLogger(DynamicMongoUtil.class);
    private static final LoadingCache<String, MongoTemplate> CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(EXPIRE_IN_SECOND))
            .<String, MongoTemplate>evictionListener((k, v, c) -> {
                //移除数据源时候关闭数据源
                logger.info("dataSource[{}] [{}] start remove", k, v.hashCode());
                logger.info("dataSource[{}] [{}] finish remove", k, v.hashCode());
            })
            .scheduler(Scheduler.systemScheduler())
            .build(s -> {
                //加载新的数据源
                logger.info("dataSource[{}] start load", s);
                MongoTemplate mongoTemplate=new MongoTemplate(new SimpleMongoClientDatabaseFactory(s));
                logger.info("dataSource[{}] [{}] finish load", s, mongoTemplate.hashCode());
                return mongoTemplate;
            });



    public static MongoTemplate getMongoTemplate(String url) {
        return CACHE.get(url);
    }

    public static void close(String url) {
        CACHE.invalidate(url);
    }

    public static void closeAll() {
        CACHE.invalidateAll();
    }

    public static MongoTemplate getTest() {
        return getMongoTemplate("mongodb://10.0.11.50:27017/ai");
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> dataList1 = getTest().find(new Query().limit(10),String.class,"signal_gb");
        logger.info("{}",dataList1.size());
        List<String> dataList2 = getTest().find(new Query().limit(10),String.class,"signal_gb");
        logger.info("{}",dataList2.size());
        TimeUnit.SECONDS.sleep(10);
        List<String> dataList3 = getTest().find(new Query().limit(10),String.class,"signal_gb");
        logger.info("{}",dataList3.size());
        closeAll();

    }
}