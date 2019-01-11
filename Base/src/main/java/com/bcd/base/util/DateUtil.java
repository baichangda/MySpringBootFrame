package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 日期帮助类
 * 1、所有涉及到时区逻辑,日期转换均转换成 ZonedDateTime 运算然后再 转回Date
 * 2、所有涉及到时区的参数,最好使用ZonedId.of()里面传入时区英文,原因如下:
 *    在初始化ZonedDateTime时候，如果传入的时区参数ZoneId.of()中参数为
 *    @see ZoneId#SHORT_IDS 中非偏移量
 *    和
 *    偏移量(+08:00) 时候会导致不一样的结果
 *    前者为带时区处理了夏令时,后者仅仅为时区偏移量不考虑夏令时
 *    详情参见:
 *    @see ZoneId#of(String)
 *
 *
 */
public class DateUtil {

    private final static Logger logger= LoggerFactory.getLogger(DateUtil.class);

    public final static String DATE_FORMAT_DAY="yyyy-MM-dd";
    public final static String DATE_FORMAT_SECOND="yyyy-MM-dd HH:mm:ss";

    /**
     * 将日期转换为字符串
     * @param date
     * @param format
     * @param zoneId
     * @return
     */
    public static String dateToString(Date date,String format,ZoneId zoneId){
        if(date==null||format==null||zoneId==null){
            return null;
        }
        return DateTimeFormatter.ofPattern(format).withZone(zoneId).format(date.toInstant());
    }


    /**
     * 将日期字符串转换为 时间类型
     * @param dateStr
     * @param format
     * @param zoneId
     * @return
     */
    public static Date stringToDate(String dateStr,String format,ZoneId zoneId){
        if(dateStr==null||format==null||zoneId==null){
            return null;
        }
        return Date.from(Instant.from(DateTimeFormatter.ofPattern(format).withZone(zoneId).parse(dateStr)));
    }




    /**
     * 获取最近在当前日期之前的最后一个日期单位
     * @param date
     * @param unit 支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneId 时区
     * @return
     */
    public static Date getFloorDate(Date date,ChronoUnit unit,ZoneId zoneId){
        if(date==null){
            return null;
        }
        ChronoUnit[] units=new ChronoUnit[]{ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS};
        if(Arrays.stream(units).noneMatch(e->e==unit)){
            throw BaseRuntimeException.getException("[DateUtil.getFloorDate],ChronoUnit["+unit.toString()+"] Not Support!");
        }
        ZonedDateTime zdt=ZonedDateTime.ofInstant(date.toInstant(),zoneId);
        if(unit.ordinal()<=ChronoUnit.DAYS.ordinal()){
            zdt=zdt.truncatedTo(unit);
        }else{
            zdt=zdt.truncatedTo(ChronoUnit.DAYS);
            switch (unit){
                case MONTHS:{
                    zdt=zdt.withDayOfMonth(1);
                    break;
                }
                case YEARS:{
                    zdt=zdt.withDayOfMonth(1);
                    zdt=zdt.withMonth(1);
                    break;
                }
                default:{
                    break;
                }
            }
        }
        return Date.from(zdt.toInstant());
    }



    /**
     * 获取最近在当前日期之后的第一个日期单位
     * @param date
     * @param unit 支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneId 时区
     * @return
     */
    public static Date getCeilDate(Date date,ChronoUnit unit,ZoneId zoneId){
        if(date==null){
            return null;
        }
        ChronoUnit[] units=new ChronoUnit[]{ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS};
        if(Arrays.stream(units).noneMatch(e->e==unit)){
            throw BaseRuntimeException.getException("[DateUtil.getCeilDate],ChronoUnit["+unit.toString()+"] Not Support!");
        }
        ZonedDateTime zdt=ZonedDateTime.ofInstant(date.toInstant(),zoneId);
        if(unit.ordinal()<=ChronoUnit.DAYS.ordinal()){
            zdt=zdt.truncatedTo(unit);
            zdt=zdt.plus(1,unit);

        }else{
            zdt=zdt.truncatedTo(ChronoUnit.DAYS);
            switch (unit){
                case MONTHS:{
                    zdt=zdt.withDayOfMonth(1);
                    zdt=zdt.plusMonths(1);
                    break;
                }
                case YEARS:{
                    zdt=zdt.withDayOfMonth(1);
                    zdt=zdt.withMonth(1);
                    zdt=zdt.plusYears(1);
                    break;
                }
                default:{
                    break;
                }
            }
        }
        return Date.from(zdt.toInstant());
    }


