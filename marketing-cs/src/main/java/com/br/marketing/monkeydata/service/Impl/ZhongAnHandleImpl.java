package com.br.marketing.monkeydata.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.Marketing;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import com.br.marketing.monkeydata.service.IMonkeyDataHandle;
import com.github.pagehelper.Page;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class ZhongAnHandleImpl implements IMonkeyDataHandle<MarketingSyncUser, MarketingTransferSyncUser,PageCondition> {

    @Override
    public Boolean isThread() {
        return true;
    }

    @Override
    public Integer getThread() {
        return IMonkeyDataHandle.super.getThread();
    }

    @Override
    public Result<IterationResult<MarketingSyncUser,PageCondition>> getInputData(PageCondition condition) {
        if(condition.getPageIndex().equals(10)){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        List<MarketingSyncUser> marketingSyncUsers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MarketingSyncUser syncUser = new MarketingSyncUser();
            syncUser.setId(Long.valueOf(i));
            syncUser.setCustNum(""+condition.getPageIndex()+i);
            marketingSyncUsers.add(syncUser);
        }
        condition.setPageIndex(condition.getPageIndex()+1);
        IterationResult<MarketingSyncUser, PageCondition> content = new IterationResult<>();
        content.setInputDataList(marketingSyncUsers);
        content.setInDatacondition(condition);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
    }

    @Override
    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingSyncUser> inList) {
        List<MarketingTransferSyncUser> out = new ArrayList<>();
        inList.forEach(t->{
            MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
            transferSyncUser.setId(t.getId());
            transferSyncUser.setCustNum(t.getCustNum());
            out.add(transferSyncUser);
        });
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(out);
    }

    @Override
    public Result resultAction(List<MarketingTransferSyncUser> outputDataList) {
        System.out.println(JSON.toJSONString(outputDataList));
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result action(PageCondition condition) {
        return IMonkeyDataHandle.super.action(condition);
    }
}
