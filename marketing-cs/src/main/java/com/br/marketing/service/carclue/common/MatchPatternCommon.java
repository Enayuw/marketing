package com.br.marketing.service.carclue.common;

import com.br.common.util.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MatchPatternCommon {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s*");

    /**
     * 精确/完全匹配方法
     *
     * @param content  匹配值
     * @param listData 匹配集合
     * @return Boolean true：匹配成功 false：未匹配
     */
    public static Boolean completeMatch(String content, List<String> listData) {
        if (StringUtils.isEmpty(content)) {
            return Boolean.FALSE;
        }
        if (listData.contains(content)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }


    /**
     * 模糊匹配方法
     *
     * @param content  匹配值
     * @param listData 匹配集合
     * @return Boolean true：匹配成功 false：未匹配
     */
    public static Boolean fuzzyMatch(String content, List<String> listData) {
        if (StringUtils.isEmpty(content)) {
            return Boolean.FALSE;
        }
        if (listData.contains(content)) {
            return Boolean.TRUE;
        }
        List result = listData.stream().filter(target ->
                (StringUtils.containsIgnoreCase(content, target) || StringUtils.containsIgnoreCase(target, content))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(result)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }

    /**
     * 模糊匹配取最短的字段
     *
     * @param content  匹配值
     * @param listData 匹配集合
     * @param filterStr 需要过滤的特殊字符
     * @return String 匹配字符串
     */
    public static String fuzzyMatchByShort(String content, List<String> listData, String filterStr) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (listData.contains(content)) {
            return content;
        }
        String filterRegex = "[" + filterStr + "]";
        String source = removeWhitespace(content).replaceAll(filterRegex, "");
        Map<String, String> targetMap =  new HashMap<>();
        listData.forEach(data->{
            targetMap.put(removeWhitespace(data).replaceAll(filterRegex, ""),data);
        });
        List<String> result = targetMap.keySet().stream().filter(target ->
                (StringUtils.containsIgnoreCase(source, target) || StringUtils.containsIgnoreCase(target, source))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        String shortestString = result.stream().min(Comparator.comparingInt(String::length)).orElse(null);

        return StringUtils.isNotEmpty(shortestString) ? targetMap.get(shortestString) : null;

    }



    public static String removeWhitespace(String content) {
        Matcher matcher = WHITESPACE_PATTERN.matcher(content);
        // 使用replaceAll方法替换所有匹配到的空白字符为空字符串
        String result = matcher.replaceAll("");
        return result;
    }


}
