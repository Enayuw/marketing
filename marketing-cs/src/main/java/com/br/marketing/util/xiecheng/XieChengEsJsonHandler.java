package com.br.marketing.util.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XieChengEsJsonHandler {


    /**
     * 携程JsonEs条件预处理
     *
     * @param jsonObject         json条件
     * @param collidingFilterDTO 撞库条件实体
     * @return
     */
    public static void handlerJson(JSONObject jsonObject, XieChengCollidingFilterDTO collidingFilterDTO) {
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Map<String, String> releaseTimeMap = new HashMap<>();
        Map<String, String> couponCodeMap = new HashMap<>();
        Map<String, String> couponDescMap = new HashMap<>();
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonData = (JSONObject) iterator.next();
            if (jsonData.getString("type").equals("operation")) {
                String keyValue = jsonData.getString("key");
                switch (keyValue) {
                    case "release_time":
                        releaseTimeMap.put("value", jsonData.getString("value"));
                        releaseTimeMap.put("operation", jsonData.getString("operation"));
                        collidingFilterDTO.setReleaseTime(releaseTimeMap);
                        iterator.remove();
                        break;
                    case "result":
                        collidingFilterDTO.setResult(jsonData.getString("value"));
                        iterator.remove();
                        break;
                    case "clean_time":
                        collidingFilterDTO.setCleanTime(jsonData.getString("value"));
                        iterator.remove();
                        break;
                    case "coupon_code":
                        couponCodeMap.put("value", jsonData.getString("value"));
                        couponCodeMap.put("operation", jsonData.getString("operation"));
                        collidingFilterDTO.setCoupon_code(couponCodeMap);
                        iterator.remove();
                        break;
                    case "coupon_desc":
                        couponDescMap.put("value", jsonData.getString("value"));
                        couponDescMap.put("operation", jsonData.getString("operation"));
                        collidingFilterDTO.setCoupon_desc(couponDescMap);
                        iterator.remove();
                        break;
                    default:
                }
            }
        }

    }


    public static String zkTrueCondition(XieChengCollidingFilterDTO collidingFilterDTO) {
        StringBuilder zkTrueCondition = new StringBuilder();
        Map<String, String> releaseTime = collidingFilterDTO.getReleaseTime();
        Map<String, String> couponCode = collidingFilterDTO.getCoupon_code();
        Map<String, String> couponDesc = collidingFilterDTO.getCoupon_desc();

        if (!CollectionUtils.isEmpty(releaseTime)) {
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("release_time", releaseTime.get("operation"), releaseTime.get("value")));
        }
        if (!CollectionUtils.isEmpty(couponCode)) {
            if(StringUtils.isNotEmpty(zkTrueCondition.toString())){
                zkTrueCondition.append(" and ");
            }
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("coupon_code", couponCode.get("operation"), couponCode.get("value")));
        }
        if (!CollectionUtils.isEmpty(couponDesc)) {
            if(StringUtils.isNotEmpty(zkTrueCondition.toString())){
                zkTrueCondition.append(" and ");
            }
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("coupon_desc", couponDesc.get("operation"), couponDesc.get("value")));
        }

        return zkTrueCondition.toString();
    }


}
