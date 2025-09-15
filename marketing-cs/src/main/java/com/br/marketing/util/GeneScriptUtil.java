package com.br.marketing.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.bean.CrossIndexBean;
import com.br.marketing.common.bean.ScoreLable;
import com.br.marketing.common.bean.SingleIndexBean;
import com.br.marketing.common.utils.StringUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;
import java.util.stream.Collectors;

public class GeneScriptUtil {

    private static final String SPACE_FRAG = " ";

    private static final String SINGLE_QUOTATION = "'";

    private static final String PARENTHESIS_FRAG_LEFT = "(";

    private static final String PARENTHESIS_FRAG_RIGHT = ")";

    private static final String IF_FRAG_LEFT = "if (";

    private static final String CONDITION_MAP_GET_LEFT = "conditionMap.get('";

    private static final String CONDITION_NULL = "') == null";

    private static final String CONDITION_NOT_NULL = "') != null";

    private static final String CONDITION_NULL_SPEL = " == null";

    private static final String CONDITION_NOT_NULL_SPEL = " != null";

    private static final String MAP_CONSTRUCT_FRAG = "Map conditionMap = new HashMap();" +
            "for (item in params['_source']['condition']){if(item['d_value'] != null)" +
            "{conditionMap.put(item['field_key'], item['d_value']);}}";

    private static final String RETURN_FRAG_LEFT = " { return '";

    private static final String RETURN_FRAG_RIGHT = "'; } ";

    private static final String RETURN_FRAG_END = " return '';";
    private static final String LIST_VALUE = "listValue";

    private static final String VALUE_TYPE = "valueType";

    private static final String LOGIC_AND = "and";

    private static final String LOGIC_OR = "or";

    private static final String LOGIC_OPERATOR_AND = "&&";

    private static final String LOGIC_OPERATOR_OR = "||";

    private static final String OPERATION_BETWEEN_OPEN = "between_open";

    private static final String OPERATION_BETWEEN = "between";

    private static final String OPERATION_BETWEEN_LEFT = "between_left";

    private static final String OPERATION_BETWEEN_RIGHT = "between_right";

    private static final String SECTION_IDENTIFIER_LEFT = ":left";

    private static final String SECTION_IDENTIFIER_RIGHT = ":right";

    private static final String OPERATOR_LESS = "<";

    private static final String OPERATOR_LESS_EQUAL = "<=";

    private static final String OPERATOR_GREATER = ">";

    private static final String OPERATOR_GREATER_EQUAL = ">=";

    private static final String PLACE_HOLDER = "#";

    private static Map<String, String> logicMap;

    private static Map<String, String> opetatorMap;

    static {
        logicMap = ImmutableMap.of(
                LOGIC_AND, LOGIC_OPERATOR_AND,
                LOGIC_OR, LOGIC_OPERATOR_OR);

        opetatorMap = ImmutableMap.<String, String>builder()
                .put(OPERATION_BETWEEN_OPEN + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER)
                .put(OPERATION_BETWEEN_OPEN + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS)
                .put(OPERATION_BETWEEN + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER_EQUAL)
                .put(OPERATION_BETWEEN + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS_EQUAL)
                .put(OPERATION_BETWEEN_LEFT + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER)
                .put(OPERATION_BETWEEN_LEFT + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS_EQUAL)
                .put(OPERATION_BETWEEN_RIGHT + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER_EQUAL)
                .put(OPERATION_BETWEEN_RIGHT + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS).build();
    }

    /**
     * @param scoreLables
     * @return java.lang.String
     * @description 生成es打标脚本
     * @author hedongshuo
     * @date 2024/10/26 18:32
     **/
    public static String esLableScript(String scoreLables) {
        List<ScoreLable> list = getScoreLables(scoreLables, true);
        StringBuilder listValueSource = new StringBuilder();
        StringBuilder valueTypeSource = new StringBuilder();
        //构造Map片段
        listValueSource.append(MAP_CONSTRUCT_FRAG);
        valueTypeSource.append(MAP_CONSTRUCT_FRAG);
        //条件片段
        for (ScoreLable scoreLable : list) {
            listValueSource.append(IF_FRAG_LEFT).append(scoreLable.getConditionSource())
                    .append(PARENTHESIS_FRAG_RIGHT).append(RETURN_FRAG_LEFT)
                    .append(scoreLable.getListValue()).append(RETURN_FRAG_RIGHT);
            valueTypeSource.append(IF_FRAG_LEFT).append(scoreLable.getConditionSource())
                    .append(PARENTHESIS_FRAG_RIGHT).append(RETURN_FRAG_LEFT)
                    .append(scoreLable.getValueType()).append(RETURN_FRAG_RIGHT);
        }
        //未标记，return片段
        listValueSource.append(RETURN_FRAG_END);
        valueTypeSource.append(RETURN_FRAG_END);
        //生成script_fields
        return geneScript(listValueSource.toString(), valueTypeSource.toString());
    }

