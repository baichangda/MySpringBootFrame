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
 * 1、所有涉及到时区逻辑,日期转换均转换成 LocalDateTime 运算然后再 转回Date
 *
 * 不考虑夏令时问题
 * {@link ZoneId#of(String)} 中传入+8和时区英文，前者仅仅是偏移量，后者会导致夏令时
 */
public class DateUtil {

    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public final static String DATE_FORMAT_DAY = "yyyyMMdd";
    public final static String DATE_FORMAT_SECOND = "yyyyMMddHHmmss";


    /**
     * 获取最近在当前日期之前的最后一个日期单位
     *
     * @param date
     * @param unit   支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneOffset 时区
     * @return
     */
    public static Date getFloorDate(Date date, ChronoUnit unit, ZoneOffset zoneOffset) {
        if (date == null) {
            return null;
        }
        ChronoUnit[] units = new ChronoUnit[]{ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS};
        if (Arrays.stream(units).noneMatch(e -> e == unit)) {
            throw BaseRuntimeException.getException("[DateUtil.getFloorDate],ChronoUnit[" + unit.toString() + "] Not Support!");
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);
        if (unit.ordinal() <= ChronoUnit.DAYS.ordinal()) {
            ldt = ldt.truncatedTo(unit);
        } else {
            ldt = ldt.truncatedTo(ChronoUnit.DAYS);
            switch (unit) {
                case MONTHS: {
                    ldt = ldt.withDayOfMonth(1);
                    break;
                }
                case YEARS: {
                    ldt = ldt.withDayOfMonth(1);
                    ldt = ldt.withMonth(1);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return Date.from(ldt.toInstant(zoneOffset));
    }


    /**
     * 获取最近在当前日期之后的第一个日期单位
     *
     * @param date
     * @param unit   支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneOffset 时区
     * @return
     */
    public static Date getCeilDate(Date date, ChronoUnit unit, ZoneOffset zoneOffset) {
        if (date == null) {
            return null;
        }
        ChronoUnit[] units = new ChronoUnit[]{ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS};
        if (Arrays.stream(units).noneMatch(e -> e == unit)) {
            throw BaseRuntimeException.getException("[DateUtil.getCeilDate],ChronoUnit[" + unit.toString() + "] Not Support!");
        }
        LocalDateTime zdt = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);
        if (unit.ordinal() <= ChronoUnit.DAYS.ordinal()) {
            zdt = zdt.truncatedTo(unit);
            zdt = zdt.plus(1, unit);

        } else {
            zdt = zdt.truncatedTo(ChronoUnit.DAYS);
            switch (unit) {
                case MONTHS: {
                    zdt = zdt.withDayOfMonth(1);
                    zdt = zdt.plusMonths(1);
                    break;
                }
                case YEARS: {
                    zdt = zdt.withDayOfMonth(1);
                    zdt = zdt.withMonth(1);
                    zdt = zdt.plusYears(1);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return Date.from(zdt.toInstant(zoneOffset));
    }


    /**
     * 获取开始时间结束时间按照 日期单位 形成多个日期区间
     * 第一个区间开始时间为传入开始时间
     * 最后一个区间结束时间为传入结束时间
     *
     * @param startDate
     * @param endDate
     * @param unit      支持 ChronoUnit.DAYS,ChronoUnit.WEEKS,ChronoUnit.MONTHS
     * @param zoneOffset    时区
     * @return 每一个数组第一个为开始时间, 第二个为结束时间;开始时间为当天0.0.0,结束时间为当天23.59.59
     */
    public static List<Date[]> rangeDate(Date startDate, Date endDate, ChronoUnit unit, ZoneOffset zoneOffset) {
        List<Date[]> returnList = new ArrayList<>();
        LocalDateTime ldt1 = LocalDateTime.ofInstant(startDate.toInstant(), zoneOffset);
        LocalDateTime ldt2 = LocalDateTime.ofInstant(endDate.toInstant(), zoneOffset);
        switch (unit) {
            case DAYS: {
                LocalDateTime start = ldt1.with(ChronoField.SECOND_OF_DAY, 0);
                LocalDateTime end = ldt1.with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds() - 1);
                while (true) {
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!ldt2.isBefore(start) && !ldt2.isAfter(end)) {
                        break;
                    } else {
                        start = start.plusDays(1);
                        end = end.plusDays(1);
                    }
                }

                break;
            }
            case WEEKS: {
                int dayOfWeek = ldt1.get(ChronoField.DAY_OF_WEEK);
                LocalDateTime start = ldt1.plusDays(1 - dayOfWeek).with(ChronoField.SECOND_OF_DAY, 0);
                LocalDateTime end = ldt1.plusDays(7 - dayOfWeek).with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds() - 1);
                while (true) {
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!ldt2.isBefore(start) && !ldt2.isAfter(end)) {
                        break;
                    } else {
                        start = start.plusWeeks(1);
                        end = end.plusWeeks(1);
                    }
                }
                if (!returnList.isEmpty()) {
                    Date[] firstEle = returnList.get(0);
                    Date[] lastEle = returnList.get(returnList.size() - 1);
                    firstEle[0] = Date.from(ldt1.with(ChronoField.SECOND_OF_DAY, 0).toInstant(zoneOffset));
                    lastEle[1] = Date.from(ldt2.with(ChronoField.SECOND_OF_DAY, 0).toInstant(zoneOffset));
                }
                break;
            }
            case MONTHS: {
                LocalDateTime temp = ldt1;
                while (true) {
                    int dayOfMonth = temp.get(ChronoField.DAY_OF_MONTH);
                    int max = temp.getMonth().maxLength();
                    LocalDateTime start = temp.plusDays(1 - dayOfMonth).with(ChronoField.SECOND_OF_DAY, 0);
                    LocalDateTime end = temp.plusDays(max - dayOfMonth).with(ChronoField.SECOND_OF_DAY, ChronoUnit.DAYS.getDuration().getSeconds() - 1);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!ldt2.isBefore(start) && !ldt2.isAfter(end)) {
                        break;
                    } else {
                        temp = temp.plusMonths(1);
                    }
                }
                if (!returnList.isEmpty()) {
                    Date[] firstEle = returnList.get(0);
                    Date[] lastEle = returnList.get(returnList.size() - 1);
                    firstEle[0] = Date.from(ldt1.with(ChronoField.SECOND_OF_DAY, 0).toInstant(zoneOffset));
                    lastEle[1] = Date.from(ldt2.with(ChronoField.SECOND_OF_DAY, 0).toInstant(zoneOffset));
                }
                break;
            }
            default: {
                throw BaseRuntimeException.getException("[DateUtil.rangeDate],ChronoUnit[" + unit.toString() + "] Not Support!");
            }
        }
        return returnList;
    }


    /**
     * 计算两个时间相差多少日期单位(不足一个日期单位的的按一个日期单位算)
     *
     * @param d1   开始时间
     * @param d2   结束时间
     * @param unit 支持ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS
     * @param up 如果存在小数位,是向上取整还是向下取整;true代表向上;false代表向下
     * @return 相差日期单位数
     */
    public static long getDiff(Date d1, Date d2, ChronoUnit unit,boolean up) {
        ChronoUnit[] units = new ChronoUnit[]{ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS};
        if (Arrays.stream(units).noneMatch(e -> e == unit)) {
            throw BaseRuntimeException.getException("[DateUtil.getDiff],ChronoUnit[" + unit.toString() + "] Not Support!");
        }
        long diff;
        switch (unit) {
            case DAYS: {
                diff = ChronoUnit.DAYS.getDuration().toMillis();
                break;
            }
            case HOURS: {
                diff = ChronoUnit.HOURS.getDuration().toMillis();
                break;
            }
            case MINUTES: {
                diff = ChronoUnit.MINUTES.getDuration().toMillis();
                break;
            }
            case SECONDS: {
                diff = ChronoUnit.SECONDS.getDuration().toMillis();
                break;
            }
            case MILLIS: {
                diff = ChronoUnit.MILLIS.getDuration().toMillis();
                break;
            }
            default: {
                throw BaseRuntimeException.getException("[DateUtil.getDiff],ChronoUnit[" + unit.toString() + "] Not Support!");
            }
        }
        long begin = d1.getTime();
        long end = d2.getTime();
        double res = (end - begin) / ((double) diff);
        if(up){
            return (int) Math.ceil(res);
        }else{
            return (int) Math.floor(res);
        }
    }


    /**
     * 会改变参数值
     * 格式化日期参数开始日期和结束日期
     * 格式规则为:
     * 开始日期去掉时分秒
     * 结束日期设置为当天 23:59:59
     *
     * @param startDate
     * @param endDate
     * @param zoneOffset
     */
    public static void formatDateParam(Date startDate, Date endDate, ZoneOffset zoneOffset) {
        if (startDate != null) {
            startDate.setTime(getFloorDate(startDate, ChronoUnit.DAYS, zoneOffset).getTime());
        }
        if (endDate != null) {
            Date tempDate = getCeilDate(endDate, ChronoUnit.DAYS, zoneOffset);
            Calendar endC = Calendar.getInstance();
            endC.setTime(tempDate);
            endC.add(Calendar.SECOND, -1);
            endDate.setTime(endC.getTimeInMillis());
        }
    }

    /**
     * 获取一个日期的数字表示形式
     * 例如:
     * 2018-3-12 15:13:12 888 表示成 20180312151312888
     *
     * @param date
     * @param unit   最小的日期单位 支持 ChronoUnit.MILLIS,ChronoUnit.SECONDS,ChronoUnit.MINUTES,ChronoUnit.HOURS,ChronoUnit.DAYS,ChronoUnit.MONTHS,ChronoUnit.YEARS
     * @param zoneOffset 时区
     * @return
     */
    public static Long getDateNum(Date date, ChronoUnit unit, ZoneOffset zoneOffset) {
        if (date == null) {
            return null;
        }
        ChronoUnit[] units = new ChronoUnit[]{ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS};
        if (Arrays.stream(units).noneMatch(e -> e == unit)) {
            throw BaseRuntimeException.getException("[DateUtil.getDateNum],ChronoUnit[" + unit.toString() + "] Not Support!");
        }
        StringBuilder sb = new StringBuilder();
        LocalDateTime zdt = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);
        int unitIndex = unit.ordinal();
        if (unitIndex <= ChronoUnit.YEARS.ordinal()) {
            sb.append(zdt.getYear());
        }
        if (unitIndex <= ChronoUnit.MONTHS.ordinal()) {
            sb.append(prepend_0(zdt.getMonthValue(), 2));
        }
        if (unitIndex <= ChronoUnit.DAYS.ordinal()) {
            sb.append(prepend_0(zdt.getDayOfMonth(), 2));
        }
        if (unitIndex <= ChronoUnit.HOURS.ordinal()) {
            sb.append(prepend_0(zdt.getHour(), 2));
        }
        if (unitIndex <= ChronoUnit.MINUTES.ordinal()) {
            sb.append(prepend_0(zdt.getMinute(), 2));
        }
        if (unitIndex <= ChronoUnit.SECONDS.ordinal()) {
            sb.append(prepend_0(zdt.getSecond(), 2));
        }
        if (unitIndex <= ChronoUnit.MILLIS.ordinal()) {
            sb.append(prepend_0(zdt.get(ChronoField.MILLI_OF_SECOND), 3));
        }
        return Long.parseLong(sb.toString());
    }

    /**
     * 数字前面补齐0
     * @param num
     * @param len
     * @return
     */
    private static String prepend_0(int num,int len){
        String numStr=num+"";
        if(numStr.length()==len){
            return numStr;
        }else{
            StringBuilder sb=new StringBuilder();
            for(int i=numStr.length();i<=len;i++){
                sb.append("0");
            }
            sb.append(numStr);
            return sb.toString();
        }
    }

    /**
     * 判断两个日期是否相等
     * 对比顺序
     * 年、月、日、时、分、秒、毫秒
     *
     * @param d1
     * @param d2
     * @param field 对比的最小日期单位 支持 ChronoField.YEAR,ChronoField.MONTH_OF_YEAR,ChronoField.DAY_OF_MONTH,ChronoField.HOUR_OF_DAY,ChronoField.MINUTE_OF_HOUR,ChronoField.SECOND_OF_MINUTE,ChronoField.MILLI_OF_SECOND
     * @return
     */
    public static boolean isEqual(Date d1, Date d2, ChronoField field) {
        ChronoField[] fields = new ChronoField[]{ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.MILLI_OF_SECOND};
        if (Arrays.stream(fields).noneMatch(e -> e == field)) {
            throw BaseRuntimeException.getException("[DateUtil.isEqual],ChronoField[" + field.toString() + "] Not Support!");
        }
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime ldt1 = LocalDateTime.ofInstant(d1.toInstant(), zoneId);
        LocalDateTime ldt2 = LocalDateTime.ofInstant(d2.toInstant(), zoneId);
        for (ChronoField curField : fields) {
            int curVal1 = ldt1.get(curField);
            int curVal2 = ldt2.get(curField);
            if (curVal1 != curVal2) {
                return false;
            }
            if (curField == field) {
                break;
            }
        }
        return true;
    }

    public static void main(String[] args) {
//        ZoneId zoneId1 = ZoneId.of("Asia/Shanghai");
//        ZoneId zoneId2 = ZoneId.of("+8");
//        ZonedDateTime zdt1 = LocalDateTime.of(1988, 6, 30, 11, 11).atZone(zoneId1);
//        ZonedDateTime zdt2 = LocalDateTime.of(1988, 6, 30, 11, 11).atZone(zoneId2);
//        logger.debug("{}", zdt1.toInstant().toEpochMilli());
//        logger.debug("{}", zdt2.toInstant().toEpochMilli());
//        logger.debug("{}", OffsetDateTime.now().getOffset());
        Date d1= Date.from(LocalDateTime.of(2019,10,1,0,0,0).toInstant(ZoneOffset.of("+8")));
        Date d2=Date.from(LocalDateTime.of(2020,4,1,0,0,0).toInstant(ZoneOffset.of("+8")));
        long diff= getDiff(d1,d2,ChronoUnit.DAYS,true);

        System.out.println(diff);

        System.out.println(getCeilDate(new Date(),ChronoUnit.DAYS,ZoneOffset.of("+8")));
    }
}
