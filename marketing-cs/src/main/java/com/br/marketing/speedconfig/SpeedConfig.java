package com.br.marketing.speedconfig;

import com.br.speed.client.SpeedMgrBean;
import com.br.speed.client.common.annotations.SpeedFile;
import com.br.speed.client.common.append.ISpeedAppendPipeline;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpeedConfig implements ISpeedAppendPipeline {

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

                break;

        }
        log.info(s.concat("======").concat(s1).concat(s2));
    }

    @Override
    public void reloadSpeedItem(String s, String s1, String s2, ApplicationContext applicationContext) throws Exception {

    }
}
