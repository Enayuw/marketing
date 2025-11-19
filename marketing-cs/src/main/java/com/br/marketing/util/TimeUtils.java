package com.br.marketing.util;

import com.br.marketing.entity.common.TimeRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 日期工具类
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.check.utils
 * @Description: 日期工具类
 * @CreateTime: 2022-07-01 14 :39
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Slf4j
public class TimeUtils {
    private TimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_ALL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_STRING = "yyyyMMdd";
    public static final String YMDHMS = "yyyyMMddHHmmss";



    public static List<String> pattern = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-M-dd HH:mm:ss",
            "yyyy/M/dd HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss"
            // 可以添加更多可能的格式
    );
    /**
     * 转化日期
     *
     * @param date
     * @return
     */
    public static String parseDateToString3return(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(date);
    }

    /**
     * 转化日期
     *
     * @param date
     * @return
     */
    public static String parseDateToStr(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_ALL);
        return df.format(date);
    }

    /**
     * 转化日期
     *
     * @param date
     * @return
     */
    public static String parseDateToStrNo(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_STRING);
        return df.format(date);
    }

    /**
     * 格式转换 字符串转 时间
     *
     * @param time
     * @return
     */
    public static Date parseStringToDate(String time) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date date = null;
        if (StringUtils.isBlank(time)) {
            return null;
        } else {
            try {
                date = df.parse(time);
            } catch (ParseException e) {
                log.error("年月日parseStringToDate错误", e);
            }
        }
        return date;
    }

    /**
     * 格式转换 字符串转 时间
     *
     * @param time
     * @return
     */
    public static Date parseStringToTime(String time) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_ALL);
        Date date = null;
        if (StringUtils.isBlank(time)) {
            return null;
        } else {
            try {
                date = df.parse(time);
            } catch (ParseException e) {
                log.error("parseStringToTime错误", e);
            }
        }
        return date;
    }

    /**
     * 获取当前日期
     *
     * @param format
     * @return
     */
    public static String getNowDate(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    /**
     * 把日期(yyyy-MM-dd)往后增加n小时
     *
     * @return
     */
    public static String addDayHours(String basicDate, int hours) {
        if (StringUtils.isBlank(basicDate)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_ALL);
        String tmpDateStr = null;
        try {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(parseStringToDate(basicDate));
            // 把日期往后增加n小时.整数往后推,负数往前移动
            calendar.add(Calendar.HOUR, hours);
            // 这个时间就是日期往后推n小时的结果
            tmpDateStr = df.format(calendar.getTime());
        } catch (Exception e) {
            log.error("addDayHours错误", e);
        }
        return tmpDateStr;
    }

    /**
     * 指定日期加或减days天
     *
     * @param date 日期
     * @param days 天数
     * @return
     */
    public static Date addDay(Date date, int days) {
        Calendar dateC = Calendar.getInstance();
        dateC.setTime(date);
        dateC.add(Calendar.DAY_OF_YEAR, days);
        return dateC.getTime();
    }

    /**
     * 根据日期获取（前或后N天）的时间
     *
     * @param basicDate
     * @param n
     * @return
     */
    public static String nDaysAfterOneDateString(String basicDate, int n) {
        if (basicDate == null || "".equals(basicDate)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String tmpDateStr = null;
        try {
            Date tmpDate = df.parse(basicDate);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(tmpDate);
            // 把日期往后增加一天.整数往后推,负数往前移动
            calendar.add(Calendar.DATE, n);
            // 这个时间就是日期往后推一天的结果
            tmpDateStr = df.format(calendar.getTime());
        } catch (ParseException e) {
            log.error("年月日nDaysAfterOneDateString错误", e);
        }
        return tmpDateStr;
    }

    /**
     * @param model 1:">" 2:"<" 3:">=" 4:"<=" 5:"="
     * @param date1
     * @param date2
     * @return 比较两个时间的大小 返回true 或者false
     */
    public static boolean compareDate(String date1, String date2, Integer model, String format) {
        DateFormat df = new SimpleDateFormat(format);
        boolean b = false;
        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            switch (model) {
                case 1:
                    if (dt1.getTime() > dt2.getTime()) {
                        b = true;
                    }
                    break;
                case 2:
                    if (dt1.getTime() < dt2.getTime()) {
                        b = true;
                    }
                    break;
                case 3:
                    if (dt1.getTime() >= dt2.getTime()) {
                        b = true;
                    }
                    break;
                case 4:
                    if (dt1.getTime() <= dt2.getTime()) {
                        b = true;
                    }
                    break;
                case 5:
                    if (dt1.getTime() == dt2.getTime()) {
                        b = true;
                    }
                    break;
                default:
                    break;
            }

        } catch (ParseException e) {
            log.error("年月日compareDate错误", e);
        }
        return b;
    }

    /**
     * 日期格式校验
     *
     * @param str
     * @return
     */
    public static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        // 指定日期格式
        SimpleDateFormat a = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat b = new SimpleDateFormat(DATE_STRING);
        if (str.contains("-") && str.length() == 10) {
            try {
                // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
                a.setLenient(false);
                a.parse(str);
            } catch (ParseException e) {
                log.error("isValidDate error,str:{}", str, e);
                // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
                convertSuccess = false;
            }
        } else if (str.length() == 8) {
            try {
                // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
                b.setLenient(false);
                b.parse(str);
            } catch (ParseException e) {
                log.error("isValidDate error,str:{}", str, e);
                // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
                convertSuccess = false;
            }
        } else {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 月份第一天
     *
     * @param basicDate
     * @return
     */
    public static String getFirstDayToDate(String basicDate) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(parseStringToDate(basicDate));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return df.format(calendar.getTime());
    }

    /**
     * 月份最后一天
     *
     * @param basicDate
     * @return
     */
    public static String getLastDayToDate(String basicDate) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(parseStringToDate(basicDate));
        //加一个月
        calendar.add(Calendar.MONTH, 1);
        //设置为该月第一天
        calendar.set(Calendar.DATE, 1);
        //再减一天即为上个月最后一天
        calendar.add(Calendar.DATE, -1);
        return df.format(calendar.getTime());
    }

    /**
     * 获取当前到12点的秒数
     *
     * @param currentDate
     * @return
     */
    public static Integer getRemainSecondsOneDay(Date currentDate) {
        //使用plusDays加传入的时间加1天，将时分秒设置成0
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                        ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        //使用ChronoUnit.SECONDS.between方法，传入两个LocalDateTime对象即可得到相差的秒数
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }

    public static String millisecondsToString(long milliseconds) {
        final long day = TimeUnit.MILLISECONDS.toDays(milliseconds);

        final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));

        final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        final long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        final long ms = TimeUnit.MILLISECONDS.toMillis(milliseconds)
                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(milliseconds));

        System.out.println("milliseconds :-" + milliseconds);
        return String.format(String.format("%d 天 %d 小时 %d 分 %d 秒", day, hours, minutes, seconds));
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * @author jqlin
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean timeCompare(String startTime,String endTime){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localStartTime = LocalTime.parse(startTime,dtf);
        LocalTime localEndTime = LocalTime.parse(endTime,dtf);
        return LocalTime.now().isAfter(localStartTime) && LocalTime.now().isBefore(localEndTime);
    }
    /**
     * 处理时间格式的方法
     *
     * @param value 待处理的时间类型的值
     * @return String 格式化后的时间值（yyyy-MM-dd HH:mm:ss）
     */
    public static String getFormatterValue(String value,String pattern) {
        Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                date = sdf.parse(value);
            } catch (ParseException e) {
                if (log.isInfoEnabled()) {
                    log.warn("无法解析日期;格式:{};原值:{}", pattern, value);
                }
            }
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            value = formatter.format(date);
        } else {
            log.error("无法解析日期:{}", value);
        }
        return value;
    }

    /**
     * 将一段时间，按自然日分割
     * @param releaseTimeBegin
     * @param releaseTimeEnd
     * @return
     */
    public static List<TimeRange> splitByNaturalDays(LocalDateTime releaseTimeBegin, LocalDateTime releaseTimeEnd) {
        // 验证时间范围有效性
        if (releaseTimeBegin == null || releaseTimeEnd == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        if (releaseTimeBegin.isAfter(releaseTimeEnd)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        List<TimeRange> result = new ArrayList<>();

        // 获取开始日期和结束日期
        LocalDate startDate = releaseTimeBegin.toLocalDate();
        LocalDate endDate = releaseTimeEnd.toLocalDate();

        // 如果开始和结束在同一天
        if (startDate.equals(endDate)) {
            result.add(new TimeRange(releaseTimeBegin, releaseTimeEnd));
            return result;
        }

        // 处理第一天
        LocalDateTime firstDayEnd = startDate.atTime(LocalTime.MAX);
        result.add(new TimeRange(releaseTimeBegin, firstDayEnd));

        // 处理中间的完整天数
        LocalDate currentDate = startDate.plusDays(1);
        while (currentDate.isBefore(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(LocalTime.MAX);
            result.add(new TimeRange(dayStart, dayEnd));
            currentDate = currentDate.plusDays(1);
        }

        // 处理最后一天
        LocalDateTime lastDayStart = endDate.atStartOfDay();
        result.add(new TimeRange(lastDayStart, releaseTimeEnd));

        return result;
    }

    //7883
    public static void main(String[] args) {
        System.out.println(getRemainSecondsOneDay(new Date()));
    }
}
