package com.br.marketing.service.Impl.zhijia;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.ZhiJiaClueBackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 宜信基础数据实现类
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:39
 */
@Service
@Slf4j
public class ZhiJiaCarInfoGetServiceImpl implements ZhiJiaCarInfoGetService {


    @Override
    public JSONObject getZhiJiaCarInfo(ZhiJiaClueBackData zhiJiaClueBackInfo) {
        JSONObject jsonObject = new JSONObject();
        String brandid = "";
        String seriesid = "";
        String brandName = zhiJiaClueBackInfo.getBrandName();
        String seriesName = zhiJiaClueBackInfo.getSeriesName();

        jsonObject.put("brandid",brandid);
        jsonObject.put("seriesid",seriesid);
        return jsonObject;
    }

    /**
     * 精准匹配两个字符串，返回匹配结果
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等则返回true，否则返回false
     */
    public static boolean preciseMatch(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 车品牌模糊匹配，返回布尔结果
     *
     * @param brand 车品牌名称
     * @param pattern 匹配模式
     * @return 如果brand匹配pattern则返回true，否则返回false
     */
    public static boolean complexFuzzyMatch(String brand, String pattern) {
        if (brand == null || pattern == null) {
            return false;
        }

        // 忽略大小写的匹配
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(brand);

        return matcher.find();
    }

    /**
     * 车系模糊匹配，返回布尔结果
     *
     * @param carSeries 车系名称
     * @param pattern 匹配模式
     * @return 如果carSeries匹配pattern则返回true，否则返回false
     */
    public static boolean complexCarSeriesFuzzyMatch(String carSeries, String pattern) {
        if (carSeries == null || pattern == null) {
            return false;
        }

        // 忽略大小写并去除非字母数字字符进行匹配
        String normalizedCarSeries = carSeries.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String normalizedPattern = pattern.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // 编译正则表达式
        Pattern compiledPattern = Pattern.compile(normalizedPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(normalizedCarSeries);

        return matcher.find();
    }
}
