package com.br.marketing.check.controller;

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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.Date;

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
        iApiToDbService.pushToDb(apiCode,null);
        return "success";
    }

    @GetMapping("pushDassTest")
    public String pushDassTest(@RequestParam("localId") Long localId){
        Result result = pushDataService.pushDassData(localId);
        return "success";
    }

    @GetMapping("retryMethod")
    public String retryMethod(@RequestParam("serviceName")String serviceName,@RequestParam("methodName")String methodName,@RequestParam("params")String params){
        String[] split = params.split("\\|");
        ServiceMBean methodBean = (ServiceMBean) CkeckApplication.ac.getBean(serviceName);
        try {
            methodBean.invoke(methodName, split, null);
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

}
