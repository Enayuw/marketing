package com.br.marketing.service.rulecenter.impl.push;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Service
@Slf4j
public class DataLabelPushStrategy extends AbstractRuleCenterPushStrategy {


    @Override
    protected void updatePushStatus(RuleCenterPushContext context, Result<Boolean> result) {

    }

    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return null;
    }
}
