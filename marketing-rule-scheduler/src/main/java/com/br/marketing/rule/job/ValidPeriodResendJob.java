package com.br.marketing.rule.job;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.entity.ValidityPeriodResendRecordExample;
import com.br.marketing.mapper.ValidityPeriodResendRecordMapperBase;
import com.br.marketing.service.Impl.validityperiod.ValidityPeriodResendStrategySelector;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 有效期变更重新任务
 *
 * @author senyang.zheng
 * @date 2023/10/10
 */
@Component
@Slf4j
public class ValidPeriodResendJob extends AbstractSimpleElasticJob {

    @Resource
    private ValidityPeriodResendRecordMapperBase validityPeriodResendRecordMapperBase;
    @Resource
    private ValidityPeriodResendStrategySelector selector;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        this.process(getWaitingRecord());
    }


    /**
     * 获取待推送记录
     *
     * @return {@link List }<{@link ValidityPeriodResendRecord }>
     * @author senyang.zheng
     * @date 2023/10/10
     */
    private List<ValidityPeriodResendRecord> getWaitingRecord() {
        ValidityPeriodResendRecordExample example = new ValidityPeriodResendRecordExample();
        example.createCriteria().andResendStatusEqualTo(0).andIsDeleteEqualTo(0);
        example.setOrderByClause("create_time DESC");
        return validityPeriodResendRecordMapperBase.selectByExample(example);
    }


    /**
     * 执行重推操作
     *
     * @param validityPeriodResendRecords 待执行记录
     * @author senyang.zheng
     * @date 2023/10/10
     */
    private <T> void process(List<ValidityPeriodResendRecord> validityPeriodResendRecords) {
        if (CollectionUtil.isEmpty(validityPeriodResendRecords)) {
            log.info("没有待执行的重推任务");
        }
        for (ValidityPeriodResendRecord record : validityPeriodResendRecords) {
            try {
                long start = System.currentTimeMillis();
                log.warn("ValidPeriodResendJob重推任务 recordId:{} start", record.getId());
                int page = 0;
                int pageSize = 2000;
                if (JSONObject.isValid(record.getResendData())) {
                    JSONObject resendData = JSONObject.parseObject(record.getResendData());
                    if (resendData.getInteger("pageSize") != null) {
                        pageSize = resendData.getInteger("pageSize");
                    }
                }
                for (; ; ) {
                    //获取推送数据
                    log.warn("ValidPeriodResendJob重推任务 recordId:{},page:{},pageSize:{}", record.getId(), page, pageSize);
                    List<T> data = selector.fetchData(record, page, pageSize);
                    if (data.isEmpty()) {
                        break;
                    }
                    //执行推送逻辑
                    selector.resend(data, record);
                    if (data.size() < pageSize) {
                        break;
                    }
                    ++page;
                }
                //修改记录状态为执行完成
                record.setResendStatus(1);
                validityPeriodResendRecordMapperBase.updateByPrimaryKey(record);
                long end = System.currentTimeMillis();
                log.warn("ValidPeriodResendJob重推任务 recordId:{} end，耗时:{}", record.getId(), end - start);
            } catch (Exception e) {
                //捕获异常不影响其他任务
                log.error("重推任务执行失败,record:{}", record, e);
            }
        }
    }
}
