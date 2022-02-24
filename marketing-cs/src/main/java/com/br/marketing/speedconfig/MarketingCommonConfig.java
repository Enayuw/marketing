package com.br.marketing.speedconfig;


import com.br.speed.client.common.annotations.SpeedFile;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpeedFile(filename = "marketingcommon.properties",topic = "marketing")
public class MarketingCommonConfig {
    private String pushCustomer;

    public String getPushCustomer() {
        return pushCustomer;
    }

    public void setPushCustomer(String pushCustomer) {
        this.pushCustomer = pushCustomer;
    }
}
