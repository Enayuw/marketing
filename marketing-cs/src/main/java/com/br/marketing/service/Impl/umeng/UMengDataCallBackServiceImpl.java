package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.UMengCryptoUtil;
import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengInterfaceLog;
import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.mapper.UMengInterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public Result marketingCallback(String encryptParam, HttpServletRequest request) {
        String decryptParam = UMengCryptoUtil.decryptBody(marketingCommonConfig.getUMengBizInfoMap().get("bizSecret"),encryptParam);
        log.warn("uMeng callBack,encryptParam:{},decryptBody:{}",encryptParam, decryptParam);
        Result result = new Result().success();
        UMengInterfaceLog interfaceLog = new UMengInterfaceLog();
        try {
            JSONObject requestData = JSONObject.parseObject(decryptParam);
            Long localId = 0L;
            String taskId = requestData.getString("task_id");
            if (StringUtils.isNotEmpty(taskId)) {
                UMengTimingTask timingTask = timingTaskService.getDataByTaskId(taskId);
                localId = timingTask.getLocalId();
            }
            interfaceLog = buildInferfaceLog(localId,request,encryptParam,requestData);
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            result = result.failure();
        }
        interfaceLog.setResult(JSONObject.toJSONString(result));
        umengInterfaceLogMapper.insertSelective(interfaceLog);
        log.warn("友盟智能时机回调代运营数据上传接口被调用");
        return result;
    }

    @Override
    public Result callPolicyData(Long localId, String apiCode, String strategyCode, List<UMengData> uMengDataList) {
        Result result = new Result().success();
        int randomNumber = 10000 + secureRandom.nextInt(90000);
        List<PushMarketingUserDetailDTO>  list = convertPushUserList(strategyCode,uMengDataList);
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

    private List<PushMarketingUserDetailDTO> convertPushUserList(String strategyCode,List<UMengData> uMengDataList) {
        List<PushMarketingUserDetailDTO> resultList = new ArrayList<>();
        uMengDataList.stream().forEach(umengData -> {
            PushMarketingUserDetailDTO pushMarketingUserDetailDTO = new PushMarketingUserDetailDTO();
            pushMarketingUserDetailDTO.setCaseNumber(umengData.getCusNum());
            pushMarketingUserDetailDTO.setPhone(umengData.getCell());
            JSONObject variablesInfo = getVariablesJsonObject(umengData);
            pushMarketingUserDetailDTO.setVariables(variablesInfo);
            pushMarketingUserDetailDTO.setStrategyCode(strategyCode);
            resultList.add(pushMarketingUserDetailDTO);
        });
        return resultList;
    }

    private static JSONObject getVariablesJsonObject(UMengData umengData) {
        JSONObject variablesInfo = new JSONObject();
        variablesInfo.put("caseNumber", umengData.getCusNum());
        variablesInfo.put("phone", umengData.getCell());
        variablesInfo.put("usertype", umengData.getUsertype());
        variablesInfo.put("id", umengData.getIdCard());
        variablesInfo.put("name", umengData.getName());
        variablesInfo.put("pd_cell_type", umengData.getPdCellType());
        variablesInfo.put("pd_cell_province", umengData.getPdCellProvince());
        return variablesInfo;
    }

    private UMengInterfaceLog buildInferfaceLog(Long localId, HttpServletRequest request,String encryptParam, JSONObject requestParam) {
        String headerBizId = request.getHeader("bizid");
        String header = "bizid:" + headerBizId;
        UMengInterfaceLog uMengInterfaceLog = new UMengInterfaceLog();
        uMengInterfaceLog.setLocalId(localId);
        uMengInterfaceLog.setRequestType(3);
        try {
            uMengInterfaceLog.setRequestId(requestParam.getString("data_id"));
            uMengInterfaceLog.setEventType(requestParam.getString("event_type"));
            uMengInterfaceLog.setPhoneSha256(requestParam.getString("phone_sha256"));
        }catch (Exception e){
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE+"参数字段不存在"), e);
        }
        uMengInterfaceLog.setRequestParam(requestParam.toJSONString());
        uMengInterfaceLog.setEncryptParam(encryptParam);
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
