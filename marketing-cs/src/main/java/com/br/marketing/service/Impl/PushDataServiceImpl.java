package com.br.marketing.service.Impl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDTO;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.twosevenservice.TwoSevenService;
import com.br.marketing.client.twosevenservice.intput.RequestSevenDTO;
import com.br.marketing.client.twosevenservice.output.ResponseSevenZDTO;
import com.br.marketing.client.twosevenservice.output.SevenDetailVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.TwosevenFileMapper;
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
    TwosevenFileMapper twosevenFileMapper;
    
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

    @Autowired
    TwoSevenService twoSevenService;

    @Autowired
    MarketingApiService marketingApiService;

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

    @Override
    public Result pushSevenTransferData(Long id) {
        Boolean isContiue = false;
        Result result = this.pushAction(id);
        if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
            RetryMainLog retryMainLog = new RetryMainLog();
            retryMainLog.setRetryType(1);
            retryMainLog.setRetryParam(JSON.toJSONString(id));
            retryMainLog.setRetryParamType(id.getClass().getName());
            retryMainLog.setRetryService("pushDataServiceImpl");
            retryMainLog.setRetryMethod("pushAction");
            retryMainLog.setRetryNum(0);
            retryMainLog.setRetryMaxNum(3);
            retryMainLog.setRetryStatus(1);
            retryMainLog.setCreateTime(new Date());
            retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
            retryMainLogMapper.insertSelective(retryMainLog);
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    public Result pushAction(Long id){
        Boolean actionMark = true;
        Long minId = null;
        String key = "seven:push:transfer:threadnum";
        Integer threadNum = 5;
        if(redisChgService.exists(key)&& StringUtils.isNotBlank(redisChgService.get(key))){
            threadNum = Integer.valueOf(redisChgService.get(key));
        }

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if(localFile == null){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        List<TwosevenFile> errorData = new ArrayList<>();
        Integer number = 0;
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        while(actionMark) {
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
            List<TransferDataItemDTO> dataItems = new ArrayList<>();
            List<Long> twoFileIds = new ArrayList<>();
            List<TwosevenFile> data = twosevenFileMapper.getPushData(id, minId);
            if(data.size()<=0){
                actionMark= false;
            }
            //region 调用撞库接口
            for (TwosevenFile datum : data) {
                threadPool.submit(()->{
                    TwosevenFile updateData = new TwosevenFile();
                    updateData.setId(datum.getId());
                    RequestSevenDTO dto = new RequestSevenDTO();
                    dto.setMobile(datum.getMobile());
                    Result<ResponseSevenZDTO> responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto);
                    if(!ResultCode.SUCCESS.getValue().equals(responseSevenZDTOResult.getCode())){
                        responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto);
                    }
                    if(ResultCode.SUCCESS.getValue().equals(responseSevenZDTOResult.getCode())) {
                        ResponseSevenZDTO responSeven = responseSevenZDTOResult.getData();
                        if ("200".equals(responSeven.getRet())) {
                            SevenDetailVO sevenDetailVO = responSeven.getVolist().get(0);
                            if ("1".equals(sevenDetailVO.getStatus())) {
                                TransferDataItemDTO dataItemDTO = new TransferDataItemDTO();
                                dataItemDTO.setApiCode(datum.getApiCode());
                                dataItemDTO.setCustNum(datum.getCustNum());
                                dataItemDTO.setUserType(datum.getUserType());
                                dataItemDTO.setIfTransform("1");
                                updateData.setTransferOk("1");
                                twoFileIds.add(datum.getId());
                                dataItems.add(dataItemDTO);
                            }else{
                                updateData.setTransferOk("0");
                            }
                            twosevenFileMapper.updateByPrimaryKeySelective(updateData);
                        }else if("1002".equals(responSeven.getRet())){
                            updateData.setTransferOk("1002");
                            updateData.setDataMessage("无号码数据");
                            twosevenFileMapper.updateByPrimaryKeySelective(updateData);
                        }
                    }else{
                        if(errorData.size()<=0) {
                            errorData.add(datum);
                        }
                    }
                });
            }
            threadPool.shutdown();
            while (true){
                if(threadPool.isTerminated()){
                    break;
                }
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                }
            }
            //endregion

            //region 推送转化接口
            if(dataItems.size()==0){
                continue;
            }
            TransferDataDTO transferDataDTO = new TransferDataDTO();
            transferDataDTO.setDataItems(dataItems);
            transferDataDTO.setRequestId(localFile.getApiCode().concat("_")
                    .concat(yyyyMMddHHmmss).concat("_")
                    .concat(number.toString()));
            PushTransferDataDTO pushTransferDataDTO = new PushTransferDataDTO();
            pushTransferDataDTO.setTwoFileIds(twoFileIds);
            PushTransferDataDetailDTO detailDTO = new PushTransferDataDetailDTO();
            pushTransferDataDTO.setDto(detailDTO);
            detailDTO.setApiCode(localFile.getApiCode());
            detailDTO.setJsonData(JSON.toJSONString(transferDataDTO));
            Result<Boolean> booleanResult = marketingApiService.pushTransfer(pushTransferDataDTO);
            /** 调用转化接口失败需要重试 */
            if(ResultCode.FAIL.getValue().equals(booleanResult.getCode())&&booleanResult.getData()){
                RetryMainLog retryMainLog = new RetryMainLog();
                retryMainLog.setRetryType(1);
                retryMainLog.setRetryParam(JSON.toJSONString(pushTransferDataDTO));
                retryMainLog.setRetryParamType(pushTransferDataDTO.getClass().getName());
                retryMainLog.setRetryService("marketingApiService");
                retryMainLog.setRetryMethod("pushTransfer");
                retryMainLog.setRetryNum(0);
                retryMainLog.setRetryMaxNum(3);
                retryMainLog.setRetryStatus(1);
                retryMainLog.setCreateTime(new Date());
                retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                retryMainLogMapper.insertSelective(retryMainLog);
            }
            //endregion
            number++;
        }
        /** 调用撞库接口有网络失败的 需要重试 */
        if(errorData.size()>0){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
