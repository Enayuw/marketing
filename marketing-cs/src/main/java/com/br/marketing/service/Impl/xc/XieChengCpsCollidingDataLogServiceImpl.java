package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.SnowflakeIdGenerator;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import com.br.marketing.mapper.XieChengCpsCollidingDataLogMapper;
import com.br.marketing.retry.DatabaseOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;


    @Override
    public XieChengCpsCollidingDataLog buildSuccessXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
                                                                               JSONObject returnData, String httpcode, Integer businessCode) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");
        String hitRequestNo = returnData.getString("hitRequestNo");
        XieChengCpsCollidingDataLog XieChengCpsCollidingDataLog = new XieChengCpsCollidingDataLog();
        XieChengCpsCollidingDataLog.setIdempotentKey(snowflakeIdGenerator.nextIdString());
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
    public XieChengCpsCollidingDataLog buildFailXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
                                                                            String cellSha256CodeList, JSONObject resJson) {
        String httpcode = resJson.getString("httpcode");
        XieChengCpsCollidingDataLog XieChengCpsCollidingDataLog = new XieChengCpsCollidingDataLog();
        XieChengCpsCollidingDataLog.setIdempotentKey(snowflakeIdGenerator.nextIdString());
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
                MarketingXieChengConstants.CPS_LOG_TOPIC,
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

        if (CollectionUtils.isEmpty(sha256CodeListFalseList)) {
            return;
        }

        String jsonString = JSON.toJSONString(sha256CodeListFalseList);
        rocketMqSwitch.syncSend(MarketingXieChengConstants.CPS_PUSH_ROBOT_TOPIC
                , MarketingXieChengConstants.TAG_MARKETING_XIECHENG_CPS_PUSH_ROBOT, jsonString);
    }

    @Override
    public Result<Boolean> saveXieChengCpsCollidingDataLog(List<XieChengCpsCollidingDataLog> collidingLogs) {
        try {
            // 批量插入数据
            xieChengCpsCollidingDataLogMapper.batchInsert(collidingLogs);
        } catch (DuplicateKeyException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS撞库保存日志，发现重复消息"), e);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS撞库批量保存日志异常！"), e);

            // 批量插入失败，尝试重试机制
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            try {
                dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                    @Override
                    public void execute() {
                        xieChengCpsCollidingDataLogMapper.batchInsert(collidingLogs);
                    }

                    @Override
                    public Object getParams() {
                        return collidingLogs;
                    }

                    @Override
                    public String getMapperClass() {
                        return "com.br.marketing.mapper.XieChengCpsCollidingDataLogMapper";
                    }

                    @Override
                    public String getMapperMethod() {
                        return "batchInsert";
                    }
                }, "携程cps撞库日志批量写入", config);
            } catch (Exception ex) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                        , "携程CPS撞库保存日志，3次重试后仍然异常，需要人工处理，log：" + JSON.toJSONString(collidingLogs)), ex);
            }
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
