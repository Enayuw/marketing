package com.br.marketing.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.DateHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class EsConditionTransferSqlUtil {

    // 添加这个常量 - 用于匹配数字（整数或小数，可选的正负号）
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[+-]?\\d+(\\.\\d+)?$");


    /**
     * 运算条件转化为SQL条件（添加key前缀,sourceCode_key）
     *
     * @param jsonObject  json条件
     * @param parentLogic 上层逻辑节点
     * @return
     */
    public static String jsonTransferSqlByFillKey(JSONObject jsonObject, String parentLogic) {
        String logic = jsonObject.getString("logic");
        JSONArray dataArray = jsonObject.getJSONArray("data");
        StringBuilder sqlResult = new StringBuilder();
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject jsonNodeObject = dataArray.getJSONObject(i);
            //数值操作运算符处理
            if (jsonNodeObject.getString("type").equals("operation")) {
                String key = jsonNodeObject.getString("key");
                String sourceCode = jsonNodeObject.getString("sourceCode");
                String filedDeal = assemblefiled(sourceCode.concat("_").concat(key), jsonNodeObject.getString("operation"),
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
                sqlResult.append(jsonTransferSqlByFillKey(jsonNodeObject, logic));
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
     * ES运算条件转化为SQL条件
     *
     * @param jsonObject  json条件
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
     *
     * @param key       字段名
     * @param operation 运算符
     * @param value     值
     * @return
     */
    public static String assemblefiled(String key, String operation, Object value) {

        String sqlTep;
        List<String> operateList = Lists.newArrayList("=", "!=", "<", "<=", ">", ">=", "in", "not_in", "between", "between_right",
                "between_left", "between_open", "%");
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
                StringBuilder inStrValue = new StringBuilder();
                inList.forEach((String str) -> {
                    inStrValue.append("\"").append(str).append("\"").append(",");
                });
                sqlTep = key.concat(" in (").concat(inStrValue.substring(0, inStrValue.length() - 1).concat(" )"));
                break;
            case "not_in":
                List<String> notinList = (List) value;
                StringBuilder notStrValue = new StringBuilder();
                notinList.forEach((String str) -> {
                    notStrValue.append("\"").append(str).append("\"").append(",");
                });
                sqlTep = key.concat(" not in (").concat(notStrValue.substring(0, notStrValue.length() - 1).concat(" )"));
                break;
            case "between":
                List<String> betweenList = Arrays.asList(((String) value).split(","));
                String startValue = betweenList.get(0).trim();
                String endValue = betweenList.get(1).trim();
                // 判断是否为数字，数字不加引号
                if (isNumeric(startValue) && isNumeric(endValue)) {
                    sqlTep = ("(").concat(key).concat(" >=").concat(startValue).concat(" and ").concat(key).concat(" <=")
                            .concat(endValue).concat(")");
                } else {
                    sqlTep = ("(").concat(key).concat(" >=\"").concat(startValue).concat("\" and ").concat(key).concat(" <=\"")
                            .concat(endValue).concat("\")");
                }
                break;
            case "between_right":
                List<String> betweenRightList = Arrays.asList(((String) value).split(","));
                String rightStartValue = betweenRightList.get(0).trim();
                String rightEndValue = betweenRightList.get(1).trim();
                // 判断是否为数字，数字不加引号
                if (isNumeric(rightStartValue) && isNumeric(rightEndValue)) {
                    sqlTep = ("(").concat(key).concat(" >=").concat(rightStartValue).concat(" and ").concat(key).concat(" <")
                            .concat(rightEndValue).concat(")");
                } else {
                    sqlTep = ("(").concat(key).concat(" >=\"").concat(rightStartValue).concat("\" and ").concat(key).concat(" <\"")
                            .concat(rightEndValue).concat("\")");
                }
                break;
            case "between_left":
                List<String> betweenLeftList = Arrays.asList(((String) value).split(","));
                String leftStartValue = betweenLeftList.get(0).trim();
                String leftEndValue = betweenLeftList.get(1).trim();
                // 判断是否为数字，数字不加引号
                if (isNumeric(leftStartValue) && isNumeric(leftEndValue)) {
                    sqlTep = ("(").concat(key).concat(" >").concat(leftStartValue).concat(" and ").concat(key).concat(" <=")
                            .concat(leftEndValue).concat(")");
                } else {
                    sqlTep = ("(").concat(key).concat(" >\"").concat(leftStartValue).concat("\" and ").concat(key).concat(" <=\"")
                            .concat(leftEndValue).concat("\")");
                }
                break;
            case "between_open":
                List<String> betweenOpenList = Arrays.asList(((String) value).split(","));
                String openStartValue = betweenOpenList.get(0).trim();
                String openEndValue = betweenOpenList.get(1).trim();
                // 判断是否为数字，数字不加引号
                if (isNumeric(openStartValue) && isNumeric(openEndValue)) {
                    sqlTep = ("(").concat(key).concat(" >").concat(openStartValue).concat(" and ").concat(key).concat(" <")
                            .concat(openEndValue).concat(")");
                } else {
                    sqlTep = ("(").concat(key).concat(" >\"").concat(openStartValue).concat("\" and ").concat(key).concat(" <\"")
                            .concat(openEndValue).concat("\")");
                }
                break;
            case "%":
                sqlTep = key.concat(" like ").concat("\"%").concat(value.toString()).concat("%\"");
                break;
            case "<":
            case "<=":
            case ">":
            case ">=":
                if (isNumeric(value.toString())) {
                    sqlTep = key.concat(operation).concat(value.toString());
                } else {
                    sqlTep = key.concat(operation).concat("\"").concat(value.toString()).concat("\"");
                }
                break;
            default:
                sqlTep = key.concat(operation).concat("\"").concat(value.toString()).concat("\"");

        }
        return sqlTep;

    }


    /**
     * 判断字符串是否为数字（整数或小数）
     * 支持格式：123, -456, +789, 12.34, -0.5, +100.25
     *
     * @return true-是数字，false-不是数字
     */
    private static boolean isNumeric(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str).matches();
    }


}
