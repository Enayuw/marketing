package com.br.marketing.monkeydata.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestAbstract extends IMonkeyDataTestHandle<MarketingSyncUser, MarketingTransferSyncUser, PageCondition>{
    @Override
    public Result<IterationResult<MarketingSyncUser, PageCondition>> getInputData(PageCondition condition) {
        return null;
    }

    @Override
    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingSyncUser> inList) {
        return null;
    }

    @Override
    public Result resultAction(List<MarketingTransferSyncUser> outputDataList) {
        return null;
    }
}
