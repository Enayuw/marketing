package com.br.marketing.check.job;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.entity.ValidityPeriodResendRecordExample;
import com.br.marketing.enums.ValidityPeriodResendEnum;
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
        for (ValidityPeriodResendRecord validityPeriodResendRecord : validityPeriodResendRecords) {
            ValidityPeriodResendEnum resendType = ValidityPeriodResendEnum.getEnumByCode(validityPeriodResendRecord.getResendType());
            //获取推送数据
            List<T> data = selector.fetchData(validityPeriodResendRecord, resendType);
            //执行推送逻辑
            selector.resend(data, resendType);
            //修改记录状态为执行完成
            validityPeriodResendRecord.setResendStatus(1);
            validityPeriodResendRecordMapperBase.updateByPrimaryKey(validityPeriodResendRecord);
        }
    }
}
