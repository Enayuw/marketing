package com.br.marketing.service.Impl;

import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.dto.qifu.UpLoadCleanDTO;
import com.br.marketing.entity.Log360ai;
import com.br.marketing.entity.Log360aiExample;
import com.br.marketing.mapper.Log360aiMapper;
import com.br.marketing.service.Impl.qifu.valobj.QiFuCleanStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.strategy.pushinfolist.IPushInfoListStrategy;
import com.br.marketing.service.strategy.pushinfolist.PushInfoListStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PushInfoServiceImpl implements PushInfoService {

    @Resource
    private Log360aiMapper log360aiMapper;

    @Resource
    private PushInfoListStrategyFactory pushInfoListStrategyFactory;

    @Override
    public PageResultReturn getPushInfoList(PushInfoFilterDTO dto) {
        try {
            // 根据任务类型获取对应策略并执行
            IPushInfoListStrategy strategy = pushInfoListStrategyFactory.getStrategy(dto.getTaskType());
            return strategy.execute(dto);
        } catch (Exception e) {
            log.error("查询推送信息列表失败，taskType: {}, apiCode: {}", dto.getTaskType(), dto.getmApiCode(), e);
            throw new RuntimeException("查询推送信息列表失败: " + e.getMessage(), e);
        }
    }

    @Autowired
    MarketingApiService marketingApiService;

    @Override
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result<Boolean> pushUploadByRetry(UploadDataDTO dto, Integer retry) {
        return marketingApiService.pushUpload(dto);
    }

    @Override
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result<Boolean> pushUploadOfCleanRetry(UpLoadCleanDTO dto, Integer retry) {
        Result<Boolean> booleanResult = marketingApiService.pushUpload(dto);
        if(!ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())){
            return booleanResult;
        }
        return booleanResult;
    }

    @Override
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result<Boolean> pushTransferByRetry(PushTransferDataDetailDTO dto, Integer retry) {
        return marketingApiService.pushMarketingApiTransfer(dto,retry);
    }


}
