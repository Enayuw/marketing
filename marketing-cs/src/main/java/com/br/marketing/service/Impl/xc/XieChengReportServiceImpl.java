package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.chain.xiecheng.XieChengReportHandlerChain;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class XieChengReportServiceImpl implements XieChengReportService{

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
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public Result pushXieChengData(Long sourceId) {
        return null;
//        XieChengData xieChengData;
//        CallRecord callRecord;
//        try {
//            //1.查询【b_call_record】
//            callRecord = callRecordMapper.selectByPrimaryKey(sourceId);
//            if (callRecord == null) {
//                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
//                        , "携程上报异常，未查询到通话明细，callRecoordId=" + sourceId));
//                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//            }
//            //2.插入【b_xiecheng_data】
//            xieChengData = keepRecord(callRecord);
//        } catch (DuplicateKeyException e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
//                    , "携程上报异常，消息重复消费入库，callRecoordId=" + sourceId));
//            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//        } catch (Exception e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
//                    , "携程上报异常，消息将退回队列中，callRecoordId=" + sourceId));
//            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
//        }
//        // 3. 获取tcId
//        String tcId = tableCreateService.getTcId(callRecord.getApiCode());
//        // 4. 创建上下文
//        XieChengReportContext context = XieChengReportContext.create(callRecord, xieChengData, tcId);
//        JSONObject condition = getPushCondition(callRecord.getApiCode());
//        if (condition == null) {
//            updateResult(context.getResultData(), 2, "该apiCode未配置规则数据");
//            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//        }
//        context.setPushConfig(XieChengReportContext.PushConfig.fromJson(condition));
//        //5.获取Redis锁
//        String lockKey = RedisKeyConstant.pushXieChengLock + ":" + context.getPushConfig().getConditionKey() + context.getSha256Tel();
//        String lockValue = UUID.randomUUID().toString();
//        redisChgService.lock(lockKey, lockValue);
//            try {
//                //3.执行责任链
//                handlerChain.handle(context);
//                if (context.isDelay()) {
//                    mqFact.setIsDelay(1);
//                    //rocket
//                    rocketMqSwitch.syncSend(
//                            MarketingXieChengConstants.TOPIC,
//                            MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT,
//                            JSON.toJSONString(mqFact));
//                    return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//
//                }
//                if (!context.isPush()) {
//                    xieChengDataMapper.updateByPrimaryKeySelective(context.getResultData());
//                    redisChgService.unlock(lockKey, lockValue);
//                    return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//                }
//                //4.推送
//                String clickId = System.currentTimeMillis() + RandomUtil.generateCode(5) + context.getSha256Tel();
//                context.getAdReqDTO().setClickId(clickId);
//                Result result = xieChengService.pushXieChengDataNew(context.getAdReqDTO());
//                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
//                    context.getResultData().setPushStatus(2);
//                } else {
//                    context.getResultData().setPushStatus(3);
//                }
//                context.getResultData().setClickId(clickId);
//                context.getResultData().setDataMessage(result.getMessage());
//                xieChengDataMapper.updateByPrimaryKeySelective(context.getResultData());
//                redisChgService.unlock(lockKey, lockValue);
//                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
//            } catch (Exception e) {
//                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
//                        "携程上报异常，sourceId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
//                redisChgService.unlock(lockKey, lockValue);
//            }
//        } catch (Exception e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
//                    "携程上报外层异常，sourceId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
//        }
//        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    /**
     * 准备数据及context
     * @param sourceId
     * @return
     */
    private XieChengReportContext prepareContext(Long sourceId) {
            // 1. 获取通话记录
            CallRecord callRecord = callRecordMapper.selectByPrimaryKey(sourceId);
            // 2. 保存携程数据
            XieChengData xieChengData = keepRecord(callRecord);
            // 3. 获取tcId
            String tcId = tableCreateService.getTcId(callRecord.getApiCode());
            // 4. 创建上下文
            return XieChengReportContext.create(callRecord, xieChengData, tcId);
    }

    private XieChengData keepRecord(CallRecord callRecord) {
        XieChengData xieChengData = new XieChengData();
        xieChengData.setApiCode(callRecord.getApiCode());
        xieChengData.setLocalId(callRecord.getId());
        xieChengData.setCallRecordId(callRecord.getId());
        xieChengData.setType("1");
        xieChengData.setActionType("IVR");
        xieChengData.setPushStatus(1);
        xieChengData.setStatus(1);
        xieChengData.setExtend(callRecord.getUserProperties());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setCreateTime(new Date());
        xieChengData.setSha256Tel(callRecord.getCaseNum());
        try {
            xieChengDataMapper.insertSelective(xieChengData);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程上报写入b_xiecheng_data异常！"), e);
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
     * 更新结果
     */
    private void updateResult(XieChengData xieChengData, int status, String message) {
        xieChengData.setStatus(status);
        xieChengData.setDataMessage(message);
        xieChengDataMapper.updateByPrimaryKeySelective(xieChengData);
    }

    /**
     * 获取推送配置
     */
    private JSONObject getPushCondition(String apiCode) {
        HashMap<String, JSONObject> condition = marketingCommonConfig.getXieChengCallPushCondition();
        if (condition == null) {
            condition = new HashMap<>();
            condition.put("3710058", getJo("1", Arrays.asList("3710058","3710078"), "3710058"));
            condition.put("3710078", getJo("1", Arrays.asList("3710058","3710078"), "3710058"));
            condition.put("3710090", getJo("2", Arrays.asList("3710090","3710091"), "3710090"));
            condition.put("3710091", getJo("2", Arrays.asList("3710090","3710091"), "3710090"));
        }
        return condition.get(apiCode);
    }

    private JSONObject getJo(String condition, List<String> soleCellApiCodes, String mainApiCode){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition",condition);
        jsonObject.put("isBlackApiCodes",soleCellApiCodes);
        jsonObject.put("convTypeApiCodes",soleCellApiCodes);
        jsonObject.put("soleCellApiCodes",soleCellApiCodes);
        jsonObject.put("mainApiCode",mainApiCode);
        return jsonObject;
    }
}
