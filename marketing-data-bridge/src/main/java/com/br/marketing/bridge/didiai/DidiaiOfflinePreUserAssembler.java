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
import java.util.concurrent.ThreadLocalRandom;

/**
 * 滴滴离线链路中，在「明文行列表」与「营销标准上传批次结构」之间做转换的纯静态工具。
 *
 * <p>按设计文档「清洗映射」口径：不经规则引擎 {@code commonClean}，由代码将 Drs 明文行映射为
 * {@link MarketingPreUserDTO}（手机号 UTF-8 MD5 小写 hex、{@code reserveField1} 等见 {@code json-mapping-rule.md}）。
 *
 * @author yueping.bai
 */
public final class DidiaiOfflinePreUserAssembler {

    private DidiaiOfflinePreUserAssembler() {}

    /**
     * 从滴滴明文行构造标准上传批次：逐行做清洗映射后填入 {@code dataItems}，并生成批次 {@code taskId}/{@code requestId}。
     *
     * @param apiCode 业务接口编号
     * @param row     汇总表当前行
     * @param rows    非空明文行列表
     * @return 非空的批次对象，可序列化后走标准上传
     */
    public static MarketingPreUserDTO buildMarketingPreUserByCleaningMapping(
            String apiCode, DrsCustomizeUploadData row, List<JSONObject> rows) {
        String taskIdStr = resolveBatchTaskId(rows, row);
        String requestIdStr =
                apiCode
                        + "_"
                        + row.getId()
                        + "_"
                        + System.currentTimeMillis()
                        + "_"
                        + ThreadLocalRandom.current().nextInt(1_000_000);
        List<MarketingPreUserDetailDTO> items = new ArrayList<>(rows.size());
        for (JSONObject r : rows) {
            items.add(mapPlainRowToDetailByCleaningMapping(r));
        }
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        dto.setTaskId(taskIdStr);
        dto.setRequestId(requestIdStr);
        dto.setDataItems(items);
        return dto;
    }

    /**
     * 解析本批次的任务号字符串：优先使用首行业务任务号；若无则依次尝试汇总表请求号、汇总表主键；仍无则使用固定占位字面量。
     */
    private static String resolveBatchTaskId(List<JSONObject> rows, DrsCustomizeUploadData row) {
        if (rows != null && !rows.isEmpty()) {
            Object tid = rows.get(0).get("taskId");
            if (tid != null) {
                return String.valueOf(tid);
            }
        }
        if (row != null && StringUtils.isNotBlank(row.getRequestId())) {
            return row.getRequestId();
        }
        if (row != null && row.getId() != null) {
            return String.valueOf(row.getId());
        }
        return "didiai_batch";
    }

    /**
     * 将单条滴滴明文 JSON 清洗映射为一条营销标准明细。
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
        Object taskIdObj = r.get("taskId");
        String strategyCode = taskIdObj == null ? "" : String.valueOf(taskIdObj);
        String userName = properties == null ? null : properties.getString("userName");
        String productName = properties == null ? null : properties.getString("productName");
        String userType =
                properties == null ? null : StringUtils.trimToNull(properties.getString("userType"));
        if (userType == null) {
            throw new IllegalStateException(
                    "离线清洗映射失败：properties.userType 缺失或空白，无法写入 reserveField1.userType");
        }
        JSONObject reserve = new JSONObject();
        reserve.put("userType", userType);
        reserve.put("strategyCode", strategyCode);
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
