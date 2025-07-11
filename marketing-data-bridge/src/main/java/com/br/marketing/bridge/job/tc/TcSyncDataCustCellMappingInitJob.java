package com.br.marketing.bridge.job.tc;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class TcSyncDataCustCellMappingInitJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingTcyrCustCellMappingMapper custCellMappingMapper;

    // 线程池配置
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private static final int PAGE_SIZE = 2000;
    private static final int BATCH_SIZE = 500;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        dealProcess(marketingCommonConfig.getTcyrApiCode());
    }

    private void dealProcess(String apiCode) {
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool"),
                marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool"));
        Long searchId = 0L;
        while (true) {
            Integer pageSize = marketingCommonConfig.getTcQuickDealShardConfig().getInteger("pageSize");
            List<MarketingSyncCustCell> custCellList = marketingSyncUserMapper.selectSyncCustCellList(apiCode,searchId,pageSize);
            if (CollectionUtils.isEmpty(custCellList)) {
                break;
            }
            actionPool.execute(() -> dealBatchCustCell(custCellList));
            searchId = custCellList.get(custCellList.size()-1).getId();
        }
    }

    private void dealBatchCustCell(List<MarketingSyncCustCell> batchList) {
        List<List<MarketingSyncCustCell>> partLists = ListUtils.partition(batchList, marketingCommonConfig.getTcDbDealShardConfig().getInteger("dbPartSize"));
        for (List<MarketingSyncCustCell> partList : partLists) {
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("INSERT INTO b_marketing_tcyr_cust_cell_mapping (cust_num,cell) VALUES ");
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < partList.size(); i++) {
                MarketingSyncCustCell custCellItem = partList.get(i);
                insertSql.append("(?,?)");
                if (i < partList.size() - 1) insertSql.append(",");
                params.add(custCellItem.getCustNum());
                params.add(custCellItem.getCellMd5());
            }
            insertSql.append(" ON DUPLICATE KEY UPDATE cell=VALUES(cell)");
            custCellMappingMapper.batchSaveCustCell(insertSql);
        }

    }
}
