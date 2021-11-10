package com.br.marketing.service.Impl;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.service.PushDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Autowired
    LocalFileMapper localFileMapper;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

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

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if(localFile == null){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        while(actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            number+=phoneSales.size();
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setLocalId(id);
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
        StringBuilder content = new StringBuilder();
        content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                .append("数量：".concat(number.toString()).concat("\r\n"))
                .append("文件推送dass结束".concat("\r\n"));
        alarmClient.sendAlarm(content.toString(),"Dass结果文件推送",appName,secretKey,
                Constants.sendCodeMap.get("uploadSuccess"));

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

}
