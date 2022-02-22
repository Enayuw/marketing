package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.PushDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Autowired
    IApiToDbService iApiToDbService;
    
    @Autowired
    PushDataService pushDataService;
    
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

}
