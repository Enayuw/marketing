package com.br.marketing.monkeydata.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransfer;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.monkeydata.entity.InputData;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.OutputData;
import com.br.marketing.monkeydata.service.IMonkeyDataHandle;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ZhongAnHandleImpl implements IMonkeyDataHandle {

    @Override
    public Boolean isThread() {
        return IMonkeyDataHandle.super.isThread();
    }

    @Override
    public Integer getThread() {
        return IMonkeyDataHandle.super.getThread();
    }

    @Override
    public Result<IterationResult> getInputData(InputDataCondition condition) {
        return null;
    }

    @Override
    public Result<List> processData(List inList) {
        return null;
    }

    @Override
    public Result resultAction(List outputDataList) {
        return null;
    }

    @Override
    public Result action(InputDataCondition condition) {
        return IMonkeyDataHandle.super.action(condition);
    }
}
