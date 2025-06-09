package com.br.marketing.util;

import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.TreeSet;

/**
 * @author peng.kang
 * @description: 验签算法
 * @date 2025/5/21 15:12
 */
public class SignUtils {
    public static String yunKeSign(Map<String, Object> paramMap, String appKey) {
        TreeSet<String> ordered = Sets.newTreeSet(paramMap.keySet());
        StringBuilder forSign = new StringBuilder();
        for (String key : ordered) {
            forSign.append(key).append("=").append(paramMap.get(key)).append("&");
        }
        forSign.append("key=").append(appKey);
        String s = forSign.toString();
        return SecureUtil.md5(s).toUpperCase();
    }
}
