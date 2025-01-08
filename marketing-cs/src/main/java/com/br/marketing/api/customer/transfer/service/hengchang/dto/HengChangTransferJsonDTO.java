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

    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    protected TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode
            , TransferDataDTO<TransferDataItemDTO> transferDataDTO) {

        //requestId：yyyymmdd_apicde_五位随机数加毫秒级时间戳
        long timestamp = System.currentTimeMillis();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        String requestId = date.concat("_") + apiCode.concat("_") + RandomStringUtils.randomNumeric(5)+timestamp;
        transferDataDTO.setRequestId(requestId);
        List<TransferDataItemDTO> objects = new ArrayList<>();
        JSONArray data = this.getUserTransferInfoList();
        int size = data.size();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            TransferDataItemDTO dto = new TransferDataItemDTO();
            dto.setApiCode(apiCode);
            // 根据uniqueId找上传数据有效期内最新的一条custNum对应的userType
            if(jsonObject.getString("uniqueId") != null){
                String custNum = jsonObject.getString("uniqueId");
                dto.setCustNum(custNum);
                Set<String> custNumSet = new HashSet<>();
                custNumSet.add(custNum);
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNumAndTaskId =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, new Date());
                SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNumAndTaskId.get(custNum);
                if(syncUserValidityPeriodsBO != null){
                    List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
                    dto.setUserType(syncUsers.get(0).getUserType());
                }
            }
            dto.setLoginTime(jsonObject.getString("lastLoginTime"));
            dto.setApplyDt(jsonObject.getString("creditGrantingTime"));
            dto.setApplyResult(jsonObject.getString("creditResult"));
            dto.setAuditTime(jsonObject.getString("creditAuditTime"));
            dto.setAuditAmount(jsonObject.getString("creditAmount"));
            dto.setIfLent(jsonObject.getString("lentStatus"));
            dto.setLentTime(jsonObject.getString("lentTime"));
            dto.setLentAmount(jsonObject.getString("lentAmount"));
            dto.setUnlentAmount(jsonObject.getString("creditBalance"));

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("cell",jsonObject.getString("phone"));
            jsonObject1.put("name",jsonObject.getString("name"));
            jsonObject1.put("creditPushRiskTime",jsonObject.getString("creditPushRiskTime"));
            jsonObject1.put("loanTime",jsonObject.getString("loanTime"));
            jsonObject1.put("loanRiskResult",jsonObject.getString("loanRiskResult"));
            jsonObject1.put("loanPushRiskTime",jsonObject.getString("loanPushRiskTime"));
            jsonObject1.put("creditChannelCode",jsonObject.getString("creditChannelCode"));
            jsonObject1.put("loanChannelCode",jsonObject.getString("loanChannelCode"));
            jsonObject1.put("isBlack",jsonObject.getString("complaintFlag"));

            if (StringUtils.isNotEmpty(jsonObject.getString("extra"))) {
                JSONObject jsonObject2 = JSONObject.parseObject(jsonObject.getString("extra"));
                for (String key : jsonObject2.keySet()) {
                    Object value = jsonObject2.get(key);
                    jsonObject1.put(key, value);
                }
            }
            dto.setReserveField1(JSON.toJSONString(jsonObject1));
            objects.add(dto);
        }
        transferDataDTO.setDataItems(objects);
        return transferDataDTO;
    }

}
