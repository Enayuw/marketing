package com.br.marketing.service.carclue.common;

import com.br.common.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchPatternCommon {


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
     * @return String 匹配字符串
     */
    public static String fuzzyMatchByShort(String content, List<String> listData) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (listData.contains(content)) {
            return content;
        }
        List<String> result = listData.stream().filter(target ->
                (StringUtils.containsIgnoreCase(content, target) || StringUtils.containsIgnoreCase(target, content))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        Optional<String> shortestString = result.stream().min(String::compareToIgnoreCase);

        return shortestString.isPresent() ? shortestString.get() : null;

    }


}
