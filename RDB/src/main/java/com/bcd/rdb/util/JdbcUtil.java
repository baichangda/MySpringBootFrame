package com.bcd.rdb.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.jdbc.rowmapper.MyColumnMapRowMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JdbcUtil {

    static Logger logger= LoggerFactory.getLogger(JdbcUtil.class);

    static ExecutorService removalListenerPool;

    static {
        removalListenerPool=Executors.newSingleThreadExecutor();
    }

    private static LoadingCache<String, JdbcData> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(5))
            .removalListener(RemovalListeners.asynchronous(removalNotification->{
                //移除数据源时候关闭数据源
                JdbcData jdbcData= (JdbcData)removalNotification.getValue();
                DruidDataSource dataSource=((DruidDataSource)jdbcData.getJdbcTemplate().getDataSource());
                logger.info("dataSource[{}] [{}] start remove",removalNotification.getKey().toString(),dataSource.hashCode());
                dataSource.close();
                logger.info("dataSource[{}] [{}] finish remove",removalNotification.getKey().toString(),dataSource.hashCode());
            }, removalListenerPool))
            .build(new CacheLoader<String, JdbcData>() {
                @Override
                public JdbcData load(String s){
                    //加载新的数据源
                    logger.info("dataSource[{}] start load",s);
                    String [] arr=s.split(",");
                    DruidDataSource dataSource=getDataSource(arr[0],arr[1],arr[2]);
                    JdbcTemplate jdbcTemplate=new JdbcTemplate(dataSource);
                    jdbcTemplate.afterPropertiesSet();
                    TransactionTemplate transactionTemplate=new TransactionTemplate(new JdbcTransactionManager(dataSource));
                    transactionTemplate.afterPropertiesSet();
                    JdbcData jdbcData=new JdbcData(jdbcTemplate,transactionTemplate);
                    logger.info("dataSource[{}] [{}] finish load",s,dataSource.hashCode());
                    return jdbcData;
                }
            });

    private static DruidDataSource getDataSource(String url, String username, String password){
        DruidDataSource dataSource= new DruidDataSource();
        dataSource.setEnable(true);
        dataSource.setMaxActive(3);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
        return dataSource;
    }

    private static String getKey(String url, String username, String password){
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        return url+","+username+","+password;
    }

    public static JdbcData getJdbcData(String url, String username, String password) throws ExecutionException {
        return cache.get(getKey(url, username, password));
    }

    public static void close(String url, String username, String password){
        cache.invalidate(getKey(url, username, password));
    }

    public static void closeAll(){
        cache.invalidateAll();
    }

    public static JdbcData getTest() throws ExecutionException {
        return getJdbcData("jdbc:postgresql://192.168.7.211:12921/test_bcd","dbuser","hlxpassword");
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<Map<String,Object>> dataList1= getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        List<Map<String,Object>> dataList2= getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);
        TimeUnit.SECONDS.sleep(10);
        List<Map<String,Object>> dataList3= getTest().getJdbcTemplate().query("select * from t_sys_user", MyColumnMapRowMapper.ROW_MAPPER);

        closeAll();
        removalListenerPool.shutdown();
    }
}

@Getter
class JdbcData{
    public JdbcData(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
}
