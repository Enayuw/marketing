package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.service.DataService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.DataTest;
import com.br.marketing.mapper.DataTestMapper;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.PushDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mbeans.ServiceMBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.serviceloader.ServiceFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Resource
    DataService dataServiceImpl;

    @Autowired
    IApiToDbService iApiToDbService;
    @Resource
    DataTestMapper dataTestMapper;
    
    @Autowired
    PushDataService pushDataService;
    
    @GetMapping("dataEliminate")
    public String dataEliminate(){
        boolean result=dataServiceImpl.dataEliminate();
        if(!result){
            return "fail";
        }
        return "success";
    }

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
