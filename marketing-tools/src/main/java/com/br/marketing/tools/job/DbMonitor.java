package com.br.marketing.tools.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Service
@EnableScheduling
public class DbMonitor {

    static String bearer;

    static LocalDateTime loginExpire;

    @Autowired
    RestTemplate restTemplate;

    final static String baseUrl="http://tidb-monitor-zw-t1.100credit.cn/";

    @Scheduled(cron = "0 0/20 * * * ?")
    public void slowDbSql(){
        if(StringUtils.isBlank(bearer)||(loginExpire!=null&&loginExpire.isBefore(LocalDateTime.now()))){
            String loginUrl = baseUrl.concat("dashboard/api/user/login");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username","u_pd_dashboard");
            jsonObject.put("password","Tidb@bairong123");
            jsonObject.put("type",0);
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(loginUrl)
                    .setRequestParam(jsonObject).setContentType(MediaType.APPLICATION_JSON_UTF8).postTransferStr();
            if(transfer.getHttpCode()==200&&StringUtils.isNotBlank(transfer.getResult())){
                JSONObject res = JSON.parseObject(transfer.getResult());
                bearer = res.getString("token");
                loginExpire = LocalDateTime.parse(res.getString("expire").substring(0,19).replace("T"," "), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }

        String slowUrl = baseUrl.concat("dashboard/api/slow_query/list");
//                .concat(String.format("?begin_time=%d&db=marketing&desc=true&digest=&end_time=%d"
//                        ,LocalDateTime.now().minusMinutes(30L).toEpochSecond(ZoneOffset.of("+8"))
//                        ,LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))))
//                .concat("&fields=query%2Ctimestamp%2Cquery_time%2Cmemory_max&limit=100&orderBy=timestamp&&text=");
        HashMap head = new HashMap();
        head.put("Authorization","Bearer ".concat(bearer));
        head.put("User-Agent","PostmanRuntime/7.28.4");
        head.put("Accept","*/*");

        head.put("Accept-Encoding","gzip, deflate, br");
        head.put("Cookie","experimentation_subject_id=Ijc4OWE1YTY2LTE3NTQtNDhiZC1iZDhlLTYxYWUzODA1YjM3NiI%3D--bc6751921c887bb67e26f7cfc47e1430a383d6b2; pgv_pvi=1204189184; _ga=GA1.2.753597270.1625485321");
//        String s = new ApiCaller(restTemplate).setUrl(slowUrl).setHttpHeaders(head).get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + bearer);
        MultiValueMap<String, String> obj = new LinkedMultiValueMap<String, String>();
        obj.add("begin_time","1657883690");
        obj.add("desc","true");
        obj.add("digest","");
        obj.add("end_time","1657885490");
        obj.add("fields","query,timestamp,query_time,memory_max");
        obj.add("limit","100");
        obj.add("orderBy","timestamp");
        obj.add("text","");
        HttpEntity <String> entity = new HttpEntity(obj, headers);
        ResponseEntity<String> s = restTemplate.exchange(slowUrl, HttpMethod.GET, entity, String.class);
        JSONArray objects = JSONArray.parseArray(s.getBody());
    }
}
