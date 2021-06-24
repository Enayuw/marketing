package com.br.marketing.task.config;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.task.Scheduler;
import com.br.speed.client.common.append.ISpeedAppendPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Created by Bairong on 2020/4/23.
 */
@Service
@Slf4j
public class ShutDownCallback implements ISpeedAppendPipeline {

    @Override
    public void reloadSpeedFile(String event, String key, String filePath, ApplicationContext context) throws Exception {

    }

    @Override
    public void reloadSpeedItem(String event, String key, String value, ApplicationContext context) throws Exception {
        if(StringUtils.isEmpty(key)||StringUtils.isEmpty(value)){
            return;
        }
        switch(key) {
            case "marketing-shutdown":{
                String status = JSONObject.parseObject(value, AgentItem.class).getStatus();
                if("off".equals(status)){
                    log.info("Scheduler close.....");
//                    System.exit(0);
                }
                break;
            }
            default:{
                log.info("append item reload speed file info "+ key + " : " + value);
            }
        }
    }

}
