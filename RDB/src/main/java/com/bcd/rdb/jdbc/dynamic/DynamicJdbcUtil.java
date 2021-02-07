package com.bcd.rdb.jdbc.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.jdbc.rowmapper.MyColumnMapRowMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DynamicJdbcUtil {

    /**
     * datasource闲置过期时间
     */
    private final static int EXPIRE_IN_SECOND = 5;

    /**
     * 扫描定时任务线程执行扫描清除周期
     */
    private final static int CLEAN_UP_POOL_PERIOD_IN_SECOND = 10;

    /**
     * 数据源最大connection激活数量
     */
    private final static int DATA_SOURCE_MAX_ACTIVE = 3;


    static Logger logger = LoggerFactory.getLogger(DynamicJdbcUtil.class);

    private static ScheduledExecutorService CLEAN_UP_POOL = Executors.newSingleThreadScheduledExecutor();

    private static LoadingCache<String, DynamicJdbcData> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(EXPIRE_IN_SECOND))
//            .removalListener(RemovalListeners.asynchronous(removalNotification->{},Executors.newSingleThreadExecutor()))
            .removalListener(removalNotification -> {
                //移除数据源时候关闭数据源
                DynamicJdbcData jdbcData = (DynamicJdbcData) removalNotification.getValue();
                DruidDataSource dataSource = ((DruidDataSource) jdbcData.getJdbcTemplate().getDataSource());
                logger.info("dataSource[{}] [{}] start remove", removalNotification.getKey().toString(), dataSource.hashCode());
                dataSource.close();
                logger.info("dataSource[{}] [{}] finish remove", removalNotification.getKey().toString(), dataSource.hashCode());
            })
            .build(new CacheLoader<String, DynamicJdbcData>() {
                @Override
                public DynamicJdbcData load(String s) {
                    //加载新的数据源
                    logger.info("dataSource[{}] start load", s);
                    String[] arr = s.split(",");
                    DruidDataSource dataSource = getDataSource(arr[0], arr[1], arr[2]);
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    jdbcTemplate.afterPropertiesSet();
                    TransactionTemplate transactionTemplate = new TransactionTemplate(new JdbcTransactionManager(dataSource));
                    transactionTemplate.afterPropertiesSet();
                    DynamicJdbcData jdbcData = new DynamicJdbcData(jdbcTemplate, transactionTemplate);
                    logger.info("dataSource[{}] [{}] finish load", s, dataSource.hashCode());
                    return jdbcData;
                }
            });

    /**
     * 启动扫描线程
     * 虽然guava cache有设置过期时间、但是guava cache并不会自动清除、而是等待下次访问才会清除
     * 所以需要另启动清除线程
     */
    static {
        CLEAN_UP_POOL.scheduleWithFixedDelay(() -> {
            CACHE.cleanUp();
        }, EXPIRE_IN_SECOND, CLEAN_UP_POOL_PERIOD_IN_SECOND, TimeUnit.SECONDS);
    }

    private static void test(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    private static DruidDataSource getDataSource(String url, String username, String password) {
        //首先测试
        test(url, username, password);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setEnable(true);
        dataSource.setMaxActive(DATA_SOURCE_MAX_ACTIVE);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        try {
            dataSource.init();
        } catch (SQLException e) {
            dataSource.close();
            throw BaseRuntimeException.getException(e);
        }
        return dataSource;
    }

    private static String getKey(String url, String username, String password) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        return url + "," + username + "," + password;
    }

    public static DynamicJdbcData getJdbcData(String url, String username, String password) {

        try {
            return CACHE.get(getKey(url, username, password));
        } catch (ExecutionException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void close(String url, String username, String password) {
        CACHE.invalidate(getKey(url, username, password));
    }

    public static void closeAll() {
        CACHE.invalidateAll();
    }

    public static DynamicJdbcData getTest() {
        return getJdbcData("jdbc:mysql://127.0.0.1:3306/msbf", "root", "123456");
    }

    public static void main(String[] args) throws InterruptedException {
        List<Map<String, Object>> dataList1 = getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        List<Map<String, Object>> dataList2 = getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        TimeUnit.SECONDS.sleep(10);
        List<Map<String, Object>> dataList3 = getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);

        closeAll();

        CLEAN_UP_POOL.shutdown();
    }
}


