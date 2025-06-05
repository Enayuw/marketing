package com.br.marketing.monkey.job.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.service.Impl.umeng.IUMengApiService;
import com.br.marketing.service.Impl.umeng.IUMengTimingTaskService;
import com.br.marketing.service.LocalFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description 友盟智能时机任务创建 job
 * @Author zhiyong.zhang
 * @CreateTime 2025/05/23
 */
@Component
@Slf4j
public class UMengTimingTaskCreateJob extends AbstractSimpleElasticJob {
    private final static String TITLE = "【uMeng-智能时机任务创建job】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Resource
    private IUMengTimingTaskService timingTaskService;

    @Resource
    private IUMengApiService uMengApiService;

    @Resource
    private LocalFileService localFileService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            List<String> uMengApiCodes = marketingCommonConfig.getApiCodeOfUMeng();
            uMengApiCodes.forEach(this::atciton);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * api_code具体执行创建智能时机任务
     * @param apiCode
     */
    private void atciton(String apiCode) {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDateTime dayStartTime = LocalDate.now(zone).atStartOfDay();
        List<LocalFile> localFileList = localFileService.getLastDataByApiCode(apiCode,dayStartTime);
        localFileList.forEach(localFile -> dealSingleAction(localFile,apiCode,dayStartTime));
    }

    private void dealSingleAction(LocalFile localFile, String apiCode,LocalDateTime dayStartTime) {
        log.warn("TITLE:{},localId:{},apiCode:{} 开始创建智能时机",TITLE,localFile.getId(),apiCode);
        //2、查询当天是否已经存在智能时机任务
        UMengTimingTask existTimingTask = timingTaskService.getTodayLastTask(localFile.getId(),apiCode,dayStartTime);
        if (existTimingTask != null) {
            log.warn("TITLE:{}, localId:{},apiCode={} 今日智能时机任务已存在,taskId:{},taskName:{}",TITLE,existTimingTask.getLocalId(),apiCode,
                    existTimingTask.getId(),existTimingTask.getTaskName());
            return;
        }
        //3、调用友盟时机任务创建接口
        UMengTimingTask uMengTimingTask = new UMengTimingTask();
        uMengTimingTask.setLocalId(localFile.getId());
        uMengTimingTask.setApiCode(apiCode);
        uMengTimingTask.setTaskName(localFile.getFileName());
        Result  createResult = uMengApiService.createTimingTask(localFile.getId(),apiCode,buildRequestParam(apiCode, localFile.getFileName()),true);
        if (createResult != null  && createResult.isSuccess()) {
            JSONObject resultObj = (JSONObject) createResult.getData();
            if (resultObj.getBoolean("status")) {
                uMengTimingTask.setStatus(1);
                uMengTimingTask.setUmengTaskId(resultObj.getString("data"));
                uMengTimingTask.setExtend(resultObj.toJSONString());
            }else {
                uMengTimingTask.setStatus(2);
                uMengTimingTask.setExtend(resultObj.toJSONString());
            }
        }else{
            uMengTimingTask.setStatus(2);
        }
        Date nowTime = new Date();
        uMengTimingTask.setCreateTime(nowTime);
        uMengTimingTask.setUpdateTime(nowTime);
        timingTaskService.insertSelective(uMengTimingTask);
    }

    /**
     * 构建uMeng智能时机任务创建参数
     * eg:
     * {
     *     "task_name": "xxx",
     *     "event_type": "1001",
     *     "touch_type": "1",
     *     "callback_url": "xxx",
     *     "callback_params": {
     *         "param1": "xxx"
     *     },
     *     "status": 0,
     *     "callback_period": "8-14,15-20",
     *     "start_time": "2023-06-29 11:30:12",
     *     "end_time": "2023-06-30 11:30:12",
     *     "aspect_data_scenes_ids": "a0dz1681375302jhp4ig2zsn,a0dz1681375302jhp4qqpjlv"
     * }
     * @param apiCode
     * @param taskName
     * @return
     */
    private String buildRequestParam(String apiCode,String taskName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDate.now().format(formatter);
        JSONObject requestParams = new JSONObject();
        JSONObject timingTaskParam =  marketingCommonConfig.getUMengTaskParamMap().get(apiCode);
        requestParams.put("task_name", taskName);
        requestParams.put("event_type", timingTaskParam.get("event_type"));
        requestParams.put("touch_type", timingTaskParam.get("touch_type"));
        requestParams.put("callback_url", timingTaskParam.get("callback_url"));
        requestParams.put("status", timingTaskParam.get("status"));
        requestParams.put("callback_period", timingTaskParam.get("callback_period"));
        requestParams.put("start_time", timingTaskParam.getString("start_time").replace("yyyy-MM-dd",date));
        requestParams.put("end_time", timingTaskParam.getString("end_time").replace("yyyy-MM-dd",date));
        return requestParams.toJSONString();
    }

}
