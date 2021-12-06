package com.br.marketing.service.Impl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.input.HaierReqDTO;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
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
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.TwosevenFileMapper;
import com.br.marketing.service.PushDataService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    HaierDataMapper haierDataMapper;

    @Autowired
    HaierReqMapper haierReqMapper;

    @Autowired
    HaierServiceClient haierServiceClient;

    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

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
        try {
            Result result = this.pushAction(id);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
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
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
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
        AtomicInteger errorMark = new AtomicInteger();
        Integer number = 0;
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        while(actionMark) {
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
            List<TransferDataItemDTO> dataItems = Collections.synchronizedList(new ArrayList<>());
            List<Long> twoFileIds = Collections.synchronizedList(new ArrayList<>());
            List<TwosevenFile> data = twosevenFileMapper.getPushData(id, minId);
            if(data.size()<=0){
                actionMark= false;
                continue;
            }
            minId = data.get(data.size()-1).getId();
            //region 调用撞库接口
            for (TwosevenFile datum : data) {
                threadPool.submit(()->{
                    TwosevenFile updateData = new TwosevenFile();
                    updateData.setId(datum.getId());
                    RequestSevenDTO dto = new RequestSevenDTO();
                    dto.setMobile(datum.getMobile());
                    String extendInfo = datum.getLocalId().toString().concat("-").concat(datum.getId().toString());
                    Result<ResponseSevenZDTO> responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto,extendInfo);
                    if(!ResultCode.SUCCESS.getValue().equals(responseSevenZDTOResult.getCode())){
                        responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto,extendInfo);
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
                        }else{
                            updateData.setTransferOk(responSeven.getRet());
                            updateData.setDataMessage(responSeven.getMsg());
                            twosevenFileMapper.updateByPrimaryKeySelective(updateData);
                        }
                    }else{
                        errorMark.getAndIncrement();
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
            Long miId = twoFileIds.get(0);
            Long maId = twoFileIds.get(twoFileIds.size()-1);
            pushTransferDataDTO.setExtendInfo(localFile.getId().toString()
                    .concat("-").concat(miId.toString())
                    .concat("-").concat(maId.toString()));
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
        if(errorMark.get()>0){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result pushHaierData() {
        Integer day = Integer.valueOf(LocalDate.now().format(yyyyMMddDF));
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        while(mark){
            List<HaierData> haierData = haierDataMapper.selectDataLimitId(day, minId);
            if(haierData.size()<=0){
                mark=Boolean.FALSE;
            }
            minId = haierData.get(haierData.size() - 1).getId() + 1;
            HashMap<String,List<HaierData>> types = new HashMap<>();
            for (HaierData haierDatum : haierData) {
                String key = haierDatum.getType();
                if(types.get(key) ==null){
                    ArrayList<HaierData> haierData1 = new ArrayList<>();
                    types.put(key,haierData1);
                }else {
                    types.get(key)
                            .add(haierDatum);
                }
            }
            for (String s : types.keySet()) {
                String type = s;
                List<HaierData> haierList = types.get(s);
                List<List<HaierData>> partition = Lists.partition(haierList, 500);
                for (List<HaierData> items : partition) {
                    Set<PushDTO.DataItems> datas = new HashSet<>();
                    ArrayList<HaierData> nolist = new ArrayList<>();
                    getDistinctData(items,nolist, type, day.toString());
                    updateHaierFalse(nolist);
                    List<Long> ids = new ArrayList<>();
                    for (HaierData item : items) {
                        datas.add(new PushDTO.DataItems(item.getTaskId(), item.getCustNum()));
                        ids.add(item.getId());
                    }
                    PushDTO.FormData formData = new PushDTO.FormData();
                    formData.setDataItems(datas);
                    formData.setBatchNo(day.toString().concat("_").concat(type));
                    formData.setType(type);
                    formData.setRequestId(getHaierRequestId(type));

                    HaierReqDTO haierReqDTO = new HaierReqDTO();
                    haierReqDTO.setIds(ids);
                    haierReqDTO.setFormData(formData);

                    try {
                        Result<Response2Entity> response2EntityResult = haierServiceClient.pushToTeleSalesWithIds(haierReqDTO, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }



        }
        return null;
    }

    @Override
    public Result queryHaierData() {
        Long minId = null;
        Boolean isAction = Boolean.TRUE;
        while (isAction){
            List<HaierReq> dataWithStatus = haierReqMapper.getDataWithStatus(minId);
            if(dataWithStatus.size()<=0){
                isAction = Boolean.FALSE;
                continue;
            }
            minId = dataWithStatus.get(dataWithStatus.size()-1).getId()+1;

            for (HaierReq reqData : dataWithStatus) {
                
            }
        }
        return null;
    }

    void getDistinctData(List<HaierData> list, List<HaierData> nolist, String type, String day){
        Integer start = Integer.valueOf(LocalDate.parse(day, yyyyMMddDF).minusDays(29L).format(yyyyMMddDF));
        Integer end = Integer.valueOf(day);
        List<String> custNums = list.stream().map(t -> t.getCustNum()).collect(Collectors.toList());
        HaierDataExample example = new HaierDataExample();
        example.createCriteria()
                .andCustNumIn(custNums)
                .andTypeEqualTo(type)
                .andPushStatusEqualTo(2)
                .andCreateDateGreaterThan(start)
                .andCreateDateLessThan(end);
        List<HaierData> repeatData = haierDataMapper.selectByExample(example);
        Set<String> custs = repeatData.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
        for (HaierData haierData : list) {
            if(custs.contains(haierData.getCustNum())){
                nolist.add(haierData);
                list.remove(haierData);
            }
        }

    }

    void updateHaierFalse(List<HaierData> list){
        if(list.size()>0) {
            List<Long> ids = list.stream().map(t -> t.getId()).collect(Collectors.toList());
            HaierDataExample updateExample = new HaierDataExample();
            updateExample.createCriteria().andIdIn(ids);
            HaierData record = new HaierData();
            record.setPushStatus(3);
            haierDataMapper.updateByExampleSelective(record, updateExample);
        }
    }



    String getHaierRequestId(String type){
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String s = RandomUtils.randomStr(4);
        return yyyyMMddHHmmss.concat("_").concat(type).concat(s);
    }

    @Override
    public Result<Response2Entity> pushHaierTransferData(Long id) {
        return null;
    }
}
