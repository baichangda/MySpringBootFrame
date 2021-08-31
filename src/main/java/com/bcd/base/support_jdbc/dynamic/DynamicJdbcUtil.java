package com.bcd.base.support_rdb.jdbc.dynamic;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_rdb.jdbc.rowmapper.MyColumnMapRowMapper;
import com.github.benmanes.caffeine.cache.*;
import com.zaxxer.hikari.HikariDataSource;
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
import java.util.concurrent.TimeUnit;

public class DynamicJdbcUtil {

    /**
     * datasource闲置过期时间
     */
    private final static int EXPIRE_IN_SECOND = 5;

    /**
     * 数据源最大connection激活数量
     *
     *
     */
    private final static int DATA_SOURCE_MAX_ACTIVE = 3;
    static Logger logger = LoggerFactory.getLogger(DynamicJdbcUtil.class);
    private static final LoadingCache<String, DynamicJdbcData> CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(EXPIRE_IN_SECOND))
            .<String, DynamicJdbcData>evictionListener((k,v,c) -> {
                //移除数据源时候关闭数据源
                HikariDataSource dataSource = (HikariDataSource) v.getJdbcTemplate().getDataSource();
                logger.info("dataSource[{}] [{}] start remove", k, dataSource.hashCode());
                dataSource.close();
                logger.info("dataSource[{}] [{}] finish remove", k, dataSource.hashCode());
            })
            .scheduler(Scheduler.systemScheduler())
            .build(s -> {
                //加载新的数据源
                logger.info("dataSource[{}] start load", s);
                String[] arr = s.split(",");
                HikariDataSource dataSource = getDataSource(arr[0], arr[1], arr[2]);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.afterPropertiesSet();
                TransactionTemplate transactionTemplate = new TransactionTemplate(new JdbcTransactionManager(dataSource));
                transactionTemplate.afterPropertiesSet();
                DynamicJdbcData jdbcData = new DynamicJdbcData(jdbcTemplate, transactionTemplate);
                logger.info("dataSource[{}] [{}] finish load", s, dataSource.hashCode());
                return jdbcData;
            });

    private static HikariDataSource getDataSource(String url, String username, String password) {
        //首先测试
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(DATA_SOURCE_MAX_ACTIVE);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.isRunning();
        return dataSource;
    }

    private static String getKey(String url, String username, String password) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        return url + "," + username + "," + password;
    }

    public static DynamicJdbcData getJdbcData(String url, String username, String password) {
        return CACHE.get(getKey(url, username, password));
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
        List<Map<String, Object>> dataList1 = getTest().getJdbcTemplate().query("SELECT * FROM t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        List<Map<String, Object>> dataList2 = getTest().getJdbcTemplate().query("SELECT * FROM t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        TimeUnit.SECONDS.sleep(10);
        List<Map<String, Object>> dataList3 = getTest().getJdbcTemplate().query("SELECT * FROM t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);

        closeAll();

    }
}


