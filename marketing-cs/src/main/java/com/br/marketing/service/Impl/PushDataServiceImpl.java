package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
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
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushDataService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PushDataServiceImpl implements PushDataService{

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

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
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secret2Key;
    @Value("${otherConfig.alarm.appName:00}")
    private String app2Name;

    @Autowired
    TwoSevenService twoSevenService;

    @Autowired
    MarketingApiService marketingApiService;

    @Autowired
    PhoneSaleExtendShuheMapper phoneSaleExtendShuheMapper;

    @Autowired
    RabbitMqProducter producter;

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
        if(SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())){
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("文件推送dass结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(),"Dass结果文件推送",appName,secretKey,
                    Constants.sendCodeMap.get("uploadSuccess"));
        }
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

    /**
     * 数禾推送电销
     * @param pushShDXDTO
     * @return
     * 传输数据样例
     *         LocalFile localFile = new LocalFile();
     *         PhoneSale phoneSale = new PhoneSale();
     *         PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
     *         PushShDXDTO pushShDXDTO = new PushShDXDTO()
     *                 .setLocalFile(localFile)
     *                 .setPhoneSale(phoneSale)
     *                 .setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
     *         localFile.setCid("");
     *         localFile.setApiCode("");
     *         localFile.setFileName("数禾-转化/客服+数据id");
     *         phoneSale.setUid("custNum");
     *         phoneSale.setPhone("手机号明文");
     *         phoneSale.setName("");
     *         phoneSale.setOrgname("shuheshenwan");
     *         phoneSale.setSource("16");
     *         phoneSale.setUserType("2");
     *         phoneSale.setLoginTime("");
     *         phoneSale.setExtend("{\"clc_usr_iso_pho_tim\":\"\",\"clc_usr_iso_idt_tim\":\"\",\"clc_usr_iso_crd_tim\":\"\",\"clc_usr_iso_inf_tim\":\"\"}");
     *         phoneSaleExtendShuhe.setCustNum("custNum");
     *         phoneSaleExtendShuhe.setAppletDate("当前日期yyyy-MM-dd");
     *         phoneSaleExtendShuhe.setAppletTime("当前时间yyyy-MM-dd HH:mm:ss");
     *         phoneSaleExtendShuhe.setStatus("a/b");
     */
    @Override
    public Result<Boolean> pushShDX(PushShDXDTO pushShDXDTO) {

        Date date = new Date();
        LocalFile localFile = pushShDXDTO.getLocalFile();
        localFile.setFileType(SftpFileTypeEnum.SHBYTRANSFORM.getValue());
        localFile.setCreateTime(date);
        String apiCode = localFile.getApiCode();
        String phone ="";
        PhoneSale phoneSale = pushShDXDTO.getPhoneSale();
        phone = phoneSale.getPhone();
        phoneSale.setCreateTime(date);
        String s = AESUtil.aesEncrypty(phoneSale.getPhone(), aesKey);
        phoneSale.setPhone(s);
        phoneSale.setPhoneAes(BrCipherMaker.getInstance().encode(phone));
        phoneSale.setApiCode(apiCode);
        phoneSale.setApiCid(localFile.getCid());
        JSONObject jo = new JSONObject();
        jo.put("face_recognitiion","0");
        jo.put("is_usr_idt","0");
        jo.put("is_bindcard","0");
        jo.put("is_usr_inf","0");
        if(StringUtils.isNotBlank(phoneSale.getExtend())){
            JSONObject jsonObject = JSON.parseObject(phoneSale.getExtend());
            String pho = jsonObject.getString("clc_usr_iso_pho_tim");
            String idt = jsonObject.getString("clc_usr_iso_idt_tim");
            String crd = jsonObject.getString("clc_usr_iso_crd_tim");
            String inf = jsonObject.getString("clc_usr_iso_inf_tim");
            if(StringUtils.isNotBlank(pho)){
                jo.put("face_recognitiion","1");
            }
            if(StringUtils.isNotBlank(idt)){
                jo.put("is_usr_idt","1");
            }
            if(StringUtils.isNotBlank(crd)){
                jo.put("is_bindcard","1");
            }
            if(StringUtils.isNotBlank(inf)){
                jo.put("is_usr_inf","1");
            }
        }
        phoneSale.setExtend(JSON.toJSONString(jo));
        PhoneSaleExtendShuhe phoneSaleExtendShuhe = pushShDXDTO.getPhoneSaleExtendShuhe();
        phoneSaleExtendShuhe.setCreateTime(date);

        PhoneSaleExtendShuheExample shuheExample = new PhoneSaleExtendShuheExample();
        shuheExample.createCriteria().andCustNumEqualTo(phoneSaleExtendShuhe.getCustNum()).andAppletDateEqualTo(phoneSaleExtendShuhe.getAppletDate());
        List<PhoneSaleExtendShuhe> phoneSaleExtendShuhes = phoneSaleExtendShuheMapper.selectByExample(shuheExample);
        if(phoneSaleExtendShuhes.size()>0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        Result result = addShuHeLock(apiCode, phoneSaleExtendShuhe.getCustNum(), phoneSaleExtendShuhe.getStatus());
        if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        localFileMapper.insertSelective(localFile);
        phoneSale.setLocalId(localFile.getId().toString());
        phoneSaleMapper.insertSelective(phoneSale);
        phoneSaleExtendShuhe.setLocalId(localFile.getId());
        phoneSaleExtendShuhe.setpId(phoneSale.getId());
        phoneSaleExtendShuheMapper.insertSelective(phoneSaleExtendShuhe);
        producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE,localFile.getId().toString());

        removeHaluoLock(apiCode, phoneSaleExtendShuhe.getCustNum(), phoneSaleExtendShuhe.getStatus());
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    private Result addShuHeLock(String apiCode,String custNum,String status){
        String key = RedisKeyConstant.shuhePushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(custNum);
        Long setnx = redisChgService.setnx(key, status, 3);
        //已经被其他数据抢占锁了
        if(setnx.equals(0L)){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    private void removeHaluoLock(String apiCode,String custNum,String status){
        String key = RedisKeyConstant.shuhePushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(custNum);
        String s = redisChgService.get(key);
        if(status.equals(s)){
            redisChgService.del(key);
        }
    }
}
