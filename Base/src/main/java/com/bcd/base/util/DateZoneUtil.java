package com.bcd.base.util;

import java.time.ZoneOffset;
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

    /**
     * @param dateStr
     * @param format
     * @return
     * @see DateUtil#stringToDate(String, String, ZoneOffset)
     */
    public static Date stringToDate(String dateStr, String format) {
        if (dateStr == null || format == null) {
            return null;
        }
        return DateUtil.stringToDate(dateStr, format, ZONE_OFFSET);
    }

    /**
     * @param date
     * @param format
     * @return
     * @see DateUtil#dateToString(Date, String, ZoneOffset)
     */
    public static String dateToString(Date date, String format) {
        if (date == null || format == null) {
            return null;
        }
        return DateUtil.dateToString(date, format, ZONE_OFFSET);
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

    /**
     * @param date
     * @param unit
     * @return
     * @see DateUtil#getDateNum(Date, ChronoUnit, ZoneOffset)
     */
    public static Long getDateNum(Date date, ChronoUnit unit) {
        return DateUtil.getDateNum(date, unit, ZONE_OFFSET);
    }
}
