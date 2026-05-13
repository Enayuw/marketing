package com.br.marketing.util.didiai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 滴滴 AI 上传明文的解析工具：把接入阶段写入汇总表的整段 UTF-8 文本，还原成与在线校验时含义一致的多行业务对象列表。
 *
 * <p>离线任务读出汇总表中的请求体字段后，先经本类拆成「行」再交给后续步骤做批次包装、规则清洗或兜底映射，最终写入营销标准上传前置库。
 * 支持的顶层形态包括：纯 JSON 数组，每元素一行；带明细数组字段的对象，则只展开该数组；若顶层是单个对象且没有明细数组，
 * 则视为一行并包成单元素列表。这样在线网关与离线任务对同一段落库字符串的理解保持一致。
 *
 * <p>本类无实例状态，全部静态方法可在多线程环境下并发调用。
 *
 * @author yueping.bai
 */
public final class DidiaiPlaintextParser {

    private DidiaiPlaintextParser() {}

    /**
     * 将明文字符串解析为行列表，供离线任务在规则清洗与兜底分支中复用。
     *
     * <p>入参为空或只含空白时返回空列表而不是空引用，便于调用方统一分支。入参非空但 JSON 非法、或结构无法展开时，
     * 抛出非法参数异常并携带原因，由上层捕获后把该条汇总记录标记为处理失败。
     *
     * @param plaintext 与在线接入阶段相同的明文字符串，允许为空
     * @return 非空引用；无数据时为长度为零的列表，元素顺序与数组或展开后的明细顺序一致，且会跳过空元素
     * @throws IllegalArgumentException 明文非空白却无法完成解析或展开时抛出，内层原因见异常链
     */
    public static List<JSONObject> toCleanInputRows(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            return Collections.emptyList();
        }
        String trimmed = plaintext.trim();
        JSONArray records;
        try {
            if (trimmed.startsWith("[")) {
                records = JSON.parseArray(trimmed);
            } else {
                JSONObject root = JSON.parseObject(trimmed);
                if (root.containsKey("dataItems")) {
                    records = root.getJSONArray("dataItems");
                    if (records == null) {
                        records = new JSONArray();
                    }
                } else {
                    records = new JSONArray();
                    records.add(root);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("明文 JSON 解析失败", e);
        }
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<JSONObject> out = new ArrayList<>(records.size());
        for (int i = 0; i < records.size(); i++) {
            JSONObject row = records.getJSONObject(i);
            if (row != null) {
                out.add(row);
            }
        }
        return out;
    }
}
