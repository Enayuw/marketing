package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DataTest;
import com.br.marketing.mapper.DataTestMapper;
import com.br.marketing.service.IApiToDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Autowired
    IApiToDbService iApiToDbService;
    @Resource
    DataTestMapper dataTestMapper;

    @GetMapping("testApiToDb")
    public String testApiToDb(){
        iApiToDbService.pushToDb("7410437",null);
        return "success";
    }
    @PostMapping("pushTest")
    public String pushTest(@RequestBody String data){
        JSONObject result =new JSONObject();
        Long time=System.currentTimeMillis();
        if(time%5==0){
            result.put("code","99");
            result.put("message","FALSE");
            return result.toJSONString();
        }else {

            String groupType="";
            String taskId="";
            Integer sum=0;
            if(StringUtils.isNotBlank(data)){
                JSONObject dataJSON=JSONObject.parseObject(data);
                JSONArray dataItems=dataJSON.getJSONArray("dataItems");
                if(dataItems !=null &&dataItems.size()>0){
                    sum=dataItems.size();
                    JSONObject item=dataItems.getJSONObject(0);
                    groupType=item.getString("groupType");
                    taskId=item.getString("taskId");
                }
            }
            DataTest dataTest = new DataTest();
            dataTest.setCreateTime(new Date());
            dataTest.setTaskId(taskId);
            dataTest.setSum(sum);
            dataTest.setGroupType(groupType);
            dataTest.setDataStr(data);
            dataTestMapper.insertSelective(dataTest);
            result.put("code","00");
            result.put("message","SUCCESS");
            return result.toJSONString();
        }
    }

}
