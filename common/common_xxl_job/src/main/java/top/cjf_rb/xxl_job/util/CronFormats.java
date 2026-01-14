package top.cjf_rb.xxl_job.util;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 常用且固定时间的Cron表达式格式化

 @author lty
 @since 1.0 */
public final class CronFormats {

    /**
     一次
     */
    static final DateTimeFormatter ONCE_FORMATTER = DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy")
                                                                     .withZone(ZoneId.systemDefault());
    /**
     每天
     */
    static final String EVERY_DAY_FORMAT = "%d %d %d * * ?";
    /**
     每周几
     */
    static final String EVERY_WEEK_FORMAT = "%d %d %d ? * %d";
    /**
     月初
     */
    static final String FIRST_DAY_OF_MONTH_FORMAT = "%d %d %d 1 * ?";
    /**
     月末
     */
    static final String LAST_DAY_OF_MONTH_FORMAT = "%d %d %d L * ?";

    /**
     获取执行一次的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String once(@NonNull Instant timePoint) {
        Assert.isTrue(Instant.now()
                             .isBefore(timePoint), "调度时间必须是未来时间!");
        return ONCE_FORMATTER.format(timePoint);
    }

    /**
     获取执行一次的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String once(@NonNull LocalDate timePoint) {
        Assert.isTrue(LocalDate.now()
                               .isBefore(timePoint), "调度时间必须是未来时间!");
        return ONCE_FORMATTER.format(timePoint.atStartOfDay());
    }

    /**
     获取执行一次的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String once(@NonNull LocalDateTime timePoint) {
        Assert.isTrue(LocalDateTime.now()
                                   .isBefore(timePoint), "调度时间必须是未来时间!");
        return ONCE_FORMATTER.format(timePoint);
    }

    /**
     获取每天, 指定时间点执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String everyDay(@NonNull LocalTime timePoint) {
        return String.format(EVERY_DAY_FORMAT, timePoint.getSecond(), timePoint.getMinute(), timePoint.getHour());
    }

    /**
     获取每周一, 指定时间点执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String everyMonday(@NonNull LocalTime timePoint) {
        return String.format(EVERY_WEEK_FORMAT, timePoint.getSecond(), timePoint.getMinute(), timePoint.getHour(), 1);
    }

    /**
     获取每周五, 指定时间点执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String everyFriday(@NonNull LocalTime timePoint) {
        return String.format(EVERY_WEEK_FORMAT, timePoint.getSecond(), timePoint.getMinute(), timePoint.getHour(), 5);
    }

    /**
     获取每周末, 指定时间点执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String everySunday(@NonNull LocalTime timePoint) {
        return String.format(EVERY_WEEK_FORMAT, timePoint.getSecond(), timePoint.getMinute(), timePoint.getHour(), 7);
    }

    /**
     获取每周几, 指定时间点执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String everyWeek(@NonNull LocalTime timePoint, DayOfWeek dayOfWeek) {
        return String.format(EVERY_WEEK_FORMAT, timePoint.getSecond(), timePoint.getMinute(), timePoint.getHour(),
                             dayOfWeek.getValue());
    }

    /**
     获取每月初, 指定时间执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String firstDayOfMonth(@NonNull LocalTime timePoint) {
        return String.format(FIRST_DAY_OF_MONTH_FORMAT, timePoint.getSecond(), timePoint.getMinute(),
                             timePoint.getHour());
    }

    /**
     获取每月末, 指定时间执行的cron表达式

     @param timePoint 时间点
     @return 时间点cron表达式
     */
    public static String lastDayOfMonth(@NonNull LocalTime timePoint) {
        return String.format(LAST_DAY_OF_MONTH_FORMAT, timePoint.getSecond(), timePoint.getMinute(),
                             timePoint.getHour());
    }

}
