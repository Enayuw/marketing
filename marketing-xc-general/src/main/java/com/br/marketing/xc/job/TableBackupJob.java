package com.br.marketing.xc.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
  * 表备份job，文档地址：https://c.100credit.cn/pages/viewpage.action?pageId=151477618
  * 携程周期表b_xiecheng_colliding_data_loop_cycle、非周期表b_xiecheng_colliding_data_rob、
  * 撞库结果日志表b_xiecheng_colliding_data_log、对比表b_xiecheng_colliding_data_contrast
  * @Author yu.xia@brgroup.com
  * @Date 2024/3/19 11:23
  */
@Component
@Slf4j
public class TableBackupJob extends AbstractSimpleElasticJob {

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String uuid = UUID.randomUUID().toString();
        log.warn("TableBackupJob-start-{}",uuid);
        String jobParameter = context.getJobParameter();
        Boolean loopCycleSkipFlag = Boolean.FALSE;
        Boolean robSkipFlag = Boolean.FALSE;
        Boolean logSkipFlag = Boolean.FALSE;
        Boolean contrastSkipFlag = Boolean.FALSE;
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
            }
        }
        //1.周期表 b_xiecheng_colliding_data_loop_cycle 备份代码
        if(loopCycleSkipFlag){

        }
        //2.非周期表b_xiecheng_colliding_data_rob 备份代码
        if(robSkipFlag){

        }
        //3.撞库结果日志表b_xiecheng_colliding_data_log 备份代码
        if(logSkipFlag){

        }
        //4.对比表b_xiecheng_colliding_data_contrast 数据删除代码
        if(contrastSkipFlag){

        }
        log.warn("TableBackupJob-end-{}",uuid);
    }

}
