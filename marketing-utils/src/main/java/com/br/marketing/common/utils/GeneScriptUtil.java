package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.bean.ScoreLable;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import java.util.*;


public class GeneScriptUtil {

    private static final String SPACE_FRAG = " ";

    private static final String BRACE_FRAG_LEFT = "{";

    private static final String BRACE_FRAG_RIGHT = "}";

    private static final String PARENTHESIS_FRAG_LEFT = "(";

    private static final String PARENTHESIS_FRAG_RIGHT = ")";

    private static final String IF_FRAG_LEFT = "if (";

    private static final String CONDITION_ONE = "(item['field_key'] == '";

    private static final String CONDITION_TWO = "' && item['d_value'] !=null && item['d_value'] ";

    private static final String CONDITION_THR = " && item['d_value'] ";

    private static final String FOR_FRAG = "for (item in params['_source']['condition'])";

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

    private static Map<String, String> logicMap;

    private static Map<String, String> opetatorMap;

    static{
        logicMap = ImmutableMap.of(
                LOGIC_AND, LOGIC_OPERATOR_AND,
                LOGIC_OR, LOGIC_OPERATOR_OR);

        opetatorMap = ImmutableMap.<String, String>builder()
                .put(OPERATION_BETWEEN_OPEN + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER)
                .put(OPERATION_BETWEEN_OPEN + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS)
                .put(OPERATION_BETWEEN + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER_EQUAL)
                .put(OPERATION_BETWEEN + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS_EQUAL)
                .put(OPERATION_BETWEEN_LEFT + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER_EQUAL)
                .put(OPERATION_BETWEEN_LEFT + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS)
                .put(OPERATION_BETWEEN_RIGHT + SECTION_IDENTIFIER_LEFT, OPERATOR_GREATER)
                .put(OPERATION_BETWEEN_RIGHT + SECTION_IDENTIFIER_RIGHT, OPERATOR_LESS_EQUAL).build();
    }

    /**
     * @description 生成es打标脚本
     * @param scoreLables
     * @return java.lang.String
     * @author hedongshuo
     * @date 2024/10/26 18:32
     **/
    public static String esLableScript(String scoreLables) throws EncoderException {
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
            sourceBuilder.append(IF_FRAG_LEFT);
            JSONObject condition = jsonObject.getJSONObject("condition");
            process(sourceBuilder, condition);
            sourceBuilder.append(PARENTHESIS_FRAG_RIGHT);
            scoreLable.setConditionSource(sourceBuilder.toString());
        }
        //list排序
        list.sort(Comparator.comparing(ScoreLable::getOrder));
        StringBuilder listValueSource = new StringBuilder();
        StringBuilder valueTypeSource = new StringBuilder();
        //for片段
        listValueSource.append(FOR_FRAG).append(BRACE_FRAG_LEFT);
        valueTypeSource.append(FOR_FRAG).append(BRACE_FRAG_LEFT);
        //条件片段
        for (ScoreLable scoreLable : list) {
            listValueSource.append(scoreLable.getConditionSource()).append(RETURN_FRAG_LEFT).append(scoreLable.getListValue()).append(RETURN_FRAG_RIGHT);
            valueTypeSource.append(scoreLable.getConditionSource()).append(RETURN_FRAG_LEFT).append(scoreLable.getValueType()).append(RETURN_FRAG_RIGHT);
        }
        //}补齐
        listValueSource.append(BRACE_FRAG_RIGHT);
        valueTypeSource.append(BRACE_FRAG_RIGHT);
        //未标记，return片段
        listValueSource.append(RETURN_FRAG_END);
        valueTypeSource.append(RETURN_FRAG_END);
        //生成script_fields
        return geneScript(listValueSource.toString(), valueTypeSource.toString());
    }

    /**
     * 处理一个Json{
     *     type:"logic/operation"
     *     logic:"or/and"
     *     data:[{...}]
     * }
     * data[{
     *     "type": "operation",
     *     "key": "scorencashon58xkcsxcd",
     *     "operation": "between_left",
     *     "value": "75,80"
     * }
     * ]
     * @param sourceBuilder
     * @param condition
     */
    public static void process(StringBuilder sourceBuilder, JSONObject condition) {
        JSONArray data = condition.getJSONArray("data");
        for (int i = 0; i < data.size(); i++) {
            JSONObject dataJson = JSON.parseObject(data.get(i).toString());
            String scoreCondition = analysisData(dataJson, i == data.size() - 1 ? "" : logicMap.get(condition.getString("logic")));
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
    private static String geneScript(String listValueSource, String valueTypeSource) throws EncoderException {
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
        //String转urlCode
        URLCodec urlCodec = new URLCodec();
        return urlCodec.encode(scriptFieldsObject.toString());
    }

    /**
     * 将多层深的data解析为条件脚本
     * @param data
     * @param logicOperator
     * @return String
     */
    public static String analysisData(JSONObject data, String logicOperator) {
        String type = data.getString("type");
        StringBuilder conditionBuilder = new StringBuilder();
        //层级无限延伸
        if ("logic".equals(type)) {
            conditionBuilder.append(PARENTHESIS_FRAG_LEFT);
            process(conditionBuilder, data);
            conditionBuilder.append(PARENTHESIS_FRAG_RIGHT);
            //底层解析
        } else if ("operation".equals(type)) {
            String key = data.getString("key");
            String operatorLeft = opetatorMap.get(data.getString("operation") + SECTION_IDENTIFIER_LEFT);
            String operatorRight = opetatorMap.get(data.getString("operation") + SECTION_IDENTIFIER_RIGHT);
            List<String> value = Arrays.asList(data.getString("value").split(","));
            String valueLeft = value.get(0);
            String valueRight = value.get(1);
            conditionBuilder.append(CONDITION_ONE).append(key).append(CONDITION_TWO).append(operatorLeft).append(SPACE_FRAG).append(valueLeft)
                    .append(CONDITION_THR).append(operatorRight).append(SPACE_FRAG).append(valueRight).append(PARENTHESIS_FRAG_RIGHT);
        }
        if (StringUtils.isNotEmpty(logicOperator)) {
            conditionBuilder.append(logicOperator);
        }
        return conditionBuilder.toString();
    }
}
