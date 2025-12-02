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
import com.br.marketing.entity.SmsCallbackAtOnce;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.enums.XcReportPushStatusEnum;
import com.br.marketing.enums.XcReportStatusEnum;
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
public class XieChengSmsReportServiceImpl implements XieChengSmsReportService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengDataMapper xieChengDataMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private XieChengReportHandlerChain handlerChain;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private DatabaseOperationService dbService;

    @Resource
    private XieChengService xieChengService;

    @Resource
    private VariableAllocationService variableAllocationService;

    @Resource
    private SmsCallbackAtOnceMapper smsCallbackAtOnceMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    private static final String ACTIONTYPE_IVR = "IVR";

    @Override
    public Result pushXieChengData(XieChengReportMessageDTO messageDTO) {
        Long sourceId = messageDTO.getSourceId();
        long start = System.currentTimeMillis();
        SmsCallbackAtOnce smsCallbackAtOnce;
        XieChengData xieChengData;
        String lockKey = null;
        String lockValue = null;
        try {
            smsCallbackAtOnce = smsCallbackAtOnceMapper.selectByPrimaryKey(sourceId);
            if (Objects.isNull(smsCallbackAtOnce)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "携程短信上报异常，未查询到短信明细，SmsCallbackAtOnceId=" + sourceId));
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
            xieChengData = keepRecord(smsCallbackAtOnce, messageDTO);
        } catch (DuplicateKeyException exception) {
            String msg = "携程上报异常，消息重复消费入库，SmsCallbackAtOnceId=" + sourceId
                    + "，idempotentKey=" + messageDTO.getIdempotentKey();
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.P_OF_VIP_GROUP.toString());
            dingDingRobotHookService.sendDingDingTextMessage(msg, groupInfo);
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        } catch (Exception exception) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程短信上报异常，通话明细查询或携程短信上报插入异常，消息将退回队列中，SmsCallbackAtOnceId=" + sourceId));
            throw exception;
        }
        try {
            //3.获取tcId
            String tcId = tableCreateService.getIcIdVt(smsCallbackAtOnce.getApiCode());
            //4.创建上下文
            XieChengReportContext context = XieChengReportContext.create(smsCallbackAtOnce, xieChengData, tcId);
            JSONObject condition = marketingCommonConfig.getXieChengCallPushCondition().get(smsCallbackAtOnce.getApiCode());
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
                            "携程短信上报handler处理异常，请查看数据库获取具体报错"));
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
                    "携程短信上报异常，SmsCallbackAtOnceId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
        } finally {
            if (lockKey != null && lockValue != null) {
                try {
                    redisChgService.unlock(lockKey, lockValue);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                            "携程短信上报解锁异常，lockKey=" + lockKey + ",lockValue=" + lockValue), e);
                }
            }
            log.warn("携程短信上报消费消息{}耗时：{}ms", sourceId, (System.currentTimeMillis() - start));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
    }

    private XieChengData keepRecord(SmsCallbackAtOnce smsCallbackAtOnce, XieChengReportMessageDTO messageDTO) {
        XieChengData xieChengData = new XieChengData();
        xieChengData.setApiCode(smsCallbackAtOnce.getApiCode());
        xieChengData.setLocalId(smsCallbackAtOnce.getId());
        xieChengData.setOriginId(smsCallbackAtOnce.getId());
        xieChengData.setType(messageDTO.getType().toString());
        String actionType = judgeActionType(smsCallbackAtOnce);
        xieChengData.setActionType(Objects.nonNull(actionType) ? actionType.toUpperCase() : ACTIONTYPE_IVR);
        xieChengData.setPushStatus(XcReportPushStatusEnum.WAITED.getValue());
        xieChengData.setStatus(XcReportStatusEnum.SUCCESS.getValue());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setCreateTime(new Date());
        xieChengData.setIdempotentKey(messageDTO.getIdempotentKey());
        xieChengData.setExtend(smsCallbackAtOnce.getReserveField1());
        xieChengData.setSha256Tel(smsCallbackAtOnce.getCaseNum());
        try {
            xieChengDataMapper.insertSelective(xieChengData);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw duplicateKeyException;
        } catch (Exception e) {
            log.warn("携程短信上报写入b_xiecheng_data异常！");
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
            },"携程短信上报b_xiecheng_data写入", config);
        }
        return xieChengData;
    }

    /**
     * 纯短信，actionType取值
     * @param smsCallbackAtOnce
     * @return
     */
    private String judgeActionType(SmsCallbackAtOnce smsCallbackAtOnce) {
        JSONObject real = variableAllocationService
                .getAllocationValue(smsCallbackAtOnce.getApiCode(),"realReportLineRate");
        if(Objects.isNull(real)) {
            return null;
        }
        if (real.containsKey("checkAll")) {
            return real.getString("checkAll");
        }
        return real.getString("onlySms");
    }

    /**
     * 更新结果
     */
    private void updateResult(XieChengData xieChengData) {
        try {
            xieChengDataMapper.updateByPrimaryKeySelective(xieChengData);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程短信上报更新b_xiecheng_data异常！"), e);
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
            },"携程短信上报b_xiecheng_data更新", config);
        }

    }

}
