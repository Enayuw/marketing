package com.br.marketing.util.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class XieChengEsJsonHandler {


    /**
     * 携程JsonEs条件预处理
     *
     * @param jsonObject json条件
     * @param collidingFilterDTO 撞库条件实体
     * @return
     */
    public static void handlerJson(JSONObject jsonObject, XieChengCollidingFilterDTO collidingFilterDTO) {
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Map<String, Object> releaseTimeMap = new HashMap<>();
        Map<String, Object> couponCodeMap = new HashMap<>();
        Map<String, Object> couponDescMap = new HashMap<>();
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonData = (JSONObject) iterator.next();
            if (jsonData.getString("type").equals("operation")) {
                String keyValue = jsonData.getString("key");
                switch (keyValue) {
                    case "release_time":
                        releaseTimeMap.put("value", jsonData.get("value"));
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
                        couponCodeMap.put("value", jsonData.get("value"));
                        couponCodeMap.put("operation", jsonData.getString("operation"));
                        collidingFilterDTO.setCoupon_code(couponCodeMap);
                        iterator.remove();
                        break;
                    case "coupon_desc":
                        couponDescMap.put("value", jsonData.get("value"));
                        couponDescMap.put("operation", jsonData.getString("operation"));
                        collidingFilterDTO.setCoupon_desc(couponDescMap);
                        iterator.remove();
                        break;
                    case "customer_group":
                        collidingFilterDTO.setCustomerGroup(jsonData.getString("value"));
                        iterator.remove();
                        break;
                    case "info":
                        collidingFilterDTO.setInfo(jsonData.getString("value"));
                        iterator.remove();
                        break;
                    case "blacklist_delete":
                        collidingFilterDTO.setBlacklist_delete(jsonData.getString("value"));
                        iterator.remove();
                        break;
                    default:
                }
            }
        }

    }


    public static String zkTrueCondition(XieChengCollidingFilterDTO collidingFilterDTO) {
        StringBuilder zkTrueCondition = new StringBuilder();
        Map<String, Object> releaseTime = collidingFilterDTO.getReleaseTime();
        Map<String, Object> couponCode = collidingFilterDTO.getCoupon_code();
        Map<String, Object> couponDesc = collidingFilterDTO.getCoupon_desc();
        String customerGroup = collidingFilterDTO.getCustomerGroup();
        String info = collidingFilterDTO.getInfo();

        if (!CollectionUtils.isEmpty(releaseTime)) {
            Object value = releaseTime.get("value");
            if (("=").equals(releaseTime.get("operation"))) {
                value = DateHelper.dateTNtransfer((String) releaseTime.get("value"));
            }

            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("release_time", (String) releaseTime.get("operation"),
                    value));
        }
        if (!CollectionUtils.isEmpty(couponCode)) {
            if (StringUtils.isNotEmpty(zkTrueCondition.toString())) {
                zkTrueCondition.append(" and ");
            }
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("coupon_code", (String) couponCode.get("operation"),
                    couponCode.get("value")));
        }
        if (!CollectionUtils.isEmpty(couponDesc)) {
            if (StringUtils.isNotEmpty(zkTrueCondition.toString())) {
                zkTrueCondition.append(" and ");
            }
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("coupon_desc", (String) couponDesc.get("operation"),
                    couponDesc.get("value")));
        }
        if (StringUtils.isNotEmpty(customerGroup)) {
            if (StringUtils.isNotEmpty(zkTrueCondition.toString())) {
                zkTrueCondition.append(" and ");
            }
            zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("customer_group", "=", customerGroup));
        }
        if (!Objects.isNull(info)) {
            if (StringUtils.isNotEmpty(zkTrueCondition.toString())) {
                zkTrueCondition.append(" and ");
            }
            if (info.equals("") || info.equalsIgnoreCase("NULL")) {
                zkTrueCondition.append("info is null");
            } else {
                zkTrueCondition.append(EsConditionTransferSqlUtil.assemblefiled("info", "=", info));
            }
        }
        return zkTrueCondition.toString();
    }


}
