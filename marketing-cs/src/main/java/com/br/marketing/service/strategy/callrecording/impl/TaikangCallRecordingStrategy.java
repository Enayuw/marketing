package com.br.marketing.service.strategy.callrecording.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.client.taikang.TaikangClient;
import com.br.marketing.client.taikang.TaikangMarketingEvent;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.CallRecordLLMResultV2;
import com.br.marketing.entity.TaikangTransferDataLog;
import com.br.marketing.mapper.TaikangTransferDataLogMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.strategy.callrecording.CallRecordingInsertStrategy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.base.Splitter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaikangCallRecordingStrategy implements CallRecordingInsertStrategy {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TaikangClient taikangClient;
    @Resource
    private TaikangTransferDataLogMapper taikangTransferDataLogMapper;

    /**
     * 是否需要处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2025/11/26
     */
    @Override
    public Boolean isProcessingRequired(CallRecordLLMResultV2 callRecordLLMResultV2) {
        Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
        String apiCodes = taikangConfig.getOrDefault("apiCode", "3750004");
        List<String> apiCodeList = Splitter.on(",").splitToList(apiCodes);
        return apiCodeList.contains(callRecordLLMResultV2.getApiCode());
    }

    /**
     * 通话明细数据处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @author senyang.zheng
     * @date 2025/11/26
     */
    @Override
    public void process(CallRecordLLMResultV2 callRecordLLMResultV2) {
        Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
        String nameKey = taikangConfig.getOrDefault("nameKey", "et_returnName");
        String cellKey = taikangConfig.getOrDefault("cellKey", "cell");
        String remarkKey = taikangConfig.getOrDefault("remarkKey", "return_result1");
        String applicantName = Optional.ofNullable(callRecordLLMResultV2.getReserveField1())
                .map(TaikangCallRecordingStrategy::safeParseToJson)
                .map((JSONObject reserveJson) -> reserveJson.getString(nameKey))
                .orElse(null);
        String cell = Optional.ofNullable(callRecordLLMResultV2.getReserveField1())
                .map(TaikangCallRecordingStrategy::safeParseToJson)
                .map((JSONObject reserveJson) -> reserveJson.getString(cellKey))
                .orElse(null);
        String remark = Optional.ofNullable(callRecordLLMResultV2.getReserveField1())
                .map(TaikangCallRecordingStrategy::safeParseToJson)
                .map((JSONObject reserveJson) -> reserveJson.getString(remarkKey))
                .orElse(null);
        if (cell == null) {
            cell = callRecordLLMResultV2.getCustNum();
        }
        String applicantPhone = RpcClientProxy.decode(cell, "cell", "md5", "");
        if (StringUtils.isEmpty(applicantPhone)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户，该cell:" + cell +
                    "解密失败，请关注！！！"));
        }
        try {
            String browseDate = DateUtil.format(new Date(callRecordLLMResultV2.getCallStartTime()), DatePattern.NORM_DATETIME_PATTERN);
            TaikangMarketingEvent taikangMarketingEvent = new TaikangMarketingEvent();
            taikangMarketingEvent.setApplicantPhone(applicantPhone);
            taikangMarketingEvent.setBrowseDate(browseDate);
            taikangMarketingEvent.setApplicantName(applicantName);
            taikangMarketingEvent.setRemark(remark);
            String response = taikangClient.process(taikangMarketingEvent);
            TaikangTransferDataLog taikangTransferDataLog = new TaikangTransferDataLog();
            taikangTransferDataLog.setCallRecordId(callRecordLLMResultV2.getId());
            taikangTransferDataLog.setApiCode(callRecordLLMResultV2.getApiCode());
            taikangTransferDataLog.setCell(cell);
            taikangTransferDataLog.setName(applicantName);
            String httpCode = Optional.ofNullable(response)
                    .map(TaikangCallRecordingStrategy::safeParseToJson)
                    .map((JSONObject res) -> res.getString("httpcode"))
                    .orElse(null);
            taikangTransferDataLog.setHttpCode(httpCode);
            String businessCode = Optional.ofNullable(response)
                    .map(TaikangCallRecordingStrategy::safeParseToJson)
                    .map((JSONObject res) -> res.getString("content"))
                    .map(TaikangCallRecordingStrategy::safeParseToJson)
                    .map((JSONObject cnt) -> cnt.getString("code"))
                    .orElse(null);
            taikangTransferDataLog.setBusinessCode(businessCode);
            taikangTransferDataLog.setReturnContent(response);
            taikangTransferDataLogMapper.insertSelective(taikangTransferDataLog);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(),
                    "泰康大健康线索线索推送客户记录日志异常，拨打明细id:" + callRecordLLMResultV2.getId()));
        }
    }

    /**
     * 安全解析 JSON 字符串为 JSONObject，解析失败返回 null 并记录日志
     */
    private static JSONObject safeParseToJson(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            // 记录解析失败但不抛异常，便于 Optional 链继续工作
            log.warn("解析 JSON 失败，input: {}", jsonStr, e);
            return null;
        }
    }
}
