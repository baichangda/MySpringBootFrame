package com.bcd.base.config.redis.schedule.demo;

import com.bcd.base.config.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.base.util.FormatUtil;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//@Component
//@Configurable
//@EnableScheduling
public class TestSchedule {

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test1(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test1 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test1 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test2(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test2 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test2 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test3(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test3 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test3 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test4(){
        System.out.println(Thread.currentThread().getId()+"===========test4 "+sdf.format(new Date()));
    }
}
