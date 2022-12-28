package com.br.marketing.monkeydata.handle.commonhandle;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhen.li1
 * @desc 公共 入参数据处理器
 */
@Service
public class InputCommonHandle {

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    public Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> getMarketingSyncUserByPage(MarketingSyncCondition inputData) {
        Long minId = null;
        List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByAppletDateRange(inputData.getApiCode(), inputData.getAppletDateStart(), inputData.getAppletDateEnd(), inputData.getMinId());
        if (marketingSyncUserList.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        minId = marketingSyncUserList.get(marketingSyncUserList.size() - 1).getId() + 1;
        IterationResult<MarketingSyncUser, MarketingSyncCondition> content = new IterationResult<>();
        inputData.setMinId(minId);
        content.setInDatacondition(inputData);
        content.setInputDataList(marketingSyncUserList);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
    }


}
