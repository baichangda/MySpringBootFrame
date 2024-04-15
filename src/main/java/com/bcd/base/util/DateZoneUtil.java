package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 此类为日期帮助类(专属于某个时区)
 * 方法都参见
 *
 * @see DateUtil
 * <p>
 * 所有的操作方法都基于某个时区
 */
public class DateZoneUtil {

    /**
     * 注意
     * {@link DateTimeFormatter#withZone(ZoneId)}如果不设置时区
     * 则不能格式化和解析不带时区的日期类、例如{@link Instant}
     */
    public final static ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    public final static ZoneOffset ZONE_OFFSET = ZoneOffset.of("+8");

    public final static DateTimeFormatter DATE_TIME_FORMATTER_DAY = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_DAY).withZone(ZONE_OFFSET);
    public final static DateTimeFormatter DATE_TIME_FORMATTER_SECOND = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_SECOND).withZone(ZONE_OFFSET);
    public final static DateTimeFormatter DATE_TIME_FORMATTER_MILLISECOND = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_MILLISECOND).withZone(ZONE_OFFSET);

    /**
     * 根据dateStr长度转换成不同的时间
     * {@link DateUtil#DATE_FORMAT_DAY} 长度8
     * {@link DateUtil#DATE_FORMAT_SECOND} 长度14
     *
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        int len = dateStr.length();
        return switch (len) {
            case 8 -> DateZoneUtil.stringToDate_day(dateStr);
            case 14 -> DateZoneUtil.stringToDate_second(dateStr);
            case 17 -> DateZoneUtil.stringToDate_millisecond(dateStr);
            default -> throw BaseRuntimeException.getException("dateStr[{}] not support", dateStr);
        };
    }

    /**
     * @param dateStr
     * @return
     */
    public static Date stringToDate_day(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return Date.from(Instant.from(DATE_TIME_FORMATTER_DAY.parse(dateStr)));
    }

    /**
     * @param dateStr
     * @return
     */
    public static Date stringToDate_second(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return Date.from(Instant.from(DATE_TIME_FORMATTER_SECOND.parse(dateStr)));
    }

    /**
     * @param dateStr
     * @return
     */
    public static Date stringToDate_millisecond(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return Date.from(Instant.from(DATE_TIME_FORMATTER_MILLISECOND.parse(dateStr)));
    }

    /**
     * @param date
     * @return
     */
    public static String dateToString_day(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_TIME_FORMATTER_DAY.format(date.toInstant());
    }

    /**
     * @param date
     * @return
     */
    public static String dateToString_second(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_TIME_FORMATTER_SECOND.format(date.toInstant());
    }

    /**
     * @param date
     * @return
     */
    public static String dateToString_millisecond(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_TIME_FORMATTER_MILLISECOND.format(date.toInstant());
    }


    /**
     * @param date
     * @param unit
     * @return
     * @see DateUtil#getFloorDate(Date, ChronoUnit, ZoneOffset)
     */
    public static Date getFloorDate(Date date, ChronoUnit unit) {
        return DateUtil.getFloorDate(date, unit, ZONE_OFFSET);
    }

    /**
     * @param date
     * @param unit
     * @return
     * @see DateUtil#getCeilDate(Date, ChronoUnit, ZoneOffset)
     */
    public static Date getCeilDate(Date date, ChronoUnit unit) {
        return DateUtil.getCeilDate(date, unit, ZONE_OFFSET);
    }

    /**
     * @param startDate
     * @param endDate
     * @param skip
     * @param unit
     * @return
     * @see DateUtil#range(Date, Date, int, ChronoUnit, ZoneOffset)
     */
    public static List<Date[]> range(Date startDate, Date endDate, int skip, ChronoUnit unit) {
        return DateUtil.range(startDate, endDate, skip, unit, ZONE_OFFSET);
    }

    /**
     * @param startDate
     * @param endDate
     * @see DateUtil#formatDateParam(Date, Date, ZoneOffset)
     */
    public static void formatDateParam(Date startDate, Date endDate) {
        DateUtil.formatDateParam(startDate, endDate, ZONE_OFFSET);
    }

    public static void main(String[] args) {
//        Date time = stringToDate_day("20111111");
//        System.out.println(time);
//        System.out.println(dateToString_day(time));
//        System.out.println(dateToString_second(time));
//
//
//        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_DAY).withZone(ZONE_OFFSET);
//        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_SECOND).withZone(ZONE_OFFSET);
//        System.out.println(LocalDate.from(formatter1.parse("20111111")).atTime(LocalTime.MIN).toInstant(ZONE_OFFSET).toEpochMilli() / 1000);
//        System.out.println(Instant.from(formatter2.parse("20111111000000")).toEpochMilli() / 1000);
//
//        Date d1 = new Date();
//        Date d2 = new Date();
//        formatDateParam(d1, d2);
//        System.out.println(d1);
//        System.out.println(d2);
//
//        Date newD1 = getFloorDate(d1, ChronoUnit.HOURS);
//        System.out.println(d1.getTime());
//        System.out.println(newD1.getTime());
//        System.out.println(DateUtil.getDiff(d1, newD1, ChronoUnit.SECONDS, true));

        LocalDateTime ldt1 = LocalDateTime.of(2023, 2, 1, 1, 1, 1);
        LocalDateTime ldt2 = LocalDateTime.of(2023, 8, 25, 1, 20, 1);
        Duration between = Duration.between(ldt1, ldt2);
        System.out.println(between.toDays());
        System.out.println(between.toHours());
        System.out.println(ChronoUnit.DAYS.between(ldt1,ldt2));
        System.out.println(ChronoUnit.MONTHS.between(ldt1,ldt2));
    }
}
