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
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
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
    XieChengService xieChengService;

    @Override
    public Result pushXieChengData(Long sourceId) {
        try {

            //1.准备context
            XieChengReportContext context = prepareContext(sourceId);
            JSONObject condition = getPushCondition(context.getCallRecord().getApiCode());
            if (condition == null) {
                updateResult(context.getResultData(), 2, "该apiCode未配置规则数据");
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
            context.setPushConfig(XieChengReportContext.PushConfig.fromJson(condition));
            //2.获取Redis锁
            String lockKey = RedisKeyConstant.pushXieChengLock + ":" + context.getPushConfig().getConditionKey() + context.getSha256Tel();
            String lockValue = UUID.randomUUID().toString();
            redisChgService.lock(lockKey, lockValue);
            try {
                //3.执行责任链
                handlerChain.handle(context);
                if (!context.isContinueFlag()) {
                    xieChengDataMapper.updateByPrimaryKeySelective(context.getResultData());
                    redisChgService.unlock(lockKey, lockValue);
                    return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
                }
                //4.推送
                String clickId = System.currentTimeMillis() + RandomUtil.generateCode(5) + context.getSha256Tel();
                context.getAdReqDTO().setClickId(clickId);
                Result result = xieChengService.pushXieChengDataNew(context.getAdReqDTO());
                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    context.getResultData().setPushStatus(2);
                } else {
                    context.getResultData().setPushStatus(3);
                }
                context.getResultData().setClickId(clickId);
                context.getResultData().setDataMessage(result.getMessage());
                xieChengDataMapper.updateByPrimaryKeySelective(context.getResultData());
                redisChgService.unlock(lockKey, lockValue);
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程上报异常，sourceId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
                redisChgService.unlock(lockKey, lockValue);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程上报外层异常，sourceId=" + sourceId + ",errorMessage=" + e.getMessage()), e);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
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
        xieChengData.setType("1");
        xieChengData.setActionType("IVR");
        xieChengData.setPushStatus(1);
        xieChengData.setStatus(1);
        xieChengData.setExtend(callRecord.getUserProperties());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setCreateTime(new Date());
        xieChengData.setSha256Tel(callRecord.getCaseNum());
        xieChengDataMapper.insertSelective(xieChengData);
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
