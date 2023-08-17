package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 日期帮助类
 * 1、所有涉及到时区逻辑,日期转换均转换成 LocalDateTime 运算然后再 转回Date
 * <p>
 * 不考虑夏令时问题
 * {@link ZoneId#of(String)} 中传入+8和时区英文，前者仅仅是偏移量，后者会导致夏令时
 */
public class DateUtil {

    public final static String DATE_FORMAT_DAY = "yyyyMMdd";
    public final static String DATE_FORMAT_SECOND = "yyyyMMddHHmmss";
    public final static String DATE_FORMAT_MILLISECOND = "yyyyMMddHHmmssSSS";
    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    /**
     * 获取最近在当前日期之前的最后一个日期单位
     *
     * @param date
     * @param unit       支持
     *                   {@link ChronoUnit#MILLIS}
     *                   {@link ChronoUnit#SECONDS}
     *                   {@link ChronoUnit#MINUTES}
     *                   {@link ChronoUnit#HOURS}
     *                   {@link ChronoUnit#DAYS}
     *                   {@link ChronoUnit#MONTHS}
     *                   {@link ChronoUnit#YEARS}
     * @param zoneOffset 时区
     * @return
     */
    public static Date getFloorDate(Date date, ChronoUnit unit, ZoneOffset zoneOffset) {
        if (date == null) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);
        if (unit.ordinal() <= ChronoUnit.DAYS.ordinal()) {
            ldt = ldt.truncatedTo(unit);
            ldt = ldt.plus(-1, unit);
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
                    throw BaseRuntimeException.getException("[DateUtil.getFloorDate],unit[{}}] Not Support!", unit.toString());
                }
            }
        }
        return Date.from(ldt.toInstant(zoneOffset));
    }


    /**
     * 获取最近在当前日期之后的第一个日期单位
     *
     * @param date
     * @param unit       支持
     *                   {@link ChronoUnit#MILLIS}
     *                   {@link ChronoUnit#SECONDS}
     *                   {@link ChronoUnit#MINUTES}
     *                   {@link ChronoUnit#HOURS}
     *                   {@link ChronoUnit#DAYS}
     *                   {@link ChronoUnit#MONTHS}
     *                   {@link ChronoUnit#YEARS}
     * @param zoneOffset 时区
     * @return
     */
    public static Date getCeilDate(Date date, ChronoUnit unit, ZoneOffset zoneOffset) {
        if (date == null) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), zoneOffset);
        if (unit.ordinal() <= ChronoUnit.DAYS.ordinal()) {
            ldt = ldt.truncatedTo(unit);
            ldt = ldt.plus(1, unit);
        } else {
            ldt = ldt.truncatedTo(ChronoUnit.DAYS);
            switch (unit) {
                case MONTHS: {
                    ldt = ldt.withDayOfMonth(1);
                    ldt = ldt.plusMonths(1);
                    break;
                }
                case YEARS: {
                    ldt = ldt.withDayOfMonth(1);
                    ldt = ldt.withMonth(1);
                    ldt = ldt.plusYears(1);
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[DateUtil.getCeilDate],unit[{}}] Not Support!", unit.toString());
                }
            }
        }
        return Date.from(ldt.toInstant(zoneOffset));
    }


    /**
     * 获取开始时间结束时间按照 日期单位 形成多个日期区间
     * 第一个区间开始时间为传入开始时间
     * 最后一个区间结束时间为传入结束时间
     * <p>
     * 注意:
     * 返回的结果包含开头时间、不包含结尾时间
     *
     * @param startDate  包含
     * @param endDate    不包含
     * @param amount     时间区间跨度
     * @param unit       支持
     *                   {@link ChronoUnit#MINUTES}
     *                   {@link ChronoUnit#HOURS}
     *                   {@link ChronoUnit#DAYS}
     *                   {@link ChronoUnit#WEEKS}
     *                   {@link ChronoUnit#MONTHS}
     * @param zoneOffset 时区
     * @return 每一个数组第一个为开始时间, 第二个为结束时间
     */
    public static List<Date[]> range(Date startDate, Date endDate, int amount, ChronoUnit unit, ZoneOffset zoneOffset) {
        List<Date[]> returnList = new ArrayList<>();
        LocalDateTime ldt1 = LocalDateTime.ofInstant(startDate.toInstant(), zoneOffset);
        LocalDateTime ldt2 = LocalDateTime.ofInstant(endDate.toInstant(), zoneOffset);
        switch (unit) {
            case MINUTES: {
                LocalDateTime start = ldt1.withSecond(0).withNano(0);
                LocalDateTime end;
                while (true) {
                    end = start.plusMinutes(amount);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!end.isBefore(ldt2)) {
                        break;
                    }
                    start = end;
                }
                returnList.get(0)[0] = Date.from(ldt1.toInstant(zoneOffset));
                returnList.get(returnList.size() - 1)[1] = Date.from(ldt2.toInstant(zoneOffset));
                break;
            }
            case HOURS: {
                LocalDateTime start = ldt1.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime end;
                while (true) {
                    end = start.plusHours(amount);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!end.isBefore(ldt2)) {
                        break;
                    }
                    start = end;
                }
                returnList.get(0)[0] = Date.from(ldt1.toInstant(zoneOffset));
                returnList.get(returnList.size() - 1)[1] = Date.from(ldt2.toInstant(zoneOffset));
                break;
            }
            case DAYS: {
                LocalDateTime start = ldt1.withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime end;
                while (true) {
                    end = start.plusDays(amount);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!end.isBefore(ldt2)) {
                        break;
                    }
                    start = end;
                }
                returnList.get(0)[0] = Date.from(ldt1.toInstant(zoneOffset));
                returnList.get(returnList.size() - 1)[1] = Date.from(ldt2.toInstant(zoneOffset));
                break;
            }
            case WEEKS: {
                int dayOfWeek = ldt1.get(ChronoField.DAY_OF_WEEK);
                LocalDateTime start = ldt1.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1 - dayOfWeek);
                LocalDateTime end;
                while (true) {
                    end = start.plusWeeks(amount);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!end.isBefore(ldt2)) {
                        break;
                    }
                    start = end;
                }
                returnList.get(0)[0] = Date.from(ldt1.toInstant(zoneOffset));
                returnList.get(returnList.size() - 1)[1] = Date.from(ldt2.toInstant(zoneOffset));
                break;
            }
            case MONTHS: {
                LocalDateTime start = ldt1.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime end;
                while (true) {
                    end = start.plusMonths(amount);
                    returnList.add(new Date[]{Date.from(start.toInstant(zoneOffset)), Date.from(end.toInstant(zoneOffset))});
                    if (!end.isBefore(ldt2)) {
                        break;
                    }
                    start = end;
                }
                returnList.get(0)[0] = Date.from(ldt1.toInstant(zoneOffset));
                returnList.get(returnList.size() - 1)[1] = Date.from(ldt2.toInstant(zoneOffset));
                break;
            }
            default: {
                throw BaseRuntimeException.getException("[DateUtil.range],unit[{}}] Not Support!", unit.toString());
            }
        }
        return returnList;
    }


    /**
     * 计算两个时间相差多少日期单位(不足一个日期单位的的按一个日期单位算)
     * d2-d1
     *
     * @param d1   开始时间
     * @param d2   结束时间
     * @param unit 支持
     *             {@link ChronoUnit#MILLIS}
     *             {@link ChronoUnit#SECONDS}
     *             {@link ChronoUnit#MINUTES}
     *             {@link ChronoUnit#HOURS}
     *             {@link ChronoUnit#DAYS}
     * @param up   如果存在小数位,是向上取整还是向下取整;true代表向上;false代表向下
     * @return 相差日期单位数
     */
    public static long getDiff(Date d1, Date d2, ChronoUnit unit, boolean up) {
        long unitMillis;
        switch (unit) {
            case DAYS: {
                unitMillis = ChronoUnit.DAYS.getDuration().toMillis();
                break;
            }
            case HOURS: {
                unitMillis = ChronoUnit.HOURS.getDuration().toMillis();
                break;
            }
            case MINUTES: {
                unitMillis = ChronoUnit.MINUTES.getDuration().toMillis();
                break;
            }
            case SECONDS: {
                unitMillis = ChronoUnit.SECONDS.getDuration().toMillis();
                break;
            }
            case MILLIS: {
                return d2.getTime() - d1.getTime();
            }
            default: {
                throw BaseRuntimeException.getException("[DateUtil.getDiff],unit[{}] Not Support!", unit.toString());
            }
        }
        long begin = d1.getTime();
        long end = d2.getTime();
        long diff = end - begin;
        if (diff > 0) {
            double res = diff / ((double) unitMillis);
            if (up) {
                return (int) Math.ceil(res);
            } else {
                return (int) Math.floor(res);
            }
        } else if (diff < 0) {
            double res = diff / ((double) unitMillis);
            if (up) {
                return -(int) Math.ceil(-res);
            } else {
                return -(int) Math.floor(-res);
            }
        } else {
            return 0;
        }


    }


    /**
     * 会改变参数值
     * 格式化日期参数开始日期和结束日期
     * 格式规则为:
     * 开始日期去掉时分秒
     * 结束日期+1天且去掉时分秒
     *
     * @param startDate  包括
     * @param endDate    不包括
     * @param zoneOffset 时区偏移量
     */
    public static void formatDateParam(Date startDate, Date endDate, ZoneOffset zoneOffset) {
        if (startDate != null) {
            startDate.setTime(getFloorDate(startDate, ChronoUnit.DAYS, zoneOffset).getTime());
        }
        if (endDate != null) {
            endDate.setTime(getCeilDate(endDate, ChronoUnit.DAYS, zoneOffset).getTime());
        }
    }

    public static void main(String[] args) {
        LocalDateTime ldt1 = LocalDateTime.of(2023, 8, 16, 6, 10, 10, 0);
        LocalDateTime ldt2 = ldt1.plusDays(20);
        ZoneOffset zoneOffset = ZoneOffset.of("+8");
        List<Date[]> range = DateUtil.range(Date.from(ldt1.toInstant(zoneOffset)), Date.from(ldt2.toInstant(zoneOffset)), 1, ChronoUnit.MONTHS, zoneOffset);
        for (Date[] dates : range) {
            System.out.println(dates[0] + "," + dates[1]);
        }
    }

}
