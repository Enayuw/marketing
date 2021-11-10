package com.br.marketing.service.Impl;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.service.PushDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class PushDataServiceImpl implements PushDataService{

    @Autowired
    PhoneSaleMapper phoneSaleMapper;
    
    @Autowired
    DassServiceClient dassServiceClient;

    @Autowired
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public Result pushDassData(Long id) {
        Boolean isContiue = false;
        Boolean actionMark = true;
        Long minId = null;
        String key = "dass:push:threadnum";
        Integer threadNum = 5;
        if(redisChgService.exists(key)&& StringUtils.isNotBlank(redisChgService.get(key))){
            threadNum = Integer.valueOf(redisChgService.get(key));
        }
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        while(actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setList(phoneSales);
                minId = phoneSale.getId();
                threadPool.submit(()->{
                    Result result = dassServiceClient.postHermesUserData(dto);
                    if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                        RetryMainLog mainLog = new RetryMainLog();
                        mainLog.setRetryType(1);
                        mainLog.setRetryParam(JSON.toJSONString(dto));
                        mainLog.setRetryParamType(dto.getClass().getName());
                        mainLog.setRetryService("dassServiceClient");
                        mainLog.setRetryMethod("postHermesUserData");
                        mainLog.setRetryNum(0);
                        mainLog.setRetryMaxNum(3);
                        mainLog.setRetryStatus(1);
                        mainLog.setCreateTime(new Date());
                        mainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                        retryMainLogMapper.insertSelective(mainLog);
                    }
                });
            }else{
                actionMark=false;
            }
        }
        threadPool.shutdown();
        while (true){
            if(threadPool.isTerminated()){
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

}
