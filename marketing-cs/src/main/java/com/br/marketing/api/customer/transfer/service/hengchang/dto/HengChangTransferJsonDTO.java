package com.br.marketing.api.customer.transfer.service.hengchang.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName HengChangTransferJsonDTO
 * @Author kongbx
 * @Date 2025/1/7 14:40
 */
@Data
public class HengChangTransferJsonDTO extends TransferDataAdaptee {

    /**
     * 此次营销对应的任务ID
     */
    private String taskCode;


    /**
     * 数据拆分后的子任务ID
     */
    private String batchId;

    /**
     * uniqueId	String	是	用户的唯一编号
     * phone	String	是	手机号
     * name	String	否	姓名
     * lastLoginTime	String	否	最近一次登录时间	格式：yyyy-MM-dd-HH:mm:ss
     * creditGrantingTime	String	否	授信申请时间	格式：yyyy-MM-dd-HH:mm:ss
     * creditPushRiskTime	String	否	授信推送风控时间	格式：yyyy-MM-dd-HH:mm:ss
     * creditResult	String	否	授信审核结果	1：PASS  0：拒 绝
     * creditAuditTime	String	否	授信审核时间	格式：yyyy-MM-dd-HH:mm:ss
     * creditAmount	String	否	授信额度	授信成功才有额 度，单位:分
     * loanTime	String	否	用信进件时间	格式：yyyy-MM-dd-HH:mm:ss
     * loanRiskResult	String	否	用信风控审核结果	格式：yyyy-MM-dd-HH:mm:ss
     * loanPushRiskTime	String	否	用信风控审核时间	格式：yyyy-MM-dd-HH:mm:ss
     * lentAmount	Long	否	放款金额	单位:分
     * lentStatus	Integer	否	放款结果	1、成功 0、失 败
     * lentTime	String	否	放款时间	格式：yyyy-MM-dd-HH:mm:ss
     * creditChannelCode	Integer	否	授信渠道类型	1-APP  2-API
     * loanChannelCode	Integer	否	用信渠道类型	1-APP  2-API
     * complaintFlag	Integer	否	是否客诉/黑名单	1、是 0、否
     * creditBalance	Long	否	可用额度	单位:分
     * extra	String	否	预留字段	预留字段，用于传输其他自定义字段
     */
    private JSONArray userTransferInfoList;

    @Override
    protected TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode,
                                                                  TransferDataDTO<TransferDataItemDTO> transferDataDTO) {
        // 生成请求ID
        transferDataDTO.setRequestId(generateRequestId(apiCode));

        // 处理数据转换
        transferDataDTO.setDataItems(processTransferDataItems(apiCode));
        return transferDataDTO;
    }

    /**
     * 生成请求ID: yyyyMMdd_apiCode_五位随机数加毫秒级时间戳
     */
    private String generateRequestId(String apiCode) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT))
                + "_" + apiCode + "_"
                + RandomStringUtils.randomNumeric(5)
                + System.currentTimeMillis();
    }

    /**
     * 处理数据项转换
     */
    private List<TransferDataItemDTO> processTransferDataItems(String apiCode) {
        List<TransferDataItemDTO> items = new ArrayList<>();
        JSONArray data = this.getUserTransferInfoList();

        for (int i = 0; i < data.size(); i++) {
            JSONObject sourceData = data.getJSONObject(i);
            TransferDataItemDTO item = new TransferDataItemDTO();

            // 设置基本字段
            setBasicFields(item, apiCode, sourceData);

            // 处理保留字段
            item.setReserveField1(buildReserveField(sourceData));

            items.add(item);
        }
        return items;
    }

    /**
     * 设置基本字段
     */
    private void setBasicFields(TransferDataItemDTO item, String apiCode, JSONObject sourceData) {
        item.setApiCode(apiCode);
        item.setCustNum(removeAndGet(sourceData, "uniqueId"));
        item.setLoginTime(removeAndGet(sourceData, "lastLoginTime"));
        item.setApplyDt(removeAndGet(sourceData, "creditGrantingTime"));
        item.setApplyResult(removeAndGet(sourceData, "creditResult"));
        item.setAuditTime(removeAndGet(sourceData, "creditAuditTime"));
        item.setAuditAmount(removeAndGet(sourceData, "creditAmount"));
        item.setIfLent(removeAndGet(sourceData, "lentStatus"));
        item.setLentTime(removeAndGet(sourceData, "lentTime"));
        item.setLentAmount(removeAndGet(sourceData, "lentAmount"));
        item.setUnlentAmount(removeAndGet(sourceData, "creditBalance"));
    }

    /**
     * 构建保留字段JSON
     */
    private String buildReserveField(JSONObject sourceData) {
        JSONObject reserveField = new JSONObject();

        // 添加固定字段
        addFixedFields(reserveField, sourceData);

        // 处理额外字段
        processExtraFields(reserveField, sourceData);

        // 添加剩余字段
        addRemainingFields(reserveField, sourceData);

        return JSON.toJSONString(reserveField);
    }

    /**
     * 添加固定字段
     */
    private void addFixedFields(JSONObject reserveField, JSONObject sourceData) {
        reserveField.put("cell", removeAndGet(sourceData, "phone"));
        reserveField.put("name", removeAndGet(sourceData, "name"));
        reserveField.put("creditPushRiskTime", removeAndGet(sourceData, "creditPushRiskTime"));
        reserveField.put("loanTime", removeAndGet(sourceData, "loanTime"));
        reserveField.put("loanRiskResult", removeAndGet(sourceData, "loanRiskResult"));
        reserveField.put("loanPushRiskTime", removeAndGet(sourceData, "loanPushRiskTime"));
        reserveField.put("creditChannelCode", removeAndGet(sourceData, "creditChannelCode"));
        reserveField.put("loanChannelCode", removeAndGet(sourceData, "loanChannelCode"));
        reserveField.put("isBlack", removeAndGet(sourceData, "complaintFlag"));
        reserveField.put("taskCode", this.taskCode);
        reserveField.put("batchId", this.batchId);
    }

    /**
     * 处理额外字段
     */
    private void processExtraFields(JSONObject reserveField, JSONObject sourceData) {
        String extraStr = sourceData.getString("extra");
        if (StringUtils.isNotEmpty(extraStr)) {
            JSONObject extra = JSONObject.parseObject(extraStr);
            extra.forEach(reserveField::put);
        }
        sourceData.remove("extra");
    }

    /**
     * 添加剩余字段
     */
    private void addRemainingFields(JSONObject reserveField, JSONObject sourceData) {
        sourceData.forEach(reserveField::put);
    }

    /**
     * 从JSONObject中获取并移除指定key的值
     */
    private String removeAndGet(JSONObject jsonObject, String key) {
        String value = jsonObject.getString(key);
        jsonObject.remove(key);
        return value;
    }


}
