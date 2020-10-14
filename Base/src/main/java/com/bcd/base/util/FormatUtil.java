package com.bcd.base.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/11/8.
 */
public class FormatUtil {
    private final static ConcurrentHashMap<String, DecimalFormat> cacheMap = new ConcurrentHashMap<>();

    public static DecimalFormat getFormatter(String format) {
        return cacheMap.computeIfAbsent(format, e -> {
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df;
        });
    }


    public static String formatToString(Number num, String format) {
        if (num == null) {
            return null;
        }
        //注意:此处必须先将数字转换成String然后构造BigDecimal进行四舍五入,否则会导致结果异常
        return getFormatter(format).format(new BigDecimal(num.toString()));
    }

    public static Double formatToDouble(Number num, String format) {
        if(num==null){
            return null;
        }
        String numStr = formatToString(num, format);
        return Double.parseDouble(numStr);
    }

    public static Double formatToString_n(Number num, int len) {
        if(num==null){
            return null;
        }
        StringBuilder sb = new StringBuilder("#");
        if (len > 0) {
            sb.append(".");
        }
        for (int i = 0; i < len; i++) {
            sb.append("#");
        }
        return formatToDouble(num, sb.toString());
    }

    public static Double formatToDouble_n(Number num, int len) {
        if(num==null){
            return null;
        }
        StringBuilder sb = new StringBuilder("#");
        if (len > 0) {
            sb.append(".");
        }
        for (int i = 0; i < len; i++) {
            sb.append("#");
        }
        return formatToDouble(num, sb.toString());
    }

    public static String formatToString_n_2(Number num) {
        if(num==null){
            return null;
        }
        return formatToString(num, "#.##");
    }

    public static Double formatToDouble_n_2(Number num) {
        if(num==null){
            return null;
        }
        return formatToDouble(num, "#.##");
    }


    public static void main(String[] args) {
        long t1=System.currentTimeMillis();
        Map<Integer,Integer> map=new HashMap<>();
        for(int i=0;i<10000000;i++){
            map.put(i,i);
        }
        long t2=System.currentTimeMillis();
        System.out.println(t2-t1);
    }

}
