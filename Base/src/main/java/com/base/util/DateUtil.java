package com.base.util;

import org.apache.shiro.SecurityUtils;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/4/11.
 */
public class DateUtil {
    public final static String DATE_FORMAT_DAY="yyyy-MM-dd";
    public final static String DATE_FORMAT_SECOND="yyyy-MM-dd HH:mm:ss";

    private final static int[] DATE_UNIT_ARR=new int[]{Calendar.MILLISECOND,Calendar.SECOND,Calendar.MINUTE,Calendar.HOUR_OF_DAY,
            Calendar.DATE,Calendar.MONTH,Calendar.YEAR};
    /**
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToString(Date date,String format){
        return new SimpleDateFormat(format).format(date);
    }

    /**
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToStringWithUserTimeZone(Date date,String format) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //1、格式化日期
        return getUserTimeZoneSimpleDateFormat(format).format(date);
    }

    /**
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Date stringToDateWithUserTimeZone(String dateStr,String format) throws ParseException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //1、格式化日期
        return getUserTimeZoneSimpleDateFormat(format).parse(dateStr);
    }

    private static SimpleDateFormat getUserTimeZoneSimpleDateFormat(String format) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //1、获取当前用户的时区
        Object user= SecurityUtils.getSubject().getSession().getAttribute("user");
        String timeZone=(String) BeanUtil.getFieldVal(user,"timeZone");
        //2、获取对应时区的格式化器
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return simpleDateFormat;
    }

    /**
     *
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String dateStr,String format) throws ParseException {
        return new SimpleDateFormat(format).parse(dateStr);
    }


    /**
     * 获取最近在当前日期之前的最后一个日期单位
     * @param date
     * @param calendarUnit 只支持 DateUtil.DATE_UNIT_ARR
     * @return
     */
    public static Date getFloorDate(Date date,int calendarUnit){

        Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);
        for(int i=0;i<=DATE_UNIT_ARR.length-1;i++){
            if(DATE_UNIT_ARR[i]>calendarUnit){
                if(Calendar.DATE==DATE_UNIT_ARR[i]){
                    calendar.set(DATE_UNIT_ARR[i],1);
                }else{
                    calendar.set(DATE_UNIT_ARR[i],0);
                }
            }
            if(DATE_UNIT_ARR[i]==calendarUnit){
                break;
            }
        }
        return calendar.getTime();
    }

    /**
     * 获取最近在当前日期之后的第一个日期单位
     * @param date
     * @param calendarUnit 只支持 DateUtil.DATE_UNIT_ARR
     * @return
     */
    public static Date getCeilDate(Date date,int calendarUnit){
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);
        for(int i=0;i<=DATE_UNIT_ARR.length-1;i++){
            if(DATE_UNIT_ARR[i]>calendarUnit){
                if(Calendar.DATE==DATE_UNIT_ARR[i]){
                    calendar.set(DATE_UNIT_ARR[i],1);
                }else{
                    calendar.set(DATE_UNIT_ARR[i],0);
                }
            }
            if(DATE_UNIT_ARR[i]==calendarUnit){
                calendar.add(DATE_UNIT_ARR[i],1);
                break;
            }
        }
        return calendar.getTime();
    }

    /**
     * 将开始时间、结束时间 根据日期单位划分成 时间段
     * @param startDate
     * @param endDate
     * @param calendarUnit Calendar.MONTH,Calendar.WEEK_OF_YEAR,Calendar.DATE
     * @param dateNum 当 Calendar.DATE 时候指定天数
     * @return
     */
    public static List<Date[]> parseSplitDate(Date startDate, Date endDate, int calendarUnit,int dateNum){
        List<Date[]> returnList=new ArrayList<>();
        if(startDate.getTime()>endDate.getTime()){
            return null;
        }
        Calendar c1=Calendar.getInstance();
        Calendar c2=Calendar.getInstance();
        c1.setTime(startDate);
        c2.setTime(endDate);

        Calendar curC1=Calendar.getInstance();
        Calendar curC2=null;
        curC1.setTime(startDate);
        while(curC2==null||curC2.before(c2)){
            if(curC2==null){
                curC2=Calendar.getInstance();
                curC2.setTime(startDate);
                curC2.add(calendarUnit,dateNum);
            }else{
                curC1.add(calendarUnit,dateNum);
                curC2.add(calendarUnit,dateNum);
            }
            returnList.add(new Date[]{curC1.getTime(),curC2.getTime()});
        }
        //设置最后一个区间的截至日期为endDate
        returnList.get(returnList.size()-1)[1]=endDate;

        return returnList;
    }


    /**
     * 计算两个时间相差多少天
     * @param dateBegin 开始时间
     * @param dateEnd 结束时间
     * @return 相差天数
     */
    public static int getDateDiff(Date dateBegin,Date dateEnd)
    {
        Long begin = dateBegin.getTime();
        Long end = dateEnd.getTime();

        return (int)((end-begin)/(1000*60*60*24));
    }


    /**
     * 会改变参数值
     * 格式化日期参数开始日期和结束日期
     * 格式规则为:
     *      开始日期去掉时分秒
     *      结束日期设置为当天 23:59:59
     * @param startDate
     * @param endDate
     */
    public static void formatDateParam(Date startDate,Date endDate){
        if(startDate!=null){
            startDate.setTime(getFloorDate(startDate,Calendar.DATE).getTime());
        }
        if(endDate!=null){
            Date tempDate= getCeilDate(endDate,Calendar.DATE);
            Calendar endC=Calendar.getInstance();
            endC.setTime(tempDate);
            endC.add(Calendar.SECOND,-1);
            endDate.setTime(endC.getTimeInMillis());
        }
    }


    /**
     * 将格式为yyyy-M-d,yyyy-MM-d,yyyy-M-dd,yyyy-MM-dd
     * 统一转换成yyyy-MM-dd
     * @param str
     * @return
     */
    public static Date stringToDate(String str){
        Date date = null;
        String[] splitStr = str.split("-");
        if (splitStr[1].length()==1){
            splitStr[1] = "0"+splitStr[1];
        }
        if (splitStr[2].length()==1){
            splitStr[2] = "0"+splitStr[2];
        }
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<splitStr.length;i++){
            if (i==splitStr.length-1){
                sb.append(splitStr[i]);
            }else {
                sb.append(splitStr[i]);
                sb.append("-");
            }
        }
        String dateStr = sb.toString();
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat(DATE_FORMAT_DAY);
        try {
            date = simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
