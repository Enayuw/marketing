package com.br.marketing.util.didiai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 滴滴 AI 客户端 appKey/appSecret 固定映射（临时硬编码）。
 *
 * <p>注意：当前不从 Speed/配置中心读取，需在合入主干或联调前将真实 appKey/appSecret 写入此处，或替换为配置读取实现。
 *
 * @author yueping.bai
 */
public final class DidiaiClientApps {

    private static final JSONArray APPS = buildApps();

    private DidiaiClientApps() {}

    public static String resolveAppSecret(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }
        for (int i = 0; i < APPS.size(); i++) {
            JSONObject one = APPS.getJSONObject(i);
            if (one == null) {
                continue;
            }
            if (appKey.equals(one.getString("appKey"))) {
                return one.getString("appSecret");
            }
        }
        return null;
    }

    public static String resolveFirstAppKey() {
        if (APPS.isEmpty()) {
            return null;
        }
        JSONObject first = APPS.getJSONObject(0);
        return first == null ? null : first.getString("appKey");
    }

    private static JSONArray buildApps() {
        JSONArray arr = new JSONArray();

        // TODO: 联调前填入真实 appKey/appSecret。示例：
        // JSONObject one = new JSONObject();
        // one.put("appKey", "CHANGE_ME_APP_KEY");
        // one.put("appSecret", "CHANGE_ME_APP_SECRET");
        // arr.add(one);

        return arr;
    }
}

