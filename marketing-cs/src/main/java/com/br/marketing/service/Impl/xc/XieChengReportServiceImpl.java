package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.chain.xiecheng.XieChengReportHandlerChain;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.dto.xiecheng.XieChengReportMessageDTO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.SmsCallbackAtOnceExample;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.SmsCallbackAtOnceMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.VariableAllocationService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.RandomUtil;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class XieChengReportServiceImpl implements XieChengReportService {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengDataMapper xieChengDataMapper;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    private XieChengReportHandlerChain handlerChain;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private DatabaseOperationService dbService;

    @Resource
    XieChengService xieChengService;
    @Resource
    VariableAllocationService variableAllocationService;
    @Resource
    private SmsCallbackAtOnceMapper smsCallbackAtOnceMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    private static final String ACTIONTYPE_IVR = "IVR";

    private static final String ACTIONTYPE_SMS = "SMS";

    @Override
    public Result pushXieChengData(XieChengReportMessageDTO messageDTO) {
        Long sourceId = messageDTO.getSourceId();
        long start = System.currentTimeMillis();
        CallRecord callRecord;
        XieChengData xieChengData;
        String lockKey = null;
        String lockValue = null;
        try {
            //1.查询【b_call_record】
            callRecord = callRecordMapper.selectByPrimaryKey(sourceId);
            if (callRecord == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "携程上报异常，未查询到通话明细，callRecordId=" + sourceId));
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
            //2.插入【b_xiecheng_data】
            xieChengData = keepRecord(callRecord, messageDTO);
        } catch (DuplicateKeyException exception) {
            String msg = "携程上报异常，消息重复消费入库，callRecordId=" + sourceId
                    + "，idempotentKey=" + messageDTO.getIdempotentKey();
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.P_OF_VIP_GROUP.toString());
            dingDingRobotHookService.sendDingDingTextMessage(msg, groupInfo);
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        } catch (Exception exception) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程上报异常，通话明细查询或携程上报插入异常，消息将退回队列中，callRecordId=" + sourceId));
            throw exception;
        }
        try {
            //3.获取tcId
            String tcId = tableCreateService.getIcIdVt(callRecord.getApiCode());
            //4.创建上下文
            XieChengReportContext context = XieChengReportContext.create(callRecord, xieChengData, tcId);
            JSONObject condition = marketingCommonConfig.getXieChengCallPushCondition().get(callRecord.getApiCode());
            context.setPushConfig(XieChengReportContext.PushConfig.fromJson(condition));
            context.getAdReqDTO().setConditionKey(context.getPushConfig().getConditionKey());
            //5.获取Redis锁
            lockKey = RedisKeyConstant.pushXieChengLock
                    + ":" + context.getType()
                    + ":" + context.getPushConfig().getConditionKey()
                    + ":" + context.getSha256Tel();
            lockValue = UUID.randomUUID().toString();
            redisChgService.lock(lockKey, lockValue);
            //6.执行责任链
            handlerChain.handle(context);
            if (!context.isContinueFlag()) {
                updateResult(context.getResultData());
                if (context.isExceptionFlag()) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                            "携程上报handler处理异常，请查看数据库获取具体报错"));
                }
                redisChgService.unlock(lockKey, lockValue);
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
            //7.推送
            String clickId = System.currentTimeMillis() + RandomUtil.generateCode(5) + context.getSha256Tel();
            context.getAdReqDTO().setClickId(clickId);
            Result result = xieChengService.pushXieChengDataNew(context.getAdReqDTO(), context.getPushConfig().getMock());
            if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                context.getResultData().setPushStatus(XcReportPushStatusEnum.PUSHED.getValue());
            } else {
                context.getResultData().setPushStatus(XcReportPushStatusEnum.PUSH_FAIL.getValue());
            }
            context.getResultData().setClickId(clickId);
            context.getResultData().setDataMessage(result.getMessage());
            updateResult(context.getResultData());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程上报异常，callRecordId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
        } finally {
            if (lockKey != null && lockValue != null) {
                try {
                    redisChgService.unlock(lockKey, lockValue);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                            "携程上报解锁异常，lockKey=" + lockKey + ",lockValue=" + lockValue), e);
                }
            }
            log.warn("携程上报消费消息{}耗时：{}ms", sourceId, (System.currentTimeMillis() - start));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
    }

    private XieChengData keepRecord(CallRecord callRecord, XieChengReportMessageDTO messageDTO) {
        XieChengData xieChengData = new XieChengData();
        xieChengData.setApiCode(callRecord.getApiCode());
        xieChengData.setLocalId(callRecord.getId());
        xieChengData.setOriginId(callRecord.getId());
        xieChengData.setType(messageDTO.getType().toString());
        String actionType = judgeActionType(callRecord);
        xieChengData.setActionType(actionType != null ? actionType.toUpperCase() : ACTIONTYPE_IVR);
        xieChengData.setPushStatus(XcReportPushStatusEnum.WAITED.getValue());
        xieChengData.setStatus(XcReportStatusEnum.SUCCESS.getValue());
        xieChengData.setExtend(callRecord.getUserProperties());
        xieChengData.setIdempotentKey(messageDTO.getIdempotentKey());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setCreateTime(new Date());
        xieChengData.setSha256Tel(callRecord.getCaseNum());
        try {
            xieChengDataMapper.insertSelective(xieChengData);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw duplicateKeyException;
        } catch (Exception e) {
            log.warn("携程上报写入b_xiecheng_data异常！");
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    xieChengDataMapper.insertSelective(xieChengData);
                }
                @Override
                public Object getParams() {
                    return xieChengData;
                }
                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XieChengDataMapper";
                }
                @Override
                public String getMapperMethod() {
                    return "insertSelective";
                }
            },"携程上报b_xiecheng_data写入", config);
        }
        return xieChengData;
    }

    /**
     * 计算 actionType：按挡板/短信/接通状态与配置 realReportLineRate & mockReportLineRate
     * 挡板非短信：使用 Redis 转盘（0~100）与 mock.ivr 阈值（<= 阈值为 ivr，否则 sms）
     */
    private String judgeActionType(CallRecord callRecord) {
        try {
            String apiCode = callRecord.getApiCode();
            boolean isMock = callRecord.getLineName() != null && callRecord.getLineName().contains("挡板");
            JSONObject real = variableAllocationService.getAllocationValue(apiCode,"realReportLineRate");
            JSONObject mock =  variableAllocationService.getAllocationValue(apiCode,"mockReportLineRate");

            if (real == null || mock == null) {
                return ACTIONTYPE_IVR;
            }

            // 非挡板
            if (!isMock) {
                if (real.containsKey("checkAll")) {
                    return real.getString("checkAll");
                }
                if (Objects.equals(callRecord.getCallStatus(),1) ) {
                    return real.getString("answered");
                }
                return real.getString("noAnswered");
            }

            // 挡板短信
            boolean isSms = isSms(callRecord.getCaseNum(), apiCode);
            if (isSms) {
                if (real.containsKey("checkAll")) {
                    return real.getString("checkAll");
                }
                return real.getString("mockButSms");
            }

            // 挡板非短信
            Integer ivrPercent = mock.getInteger(ACTIONTYPE_IVR);
            String random = redisChgService.rpoplpush(RedisKeyConstant.XIECHENG_REPORT_MOCK_RATE_TURNTABLE);
            return Integer.parseInt(random) <= ivrPercent ? ACTIONTYPE_IVR : ACTIONTYPE_SMS;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程上报，查询acitonType异常！"), e);
            return null;
        }
    }

    private boolean isSms(String caseNum, String apiCode) {
        try {
        SmsCallbackAtOnceExample example = new SmsCallbackAtOnceExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andCallBackTypeEqualTo(SmsCallBackTypeEnum.ONHOOK.getValue())
                .andCreateDateEqualTo(LocalDate.now().toString())
                .andCaseNumEqualTo(caseNum);
            return smsCallbackAtOnceMapper.countByExample(example) > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程上报，查询短信回调db异常！"), e);
            return false;
        }
    }

    /**
     * 更新结果
     */
    private void updateResult(XieChengData xieChengData) {
        try {
            xieChengDataMapper.updateByPrimaryKeySelective(xieChengData);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程上报更新b_xiecheng_data异常！"), e);
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    xieChengDataMapper.updateByPrimaryKeySelective(xieChengData);
                }
                @Override
                public Object getParams() {
                    return xieChengData;
                }
                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XieChengDataMapper";
                }
                @Override
                public String getMapperMethod() {
                    return "updateByPrimaryKeySelective";
                }
            },"携程上报b_xiecheng_data更新", config);
        }

    }

}