    /**
     * 将条件json转为list
     *
     * @param scoreLables
     * @return
     */
    public static List<ScoreLable> getScoreLables(String scoreLables, Boolean markWithEsFlag) {
        JSONArray array = JSON.parseArray(scoreLables);
        //构建标签list
        List<ScoreLable> list = new ArrayList<>(array.size());
        for (Object obj : array) {
            ScoreLable scoreLable = new ScoreLable();
            list.add(scoreLable);
            JSONObject jsonObject = JSON.parseObject(obj.toString());
            scoreLable.setOrder(jsonObject.getIntValue("order"));
            JSONArray labels = jsonObject.getJSONArray("labels");
            for (Object label : labels) {
                JSONObject labelJson = JSON.parseObject(label.toString());
                String labelKey = labelJson.getString("labelKey");
                String labelValue = labelJson.getString("labelValue");
                if (LIST_VALUE.equals(labelKey)) {
                    scoreLable.setListValue(labelValue);
                }
                if (VALUE_TYPE.equals(labelKey)) {
                    scoreLable.setValueType(labelValue);
                }
            }
            StringBuilder sourceBuilder = new StringBuilder();
            JSONObject condition = jsonObject.getJSONObject("condition");
            mergeScoreRange(condition);
            process(sourceBuilder, condition, markWithEsFlag);
            scoreLable.setConditionSource(sourceBuilder.toString());
        }
        //list排序
        list.sort(Comparator.comparing(ScoreLable::getOrder));
        return list;
    }

    /**
     * 处理一个Json{
     * type:"logic/operation"
     * logic:"or/and"
     * data:[{...}]
     * }
     * data[{
     * "type": "operation",
     * "key": "scorencashon58xkcsxcd",
     * "operation": "between_left",
     * "value": "75,80"
     * }
     * ]
     *
     * @param sourceBuilder
     * @param condition
     * @param markWithEsFlag
     */
    public static void process(StringBuilder sourceBuilder, JSONObject condition, Boolean markWithEsFlag) {
        JSONArray data = condition.getJSONArray("data");
        for (int i = 0; i < data.size(); i++) {
            JSONObject dataJson = JSON.parseObject(data.get(i).toString());
            String scoreCondition = analysisData(dataJson, i == data.size() - 1 ? "" : logicMap.get(condition.getString("logic")), markWithEsFlag);
            sourceBuilder.append(scoreCondition);
        }
    }

    /**
     * @param listValueSource
     * @param valueTypeSource
     * @return void
     * @description 生成脚本
     * @author hedongshuo
     * @date 2024/10/26 15:34
     **/
    private static String geneScript(String listValueSource, String valueTypeSource) {
        //3级
        JSONObject listValueScript = new JSONObject();
        listValueScript.put("lang", "painless");
        listValueScript.put("source", listValueSource);
        JSONObject valueTypeScript = new JSONObject();
        valueTypeScript.put("lang", "painless");
        valueTypeScript.put("source", valueTypeSource);
        //2级
        JSONObject listValueObject = new JSONObject();
        listValueObject.put("script", listValueScript);
        JSONObject valueTypeObject = new JSONObject();
        valueTypeObject.put("script", valueTypeScript);
        //1级
        JSONObject scriptFieldsObject = new JSONObject();
        scriptFieldsObject.put("listValue", listValueObject);
        scriptFieldsObject.put("valueType", valueTypeObject);
        return scriptFieldsObject.toString();
    }

