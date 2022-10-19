package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.job.CallingToSendJob;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.RepQueryBlackPhoneVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.RetryTestService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Autowired
    IApiToDbService iApiToDbService;
    
    @Autowired
    PushDataService pushDataService;

    @Autowired
    private DassServiceClient dassServiceClient;

    @Autowired
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private CallingToSendJob callingToSendJob;

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @GetMapping("testApiToDb")
    public String testApiToDb(@RequestParam("apiCode") String apiCode){
        iApiToDbService.pushToDb(apiCode);
        return "success";
    }

    @GetMapping("pushDassTest")
    public String pushDassTest(@RequestParam("localId") Long localId){
        Result result = pushDataService.pushDassData(localId);
        return "success";
    }

    @GetMapping("pushSevenTest")
    public String pushSevenTest(@RequestParam("localId") Long localId){
        Result result = pushDataService.pushSevenTransferData(localId);
        return "success";
    }

    @GetMapping("retryMethod")
    public String retryMethod(@RequestParam("serviceName")String serviceName,@RequestParam("methodName")String methodName
            ,@RequestParam("params")String params,@RequestParam("paramTypeStr")String paramTypeStr){
        Class<?> paramType = null;
        try {
            paramType = Class.forName(paramTypeStr);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object o = JSON.parseObject(params, paramType);
        if(!CkeckApplication.ac.containsBean(serviceName)){
            throw new RuntimeException("找不到对应的bean");
        }
        Object bean = CkeckApplication.ac.getBean(serviceName);
        List<Object> pa = new ArrayList<>();
        Method method = null;
        try {
            method = bean.getClass().getMethod(methodName, paramType);
        } catch (NoSuchMethodException e) {
            log.error(String.format("在bean %s 中不存在该方法 %s",serviceName,methodName));
            e.printStackTrace();
        }
        try {
            method.invoke(bean, o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return "success";
    }

    @Autowired
    RetryTestService retryTestService;

    @GetMapping("retryAop")
    public String retryAop(@RequestParam("isRetry") Integer isRetry){
        retryTestService.ret(isRetry,null);
        return "success";
    }

    @GetMapping("pushDassRealTimeSingleTest")
    public String pushDassRealTimeSingleTest(){
        DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        dassSingleImportDataDTO.setOrgname("shuheshoujie");
        dassSingleImportDataDTO.setName("张");
        dassSingleImportDataDTO.setPhone("AQYBBAcBUgΒ2lWV1Y");
        dassSingleImportDataDTO.setUid("2617811");
        dassSingleImportDataDTO.setUserType("1");
        dassSingleImportDataDTO.setPrioritySymbol("1");
        dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        Result result = dassServiceClient.postRealTimeUserData(dassSingleImportAdapDTO);
        log.warn("调用人工实时推送用户返回 -- {}", JSON.toJSONString(result));
        return "success";
    }

    @GetMapping("getRobotBlackPhoneTest")
    public String getRobotBlackPhoneTest() {
        ReqBlackPhoneQueryDTO reqBlackPhoneQueryDTO = new ReqBlackPhoneQueryDTO();
        BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
        blackQueryDetailDTO.setDataId("223412376");
        blackQueryDetailDTO.setApiCode("7410785");
        blackQueryDetailDTO.setPhone("CQ1WBAVUAVΒ4cCDQg");
        blackQueryDetailDTO.setEncryptType(PhoneEncryptTypeEnum.LOG_TYPE.getEncryptType());
        List blackQueryDetailDTOList = Lists.newArrayList(blackQueryDetailDTO);
        BlackPhoneDTO<BlackQueryDetailDTO> jsondata = new BlackPhoneDTO<>();
        jsondata.setMethod("queryBlackDataV2");
        jsondata.setData(blackQueryDetailDTOList);
        ReqBlackPhoneDTO reqBlackPhoneDTO = new ReqBlackPhoneDTO();
        reqBlackPhoneDTO.setApiCode("7410785");
        reqBlackPhoneDTO.setJsonData(JSON.toJSONString(jsondata));
//        reqBlackPhoneQueryDTO.setReqBlackPhoneDTO(reqBlackPhoneDTO);
//        reqBlackPhoneQueryDTO.setBlackQueryDetailDTOList(blackQueryDetailDTOList);
//        RepQueryBlackPhoneVO repQueryBlackPhoneVO = robotaiApiServiceClient.queryBlackPhone(reqBlackPhoneQueryDTO);
//        log.warn(JSON.toJSONString(repQueryBlackPhoneVO));
        return "success";


    }

    //@GetMapping("haluoCallbackEnd")
    //public String haluoCallbackEnd(String apiCode,String taskId) {
    //    log.warn("haluoCallbackEnd apiCode:{},taskId:{}",apiCode,taskId);
    //    Map<String, Object> cusMap = new HashMap<>(16);
    //    cusMap.put("apiCode", apiCode);
    //    cusMap.put("taskId", taskId);
    //    int haloCallingCount = customerCallingDialogMapper.getHaloCallingCount(cusMap);
    //    callingToSendJob.callbackEnd(haloCallingCount,apiCode,taskId);
    //    return "success";
    //

    //}


}
