package com.br.marketing.config;

import com.br.speed.client.common.annotations.SpeedFile;
import org.springframework.stereotype.Component;

@Component
@SpeedFile(filename = "marketingcommon.properties",topic = "marketing")
public class MarketingCommonPropertiesConfig {

    private String pushCustomer;

    public String getPushCustomer() {
        return pushCustomer;
    }

    public void setPushCustomer(String pushCustomer) {
        this.pushCustomer = pushCustomer;
    }
}