    /**
     * 将多层深的data解析为条件脚本
     *
     * @param data
     * @param logicOperator
     * @param markWithEsFlag
     * @return String
     */
    public static String analysisData(JSONObject data, String logicOperator, Boolean markWithEsFlag) {
        String type = data.getString("type");
        StringBuilder conditionBuilder = new StringBuilder();
        //层级无限延伸
        if ("logic".equals(type)) {
            conditionBuilder.append(PARENTHESIS_FRAG_LEFT);
            process(conditionBuilder, data, markWithEsFlag);
            conditionBuilder.append(PARENTHESIS_FRAG_RIGHT);
            //底层解析
        } else if ("operation".equals(type)) {
            conditionBuilder.append(PARENTHESIS_FRAG_LEFT);
            String key = data.getString("key");
            List<String> values = Arrays.asList(data.getString("value").split(","));
            if (values.size() < 2) {
                if (markWithEsFlag) {
                    conditionBuilder.append(CONDITION_MAP_GET_LEFT).append(key).append(CONDITION_NULL);
                } else {
                    conditionBuilder.append(PLACE_HOLDER).append(key).append(CONDITION_NULL_SPEL);
                }
            } else {
                String operatorLeft = opetatorMap.get(data.getString("operation") + SECTION_IDENTIFIER_LEFT);
                String operatorRight = opetatorMap.get(data.getString("operation") + SECTION_IDENTIFIER_RIGHT);
                String valueLeft = values.get(0);
                String valueRight = values.get(1);
                if (markWithEsFlag) {
                    conditionBuilder.append(CONDITION_MAP_GET_LEFT).append(key).append(CONDITION_NOT_NULL)
                            .append(SPACE_FRAG).append(LOGIC_OPERATOR_AND).append(SPACE_FRAG)
                            .append(CONDITION_MAP_GET_LEFT).append(key).append(SINGLE_QUOTATION).append(PARENTHESIS_FRAG_RIGHT)
                            .append(SPACE_FRAG).append(operatorLeft).append(SPACE_FRAG).append(valueLeft)
                            .append(SPACE_FRAG).append(LOGIC_OPERATOR_AND).append(SPACE_FRAG)
                            .append(CONDITION_MAP_GET_LEFT).append(key).append(SINGLE_QUOTATION).append(PARENTHESIS_FRAG_RIGHT)
                            .append(SPACE_FRAG).append(operatorRight).append(SPACE_FRAG).append(valueRight);
                } else {
                    conditionBuilder.append(PLACE_HOLDER).append(key).append(CONDITION_NOT_NULL_SPEL)
                            .append(SPACE_FRAG).append(LOGIC_OPERATOR_AND).append(SPACE_FRAG)
                            .append(PLACE_HOLDER).append(key)
                            .append(SPACE_FRAG).append(operatorLeft).append(SPACE_FRAG).append(valueLeft)
                            .append(SPACE_FRAG).append(LOGIC_OPERATOR_AND).append(SPACE_FRAG)
                            .append(PLACE_HOLDER).append(key)
                            .append(SPACE_FRAG).append(operatorRight).append(SPACE_FRAG).append(valueRight);
                }
            }
            conditionBuilder.append(PARENTHESIS_FRAG_RIGHT);
        }
        if (StringUtils.isNotEmpty(logicOperator)) {
            conditionBuilder.append(logicOperator);
        }
        return conditionBuilder.toString();
    }

    /**
     * @param scoreLables
     * @param scoreMap
     * @return com.alibaba.fastjson.JSONObject
     * @description 返回数据打标
     * @author hedongshuo
     * @date 2024/10/29 13:38
     **/
    public static JSONObject scoreLable(String scoreLables, Map<String, Double> scoreMap) {

        JSONArray array = JSON.parseArray(scoreLables);
        //按order排序
        array.sort(Comparator.comparing(obj -> JSON.parseObject(obj.toString()).getString("order")));
        //遍历评分分布分组
        for (Object obj : array) {
            JSONObject json = JSON.parseObject(obj.toString());
            JSONObject condition = json.getJSONObject("condition");
            //解析condition
            if (process(condition, scoreMap)) {
                JSONArray labels = json.getJSONArray("labels");
                JSONObject fields = new JSONObject();
                for (Object label : labels) {
                    JSONObject labelJson = JSON.parseObject(label.toString());
                    String labelKey = labelJson.getString("labelKey");
                    String labelValue = labelJson.getString("labelValue");
                    if (LIST_VALUE.equals(labelKey)) {
                        fields.put(LIST_VALUE, labelValue);
                    }
                    if (VALUE_TYPE.equals(labelKey)) {
                        fields.put(VALUE_TYPE, labelValue);
                    }
                }
                return fields;
            }
        }
        return null;
    }