    /**
     * 获取开始时间结束时间按照 日期单位 形成多个日期区间
     * 第一个区间开始时间为传入开始时间
     * 最后一个区间结束时间为传入结束时间
     * @param startDate
     * @param endDate
     * @param unit 支持 ChronoUnit.DAYS,ChronoUnit.WEEKS,ChronoUnit.MONTHS
     * @param zoneId 时区
     * @return 每一个数组第一个为开始时间,第二个为结束时间;开始时间为当天0.0.0,结束时间为当天23.59.59
     */
    public static List<Date[]> rangeDate(Date startDate, Date endDate, ChronoUnit unit,ZoneId zoneId){
        List<Date[]> returnList=new ArrayList<>();
        ZonedDateTime zdt1= ZonedDateTime.ofInstant(startDate.toInstant(),zoneId);
        ZonedDateTime zdt2= ZonedDateTime.ofInstant(endDate.toInstant(),zoneId);
        switch (unit){
            case DAYS:{
                ZonedDateTime start= zdt1.with(ChronoField.SECOND_OF_DAY,0);
                ZonedDateTime end= zdt1.with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds()-1);
                while(true){
                    returnList.add(new Date[]{Date.from(start.toInstant()),Date.from(end.toInstant())});
                    if(!zdt2.isBefore(start)&&!zdt2.isAfter(end)){
                        break;
                    }else{
                        start=start.plusDays(1);
                        end=end.plusDays(1);
                    }
                }

                break;
            }
            case WEEKS:{
                int dayOfWeek=zdt1.get(ChronoField.DAY_OF_WEEK);
                ZonedDateTime start= zdt1.plusDays(1-dayOfWeek).with(ChronoField.SECOND_OF_DAY,0);
                ZonedDateTime end= zdt1.plusDays(7-dayOfWeek).with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds()-1);
                while(true){
                    returnList.add(new Date[]{Date.from(start.toInstant()),Date.from(end.toInstant())});
                    if(!zdt2.isBefore(start)&&!zdt2.isAfter(end)){
                        break;
                    }else{
                        start=start.plusWeeks(1);
                        end=end.plusWeeks(1);
                    }
                }
                if(!returnList.isEmpty()){
                    Date[] firstEle=returnList.get(0);
                    Date[] lastEle=returnList.get(returnList.size()-1);
                    firstEle[0]=Date.from(zdt1.with(ChronoField.SECOND_OF_DAY,0).toInstant());
                    lastEle[1]=Date.from(zdt2.with(ChronoField.SECOND_OF_DAY,0).toInstant());
                }
                break;
            }
            case MONTHS:{
                ZonedDateTime temp=zdt1;
                while(true) {
                    int dayOfMonth = temp.get(ChronoField.DAY_OF_MONTH);
                    int max = temp.getMonth().maxLength();
                    ZonedDateTime start = temp.plusDays(1 - dayOfMonth).with(ChronoField.SECOND_OF_DAY, 0);
                    ZonedDateTime end = temp.plusDays(max - dayOfMonth).with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds() - 1);
                    returnList.add(new Date[]{Date.from(start.toInstant()),Date.from(end.toInstant())});
                    if(!zdt2.isBefore(start)&&!zdt2.isAfter(end)){
                        break;
                    }else{
                        temp=temp.plusMonths(1);
                    }
                }
                if(!returnList.isEmpty()){
                    Date[] firstEle=returnList.get(0);
                    Date[] lastEle=returnList.get(returnList.size()-1);
                    firstEle[0]=Date.from(zdt1.with(ChronoField.SECOND_OF_DAY,0).toInstant());
                    lastEle[1]=Date.from(zdt2.with(ChronoField.SECOND_OF_DAY,0).toInstant());
                }
                break;
            }
            default:{
                throw BaseRuntimeException.getException("[DateUtil.rangeDate],ChronoUnit["+unit.toString()+"] Not Support!");
            }
        }
        return returnList;
    }


    /**
     * 计算两个时间相差多少日期单位(不足一个日期单位的的按一个日期单位算)
     * @param d1 开始时间
     * @param d2 结束时间
     * @param unit 支持ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS
     * @return 相差日期单位数
     */
    public static long getDiff(Date d1,Date d2,ChronoUnit unit)
    {
        ChronoUnit[] units=new ChronoUnit[]{ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS};
        if(Arrays.stream(units).noneMatch(e->e==unit)){
            throw BaseRuntimeException.getException("[DateUtil.getDiff],ChronoUnit["+unit.toString()+"] Not Support!");
        }
        long diff;
        switch (unit){
            case DAYS:{
                diff=ChronoUnit.DAYS.getDuration().toMillis();
                break;
            }
            case HOURS:{
                diff=ChronoUnit.HOURS.getDuration().toMillis();
                break;
            }
            case MINUTES:{
                diff=ChronoUnit.MINUTES.getDuration().toMillis();
                break;
            }
            case SECONDS:{
                diff=ChronoUnit.SECONDS.getDuration().toMillis();
                break;
            }
            case MILLIS:{
                diff=ChronoUnit.MILLIS.getDuration().toMillis();
                break;
            }
            default:{
                throw BaseRuntimeException.getException("[DateUtil.getDiff],ChronoUnit["+unit.toString()+"] Not Support!");
            }
        }
        long begin = d1.getTime();
        long end = d2.getTime();
        double res= (end-begin)/((double)diff);
        return (int)Math.ceil(res);
    }


    /**
     * 会改变参数值
     * 格式化日期参数开始日期和结束日期
     * 格式规则为:
     *      开始日期去掉时分秒
     *      结束日期设置为当天 23:59:59
     * @param startDate
     * @param endDate
     * @param zoneId
     */
    public static void formatDateParam(Date startDate,Date endDate,ZoneId zoneId){
        if(startDate!=null){
            startDate.setTime(getFloorDate(startDate,ChronoUnit.DAYS,zoneId).getTime());
        }
        if(endDate!=null){
            Date tempDate= getCeilDate(endDate,ChronoUnit.DAYS,zoneId);
            Calendar endC=Calendar.getInstance();
            endC.setTime(tempDate);
            endC.add(Calendar.SECOND,-1);
            endDate.setTime(endC.getTimeInMillis());
        }
    }

    /**
     * 获取一个日期的数字表示形式
     * 例如:
     * 2018-3-12 15:13:12 888 表示成 20180312151312888
     * @param date
     * @param unit 最小的日期单位 支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneId 时区
     * @return
     */
    public static Long getDateNum(Date date,ChronoUnit unit,ZoneId zoneId){
        if(date==null){
            return null;
        }
        ChronoUnit[] units=new ChronoUnit[]{ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS};
        if(Arrays.stream(units).noneMatch(e->e==unit)){
            throw BaseRuntimeException.getException("[DateUtil.getDateNum],ChronoUnit["+unit.toString()+"] Not Support!");
        }
        StringBuilder sb=new StringBuilder();
        ZonedDateTime zdt=ZonedDateTime.ofInstant(date.toInstant(),zoneId);
        int unitIndex=unit.ordinal();
        if(unitIndex<=ChronoUnit.YEARS.ordinal()){
            sb.append(zdt.getYear());
        }
        if(unitIndex<=ChronoUnit.MONTHS.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.getMonthValue(),"00"));
        }
        if(unitIndex<=ChronoUnit.DAYS.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.getDayOfMonth(),"00"));
        }
        if(unitIndex<=ChronoUnit.HOURS.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.getHour(),"00"));
        }
        if(unitIndex<=ChronoUnit.MINUTES.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.getMinute(),"00"));
        }
        if(unitIndex<=ChronoUnit.SECONDS.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.getSecond(),"00"));
        }
        if(unitIndex<=ChronoUnit.MILLIS.ordinal()){
            sb.append(FormatUtil.formatToString(zdt.get(ChronoField.MILLI_OF_SECOND),"000"));
        }
        return Long.parseLong(sb.toString());
    }

    /**
     * 判断两个日期是否相等
     * 对比顺序
     * 年、月、日、时、分、秒、毫秒
     * @param d1
     * @param d2
     * @param field 对比的最小日期单位 支持 ChronoField.YEAR,ChronoField.MONTH_OF_YEAR,ChronoField.DAY_OF_MONTH,ChronoField.HOUR_OF_DAY,ChronoField.MINUTE_OF_HOUR,ChronoField.SECOND_OF_MINUTE,ChronoField.MILLI_OF_SECOND
     * @return
     */
    public static boolean isEqual(Date d1,Date d2,ChronoField field){
        ChronoField[] fields=new ChronoField[]{ChronoField.YEAR,ChronoField.MONTH_OF_YEAR,ChronoField.DAY_OF_MONTH,ChronoField.HOUR_OF_DAY,ChronoField.MINUTE_OF_HOUR,ChronoField.SECOND_OF_MINUTE,ChronoField.MILLI_OF_SECOND};
        if(Arrays.stream(fields).noneMatch(e->e==field)){
            throw BaseRuntimeException.getException("[DateUtil.isEqual],ChronoField["+field.toString()+"] Not Support!");
        }
        ZoneId zoneId=ZoneId.systemDefault();
        ZonedDateTime zdt1=ZonedDateTime.ofInstant(d1.toInstant(),zoneId);
        ZonedDateTime zdt2=ZonedDateTime.ofInstant(d2.toInstant(),zoneId);
        for (ChronoField curField : fields) {
            int curVal1=zdt1.get(curField);
            int curVal2=zdt2.get(curField);
            if(curVal1!=curVal2){
                return false;
            }
            if(curField==field){
                break;
            }
        }
        return true;
    }

    public static void main(String [] args){
        ZoneId zoneId1= ZoneId.of("Asia/Shanghai");
        ZoneId zoneId2= ZoneId.of("+8");
        ZonedDateTime zdt1=LocalDateTime.of(1988,6,30,11,11).atZone(zoneId1);
        ZonedDateTime zdt2=LocalDateTime.of(1988,6,30,11,11).atZone(zoneId2);
        logger.debug("{}",zdt1.toInstant().toEpochMilli());
        logger.debug("{}",zdt2.toInstant().toEpochMilli());
        logger.debug("{}",OffsetDateTime.now().getOffset());
    }
}
