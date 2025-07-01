package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import com.br.marketing.mapper.XieChengCpsCollidingDataLogMapper;
import com.br.marketing.retry.DatabaseOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 撞库cps日志相关service
 * @author hong.chen
 * @date 2024/03/23
 */
@Service
@Slf4j
public class XieChengCpsCollidingDataLogServiceImpl implements XieChengCpsCollidingDataLogService {
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    XieChengCpsCollidingDataLogMapper xieChengCpsCollidingDataLogMapper;
    @Resource
    private DatabaseOperationService dbService;


    @Override
    public XieChengCpsCollidingDataLog buildSuccessXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType, JSONObject returnData, String httpcode, Integer businessCode) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");
        String hitRequestNo = returnData.getString("hitRequestNo");
        XieChengCpsCollidingDataLog XieChengCpsCollidingDataLog = new XieChengCpsCollidingDataLog();
        XieChengCpsCollidingDataLog.setDataId(id);
        XieChengCpsCollidingDataLog.setPackageId(packageId);
        XieChengCpsCollidingDataLog.setDataSourceType(dataSourceType);
        XieChengCpsCollidingDataLog.setCellSha256CodeList(sha256Code);
        XieChengCpsCollidingDataLog.setReleaseTime(releaseTime);
        XieChengCpsCollidingDataLog.setResult(result);
        XieChengCpsCollidingDataLog.setInfo(info);
        XieChengCpsCollidingDataLog.setOrgChannel(orgChannel);
        XieChengCpsCollidingDataLog.setMktLevel(mktLevel);
        XieChengCpsCollidingDataLog.setHitRequestNo(hitRequestNo);
        XieChengCpsCollidingDataLog.setHttpCode(Integer.valueOf(httpcode));
        XieChengCpsCollidingDataLog.setBusinessCode(businessCode);
        XieChengCpsCollidingDataLog.setReturnContent(returnData.toString(SerializerFeature.WriteMapNullValue));
        XieChengCpsCollidingDataLog.setCreateTime(new Date());
        XieChengCpsCollidingDataLog.setUpdateTime(new Date());
        return XieChengCpsCollidingDataLog;
    }

    @Override
    public XieChengCpsCollidingDataLog buildFailXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType, String cellSha256CodeList, JSONObject resJson) {
        String httpcode = resJson.getString("httpcode");
        XieChengCpsCollidingDataLog XieChengCpsCollidingDataLog = new XieChengCpsCollidingDataLog();
        XieChengCpsCollidingDataLog.setDataId(id);
        XieChengCpsCollidingDataLog.setPackageId(packageId);
        XieChengCpsCollidingDataLog.setDataSourceType(dataSourceType);
        XieChengCpsCollidingDataLog.setCellSha256CodeList(cellSha256CodeList);
        XieChengCpsCollidingDataLog.setHttpCode(StringUtils.isEmpty(httpcode) ? null : Integer.valueOf(httpcode));
        try {
            if (StringUtils.isNotEmpty(resJson.getString("content"))) {
                JSONObject contentJson = JSONObject.parseObject(resJson.getString("content"));
                Integer businessCode = contentJson.getInteger("code");
                XieChengCpsCollidingDataLog.setBusinessCode(businessCode);
            }
        } catch (Exception e) {
            log.warn("解析businessCode异常:", e);
        }
        XieChengCpsCollidingDataLog.setReturnContent(resJson.toString(SerializerFeature.WriteMapNullValue));
        XieChengCpsCollidingDataLog.setCreateTime(new Date());
        XieChengCpsCollidingDataLog.setUpdateTime(new Date());
        return XieChengCpsCollidingDataLog;
    }

    @Override
    public void pushLogMessage(List<XieChengCpsCollidingDataLog> collidingLogs) {
        rocketMqSwitch.syncSend(
                MarketingXieChengConstants.TOPIC,
                MarketingXieChengConstants.TAG_MARKETING_XIECHENG_CPS_COLLIDING_LOG_QUEUE,
                JSONObject.toJSONString(collidingLogs));
    }

    @Override
    public void pushRobotMessage(List<XieChengCpsCollidingDataLog> collidingLogs) {
        if (CollectionUtils.isEmpty(collidingLogs)) {
            return;
        }

        List<String> sha256CodeListFalseList = collidingLogs.stream()
                .filter(item -> !item.getResult())
                .map(XieChengCpsCollidingDataLog::getCellSha256CodeList)
                .collect(Collectors.toList());
        String jsonString = JSON.toJSONString(sha256CodeListFalseList);
        rocketMqSwitch.syncSend(MarketingXieChengConstants.TOPIC
                , MarketingXieChengConstants.TAG_MARKETING_XIECHENG_CPS_PUSH_ROBOT, jsonString);
    }

    @Override
    public Result<Boolean> saveXieChengCpsCollidingDataLog(List<XieChengCpsCollidingDataLog> collidingLogs) {
        for (XieChengCpsCollidingDataLog collidingLog : collidingLogs) {
            try {
                xieChengCpsCollidingDataLogMapper.insertSelective(collidingLog);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                        , "携程CPS撞库保存日志异常！"), e);
                DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
                dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                    @Override
                    public void execute() {
                        xieChengCpsCollidingDataLogMapper.insertSelective(collidingLog);
                    }
                    @Override
                    public Object getParams() {
                        return collidingLog;
                    }
                    @Override
                    public String getMapperClass() {
                        return "com.br.marketing.mapper.XieChengCpsCollidingDataLogMapperBase";
                    }
                    @Override
                    public String getMapperMethod() {
                        return "insertSelective";
                    }
                },"携程cps撞库日志写入", config);
            }
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