    /**
     * @param condition
     * @param scoreMap
     * @return java.lang.Boolean
     * @description 传入分值scoreMap，解析condition，是否满足条件
     * @author hedongshuo
     * @date 2024/10/29 13:43
     **/
    public static Boolean process(JSONObject condition, Map<String, Double> scoreMap) {
        boolean isLogic = "logic".equals(condition.getString("type"));
        //层级无限延伸
        if (isLogic) {
            JSONArray data = condition.getJSONArray("data");
            boolean isAnd = "and".equals(condition.getString("logic"));
            for (int i = 0; i < data.size(); i++) {
                JSONObject dataJson = JSON.parseObject(data.get(i).toString());
                if (isAnd) {
                    if (!process(dataJson, scoreMap)) {
                        return false;
                    }
                } else {
                    if (process(dataJson, scoreMap)) {
                        return true;
                    }
                }
            }
            if (isAnd) {
                return true;
            } else {
                return false;
            }
            //底层解析
        } else {
            return valueOperate(condition, scoreMap);
        }
    }

    private static Boolean valueOperate(JSONObject dataJson, Map<String, Double> scoreMap) {
        String key = dataJson.getString("key");
        String operation = dataJson.getString("operation");
        List<String> values = Arrays.asList(dataJson.getString("value").split(","));
        Double dValue = scoreMap.get(key);
        if (dValue == null) {
            //类型不是区间，是 = ‘null’
            if (values.size() < 2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (values.size() < 2) {
                return false;
            }
        }
        //dValue != null && values.size() = 2
        List<Double> value = values.stream().map(Double::valueOf).collect(Collectors.toList());
        Double valueStart = value.get(0);
        Double valueEnd = value.get(1);
        if (OPERATION_BETWEEN_OPEN.equals(operation)) {
            return dValue.compareTo(valueStart) == 1 && dValue.compareTo(valueEnd) == -1;
        } else if (OPERATION_BETWEEN.equals(operation)) {
            return dValue.compareTo(valueStart) != -1 && dValue.compareTo(valueEnd) != 1;
        } else if (OPERATION_BETWEEN_LEFT.equals(operation)) {
            return dValue.compareTo(valueStart) == 1 && dValue.compareTo(valueEnd) != 1;
        } else if (OPERATION_BETWEEN_RIGHT.equals(operation)) {
            return dValue.compareTo(valueStart) != -1 && dValue.compareTo(valueEnd) == -1;
        } else {
            return true;
        }
    }

    public static ScoreLable scoreLableWithSpel(Map<String, Object> scoreMap, List<ScoreLable> scoreLables) {
        for (ScoreLable scoreLable : scoreLables) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(scoreMap);
            ExpressionParser parser = new SpelExpressionParser();
            if (parser.parseExpression(scoreLable.getConditionSource()).getValue(context, Boolean.class)) {
                return scoreLable;
            }
        }
        return null;
    }

