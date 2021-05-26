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
    public final static ZoneOffset ZONE_OFFSET = ZoneOffset.of("+8");

    public final static DateTimeFormatter DATE_TIME_FORMATTER_DAY = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_DAY).withZone(ZONE_OFFSET);
    public final static DateTimeFormatter DATE_TIME_FORMATTER_SECOND = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_SECOND).withZone(ZONE_OFFSET);

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
        switch (len) {
            case 8: {
                return DateZoneUtil.stringToDate_day(dateStr);
            }
            case 14: {
                return DateZoneUtil.stringToDate_second(dateStr);
            }
            default: {
                throw BaseRuntimeException.getException("dateStr[{}] not support", dateStr);
            }
        }
    }

    /**
     * @param dateStr
     * @return
     */
    public static Date stringToDate_day(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return Date.from(LocalDate.from(DATE_TIME_FORMATTER_DAY.parse(dateStr)).atTime(LocalTime.MIN).toInstant(ZONE_OFFSET));
    }

    /**
     * @param dateStr
     * @return
     */
    public static Date stringToDate_second(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return Date.from(LocalDateTime.from(DATE_TIME_FORMATTER_SECOND.parse(dateStr)).toInstant(ZONE_OFFSET));
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
     * @param unit
     * @return
     * @see DateUtil#rangeDate(Date, Date, ChronoUnit, ZoneOffset)
     */
    public static List<Date[]> rangeDate(Date startDate, Date endDate, ChronoUnit unit) {
        return DateUtil.rangeDate(startDate, endDate, unit, ZONE_OFFSET);
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
        Date time = stringToDate_day("20111111");
        System.out.println(time);
        System.out.println(dateToString_day(time));
        System.out.println(dateToString_second(time));


        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_DAY).withZone(ZONE_OFFSET);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT_SECOND).withZone(ZONE_OFFSET);
        System.out.println(LocalDate.from(formatter1.parse("20111111")).atTime(LocalTime.MIN).toInstant(ZONE_OFFSET).toEpochMilli() / 1000);
        System.out.println(Instant.from(formatter2.parse("20111111000000")).toEpochMilli() / 1000);

        Date d1 = new Date();
        Date d2 = new Date();
        formatDateParam(d1, d2);
        System.out.println(d1);
        System.out.println(d2);

        Date newD1 = getFloorDate(d1, ChronoUnit.HOURS);
        System.out.println(d1.getTime());
        System.out.println(newD1.getTime());
        System.out.println(DateUtil.getDiff(d1, newD1, ChronoUnit.SECONDS, true));
    }
}
