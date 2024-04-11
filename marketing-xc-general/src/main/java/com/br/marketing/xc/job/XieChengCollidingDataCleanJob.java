package com.br.marketing.xc.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.xc.XieChengCollidingDataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程数据清洗流程job
 * @author guangchao.zhang
 * @Classname XieChengCollidingDataCleanJob
 * @Description 携程数据清洗流程job
 * @Date 2022/2/16 10:02 AM
 *  {
 *       "temporary_table": "临时表名",
 *       "rollback_flag": "操作标识",
 *       "loop_cycle_switch": "是否处理周期数据：true 处理，false:不处理",
 *       "loop_cycle_filter_score": "true 过滤表达式",
 *       "rob_switch": "是否处理非周期数据：true 处理，false:不处理",
 *       "package_rule_info": [
 *           {
 *               "split_filter_score": "拆包分数表达式",
 *               "colliding_time": "撞库时间",
 *               "package_name": "包名称",
 *               "priority": "优先级"
 *           },
 *           {
 *               "split_filter_score": "拆包分数表达式",
 *               "package_name": "包名称",
 *                "colliding_time": "撞库时间",
 *               "priority": "优先级"
 *
 *           }
 *       ]
 *
 *   }
 */
@Component
@Slf4j
public class XieChengCollidingDataCleanJob extends AbstractSimpleElasticJob {


    @Resource
    private XieChengCollidingDataCleanService xieChengCollidingDataCleanService;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        // todo:
        //  1= 根据job 传入的参数 获取到临时表的名称。获取参数中的筛选条件 组成查询sql
        //  2= 将符合条件的数据去重后存储到 对比表中
        //  3= 判断参数是否需要处理周期数据
        //  3.1= 处理周期数据，周期数据中不符合要求的数据删除，is_deleted=1
        //  3.2= 将对比表中周期的数据做删除处理 is_deleted= 1
        //  4= 判断是否处理非周期数据
        //  4.1= 将非周期中的数据做删除处理，将非周期中的package_id 做删除处理
        //  4.2= 将对比表中的数据插入到非周期表中，并同时生成package 信息
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        log.warn("携程清洗开始：{}",System.currentTimeMillis());
        if (StringUtils.isNotBlank(jobParameter)) {
            xieChengCollidingDataCleanService.process(jobParameter);
        }
        log.warn("携程清洗结束:{}",System.currentTimeMillis());
    }
}