    public static void mergeScoreRange(JSONObject condition) {
        if (!"logic".equals(condition.getString("type"))) {
            return;
        }
        if (!"or".equals(condition.getString("logic"))) {
            return;
        }
        JSONArray data = condition.getJSONArray("data");
        if (data.size() == 0) {
            return;
        }
        JSONObject sample = JSON.parseObject(data.get(0).toString());
        if ("logic".equals(sample.getString("type"))) {
            JSONArray crossSample = sample.getJSONArray("data");
            JSONObject xSample = JSON.parseObject(crossSample.get(0).toString());
            String xKey = xSample.getString("key");
            JSONObject ySample = JSON.parseObject(crossSample.get(1).toString());
            String yKey = ySample.getString("key");
            List<CrossIndexBean> list = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                CrossIndexBean crossIndexBean = new CrossIndexBean();
                list.add(crossIndexBean);
                JSONObject dataJson = JSON.parseObject(data.get(i).toString());
                JSONArray crossIndex = dataJson.getJSONArray("data");
                JSONObject indexOne = JSON.parseObject(crossIndex.get(0).toString());
                JSONObject indexTwo = JSON.parseObject(crossIndex.get(1).toString());
                List<String> valuesOne = Arrays.asList(indexOne.getString("value").split(","));
                List<String> valuesTwo = Arrays.asList(indexTwo.getString("value").split(","));
                if (xKey.equals(indexOne.getString("key"))) {
                    if (valuesOne.size() < 2) {
                        crossIndexBean.setXLeftValue("");
                        crossIndexBean.setXRightValue("");
                    } else {
                        crossIndexBean.setXLeftValue(valuesOne.get(0));
                        crossIndexBean.setXRightValue(valuesOne.get(1));
                    }
                    if (valuesTwo.size() < 2) {
                        crossIndexBean.setYLeftValue("");
                        crossIndexBean.setYRightValue("");
                    } else {
                        crossIndexBean.setYLeftValue(valuesTwo.get(0));
                        crossIndexBean.setYRightValue(valuesTwo.get(1));
                    }
                } else {
                    if (valuesOne.size() < 2) {
                        crossIndexBean.setYLeftValue("");
                        crossIndexBean.setYRightValue("");
                    } else {
                        crossIndexBean.setYLeftValue(valuesOne.get(0));
                        crossIndexBean.setYRightValue(valuesOne.get(1));
                    }
                    if (valuesTwo.size() < 2) {
                        crossIndexBean.setXLeftValue("");
                        crossIndexBean.setXRightValue("");
                    } else {
                        crossIndexBean.setXLeftValue(valuesTwo.get(0));
                        crossIndexBean.setXRightValue(valuesTwo.get(1));
                    }
                }
            }
            //根据x分组，y排序，y合并
            Map<Pair<String, String>, List<CrossIndexBean>> MapByX = list.stream()
                    .collect(Collectors.groupingBy(
                            p -> Pair.of(p.getXLeftValue(), p.getXRightValue())));
            Set<Map.Entry<Pair<String, String>, List<CrossIndexBean>>> entriesByx = MapByX.entrySet();
            List<CrossIndexBean> listGroupByX = new ArrayList<>();
            for (Map.Entry<Pair<String, String>, List<CrossIndexBean>> entry : entriesByx) {
                List<CrossIndexBean> listByX = entry.getValue();
                listByX.sort(Comparator.comparingDouble((CrossIndexBean bean) -> compareValue(bean, true)));
                for (int i = listByX.size() - 1; i > 0; i--) {
                    CrossIndexBean later = listByX.get(i);
                    CrossIndexBean former = listByX.get(i - 1);
                    if (later.getYLeftValue().equals(former.getYRightValue())) {
                        former.setYRightValue(later.getYRightValue());
                        listByX.remove(i);
                    }
                }
                listGroupByX.addAll(listByX);
            }
            //根据y分组，x排序，x合并
            Map<Pair<String, String>, List<CrossIndexBean>> MapByY = listGroupByX.stream()
                    .collect(Collectors.groupingBy(
                            p -> Pair.of(p.getYLeftValue(), p.getYRightValue())));
            Set<Map.Entry<Pair<String, String>, List<CrossIndexBean>>> entriesByY = MapByY.entrySet();
            List<CrossIndexBean> listGroupByY = new ArrayList<>();
            for (Map.Entry<Pair<String, String>, List<CrossIndexBean>> entry : entriesByY) {
                List<CrossIndexBean> listByY = entry.getValue();
                listByY.sort(Comparator.comparingDouble((CrossIndexBean bean) -> compareValue(bean, false)));
                for (int i = listByY.size() - 1; i > 0; i--) {
                    CrossIndexBean later = listByY.get(i);
                    CrossIndexBean former = listByY.get(i - 1);
                    if (later.getXLeftValue().equals(former.getXRightValue())) {
                        former.setXRightValue(later.getXRightValue());
                        listByY.remove(i);
                    }
                }
                listGroupByY.addAll(listByY);
            }
            //合并完，反显为Json
            JSONArray array = new JSONArray();
            condition.put("data", array);
            for (int i = 0; i < listGroupByY.size(); i++) {
                CrossIndexBean crossIndexBean = listGroupByY.get(i);
                JSONObject crossIndexJson = new JSONObject();
                array.set(i, crossIndexJson);
                crossIndexJson.put("type", "logic");
                crossIndexJson.put("logic", "and");
                JSONArray innerData = new JSONArray();
                crossIndexJson.put("data", innerData);
                JSONObject jsonX = new JSONObject();
                JSONObject jsonY = new JSONObject();
                innerData.set(0, jsonX);
                innerData.set(1, jsonY);
                jsonX.put("type", "operation");
                jsonX.put("key", xKey);
                if (StringUtils.isEmpty(crossIndexBean.getXLeftValue())) {
                    jsonX.put("operation", "=");
                    jsonX.put("value", "");
                } else {
                    jsonX.put("operation", "between_right");
                    jsonX.put("value", crossIndexBean.getXLeftValue() + "," + crossIndexBean.getXRightValue());
                }
                jsonY.put("type", "operation");
                jsonY.put("key", yKey);
                if (StringUtils.isEmpty(crossIndexBean.getYLeftValue())) {
                    jsonY.put("operation", "=");
                    jsonY.put("value", "");
                } else {
                    jsonY.put("operation", "between_right");
                    jsonY.put("value", crossIndexBean.getYLeftValue() + "," + crossIndexBean.getYRightValue());
                }
            }
        } else {
            Map<String, List<SingleIndexBean>> map = new HashMap<>();
            // 先收集所有相同key的条件
            for (Object datum : data) {
                String singleKey = ((JSONObject)datum).getString("key");
                SingleIndexBean singleIndexBean = createSingleIndexBean(datum);
                
                if (!map.containsKey(singleKey)){
                    List<SingleIndexBean> list = new ArrayList<>();
                    list.add(singleIndexBean);
                    map.put(singleKey, list);
                } else {
                    map.get(singleKey).add(singleIndexBean);
                }
            }
            
            // 对每个key的条件列表进行排序和合并
            for (Map.Entry<String, List<SingleIndexBean>> entry : map.entrySet()) {
                List<SingleIndexBean> singleIndexBeans = entry.getValue();
                // 按左值排序（处理空值情况）
                singleIndexBeans.sort(Comparator.comparing((SingleIndexBean bean) -> {
                    if (StringUtils.isEmpty(bean.getLeftValue())) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    try {
                        return Double.parseDouble(bean.getLeftValue());
                    } catch (NumberFormatException e) {
                        return Double.NEGATIVE_INFINITY;
                    }
                }));
                
                // 合并相邻的区间
                for (int i = singleIndexBeans.size() - 1; i > 0; i--) {
                    SingleIndexBean current = singleIndexBeans.get(i);
                    SingleIndexBean previous = singleIndexBeans.get(i - 1);
                    
                    // 检查是否可以合并：前一个的右值等于当前的左值
                    if (!StringUtils.isEmpty(previous.getRightValue()) && 
                        !StringUtils.isEmpty(current.getLeftValue()) &&
                        previous.getRightValue().equals(current.getLeftValue())) {
                        // 合并区间：扩展前一个的右值，移除当前项
                        previous.setRightValue(current.getRightValue());
                        singleIndexBeans.remove(i);
                    }
                }
            }

            JSONArray array = getJsonArrayFromListMap(map);
            condition.put("data", array);
        }

    }

    private static SingleIndexBean createSingleIndexBean(Object datum) {
        SingleIndexBean singleIndexBean = new SingleIndexBean();
        JSONObject singleIndex = JSON.parseObject(datum.toString());
        List<String> values = Arrays.asList(singleIndex.getString("value").split(","));
        if (values.size() < 2) {
            singleIndexBean.setLeftValue("");
            singleIndexBean.setRightValue("");
        } else {
            singleIndexBean.setLeftValue(values.get(0));
            singleIndexBean.setRightValue(values.get(1));
        }
        return singleIndexBean;
    }


    private static JSONArray getJsonArrayFromListMap(Map<String, List<SingleIndexBean>> map) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, List<SingleIndexBean>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<SingleIndexBean> singleIndexBeans = entry.getValue();
            
            for (SingleIndexBean singleIndexBean : singleIndexBeans) {
                String leftValue = singleIndexBean.getLeftValue();
                String rightValue = singleIndexBean.getRightValue();
                JSONObject singleIndexJson = new JSONObject();
                singleIndexJson.put("type", "operation");
                singleIndexJson.put("key", key);
                if (StringUtils.isEmpty(leftValue) && StringUtils.isEmpty(rightValue)) {
                    singleIndexJson.put("operation", "=");
                    singleIndexJson.put("value", "");
                } else {
                    singleIndexJson.put("operation", "between_right");
                    singleIndexJson.put("value", leftValue + "," + rightValue);
                }
                array.add(singleIndexJson);
            }
        }
        return array;
    }

    private static double compareValue(Object bean, Boolean isX) {
        if (isX == null) {
            SingleIndexBean singleIndexBean = (SingleIndexBean) bean;
            if (StringUtils.isEmpty(singleIndexBean.getLeftValue())) {
                return -1;
            } else {
                return Double.parseDouble(singleIndexBean.getLeftValue());
            }
        } else {
            CrossIndexBean crossIndexBean = (CrossIndexBean) bean;
            if (isX) {
                if (StringUtils.isEmpty(crossIndexBean.getYLeftValue())) {
                    return -1;
                } else {
                    return Double.parseDouble(crossIndexBean.getYLeftValue());
                }
            } else {
                if (StringUtils.isEmpty(crossIndexBean.getXLeftValue())) {
                    return -1;
                } else {
                    return Double.parseDouble(crossIndexBean.getXLeftValue());
                }
            }
        }
    }
}
