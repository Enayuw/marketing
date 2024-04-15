package com.br.marketing.tools.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.net.ApiCaller;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.tools.dto.TidbSqlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class DbMonitor {

    static String bearer;

    final static String receivers = "juman.wang@brgroup.com,zhao.ma@brgroup.com,zeqiang.guo@brgroup.com,zhen.li@brgroup.com,gongyong.ma@brgroup.com";

//    final static String receivers = "juman.wang@brgroup.com";

    static LocalDateTime loginExpire;

    @Autowired
    RestTemplate restTemplate;

    final static String baseUrl = "http://tidb-monitor-zw-t1.100credit.cn/";


    @Scheduled(cron = "0 0/30 * * * ?")
    public void slowDbSql() {
        Result login = login();
        AssertResult.assertResult(login);
        querySql();
    }

    private Result login() {
        if (StringUtils.isBlank(bearer) || (loginExpire != null && loginExpire.isBefore(LocalDateTime.now()))) {
            String loginUrl = baseUrl.concat("dashboard/api/user/login");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", "u_pd_dashboard");
            jsonObject.put("password", "Tidb@bairong123");
            jsonObject.put("type", 0);
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(loginUrl)
                    .setRequestParam(jsonObject).setContentType(MediaType.APPLICATION_JSON_UTF8).postTransferStr();
            if (transfer.getHttpCode() == 200 && StringUtils.isNotBlank(transfer.getResult())) {
                JSONObject res = JSON.parseObject(transfer.getResult());
                bearer = res.getString("token");
                loginExpire = LocalDateTime.parse(res.getString("expire").substring(0, 19).replace("T", " "), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("登录失败");
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void querySql() {
        String slowUrl = baseUrl.concat("dashboard/api/slow_query/list")
                .concat(String.format("?begin_time=%d&db=marketing&desc=true&digest=&end_time=%d&fields=query,timestamp,query_time,memory_max&limit=100&orderBy=timestamp&&text="
                        , LocalDateTime.now().minusMinutes(60L).toEpochSecond(ZoneOffset.of("+8"))
                        , LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))));
        HashMap head = new HashMap();
        head.put("Authorization", "Bearer ".concat(bearer));
        String s = new ApiCaller(restTemplate).setUrl(slowUrl).setHttpHeaders(head).get();
        List<TidbSqlDTO> list = new ArrayList<>();
        if (StringUtils.isNotBlank(s)) {
            List<TidbSqlDTO> tidbSqlDTOS = JSONArray.parseArray(s, TidbSqlDTO.class);
            list = tidbSqlDTOS.stream().filter(t -> t.getQuery_time() > 2).collect(Collectors.toList());
        }
        StringBuilder sb = new StringBuilder();
        sb.append( "<table border=\"5\"  width=\"650\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:12px;\">");
        sb.append("<tr style=\"background-color: #FF0000; color:#ffffff\"><th>超时sql</th><th>执行耗时</th><th>执行时间</th></tr>");
        if (list.size() <= 0) {
            return;
        }
        list.forEach(t->{
            sb.append("<tr><th>").append(t.getQuery()).append("</th><th>")
                    .append(t.getQuery_time()).append("</th><th>")
            .append(Instant.ofEpochMilli(Long.valueOf(t.getTimestamp().split("\\.")[0])*1000L).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .append("</th></tr>");
        });

        AlarmApiClient.sendMails("marketing库慢sql报警",sb.toString(),receivers);
    }

    public void testUrlSql() {
        try {
            URL url = new URL("http://tidb-monitor-zw-t1.100credit.cn/dashboard/api/slow_query/list?begin_time=1658730456&db=marketing&desc=true&digest=&end_time=1658732256&fields=query%2Ctimestamp%2Cquery_time%2Cmemory_max&limit=100&orderBy=query_time&&text=");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTg4MDQ0ODMsIm9yaWdfaWF0IjoxNjU4NzE4MDgzLCJwIjoiTkI5Rk1oWUM5ZUI4a0dKQzl3WTljemZ5UjI1eHQrYkhMNU01TkkxVHc1QkVadHVUL0NVZDBnck1yaUFXaTVKWVpyYSt2aDZPUEdsUDlUTzhqWHlmUml0cVUrZ2xOa0hrSFE1LzB6V2YzUW9Ed0c3Yk0xd0QydjhJNHZKaXhlMlRDbytLYkc4ZkdDdGttU1hmRlZHeEdna0pvcVp1ZWt0dWdKOW9vOWJpamllVkVMK25jMlI2ZFcyYkJVSW80SkpITWlKMG4xWHhUNVo3eWFicXQ5dDB5V0RGN2lqYjBsaXRIM0sxYVZSWGU5WWdWNlJndWwyV2hYR1RiaklxM1VmWGZlRFJzVEdKNDBxdWZvazFjK05hVnhtV3FyV2FibzRmb0J3Y0JLeWZFL2ZVQ1pFYm9ORkRJQzFHTEVueEdoWHAxVjQ9In0.HM4j6ehzRLUyzUVsprGnEMK6ZXs356z4CNWnT1ghFNo");
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }

            in.close();
            // printing result from response
            System.out.println("Response:-" + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
