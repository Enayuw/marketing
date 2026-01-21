package com.br.marketing.speedconfig;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.llm.CybotstarAgentApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.service.Impl.RedisTestServiceImpl;
import com.br.marketing.service.strategy.callrecording.CallRecordingHandlerService;
import com.br.speed.client.SpeedMgrBean;
import com.br.speed.client.common.append.ISpeedAppendPipeline;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpeedConfig implements ISpeedAppendPipeline {

    final static String _regexOfannotation = "^#.*$";

    @Bean(name = "speedMgrBean", destroyMethod = "destroy")
    public SpeedMgrBean speedMgrBean() {
        SpeedMgrBean speedMgrBeanConfig = new SpeedMgrBean();
        speedMgrBeanConfig.setScanPackage("com.br");
        return speedMgrBeanConfig;
    }

    @Override
    public void reloadSpeedFile(String s, String s1, String s2, ApplicationContext applicationContext) throws Exception {
        switch (s1) {
            case SpeedNameSpace.MARKETINGCOMMON:
                Object marketingCommonConfig = applicationContext.getBean("marketingCommonConfig");
                setValue(marketingCommonConfig, s2);
                break;
            default:
                break;
        }
        if (log.isInfoEnabled()) {
            log.info(s.concat("======").concat(s1).concat(s2));
        }
    }

    @Override
    public void reloadSpeedItem(String event, String key, String value, ApplicationContext context) throws Exception {
        log.warn("配置中心item -- {} --{} 变动通知",key,value);
        AgentItem item = JSON.parseObject(value, AgentItem.class);
        String message = item.getMessage();
        String callRecordConfig = item.getCallRecordConfig();
        String cybotstarAgentConfig = item.getCybotstarAgentConfig();
        Integer redisTest = item.getRedisTest();
        Integer speedTest = item.getSpeedTest();
        switch (key) {
            case "marketing_broadcast_notice_item": {
                // {"message":"customer_rule_mapping","update_time":"2022-04-01 14:53:01"}
                if ("customer_rule_mapping".equals(message)) {
                    DataLoadingHandlerService.invalidateAll();
                }
                if ("call_record_config".equals(callRecordConfig)) {
                    CallRecordingHandlerService.invalidateAll();
                }
                if ("cybotstar_agent_config".equals(cybotstarAgentConfig)) {
                    CybotstarAgentApiClient.invalidateAll();
                }
                if(!new Integer(0).equals(redisTest)){
                    RedisTestServiceImpl redisTestServiceImpl = context.getBean("redisTestServiceImpl", RedisTestServiceImpl.class);
                    RedisChgService redisChgService = context.getBean("redisChgService",RedisChgService.class);
                    if(redisTestServiceImpl !=null){
                        redisTestServiceImpl.redisTest(redisTest,redisChgService);
                    }
                }
                break;
            }
            default: {
                log.warn("append item reload speed file info {}:{}", key, value);
            }
        }

    }

    @Override
    public void onError(String s, String s1, byte[] bytes, Long aLong, ApplicationContext applicationContext, Exception e) throws Exception {
        log.error(String.format("speed报错：%s",e.getMessage()),e);
    }

    <T> void setValue(T config, String path) {
        try (FileReader read = new FileReader(path);
             BufferedReader br = new BufferedReader(read);) {
            String row;

            while ((row = br.readLine()) != null) {
                row = row.trim();
                if (StringUtils.isEmpty(row)) {
                    continue;
                }
                if (Pattern.matches(_regexOfannotation, row)) {
                    continue;
                }
                List<String> content = Splitter.on("=").splitToList(row);
                String fieldNm = content.get(0);
                String fieldValue = content.get(1);
                Field field = null;
                try {
                    field = config.getClass().getDeclaredField(fieldNm);
                    field.setAccessible(true);
                    assignmentFieldValue(config, field, fieldValue);
                } catch (NoSuchFieldException e) {
                    String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_SPEEDCOMMONCONFIG.getCode(), "该服务Speed配置不存在字段:".concat(fieldNm), "marketingCommonConfig提示");
                    log.warn(msg);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(),e);
                } catch (Exception ex){
                    log.error(ex.getMessage(),ex);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    <T> void assignmentFieldValue(T config, Field field, String fieldValue) throws IllegalAccessException {
        if (field.getType().equals(String.class)) {
            field.set(config, fieldValue);
        } else if (field.getType().equals(Integer.class)) {
            field.set(config, Integer.valueOf(fieldValue));
        } else if (field.getType().equals(Long.class)) {
            field.set(config, Long.valueOf(fieldValue));
        } else if (field.getType().equals(Double.class)) {
            field.set(config, Double.valueOf(fieldValue));
        } else if (field.getType().equals(Float.class)) {
            field.set(config, Float.valueOf(fieldValue));
        } else if (field.getType().equals(Boolean.class)) {
            field.set(config, Boolean.valueOf(fieldValue));
        }  else {
            Object o = JSON.parseObject(fieldValue, field.getType());
            field.set(config, o);
        }
    }
}
