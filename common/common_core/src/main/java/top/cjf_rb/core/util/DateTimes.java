package top.cjf_rb.core.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Java8 时间类型工具
 */
public final class DateTimes {
    public static final String DATE_FORMAT = "uuuu-MM-dd";
    public static final String DATETIME_FORMAT = "uuuu-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_COMPACT = "uuuuMMddHHmmss";

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
                                                                                .withResolverStyle(ResolverStyle.STRICT)
                                                                                .withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
                                                                                                DATETIME_FORMAT_COMPACT)
                                                                                        .withResolverStyle(
                                                                                                ResolverStyle.STRICT)
                                                                                        .withZone(
                                                                                                ZoneId.systemDefault());
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT)
                                                                            .withResolverStyle(ResolverStyle.STRICT)
                                                                            .withZone(ZoneId.systemDefault());

    public static final Set<LocalDate> HOLIDAYS = new HashSet<>();

    /**
     * String -> Instant
     *
     * @param timeString 仅支持 yyyy-MM-dd HH:mm:ss
     */
    public static Instant toInstant(String timeString) {
        return DATETIME_FORMATTER.parse(timeString, Instant::from);
    }

    /**
     * LocalDate -> Instant
     */
    public static Instant toInstant(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
    }

    /**
     * LocalDateTime -> Instant
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault())
                            .toInstant();
    }

    /**
     * String -> Date
     *
     * @param timeString 仅支持 yyyy-MM-dd HH:mm:ss
     */
    public static Date toDate(String timeString) {
        return Date.from(toInstant(timeString));
    }

    /**
     * LocalDate -> Date
     */
    public static Date toDate(LocalDate localDate) {
        return Date.from(toInstant(localDate));
    }

    /**
     * LocalDateTime -> Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(toInstant(localDateTime));
    }

    /**
     * LocalDateTime -> Date
     *
     * @param timeString 仅支持 yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime toLocalDateTime(String timeString) {
        return DATETIME_FORMATTER.parse(timeString, LocalDateTime::from);
    }

    /**
     * Instant -> LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                      .toLocalDateTime();
    }

    /**
     * Date -> LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date.toInstant());
    }

    /**
     * String -> LocalDate
     *
     * @param timeString 仅支持 yyyy-MM-dd 和 yyyy-MM-dd HH:mm:ss
     */
    public static LocalDate toLocalDate(String timeString) {
        try {
            return DATE_FORMATTER.parse(timeString, LocalDate::from);
        } catch (DateTimeParseException ignore) {
        }

        return DATETIME_FORMATTER.parse(timeString, LocalDate::from);
    }

    /**
     * Instant -> LocalDate
     */
    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                      .toLocalDate();
    }

    /**
     * Date -> LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDate(date.toInstant());
    }

    /**
     * 获取指定日期凌晨
     *
     * @param dateTime LocalDateTime
     * @return 凌晨时间
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime.with(LocalTime.MIN);
    }

    /**
     * 获取指定日期凌晨
     *
     * @param instant instant
     * @return 凌晨时间
     */
    public static Instant startOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                            .with(LocalTime.MIN)
                            .toInstant();
    }

    /**
     * 获取今天凌晨时刻
     */
    public static Instant startOfToday() {
        return ZonedDateTime.now()
                            .with(LocalTime.MIN)
                            .toInstant();
    }

    /**
     * 获取指定日期中午
     *
     * @param dateTime LocalDateTime
     * @return 中午时间
     */
    public static LocalDateTime noonOfDay(LocalDateTime dateTime) {
        return dateTime.with(LocalTime.NOON);
    }

    /**
     * 获取指定日期中午
     *
     * @param instant Instant
     * @return 中午时间
     */
    public static Instant noonOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                            .with(LocalTime.NOON)
                            .toInstant();
    }

    /**
     * 获取今天中午时刻
     */
    public static Instant noonOfToday() {
        return ZonedDateTime.now()
                            .with(LocalTime.NOON)
                            .toInstant();
    }

    /**
     * 获取指定日期午夜
     *
     * @param dateTime LocalDateTime
     * @return 午夜时间
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime.with(LocalTime.MAX)
                       .withNano(0);
    }

    /**
     * 获取指定日期午夜
     *
     * @param instant Instant
     * @return 午夜时间
     */
    public static Instant endOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                            .with(LocalTime.MAX)
                            .withNano(0)
                            .toInstant();
    }

    /**
     * 获取今天午夜时刻
     */
    public static Instant endOfToday() {
        return ZonedDateTime.now()
                            .with(LocalTime.MAX)
                            .withNano(0)
                            .toInstant();
    }

    /**
     * 获取月初时间
     *
     * @param dateTime 指定时间
     * @return 月初时间
     */
    public static LocalDate startOfMonth(LocalDate dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 当月月初
     *
     * @return 当月月初时间
     */
    public static LocalDate startOfMonth() {
        return startOfMonth(LocalDate.now());
    }

    /**
     * 获取月末时间
     *
     * @param localDate 指定时间
     * @return 月末时间
     */
    public static LocalDate endOfMonth(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 当月月末
     *
     * @return 当月月末日期
     */
    public static LocalDate endOfMonth() {
        return endOfMonth(LocalDate.now());
    }

    /**
     * 上一个星期一
     */
    public static LocalDate previousMonday() {
        return previousDayOfWeek(LocalDate.now(), DayOfWeek.MONDAY);
    }

    /**
     * 上一个星期五
     */
    public static LocalDate previousFriday() {
        return previousDayOfWeek(LocalDate.now(), DayOfWeek.FRIDAY);
    }

    /**
     * 上一个星期日
     */
    public static LocalDate previousSunday() {
        return previousDayOfWeek(LocalDate.now(), DayOfWeek.SUNDAY);
    }

    /**
     * 获取上一个星期X
     *
     * @param localDate 基准日期
     * @param dayOfWeek 周几
     * @return 上一个星期X
     */
    public static LocalDate previousDayOfWeek(LocalDate localDate, DayOfWeek dayOfWeek) {
        return localDate.with(TemporalAdjusters.previous(dayOfWeek));
    }

    /**
     * 下一个星期一
     */
    public static LocalDate nextMonday() {
        return nextDayOfWeek(LocalDate.now(), DayOfWeek.MONDAY);
    }

    /**
     * 下一个星期五
     */
    public static LocalDate nextFriday() {
        return nextDayOfWeek(LocalDate.now(), DayOfWeek.FRIDAY);
    }

    /**
     * 下一个星期日
     */
    public static LocalDate nextSunday() {
        return nextDayOfWeek(LocalDate.now(), DayOfWeek.SUNDAY);
    }

    /**
     * 获取下一个星期X
     *
     * @param localDate 基准日期
     * @param dayOfWeek 周几
     * @return 下一个星期X
     */
    public static LocalDate nextDayOfWeek(LocalDate localDate, DayOfWeek dayOfWeek) {
        return localDate.with(TemporalAdjusters.next(dayOfWeek));
    }

    /**
     * Instant日期类型格式化输出日期字符串
     *
     * @param instant 时间
     * @return 时间格式字符串
     */
    public static String format(Instant instant) {
        return DATETIME_FORMATTER.format(toLocalDateTime(instant));
    }

    /**
     * Date日期类型格式化输出日期字符串
     *
     * @param date 时间
     * @return 时间格式字符串
     */
    public static String format(Date date) {
        return DATETIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * LocalDateTime日期类型格式化输出日期字符串
     *
     * @param localDateTime 时间
     * @return 时间格式字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return DATETIME_FORMATTER.format(localDateTime);
    }

    /**
     * LocalDate日期类型格式化输出日期字符串
     *
     * @param localDate 时间
     * @return 时间格式字符串
     */
    public static String format(LocalDate localDate) {
        return DATETIME_FORMATTER.format(localDate.with(LocalTime.MIN));
    }

    /**
     * 是否周末
     *
     * @param localDate 日期
     * @return 周末则 true, 反之, false
     */
    public static boolean isWeekend(LocalDate localDate) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek(); return DayOfWeek.SUNDAY.equals(
                dayOfWeek) || DayOfWeek.SATURDAY.equals(dayOfWeek);
    }

    public static boolean isWeekend(Instant instant) {
        return isWeekend(toLocalDate(instant));
    }

    /**
     * 是否节假日
     *
     * @param localDate LocalDate
     * @return 节假日则 true, 反之, false
     */
    public static boolean isHoliday(LocalDate localDate) {
        return HOLIDAYS.contains(localDate);
    }

    public static boolean isHoliday(Instant instant) {
        return isHoliday(toLocalDate(instant));
    }

    /**
     * 获取星期几显示名称
     */
    public static String dayOfWeekName(LocalDate localDate) {
        return localDate.getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    public static String dayOfWeekName(Instant instant) {
        return dayOfWeekName(instant.atZone(ZoneId.systemDefault())
                                    .toLocalDate());
    }

    /**
     * 获取{@param currentDate}之前的{@param days}个日期
     *
     * @param currentDate 指定的日期
     * @param days        要获取的多少个日期
     * @return List<LocalDate>
     */
    public static List<LocalDate> beforeDays(LocalDate currentDate, int days, boolean includeDay) {
        return listDates(currentDate, days, -1, false, includeDay);
    }

    /**
     * 获取{@param currentDate}之后的{@param days}个日期
     *
     * @param currentDate 指定的日期
     * @param days        要获取的多少个日期
     * @return List<LocalDate>
     */
    public static List<LocalDate> afterDays(LocalDate currentDate, int days, boolean includeDay) {
        return listDates(currentDate, days, 1, false, includeDay);
    }

    /**
     * 获取{@param currentDate}之前的{@param days}个工作日日期
     *
     * @param currentDate 指定的日期
     * @param days        要获取的多少个日期
     * @return List<LocalDate>
     */
    public static List<LocalDate> beforeWeekdays(LocalDate currentDate, int days, boolean includeDay) {
        return listDates(currentDate, days, -1, true, includeDay);
    }

    /**
     * 获取{@param currentDate}之后的{@param days}个工作日日期
     *
     * @param currentDate 指定的日期
     * @param days        要获取的多少个日期
     * @return List<LocalDate>
     */
    public static List<LocalDate> afterWeekdays(LocalDate currentDate, int days, boolean includeDay) {
        return listDates(currentDate, days, 1, true, includeDay);
    }

    private static List<LocalDate> listDates(LocalDate currentDate, int days, int step, boolean skipWeekend,
                                             boolean includeDay) {
        days = Math.abs(days); List<LocalDate> daysList = new ArrayList<>(days); LocalDate temp = currentDate;
        // 包含当天
        if (includeDay) {
            daysList.add(temp);
        } do {
            temp = temp.plusDays(step);
            // 是否跳过周末
            if (skipWeekend && isWeekend(temp)) {
                continue;
            } daysList.add(temp);
        } while (days != daysList.size());

        return daysList;
    }

    /**
     * 是否闰年
     */
    public boolean isLeapYear(Instant instant) {
        return toLocalDate(instant).isLeapYear();
    }

}
