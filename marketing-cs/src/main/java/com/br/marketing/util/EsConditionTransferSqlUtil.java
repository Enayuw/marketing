package com.br.marketing.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.DateHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
@Slf4j
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
            //数值操作运算符处理
            if (jsonNodeObject.getString("type").equals("operation")) {
                String filedDeal = assemblefiled(jsonNodeObject.getString("key"), jsonNodeObject.getString("operation"),
                        jsonNodeObject.get("value"));
                if (i < dataArray.size() - 1) {
                    //非最后一位，需拼接逻辑运算符logic
                    sqlResult.append(filedDeal).append(" ").append(logic).append(" ");
                } else {
                    sqlResult.append(filedDeal).append(" ");
                }
            } //逻辑运算符处理
            else if (jsonNodeObject.getString("type").equals("logic")) {
                //递归处理
                sqlResult.append(jsonTransferSql(jsonNodeObject, logic));
                if (i < dataArray.size() - 1) {
                    //非最后一位，需拼接逻辑运算符logic
                    sqlResult.append(logic).append(" ");
                }
            }
        }
        //内层logic运算用括号括起来
        if (com.br.marketing.common.utils.StringUtils.isNotEmpty(parentLogic)) {
            sqlResult.insert(0, " (").append(" ) ");
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
            log.error("规则中心-携程撞库操作符异常");
        }
        //时间格式特殊处理，yyyy-mm-dd转化为区间
        if ("=".equals(operation)) {
            String date = (String) value;
            if (DateHelper.isDate(date)) {
                return ("(").concat(key).concat(" >=\"").concat(date).concat(" 00:00:00\" and ").concat(key).concat(" <=\"")
                        .concat(date.concat(" 23:59:59\")"));
            }
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
                sqlTep = ("(").concat(key).concat(" >=\"").concat(betweenList.get(0)).concat("\" and ").concat(key).concat(" <=\"")
                        .concat(betweenList.get(1).concat("\")"));
                break;
            case "between_right":
                List<String> betweenRightList = Arrays.asList(((String) value).split(","));
                sqlTep = ("(").concat(key).concat(" >=\"").concat(betweenRightList.get(0)).concat("\" and ").concat(key).concat(" <\"")
                        .concat(betweenRightList.get(1).concat("\")"));
                break;
            case "between_left":
                List<String> betweenLeftList = Arrays.asList(((String) value).split(","));
                sqlTep = ("(").concat(key).concat(" >\"").concat(betweenLeftList.get(0)).concat("\" and ").concat(key).concat(" <=\"")
                        .concat(betweenLeftList.get(1).concat("\")"));
                break;
            case "between_open":
                List<String> betweenOpenList = Arrays.asList(((String) value).split(","));
                sqlTep = ("(").concat(key).concat(" >\"").concat(betweenOpenList.get(0)).concat("\" and ").concat(key).concat(" <\"")
                        .concat(betweenOpenList.get(1).concat("\")"));
                break;
            default:
                sqlTep = key.concat(operation).concat("\"").concat(value.toString()).concat("\"");

        }
        return sqlTep;

    }

    /*public static void main (String args[]){

        String s = "{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"8\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxcysxsxtg\",\"operation\":\"=\",\"value\":\"581\"}]}]}";

        JSONObject json = JSON.parseObject(s);
        System.out.println(jsonTransferSql(json,""));


    }*/

}
