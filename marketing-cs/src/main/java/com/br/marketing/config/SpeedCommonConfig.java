package com.br.marketing.config;

import com.br.speed.client.common.annotations.SpeedFile;
import com.br.speed.client.common.append.ISpeedAppendPipeline;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpeedCommonConfig implements ISpeedAppendPipeline {

    @Override
    public void reloadSpeedFile(String s, String s1, String s2, ApplicationContext applicationContext) throws Exception {
        log.info(s.concat("======").concat(s1).concat(s2));
    }

    @Override
    public void reloadSpeedItem(String s, String s1, String s2, ApplicationContext applicationContext) throws Exception {

    }
}
