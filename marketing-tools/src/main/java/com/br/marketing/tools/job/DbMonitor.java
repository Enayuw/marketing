package com.br.marketing.tools.job;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.ApiCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class DbMonitor {

    static String bearer;

    @Autowired
    RestTemplate restTemplate;

//    @Scheduled(cron = "0 0/20 * * * ?")
//    public void slowDbSql(){
//        if(StringUtils.isBlank(bearer)){
//            new ApiCaller(restTemplate).
//        }
//    }
}
