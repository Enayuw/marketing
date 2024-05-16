package com.br.marketing.util.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XieChengEsJsonHandler {


    /**
     * 携程JsonEs条件预处理
     * @param jsonObject json条件
     * @param collidingFilterDTO 撞库条件实体
     * @return
     */
    public static void handlerJson(JSONObject jsonObject, XieChengCollidingFilterDTO collidingFilterDTO) {
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Map<String, String> releaseTimeMap = new HashMap<>();
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonData = (JSONObject) iterator.next();
            if (jsonData.getString("type").equals("operation")) {
                if (jsonData.getString("key").equals("release_time")) {
                    releaseTimeMap.put("value", jsonData.getString("value"));
                    releaseTimeMap.put("operation", jsonData.getString("operation"));
                    releaseTimeMap.put("key", jsonData.getString("key"));
                    collidingFilterDTO.setReleaseTime(releaseTimeMap);
                    iterator.remove();
                } else if (jsonData.getString("key").equals("result")) {
                    collidingFilterDTO.setResult(jsonData.getString("value"));
                    iterator.remove();
                } else if (jsonData.getString("key").equals("clean_time")) {
                    collidingFilterDTO.setCleanTime(jsonData.getString("value"));
                    iterator.remove();
                }
            }
        }

    }
}
