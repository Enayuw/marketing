package com.br.marketing.service.Impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.taikang.TaikangClient;
import com.br.marketing.client.taikang.TaikangMarketingEvent;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.CallRecording;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.TaikangTransferDataLog;
import com.br.marketing.entity.TaikangTransferDataLogExample;
import com.br.marketing.mapper.CallRecordingMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.TaikangTransferDataLogMapper;
import com.br.marketing.service.TaikangLeadTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaikangLeadTransferServiceImpl implements TaikangLeadTransferService {

    @Resource
    private CallRecordingMapper callRecordingMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private TaikangClient taikangClient;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TaikangTransferDataLogMapper taikangTransferDataLogMapper;


    @Override
    public Result<Boolean> transferData(String id) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        //消息 业务幂等逻辑
        TaikangTransferDataLogExample example = new TaikangTransferDataLogExample();
        example.createCriteria().andCreateTimeBetween(DateUtil.beginOfDay(new Date()), DateUtil.endOfDay(new Date())).andCallRecordIdEqualTo(Long.valueOf(id));
        List<TaikangTransferDataLog> select = taikangTransferDataLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(select)) {
            return result;
        }

        CallRecording callRecording = callRecordingMapper.selectByPrimaryKey(Long.valueOf(id));
        if (callRecording != null) {
            Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
            String firstLevelKey = taikangConfig.getOrDefault("firstLevelKey", "returnResult");
            String secondLevelKey = taikangConfig.getOrDefault("secondLevelKey", "returnName");
            String applicantName = Optional.ofNullable(callRecording.getReserveField1())
                    .map(TaikangLeadTransferServiceImpl::safeParseToJson)
                    .map((JSONObject reserveJson) -> reserveJson.getString(firstLevelKey))
                    .map(TaikangLeadTransferServiceImpl::safeParseToJson)
                    .map((JSONObject rrJson) -> rrJson.getString(secondLevelKey))
                    .orElse(null);
            MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(callRecording.getApiCode(), callRecording.getCustNum());
            String cell = syncUser.getCell();
            String applicantPhone = BrCipherMaker.getInstance().decode(cell);
            if (applicantPhone == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户，该cell:" + cell +
                        "解密失败，请关注！！！"));
            }
            String browseDate = DateUtil.format(new Date(callRecording.getCallStartTime()), DatePattern.NORM_DATETIME_PATTERN);
            TaikangMarketingEvent taikangMarketingEvent = new TaikangMarketingEvent();
            taikangMarketingEvent.setApplicantPhone(applicantPhone);
            taikangMarketingEvent.setBrowseDate(browseDate);
            taikangMarketingEvent.setApplicantName(applicantName);
            String response = taikangClient.process(taikangMarketingEvent);
            try {
                TaikangTransferDataLog taikangTransferDataLog = new TaikangTransferDataLog();
                taikangTransferDataLog.setCallRecordId(Long.valueOf(id));
                taikangTransferDataLog.setApiCode(callRecording.getApiCode());
                taikangTransferDataLog.setCell(syncUser.getCell());
                taikangTransferDataLog.setName(applicantName);
                taikangTransferDataLog.setBusinessCode(JSONObject.parseObject(response).getInteger("code"));
                taikangTransferDataLog.setReturnContent(response);
                taikangTransferDataLogMapper.insertSelective(taikangTransferDataLog);
            } catch (NumberFormatException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户记录日志异常，拨打明细id:" + id));
            }
            log.warn("泰康大健康线索线索推送客户response:{}", response);
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户，未查询到该id:" + id +
                    "对应通话明细，请关注！！！"));
        }
        return result;
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
