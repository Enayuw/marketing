package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncCustCell;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTcyrCustCellMappingMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class TcSyncDataCustCellMappingInitJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-custNum-cell初始化任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingTcyrCustCellMappingMapper custCellMappingMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        dealProcess(marketingCommonConfig.getTcyrApiCode());
    }

    private void dealProcess(String apiCode) {
        log.warn(TITLE+"调度开始");
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTcCustCellMappingConfig().getInteger("threadPool"),
                marketingCommonConfig.getTcCustCellMappingConfig().getInteger("threadPool"));
        Long searchId = 0L;
        try {
            while (true) {
                Integer pageSize = marketingCommonConfig.getTcCustCellMappingConfig().getInteger("pageSize");
                List<MarketingSyncCustCell> custCellList = marketingSyncUserMapper.selectSyncCustCellList(apiCode,searchId,pageSize);
                if (CollectionUtils.isEmpty(custCellList)) {
                    break;
                }
                actionPool.execute(() -> dealBatchCustCell(custCellList));
                searchId = custCellList.get(custCellList.size()-1).getId();
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
        log.warn(TITLE+"调度结束");
    }

    private void dealBatchCustCell(List<MarketingSyncCustCell> batchList) {
        List<List<MarketingSyncCustCell>> partLists = ListUtils.partition(batchList, marketingCommonConfig.getTcCustCellMappingConfig().getInteger("dbPartSize"));
        for (List<MarketingSyncCustCell> partList : partLists) {
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("INSERT INTO b_marketing_tcyr_cust_cell_mapping (cust_num,cell) VALUES ");
            for (int i = 0; i < partList.size(); i++) {
                MarketingSyncCustCell custCellItem = partList.get(i);
                insertSql.append("('").append(custCellItem.getCustNum()).append("','").append(custCellItem.getCellMd5()).append("')");
                if (i < partList.size() - 1) insertSql.append(",");
            }
            insertSql.append(" ON DUPLICATE KEY UPDATE cell=VALUES(cell)");
            custCellMappingMapper.batchSaveCustCell(insertSql);
        }
    }
}
