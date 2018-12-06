package com.bcd.base.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/11/8.
 */
public class FormatUtil {
    private final static ConcurrentHashMap<String,DecimalFormat> cacheMap=new ConcurrentHashMap<>();

    public static DecimalFormat getFormatter(String format){
        return cacheMap.computeIfAbsent(format,e->{
            DecimalFormat df=new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df;
        });
    }


    public static String formatToString(Number num, String format){
        if(num==null){
            return null;
        }
        //注意:此处必须先将数字转换成String然后构造BigDecimal进行四舍五入,否则会导致结果异常
        return getFormatter(format).format(new BigDecimal(num.toString()));
    }

    public static Double formatToDouble(Number num,String format){
        String numStr=formatToString(num, format);
        return Double.parseDouble(numStr);
    }

    public static Double formatToString_n(Number num,int len){
        StringBuilder sb=new StringBuilder("#");
        if(len>0){
            sb.append(".");
        }
        for(int i=0;i<len;i++){
            sb.append("#");
        }
        return formatToDouble(num,sb.toString());
    }

    public static Double formatToDouble_n(Number num,int len){
        StringBuilder sb=new StringBuilder("#");
        if(len>0){
            sb.append(".");
        }
        for(int i=0;i<len;i++){
            sb.append("#");
        }
        return formatToDouble(num,sb.toString());
    }

    public static String formatToString_n_2(Number num){
        return formatToString(num,"#.##");
    }

    public static Double formatToDouble_n_2(Number num){
        return formatToDouble(num,"#.##");
    }




}
