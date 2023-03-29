package com.br.marketing.check.job.clean;

import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhen.Li1
 * @Classname XieChengToPolicyDataClean
 * @Description 携程b_data_distribute_detail_log表中cell清洗
 * @Date 2023/03/29
 */
@Component
@Slf4j
public class XieChengToPolicyDataClean extends AbstractSimpleElasticJob {


    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode;
        String jobParameter = context.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCode = context.getJobParameter();
        } else {
            apiCode = "3710078";
        }
        DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
        DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria().andApiCodeEqualTo(apiCode).andDistributeTypeEqualTo(DistributeTypeEnum.POLICYDATA.getValue());
        List<DataDistributeDetailLog> dataDistributeDetailLogs = dataDistributeDetailLogMapper.selectByExample(logExample);
        log.warn("携程数据清洗开始，apiCode={},number={}", apiCode, dataDistributeDetailLogs.size());
        dataDistributeDetailLogs.forEach(dataDistributeDetailLog -> {
                    String cell = dataDistributeDetailLog.getCell();
                    String decodeCell = RpcClientProxy.decode(cell, "cell", "sha", "");
                    if (StringUtils.isEmpty(decodeCell)) {
                        log.warn("cell={}解密失败", cell);
                        return;
                    }
                    dataDistributeDetailLog.setCell(BrCipherMaker.getInstance().encode(decodeCell));
                    dataDistributeDetailLogMapper.updateByPrimaryKey(dataDistributeDetailLog);
                    log.warn("携程数据更新成功");

                }
        );
        log.warn("携程数据清洗完成");

    }
}
