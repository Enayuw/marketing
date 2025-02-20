package com.br.marketing.client.hxchannel;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class HxChannelClient {

    public static void main(String[] args) {
        // 示例数据
        Map<String, String> data = new HashMap<>();
        data.put("channel_id", "umOFo6Lmtx7z8Xpk");
        data.put("task", "6+");
        data.put("page", "1");
        data.put("limit", "100");

        String channelKey = "o7nY20ah2NBuEafQV1NmuYVgr8EEWRlp";

        String sign = generateSign(data, channelKey);
        data.put("sign",sign);
        System.out.println("Generated sign: " + data);
    }

    public static String generateSign(Map<String, String> data, String channelKey) {
        // 1. 过滤掉空值和 sign 字段
        Map<String, String> filteredData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty() && !"sign".equalsIgnoreCase(entry.getKey())) {
                filteredData.put(entry.getKey().toUpperCase(), entry.getValue()); // 转大写
            }
        }

        // 2. 按 ASCII 从小到大排序（字典序）
        List<String> keys = new ArrayList<>(filteredData.keySet());
        Collections.sort(keys);

        // 3. 使用 URL 键值对格式拼接成字符串 A
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(filteredData.get(key));
        }

        // 4. 拼接 channelKey
        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append("key=").append(channelKey);

        String signTemp = sb.toString();
        System.out.println("待加密："+signTemp);

        // 5. 对 signTemp 进行 MD5 运算并转换为大写
        return DigestUtils.md5DigestAsHex(signTemp.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }
}
