package com.br.marketing.bridge.didiai;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.DrsCustomizeUploadData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 滴滴离线链路中，在「明文行列表」与「营销标准上传批次结构」之间做转换的纯静态工具。
 *
 * <p>按设计文档「清洗映射」口径：不经规则引擎 {@code commonClean}，由代码将 Drs 明文行映射为
 * {@link MarketingPreUserDTO}（手机号 UTF-8 MD5 小写 hex、{@code reserveField1} 等见 {@code json-mapping-rule.md}）。
 *
 * <p>顶层字段对齐设计 §31：接口 {@code requestId} 同时写入 {@code MarketingPreUserDTO#taskId} 与
 * {@code MarketingPreUserDTO#requestId}（§23 透传）；接口 {@code taskId} 写入明细 {@code reserveField1.taskIdDD}。
 *
 * @author yueping.bai
 */
public final class DidiaiOfflinePreUserAssembler {

    private DidiaiOfflinePreUserAssembler() {}

    /**
     * 从滴滴明文行构造标准上传批次：逐行做清洗映射后填入 {@code dataItems}。
     *
     * <p>按 §23，{@code requestId} 透传客户明文首条至顶层 {@code requestId}；按 §31，同一字符串同时写入顶层
     * {@code taskId}；每行接口 {@code taskId} 写入 {@code reserveField1.taskIdDD}。
     *
     * @param apiCode 业务接口编号（当前未使用，保留参数以兼容调用方）
     * @param row     汇总表当前行；当 {@code rows} 无法取出有效 {@code requestId} 时用于回退读取汇总表字段
     * @param rows    非空明文行列表
     * @return 非空的批次对象，可序列化后走标准上传
     */
    public static MarketingPreUserDTO buildMarketingPreUserByCleaningMapping(
            String apiCode, DrsCustomizeUploadData row, List<JSONObject> rows) {
        String batchRequestId = resolveBatchRequestId(rows, row);
        List<MarketingPreUserDetailDTO> items = new ArrayList<>(rows.size());
        for (JSONObject r : rows) {
            items.add(mapPlainRowToDetailByCleaningMapping(r));
        }
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        dto.setTaskId(batchRequestId);
        dto.setRequestId(batchRequestId);
        dto.setDataItems(items);
        return dto;
    }

    /**
     * 解析本批次的请求号字符串：优先透传首行业务请求号；若无则尝试汇总表请求号；仍无则抛异常。
     *
     * <p>按 §23 设计，{@code requestId} 由客户侧按格式生成并传入，百融侧直接透传，**不生成兜底值**。
     * 若无法取到有效 requestId，抛出 {@link IllegalStateException}，由调用方标记 sync_status=4。
     */
    private static String resolveBatchRequestId(List<JSONObject> rows, DrsCustomizeUploadData row) {
        if (rows != null && !rows.isEmpty()) {
            String rid = rows.get(0).getString("requestId");
            if (StringUtils.isNotBlank(rid)) {
                return rid;
            }
        }
        if (row != null && StringUtils.isNotBlank(row.getRequestId())) {
            return row.getRequestId();
        }
        throw new IllegalStateException(
                "离线清洗映射失败：requestId 缺失或空白，无法透传至 b_marketing_sync_info");
    }

    /**
     * 将单条滴滴明文 JSON 清洗映射为一条营销标准明细。
     *
     * <p>映射规则对齐 {@code json-mapping-rule.md} 与设计 §22：
     * <ul>
     *   <li>{@code phone} → UTF-8 MD5 小写 hex → {@code cell}
     *   <li>{@code properties.uid} → {@code custNum}
     *   <li>{@code properties.userType}（必填）→ {@code reserveField1.userType}
     *   <li>{@code properties.userName}（选填）→ {@code reserveField1.userName}
     *   <li>{@code properties.productName}（选填）→ {@code reserveField1.productName}
     *   <li>{@code properties.strategyCode}（选填）→ {@code reserveField1.strategyCode}
     *   <li>接口字段 {@code taskId} → {@code reserveField1.taskIdDD}（§31）
     * </ul>
     */
    private static MarketingPreUserDetailDTO mapPlainRowToDetailByCleaningMapping(JSONObject r) {
        MarketingPreUserDetailDTO d = new MarketingPreUserDetailDTO();
        String phone = r.getString("phone");
        if (StringUtils.isNotBlank(phone)) {
            d.setCell(DigestUtils.md5DigestAsHex(phone.getBytes(StandardCharsets.UTF_8)));
        }
        JSONObject properties = r.getJSONObject("properties");
        if (properties != null) {
            d.setCustNum(properties.getString("uid"));
        }
        d.setOperateType("3");
        String userName = properties == null ? null : properties.getString("userName");
        String productName = properties == null ? null : properties.getString("productName");
        String strategyCode = properties == null ? null : properties.getString("strategyCode");
        String userType =
                properties == null ? null : StringUtils.trimToNull(properties.getString("userType"));
        if (userType == null) {
            throw new IllegalStateException(
                    "离线清洗映射失败：properties.userType 缺失或空白，无法写入 reserveField1.userType");
        }
        JSONObject reserve = new JSONObject();
        reserve.put("userType", userType);
        Object interfaceTaskId = r.get("taskId");
        if (interfaceTaskId != null) {
            reserve.put("taskIdDD", String.valueOf(interfaceTaskId));
        }
        if (StringUtils.isNotBlank(strategyCode)) {
            reserve.put("strategyCode", strategyCode);
        }
        if (StringUtils.isNotBlank(userName)) {
            reserve.put("userName", userName);
        }
        if (StringUtils.isNotBlank(productName)) {
            reserve.put("productName", productName);
        }
        d.setReserveField1(reserve.toJSONString());
        return d;
    }
}
