package com.br.marketing.context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.SmsCallbackAtOnce;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.enums.XcReportTypeEnum;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Random;
import java.util.UUID;

@Data
@Builder
public class XieChengReportContext {
    private Integer type;
    // 基础数据
    private CallRecord callRecord;
    private SmsCallbackAtOnce smsCallbackAtOnce;
    private String apiCode;
    private AdReqDTO adReqDTO;
    private XieChengData resultData;
    private PushConfig pushConfig;
    private String tcId;
    private String sha256Tel;
    private String redisKey;
    private String redisValue;
    private boolean continueFlag;
    private boolean exceptionFlag;
    private String clickId;
    private Result pushResult;
    /**
     * 解析推送配置
     */
    @Data
    @Builder
    public static class PushConfig {
        private String conditionKey;
        private JSONArray soleCellApiCodes;
        private JSONArray isBlackApiCodes;
        private JSONArray convTypeApiCodes;
        private String mainApiCode;
        private Boolean offRepeatByPeriod;
        private Integer offRepeatCount;
        private Boolean mock;

        /**
         * 从JSONObject创建PushConfig
         */
        public static PushConfig fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            Boolean offRepeatByPeriod = json.getBoolean("offRepeatByPeriod") == null
                    ? false : json.getBoolean("offRepeatByPeriod");
            Boolean mock = json.getBoolean("mock") == null
                    ? false : json.getBoolean("mock");
            return PushConfig.builder()
                    .conditionKey(json.getString("condition"))
                    .soleCellApiCodes(json.getJSONArray("soleCellApiCodes"))
                    .isBlackApiCodes(json.getJSONArray("isBlackApiCodes"))
                    .convTypeApiCodes(json.getJSONArray("convTypeApiCodes"))
                    .mainApiCode(json.getString("mainApiCode"))
                    .offRepeatByPeriod(offRepeatByPeriod)
                    .offRepeatCount(offRepeatByPeriod ? json.getInteger("offRepeatCount") : null)
                    .mock(mock)
                    .build();
        }
    }

    /**
     * 创建上下文对象，type = 1-通话明细
     */
    public static XieChengReportContext create(CallRecord callRecord, XieChengData xieChengData, String tcId) {
        return XieChengReportContext.builder()
                .type(XcReportTypeEnum.CALL.getValue())
                .callRecord(callRecord)
                .apiCode(xieChengData.getApiCode())
                .adReqDTO(convertToAdReqDTO(xieChengData))
                .resultData(createResultData(xieChengData.getId()))
                .sha256Tel(xieChengData.getSha256Tel())
                .tcId(tcId)
                .continueFlag(true)
                .exceptionFlag(false)
                .build();
    }

    /**
     * 创建上下文对象，type = 2-短信
     */
    public static XieChengReportContext create(SmsCallbackAtOnce smsCallbackAtOnce, XieChengData xieChengData, String tcId) {
        return XieChengReportContext.builder()
                .type(XcReportTypeEnum.SMS.getValue())
                .smsCallbackAtOnce(smsCallbackAtOnce)
                .apiCode(xieChengData.getApiCode())
                .adReqDTO(convertToAdReqDTO(xieChengData))
                .resultData(createResultData(xieChengData.getId()))
                .sha256Tel(xieChengData.getSha256Tel())
                .tcId(tcId)
                .continueFlag(true)
                .exceptionFlag(false)
                .build();
    }

    /**
     * 设置错误信息并停止处理
     */
    public void setError(String message) {
        this.continueFlag = false;
        this.resultData.setStatus(2);
        this.resultData.setDataMessage(message);
    }

    /**
     * 设置成功信息
     */
    public void setSuccess() {
//        this.resultData.setStatus(1);
    }

    /**
     * 生成ClickId
     */
    public void generateClickId() {
        this.clickId = System.currentTimeMillis() + getRandomCode(5) + this.sha256Tel;
        this.adReqDTO.setClickId(this.clickId);
    }

    /**
     * 更新推送结果
     */
    public void updatePushResult(Result result) {
        this.pushResult = result;
        if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
            this.resultData.setPushStatus(2);
        } else {
            this.resultData.setPushStatus(3);
        }
        this.resultData.setDataMessage(result.getMessage());
    }

    /**
     * 获取Redis锁
     */
    public void acquireLock(RedisChgService redisChgService) {
        this.redisKey = RedisKeyConstant.pushXieChengLock + ":" +
                this.pushConfig.getConditionKey() + this.sha256Tel;
        this.redisValue = UUID.randomUUID().toString();
        redisChgService.lock(this.redisKey, this.redisValue);
    }

    /**
     * 释放Redis锁
     */
    public void releaseLock(RedisChgService redisChgService) {
        if (this.redisKey != null && this.redisValue != null) {
            redisChgService.unlock(this.redisKey, this.redisValue);
        }
    }

    /**
     * 转换为AdReqDTO
     */
    private static AdReqDTO convertToAdReqDTO(XieChengData data) {
        AdReqDTO dto = new AdReqDTO();
        BeanUtils.copyProperties(data, dto);
        return dto;
    }

    /**
     * 创建结果数据
     */
    private static XieChengData createResultData(Long dataId) {
        XieChengData result = new XieChengData();
        result.setId(dataId);
        return result;
    }

    /**
     * 生成随机码
     */
    private static String getRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}


