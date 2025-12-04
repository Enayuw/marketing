package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: jinwei.li@100credit.com
 * @Date: 2018/5/25 14:40
 */
@Slf4j
public class DateHelper {

    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";
    public static final String LINE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SLASH_DATE_FORMAT = "yyyy/MM/dd";

    public static final String SHORT_TIME_FORMAT = "HHmmss";
    public static final String LINE_TIME_FORMAT = "HH-mm-ss";
    public static final String SLASH_TIME_FORMAT = "HH/mm/ss";
    public static final String COLON_TIME_FORMAT = "HH:mm:ss";

    public static final String SHORT_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    public static final String LINE_DATE_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    public static final String SLASH_DATE_TIME_FORMAT = "yyyy/MM/dd/HH/mm/ss";

    public static final String LINE_DATE_COLON_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String LINE_DATE_COLON_TIME_FORMAT_SSS = "yyyy-MM-dd HH:mm:ss[:SSS]";

    private static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormat.forPattern("yyyy-MM-dd"),
            DateTimeFormat.forPattern("yy-MM-dd")
    );

    //key为正则 value为日期格式
    private static Map<String, String> PATTERNS = new HashMap();

    static {
        {
            PATTERNS.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
            PATTERNS.put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
            PATTERNS.put("^\\d{6,8}$", "yyyyMMdd");
            PATTERNS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss");
        }
    }

    ;

    /**
     * 转换字符串为日期
     *
     * @param str
     * @return
     */
    public static Date parseDate(String str) {
        if (str == null) {
            throw new IllegalArgumentException("日期格式错误");
        }
        str = str.trim();
        for (Map.Entry<String, String> entry : PATTERNS.entrySet()) {
            String key = entry.getKey();
            if (str.matches(key)) {
                //日期转换
                DateTimeFormatter format = DateTimeFormat.forPattern(entry.getValue());
                DateTime dateTime = DateTime.parse(str, format);
                return dateTime.toDate();
            }
        }
        throw new IllegalArgumentException("日期格式错误");
    }

    public static String getDateAdd(int days) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        return sf.format(c.getTime());
    }

    public static String getDateAddYyMmDd(int days) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return sf.format(c.getTime());
    }

    public static String getDateAddYyMmDdHhMmSs(int days) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return sf.format(c.getTime());
    }

    public static String getDateByMinute(int minute) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, minute);
        return sf.format(c.getTime());
    }

    public static String getDateByHour(int minute) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, minute);
        return sf.format(c.getTime());
    }

    public static Date getDateByHour(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTime();
    }

    public static int daysBetween(String dateStr) throws ParseException {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date compareDate = sdf.parse(dateStr);

        Calendar cal = Calendar.getInstance();
        cal.setTime(compareDate);
        long time1 = cal.getTimeInMillis();

        cal.setTime(today);
        long time2 = cal.getTimeInMillis();
        long betweenDays = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(betweenDays));
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.parseLong(seconds + "000")));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            log.error("date2TimeStamp error", e);
        }
        return "";
    }

    /**
     * 两个时间之间相差距离多少天
     *
     * @return 相差天数
     */
    public static long getDistanceDays(String str1, String str2) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date one;
        Date two;
        long days = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff = time2 - time1;
            days = diff / (1000 * 60 * 60 * 24);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    /**
     * 和当前日期相差多少分钟
     *
     * @param str 时间参数  格式：2009-01-01 12:00:00
     * @return long
     */
    public static long getDistanceMinutes(String str) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long now = System.currentTimeMillis();
        long min = 0;
        try {
            Date two = df.parse(str);
            long time = two.getTime();
            long diff = time - now;
            min = diff / (60 * 1000);
        } catch (ParseException e) {
            log.error("getDistanceMinutes error", e);
        }
        return min > 0 ? min : min * -1;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return long[] 返回值为：{天, 时, 分, 秒}
     */
    public static long[] getDistanceTimes(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long[] times = {day, hour, min, sec};
        return times;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：xx天xx小时xx分xx秒
     */
    public static String getDistanceTime(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day + "天" + hour + "小时" + min + "分" + sec + "秒";
    }

    public static Date getNowDayStartTime() {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).concat(" 00:00:00");
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static Date getNowDayEndTime() {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).concat(" 23:59:59");
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static Date addDays(Date date, Integer days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    /**
     * 时间日期转换
     *
     * @param strDate 字符串
     * @return 字符串yyyy-MM-dd HH:mm:ss
     */
    public static String strToDateLong(String strDate) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return str;
    }

    /**
     * 时间日期定制
     *
     * @param date+hhmmss（Date+时分秒）
     * @return Date
     */
    public static Date getDatePlusHourMinuteSecond(Date date, String hhmmss) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd").format(date).concat(hhmmss);
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取传入时间与第二天凌晨相差秒数
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

    public static LocalDate strToLocalDate(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        LocalDate res = null;
        try {
            if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{2}/\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:[SSS]"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return res;
    }

//    public static void main(String[] args) {
//        Long aLong = strToMill("2023-09-14");
//        Long bLong = strToMill("2023-09-14 09");
//        Long cLong = strToMill("2023-09-14 09:52");
//        Long dLong = strToMill("2023-09-14 09:52:20");
//        Long eLong = strToMill("2023/09/14");
//        Long fLong = strToMill("2023/09/14 09:52");
//        Long gLong = strToMill("2023/09/14 09:52:20");
//        System.out.println("1-----"+aLong);
//        System.out.println("2-----"+bLong);
//        System.out.println("3-----"+cLong);
//        System.out.println("4-----"+dLong);
//        System.out.println("5-----"+eLong);
//        System.out.println("6-----"+fLong);
//        System.out.println("7-----"+gLong);
//    }

    public static Long strToMill(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        Long res = null;
        try {
            if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{2}/\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                s += " 00:00:00";
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:[SSS]")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}$", date)) {
                String s = date.replaceAll("/", "-");
                res = LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH")).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * 判断string是否为日期yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static boolean isDate(String date) {
        boolean isDate = Boolean.FALSE;
        try {
            DateUtils.parseDateStrictly(date, LINE_DATE_FORMAT);
            isDate = Boolean.TRUE;
        } catch (ParseException e) {
            isDate = Boolean.FALSE;

        }
        return isDate;
    }



    /**
     * 转化T+n/T-n为日期格式
     *
     * @param  dateStr
     * @return LocalDate
     */


    public static String dateTNtransfer(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        String res = null;

        try {
            if (Pattern.matches("^T([+-])(\\d+)$", dateStr)) {
                String operator = dateStr.substring(1, 2);
                int num = ("-").equals(operator) ? Integer.valueOf(dateStr.substring(1)) : Integer.valueOf(dateStr.substring(2));
                res = LocalDate.now().plusDays(num).toString();
            } else {
                res = dateStr;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * @description Date转为时分秒时间戳
     * @param date
     * @return java.lang.String
     * @author hedongshuo
     * @date 2024/8/21 20:55
     **/
    public static String dateToDateTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = format.format(date);
        return dateTime;
    }


    /**
     * 获取当前时间减去时间范围后的日期（无时间部分）
     *
     * @param unit   时间单位，d-天，m-月
     * @param amount 时间量级（必须 >= 0）
     * @return 计算后的日期，格式：yyyy-MM-dd
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static LocalDate getPreviousDate(String unit, int amount) {
        return calculateDate(unit, amount);
    }

    /**
     * 获取当前时间减去时间范围后的日期时间（包含时间部分）
     *
     * @param unit   时间单位，d-天，m-月
     * @param amount 时间量级（必须 >= 0）
     * @return 计算后的日期时间，格式：yyyy-MM-dd HH:mm:ss
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static LocalDateTime getPreviousDateTime(String unit, int amount) {
        return calculateDateTime(unit, amount);
    }


    // 核心计算方法（返回 LocalDate）
    private static LocalDate calculateDate(String unit, int amount) {
        validateParameters(unit, amount);
        LocalDate now = LocalDate.now();
        switch (unit.toLowerCase()) {
            case "d":
                return now.minusDays(amount);
            case "m":
                return now.minusMonths(amount);
            default:
                throw new IllegalArgumentException("不支持的时间单位: " + unit);
        }
    }

    // 核心计算方法（返回 LocalDateTime）
    private static LocalDateTime calculateDateTime(String unit, int amount) {
        validateParameters(unit, amount);
        LocalDateTime now = LocalDateTime.now();
        switch (unit.toLowerCase()) {
            case "d":
                return now.minusDays(amount);
            case "m":
                return now.minusMonths(amount);
            default:
                throw new IllegalArgumentException("不支持的时间单位: " + unit);
        }
    }

    // 参数校验
    private static void validateParameters(String unit, int amount) {
        Objects.requireNonNull(unit, "时间单位不能为 null");
        if (amount < 0) {
            throw new IllegalArgumentException("时间量级不能为负数");
        }
        if (!unit.equalsIgnoreCase("d") && !unit.equalsIgnoreCase("m")) {
            throw new IllegalArgumentException("仅支持 d（天）和 m（月）");
        }
    }

    public static Date stringToDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        String trimmedDate = dateString.trim();
        int length = trimmedDate.length();
        try {
            if (length == 19) { // yyyy-MM-dd HH:mm:ss
                return DateTime.parse(trimmedDate, DATE_FORMATS.get(0)).toDate();
            } else if (length == 10) { // yyyy-MM-dd
                return DateTime.parse(trimmedDate, DATE_FORMATS.get(1)).toDate();
            } else if (length == 8) { // yy-MM-dd
                return DateTime.parse(trimmedDate, DATE_FORMATS.get(2)).toDate();
            }
        } catch (Exception e) {
            log.error(dateString + "日期转化异常：" + e.getMessage());
        }
        return null;
    }

    public static String timestampToDateTime(Long timestamp) {
        try {
            if (Objects.isNull(timestamp)) {
                return null;
            }
            // 10位转13位
            if (timestamp < 9999999999L) {
                timestamp = timestamp * 1000;
            }
            return DateFormatUtils.format(timestamp, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error("时间戳转化为日期错误，timestamp={}", timestamp);
            return String.valueOf(timestamp);
        }
    }

    /**
     * Date转日期
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(LINE_DATE_FORMAT);
            return sdf.format(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误: " + LINE_DATE_FORMAT, e);
        }
    }

    /**
     * Date转时分秒
     * @param date
     * @return
     */
    public static String formatHMS(Date date) {
        if (date == null) {
            return null;
        }
        LocalTime localTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern(COLON_TIME_FORMAT);
        return localTime.format(formatter);
    }




}
