package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.UMengCryptoUtil;
import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengInterfaceLog;
import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.mapper.UMengInterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UMengDataCallBackServiceImpl implements IUMengDataCallbackService {

    private final static String TITLE = "【uMeng-智能时机回调】";

    private static final SecureRandom secureRandom = new SecureRandom();


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private IUMengDataService userDataService;

    @Resource
    private IUMengTimingTaskService timingTaskService;

    @Resource
    private UMengInterfaceLogMapper umengInterfaceLogMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;



    @Override
    public Result marketingCallback(String requestBody, HttpServletRequest request) {
        log.warn("uMeng callBack,decryptBody:{}", requestBody);
        Result result = new Result().success();
        UMengInterfaceLog interfaceLog = new UMengInterfaceLog();
        try {
            JSONObject requestData = JSONObject.parseObject(requestBody);
            String taskId = requestData.getString("task_id");
            String eventType =  requestData.get("event_type").toString();
            String cellSha256 = requestData.getString("phone_sha256");
            UMengTimingTask timingTask = timingTaskService.getDataByTaskId(taskId);
            interfaceLog = buildInferfaceLog(timingTask.getLocalId(),request,eventType,requestData.toJSONString());
            if (eventType.equals("22") || eventType.equals("1001")) {
                String strategyCode = marketingCommonConfig.getUMengPushPolicyStrategyCode().get(timingTask.getApiCode());
                List<UMengData> uMengDataList = userDataService.selectDeviceByCell(timingTask.getLocalId(),cellSha256);
                Result pushResult = this.callPolicyData(timingTask.getLocalId(),timingTask.getApiCode(),strategyCode,uMengDataList);
                log.warn("uMeng callPolicyData,localId:{},cellSha256:{},strategyCode:{},result:{}",timingTask.getLocalId(),cellSha256,
                        strategyCode,JSONObject.toJSONString(pushResult));
                if (pushResult != null && pushResult.isSuccess()) {
                    List<Long> idList = uMengDataList.stream().map(UMengData::getId).collect(Collectors.toList());
                    userDataService.updatePushStausByIds(idList,2);
                }
            }
            umengInterfaceLogMapper.insertSelective(interfaceLog);
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            result = result.failure();
        }
        return result;
    }

    @Override
    public Result callPolicyData(Long localId, String apiCode, String strategyCode, List<UMengData> uMengDataList) {
        Result result = new Result().success();
        int randomNumber = 10000 + secureRandom.nextInt(90000);
        List<PushMarketingUserDetailDTO>  list = convertPushUserList(uMengDataList);
        if(!list.isEmpty()){
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
            taskInfoDTO.setData(list);
            taskInfoDTO.setAccessNumber(yyyyMMdd+"_"+apiCode +"_a"+randomNumber);
            taskInfoDTO.setMethod("caseAdd");
            taskInfoDTO.setBatchNumber(yyyyMMdd+"_"+ apiCode+"_a");
            taskInfoDTO.setBatchName(yyyyMMdd+"_"+ apiCode+"_a");
            taskInfoDTO.setStrategyCode(strategyCode);
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(apiCode);
            pushMarketingUserDTO.setJsonData(taskInfoDTO);
            PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
            retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
            result = methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
        }
        return result;
    }

    private List<PushMarketingUserDetailDTO> convertPushUserList(List<UMengData> uMengDataList) {
        List<PushMarketingUserDetailDTO> resultList = new ArrayList<>();
        uMengDataList.stream().forEach(umengData -> {
            PushMarketingUserDetailDTO pushMarketingUserDetailDTO = new PushMarketingUserDetailDTO();
            pushMarketingUserDetailDTO.setCaseNumber(umengData.getCusNum());
            //TODO cell加解密是否正确
            pushMarketingUserDetailDTO.setPhone(umengData.getCell());
//            pushMarketingUserDetailDTO.setPhone(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(umengData.getCell()).getBytes()));
            resultList.add(pushMarketingUserDetailDTO);
        });
        return resultList;
    }

    private UMengInterfaceLog buildInferfaceLog(Long localId, HttpServletRequest request, String eventType, String requestParam) {
        String headerBizId = request.getHeader("bizid");
        String header = "bizid:" + headerBizId;
        UMengInterfaceLog uMengInterfaceLog = new UMengInterfaceLog();
        uMengInterfaceLog.setLocalId(localId);
        uMengInterfaceLog.setRequestType(3);
        uMengInterfaceLog.setRequestId("");
        uMengInterfaceLog.setEventType(eventType);
        uMengInterfaceLog.setRequestParam(requestParam);
        uMengInterfaceLog.setUrl(request.getRequestURL().toString());
        uMengInterfaceLog.setHeader(header);
        uMengInterfaceLog.setHttpCode(200);
        uMengInterfaceLog.setCallTime(1);
        Date now = new Date();
        uMengInterfaceLog.setCreateTime(now);
        uMengInterfaceLog.setUpdateTime(now);
        uMengInterfaceLog.setExpire("0");
        return uMengInterfaceLog;
    }




}
