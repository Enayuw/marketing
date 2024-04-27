package com.br.marketing.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class EsConditionTransferSqlUtil {

    /**
     * ES运算条件转化为SQL条件
     * @param jsonObject json条件
     * @param parentLogic 上层逻辑节点
     * @return
     */
    public static String jsonTransferSql(JSONObject jsonObject, String parentLogic) {
        String logic = jsonObject.getString("logic");
        JSONArray dataArray = jsonObject.getJSONArray("data");
        StringBuilder sqlResult = new StringBuilder();
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject jsonNodeObject = dataArray.getJSONObject(i);
            if (jsonNodeObject.getString("type").equals("operation")) {
                String filedDeal = assemblefiled(jsonNodeObject.getString("key"), jsonNodeObject.getString("operation"),
                        jsonNodeObject.get("value"));
                if (i < dataArray.size() - 1) {
                    sqlResult.append(filedDeal).append(" ").append(logic).append(" ");
                } else {
                    sqlResult.append(filedDeal).append(" ");
                }
            } else if (jsonNodeObject.getString("type").equals("logic")) {
                //递归处理
                sqlResult.append(jsonTransferSql(jsonNodeObject, logic));
            }
        }
        if (com.br.marketing.common.utils.StringUtils.isNotEmpty(parentLogic)) {
            sqlResult.insert(0, " (").append(" )");
        }
        return sqlResult.toString();

    }


    /**
     * SQL条件运算符拼接
     * @param key 字段名
     * @param operation 运算符
     * @param value  值
     * @return
     */
    public static String assemblefiled(String key, String operation, Object value) {

        String sqlTep;
        List<String> operateList = Lists.newArrayList("=", "!=", "<", "<=", ">", ">=", "in", "not_in", "between", "between_right",
                "between_left", "between_open");
        if (!operateList.contains(operation)) {
            System.out.println("操作符异常");
        }
        switch (operation) {

            case "in":
                List<String> inList = (List) value;
                sqlTep = key.concat(" in (").concat(String.join(",", inList).concat(" )"));
                break;
            case "not_in":
                List<String> notinList = (List) value;
                sqlTep = key.concat(" not in (").concat(String.join(",", notinList).concat(" )"));
                break;
            case "between":
                List<String> betweenList = Arrays.asList(((String) value).split(","));
                sqlTep = key.concat(" >=\"").concat(betweenList.get(0)).concat("\" and ").concat(key).concat(" <=\"").concat(betweenList.get(1)
                        .concat("\""));
                break;
            case "between_right":
                List<String> betweenRightList = Arrays.asList(((String) value).split(","));
                sqlTep = key.concat(" >=\"").concat(betweenRightList.get(0)).concat("\" and ").concat(key).concat(" <\"").concat(betweenRightList.
                        get(1).concat("\""));
                break;
            case "between_left":
                List<String> betweenLeftList = Arrays.asList(((String) value).split(","));
                sqlTep = key.concat(" >\"").concat(betweenLeftList.get(0)).concat("\" and ").concat(key).concat(" <=\"").concat(betweenLeftList.get(1)
                        .concat("\""));
                break;
            case "between_open":
                List<String> betweenOpenList = Arrays.asList(((String) value).split(","));
                sqlTep = key.concat(" >\"").concat(betweenOpenList.get(0)).concat("\" and ").concat(key).concat(" <\"").concat(betweenOpenList.get(1)
                        .concat("\""));
                break;
            default:
                sqlTep = key.concat(operation).concat(value.toString());

        }
        return sqlTep;

    }

}
