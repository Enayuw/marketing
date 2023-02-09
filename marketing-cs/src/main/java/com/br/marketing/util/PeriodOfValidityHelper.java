package com.br.marketing.util;

import java.math.BigDecimal;
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
    private final static Pattern PATTERN_RANGE_LEFT = Pattern.compile("^(\\[|\\()");
    private final static Pattern PATTERN_RANGE_RIGHT = Pattern.compile("([\\])])$");
    private final static String END_OF_MONTH = "M";

    /**
     * 2023-02-08 17:13
     * 有效期标准格式
     * [T+N] 包含数据当天,有效期就是1+N天
     * [T+0] 表示就是当天有效
     * [M] 有效期自然月
     *
     * @param periodOfValidityDayMap 有效期配置 eg:{"test":"T+30"}
     * @param key                    配置有效期key eg:test
     * @return null时为当前月底
     */
    public Integer getPeriodOfValidityDay(Map<Object, String> periodOfValidityDayMap, Object key)
            throws IllegalAccessException {
        if (periodOfValidityDayMap.containsKey(key)) {
            return periodOfValidityDay(periodOfValidityDayMap.get(key));
        }
        throw new IllegalAccessException("未知的配置有效期key:" + key);
    }

    /**
     * 2023-02-08 17:23
     * <p>
     * 有效期标准格式
     * [T+N] 包含数据当天,有效期范围是1+N天
     * [T+0] 表示就是当天有效
     * [M] 有效期自然月
     * <p>
     * eg：T+29 T为2023/3/1,有效期范围为2023/3/1至2023/3/30,共30天
     *
     * @param periodOfValidityStr 有效期配置,T+N代表当天+N天，共1+N天；T+0 代表当天；M代表到自然月月底
     * @return 返回null时为自然月末日期
     */
    public Integer periodOfValidityDay(String periodOfValidityStr) throws IllegalAccessException {
        Matcher matcher = PATTERN_DAY.matcher(periodOfValidityStr);
        if (matcher.find()) {
            String dayStr = matcher.group();
            return new BigDecimal(dayStr).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        } else if (periodOfValidityStr.contains(END_OF_MONTH)) {
            return null;
        } else {
            throw new IllegalAccessException("有效期格式错误，无法解析配置内容：" + periodOfValidityStr);
        }
    }
}
