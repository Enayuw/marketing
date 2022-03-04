package com.br.marketing.speedconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.StringUtils;
import com.br.speed.client.SpeedMgrBean;
import com.br.speed.client.common.append.ISpeedAppendPipeline;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        switch (s1){
            case SpeedNameSpace.MARKETINGCOMMON:
                Object marketingCommonConfig = applicationContext.getBean("marketingCommonConfig");
                setValue(marketingCommonConfig,s2);
                break;
            default:
                break;
        }
        if(log.isInfoEnabled()){
            log.info(s.concat("======").concat(s1).concat(s2));
        }
    }

    @Override
    public void reloadSpeedItem(String s, String s1, String s2, ApplicationContext applicationContext) throws Exception {

    }


    <T>void setValue(T config,String path){
        try(FileReader read = new FileReader(path);
            BufferedReader br = new BufferedReader(read);){
            String row;

            while ((row = br.readLine().trim()) != null){
                if(StringUtils.isEmpty(row)){
                    continue;
                }
                if(Pattern.matches(_regexOfannotation,row)){
                    continue;
                }
                List<String> content = Splitter.on("=").splitToList(row);
                String fieldNm = content.get(0);
                String fieldValue = content.get(1);
                Field field = null;
                try {
                    field = config.getClass().getDeclaredField(fieldNm);
                    field.setAccessible(true);
                    assignmentFieldValue(config,field,fieldValue);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    <T>void assignmentFieldValue(T config,Field field,String fieldValue) throws IllegalAccessException {
        if(field.getType().equals(String.class)){
            field.set(config,fieldValue);
        }else if(field.getType().equals(Integer.class)){
            field.set(config,Integer.valueOf(fieldValue));
        }else if(field.getType().equals(Long.class)){
            field.setLong(config,Long.valueOf(fieldValue));
        }else if(field.getType().equals(Double.class)){
            field.setDouble(config,Double.valueOf(fieldValue));
        }else if(field.getType().equals(Float.class)){
            field.setFloat(config,Float.valueOf(fieldValue));
        }else if(field.getType().equals(Boolean.class)){
                field.setBoolean(config,Boolean.valueOf(fieldValue));
        }else {
            Object o = JSON.parseObject(fieldValue, field.getType());
            field.set(config,o);
        }
    }
}
