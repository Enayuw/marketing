package com.br.marketing.speedconfig;


import org.springframework.stereotype.Component;

@Component
public class MarketingCommonPropertiesConfig {

    private String pushCustomer;

    public String getPushCustomer() {
        return pushCustomer;
    }

    public void setPushCustomer(String pushCustomer) {
        this.pushCustomer = pushCustomer;
    }
}
