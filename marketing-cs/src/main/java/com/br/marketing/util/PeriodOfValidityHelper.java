package com.br.marketing.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 计算有效期辅助工具
 *
 * @author Guo Zeqiang
 * @dateTime 2023-02-08 17:07
 */
public class PeriodOfValidityHelper {

    /**
     * 2023-02-08 17:03
     * 解析配置有效期格式
     */
    private final static Pattern PATTERN_DAY = Pattern.compile("[-+]?\\d+(\\.\\d+)?");
    //    private final static Pattern PATTERN_RANGE_LEFT = Pattern.compile("^(\\[|\\()");
    //    private final static Pattern PATTERN_RANGE_RIGHT = Pattern.compile("([\\])])$");
    private final static String END_OF_MONTH = "M";
    private final static String T = "T";

    /**
     * 2023-02-08 17:13
     * 获取对应key的有效期天数
     *
     * @param periodOfValidityDayMap 有效期配置 eg:{"test":"[T+30]"}
     * @param key                    配置有效期key eg:test
     * @return 返回{@code null}时为自然月末日期
     */
    public static Integer getPeriodOfValidityDay(Map<Object, String> periodOfValidityDayMap, Object key)
            throws IllegalArgumentException {
        if (periodOfValidityDayMap.containsKey(key)) {
            return getPeriodOfValidityDay(periodOfValidityDayMap.get(key));
        }
        throw new IllegalArgumentException("未知的配置有效期key:" + key);
    }

    /**
     * 2023-02-14 10:13
     * 获取对应key的有效期天数，
     * 在获取距离N个自然月月末的天数时，需指定开始计算日期{@code date}
     *
     * @param periodOfValidityDayMap 有效期配置 eg:{"test":"[T+30]"}
     * @param key                    配置有效期key eg:test
     * @param date                   计算N个自然月月末时，指定的开始计算日期，为{@code null}时使用当前时间
     * @return 返回{@code null}时为自然月末日期
     */
    public static Integer getPeriodOfValidityDay(Map<Object, String> periodOfValidityDayMap, Object key, Date date)
            throws IllegalArgumentException {
        if (periodOfValidityDayMap.containsKey(key)) {
            return getPeriodOfValidityDay(periodOfValidityDayMap.get(key), date);
        }
        throw new IllegalArgumentException("未知的配置有效期key:" + key);
    }

    /**
     * 2023-02-08 17:23
     * 获取有效期配置中的天数
     *
     * <p>
     * 有效期标准格式
     * [T+N] 代表当天到N天,有效期共N+1天，例如：[T+30]，有效期共：31天（包含当天），假如当前日期为01月01日，有效期范围是01月01日~01月31日闭区间
     * <p>
     * [T+0] 代表当天，例如：[T+0]，有效期共：1天（当天），假如当前日期为01月01日，有效期范围是01月01日~01月01日闭区间。
     * <p>
     * [M] 代表当天到当前自然月月末，例如：[M]，有效期为当天到当月月末，假如当前日期为01月15日，有效期范围是01月15日~01月31日闭区间。
     * <p>
     *
     * @param periodOfValidityStr 有效期配置,T+N代表当天+N天，共1+N天；T+0 代表当天；M代表到自然月月底
     * @return 返回{@code null}时为自然月末日期
     */
    public static Integer getPeriodOfValidityDay(String periodOfValidityStr) throws IllegalArgumentException {
        return getPeriodOfValidityDay(periodOfValidityStr, null);
    }

    /**
     * 2023-02-14 09:23
     * 获取有效期配置中的天数，
     * 在获取距离N个自然月月末的天数时，需指定开始计算日期{@code date}
     *
     * <p>
     * 有效期标准格式
     * [T+/-N] 代表当天到N天,有效期共N+1天，例如：[T+30]，有效期共：31天（含当天），假如当前日期为01月01日，有效期范围是01月01日~01月31日闭区间
     * <p>
     * [T+/-0]或[T] 代表当天，例如：[T+0]，有效期共：1天（当天），假如当前日期为01月01日，有效期范围是01月01日~01月01日闭区间。
     * <p>
     * [M+/-N] 代表当天到下N个自然月月末，例如：[M+1]，有效期为当天到下月月末，假如当前日期为03月15日，有效期范围是03月15日~04月30日闭区间
     * <p>
     * [M+/-0]或[M] 代表当天到当前自然月月末，例如：[M]，有效期为当天到当月月末，假如当前日期为01月15日，有效期范围是01月15日~01月31日闭区间。
     * <p>
     * eg：[T+29] T为2023/3/1,有效期范围为2023/3/1至2023/3/30,共30天
     *
     * @param periodOfValidityStr 有效期配置,[T+N]代表当天+N天，共N+1天；[T+0] 代表当天；[M]代表到自然月月底;[M+N]代表当前时间到N个月月末
     * @param date                计算N个自然月月末时，指定的开始计算日期，为{@code null}时使用当前时间
     * @return 返回{@code null}时为当前自然月末日期
     */
    public static Integer getPeriodOfValidityDay(String periodOfValidityStr, Date date) throws IllegalArgumentException {
        if (StringUtils.isBlank(periodOfValidityStr)) {
            throw new IllegalArgumentException("未配置有效期！");
        }
        Matcher matcher = PATTERN_DAY.matcher(periodOfValidityStr);
        Integer day = null;
        if (matcher.find()) {
            String dayStr = matcher.group();
            day = new BigDecimal(dayStr).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        if (periodOfValidityStr.contains(T)) {
            return day == null ? 0 : day;
        } else if (periodOfValidityStr.contains(END_OF_MONTH)) {
            if (day == null) {
                return day;
            }
            LocalDate localDate = date == null ? LocalDate.now()
                    : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate lastLocalDate = localDate.plusMonths(day).with(TemporalAdjusters.lastDayOfMonth());
            return Long.valueOf(localDate.until(lastLocalDate, ChronoUnit.DAYS)).intValue();
        } else {
            throw new IllegalArgumentException("有效期格式错误，无法解析配置内容:" + periodOfValidityStr);
        }
    }
}
