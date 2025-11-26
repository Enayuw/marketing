package com.br.marketing.xc.job;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 2025-11-26废弃
 * 描述：： 携程上报数据补推
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName XieChengSmsCollidingDataVtToSendJob
 * @author: it-yml
 * @create: 2025-06-09 11:31
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class XieChengPushDataBackToSendJob extends AbstractSimpleElasticJob {
    /**
     * 推送实现
     */
    @Resource
    private XieChengService xieChengService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengDataMapper xieChengDataMapper;




    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String sqlParameter = context.getJobParameter();
        Long minId = 0L;
        if(sqlParameter.trim() != null && sqlParameter.length() > 0) {
            ThreadPoolExecutor threadPool =
                    BrExecutors.getThreadPool(50,50);
            while (true){
                List<AdReqDTO>  adReqDTOList =  xieChengDataMapper.executeBackSql(sqlParameter,minId);
                if(CollectionUtils.isEmpty(adReqDTOList)){
                    break;
                }
                minId = adReqDTOList.get(adReqDTOList.size()-1).getId();
                for(AdReqDTO adReqDTO : adReqDTOList){
                    threadPool.submit(()->{
                        pushXieChengData(adReqDTO);
                    });
                }
            }
            // 关闭线程池
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("携程上报补推");
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                        , "携程上报补推：日志保存线程池结束异常！"), ex);
                Thread.currentThread().interrupt();
            }
        }

    }

    private void pushXieChengData(AdReqDTO adReqDTO) {
        try {
            XieChengData resultData = new XieChengData();
            resultData.setId(adReqDTO.getId());
            String clickId = System.currentTimeMillis() + getCode(5) + adReqDTO.getSha256Tel();
            adReqDTO.setClickId(clickId);
            resultData.setClickId(clickId);
            //region 获取配置信息
            String apiCode = adReqDTO.getApiCode();
            HashMap<String, JSONObject> xieChengCallPushCondition = marketingCommonConfig.getXieChengCallPushCondition();
            JSONObject condition = xieChengCallPushCondition.get(apiCode);

            String conditionKey = condition.getString("condition");
            adReqDTO.setConditionKey(conditionKey);
            try {
                // 携程推送新接口
                Result result = xieChengService.pushXieChengDataNew(adReqDTO);
                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    resultData.setPushStatus(2);
                } else {
                    resultData.setPushStatus(3);
                }
                resultData.setDataMessage(result.getMessage());
                //endregion
                xieChengDataMapper.updateByPrimaryKeySelective(resultData);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程补推上报异常，id=" + adReqDTO.getId() + "，localId=" + adReqDTO.getLocalId() + "errorMessage=" + e.getMessage()), e);
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程补推上报异常,errorMessage=" + e.getMessage()), e);
        }
    }

    private JSONObject getJo(String condition, List<String> soleCellApiCodes, String mainApiCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition", condition);
        jsonObject.put("isBlackApiCodes", soleCellApiCodes);
        jsonObject.put("convTypeApiCodes", soleCellApiCodes);
        jsonObject.put("soleCellApiCodes", soleCellApiCodes);
        jsonObject.put("mainApiCode", mainApiCode);
        return jsonObject;
    }
    /**
     * 随机生成由数字、字母组成的N位验证码
     *
     * @return 返回一个字符串
     */
    public static String getCode(int n) {
        char arr[] = new char[n];
        int i = 0;
        while (i < n) {
            char ch = (char) (int) (Math.random() * 124);
            if (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[i++] = ch;
            }
        }
        //将数组转为字符串
        return new String(arr);
    }
}
