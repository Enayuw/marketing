package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.FileCheckService;
import com.br.marketing.check.service.Impl.EncryptFileServiceImpl;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by Bairong on 2020/1/15.
 */
@RestController
@RequestMapping("/fileCheck/")
@Slf4j
public class FileCheckContrroller {

    @Resource
    FileCheckService fileCheckServiceImpl;



    @Resource
    private EncryptFileServiceImpl encryptFileServiceImpl;

    @GetMapping("encryptFile")
    public String encryptFile(String apiCode,String fileName,String path){
        log.info("encryptFile ----{}----{}--------{}",apiCode,fileName,path);
        boolean b= encryptFileServiceImpl.encryptFile(apiCode,fileName,path);
        if(b){
            return "success";
        }else{
            return "fail";
        }
    }

    @GetMapping("encrypt")
    public String encrypt(String file,String password,String path){
        log.info("encryptFile ----{}----{}--------{}",file,password,path);
        boolean b= encryptFileServiceImpl.encrypt(file,password,path);
        if(b){
            return "success";
        }else{
            return "fail";
        }
    }


    @GetMapping("checkDataFile")
    public String checkDataFile (String path,String filename){
        log.info("dataFileCheck   path--{}。filename--{}",path,filename);

        boolean b = fileCheckServiceImpl.checkDataFile(path, filename);
        if(b){
            return "success";
        }else{
            return "fail";
        }
    }


    @GetMapping("strategyIdCheck")
    public String strategyIdCheck (String apiCode,String strategyId){
        log.info("strategyIdCheck   api_code--{}。strategyId--{}",apiCode,strategyId);
        boolean b = fileCheckServiceImpl.strategyIdCheck(apiCode, strategyId);
        log.info("result--{}",b);
        if(b){
            return "success";
        }else{
            return "fail";
        }
    }

    @GetMapping("/getConfig")
    public String getConfig(String apiCode,String key) {

        MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(merchantParam);
        String value = jsonObject.getString(key);
        if("call_method".equals(key)){
            if("1".equals(value)){
                value="3";
            }else if("3".equals(value)){
                value="1";
            }
        }
        log.info("getConfig  api_code--{},key--{},value--{}",apiCode,key,value);
        return value;
    }
    @GetMapping("/checkApicode")
    public String checkApicode(String apiCode) {
        log.info("checkApicode api_code--{}",apiCode);
        String result="success";
        MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        if (merchantParam==null){
            result="fail";
        }
        return result;
    }


}
