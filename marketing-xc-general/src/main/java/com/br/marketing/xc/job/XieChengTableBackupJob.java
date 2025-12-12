package com.br.marketing.xc.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.xc.TableBackupService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 2025-11-26 废弃
  * 表备份job，文档地址：https://c.100credit.cn/pages/viewpage.action?pageId=151477618
  * 携程周期表b_xiecheng_colliding_data_loop_cycle、非周期表b_xiecheng_colliding_data_rob、
  * 撞库结果日志表b_xiecheng_colliding_data_log、对比表b_xiecheng_colliding_data_contrast
  * @Author yu.xia@brgroup.com
  * @Date 2024/3/19 11:23
  */
@Component
@Slf4j
public class XieChengTableBackupJob extends AbstractSimpleElasticJob {

    final static DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Resource
    private TableBackupService tableBackupService;
    /**
     * 依次执行周期表
     * job参数：{"loopCycleSkip":true,"robSkip":true,"logSkip":true,"contrastSkip":true,"currentDate":"2024-03-22"}
     * 详解：
     * 	"loopCycleSkip": true跳过周期表备份删除, false正常执行不跳过
     * 	"robSkip": true跳过非周期表备份删除, false正常执行不跳过
     * 	"logSkip": true跳过日志表备份删除, false正常执行不跳过
     * 	"contrastSkip": true跳过对比表删除, false正常执行不跳过
     * 	"currentDate": 执行什么时间（不包括currentDate）之前的数据备份
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/22 11:24
     * @param context job参数
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String uuid = UUID.randomUUID().toString();
        String jobParameter = context.getJobParameter();
        log.warn("TableBackupJob-start-{}-jobParam:[{}]",uuid,jobParameter);
        Boolean loopCycleSkipFlag = Boolean.FALSE;
        Boolean robSkipFlag = Boolean.FALSE;
        Boolean logSkipFlag = Boolean.FALSE;
        Boolean contrastSkipFlag = Boolean.FALSE;
        LocalDateTime currentTime = LocalDateTime.now();
//        // 获取当前时间
//        String nowString = currentTime.minusDays(30).format(YMD);
        // 获取当前时间前14天
        String daysAgo14 = currentTime.minusDays(14).format(YMD);
        if (StringUtils.isNotBlank(jobParameter)) {
            JSONObject param = JSON.parseObject(jobParameter);
            if(null != param){
                Boolean loopCycle = param.getBoolean("loopCycleSkip");
                if(null != loopCycle){
                    loopCycleSkipFlag = loopCycle;
                }
                Boolean rob = param.getBoolean("robSkip");
                if(null != rob){
                    robSkipFlag = rob;
                }
                Boolean log = param.getBoolean("logSkip");
                if(null != log){
                    logSkipFlag = log;
                }
                Boolean contrast = param.getBoolean("contrastSkip");
                if(null != contrast){
                    contrastSkipFlag = contrast;
                }
                String currentDate = param.getString("currentDate");
                if(StringUtils.isNotBlank(currentDate)){
//                    nowString = currentDate;
                    daysAgo14 = currentDate;
                }
            }
        }
        int limit = 10000;

        //1.周期表
        if(!loopCycleSkipFlag){
            tableBackupService.loopCycleHandle(daysAgo14,limit);
        }
        //2.非周期表
        if(!robSkipFlag){
            tableBackupService.robHandle(daysAgo14,limit);
        }
        //3.撞库结果日志表
        if(!logSkipFlag){
            tableBackupService.logHandle(daysAgo14,limit);
        }
        //4.对比表
        if(!contrastSkipFlag){
            tableBackupService.contrastHandle(daysAgo14,limit);
        }
        log.warn("TableBackupJob-end-{}",uuid);
    }

}
