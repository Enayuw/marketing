package com.br.marketing.innerapi.task;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.PushTransferCustomerLog;
import com.br.marketing.service.PushTransferCustomerLogService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 接口转化推送客服失败记录补偿任务
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 17:48
 */
@Component
@Slf4j
public class TaskPushTransferToCustomerJob extends AbstractSimpleElasticJob {


    @Resource(name = "loadBalanced")
    private RestTemplate restTemplate;

    @Value("${api.robotAiApiService.pushTransferUrl:http://robotai-api-service/api/robotOutbound}")
    private String pushTransferUrl;

    @Resource
    private PushTransferCustomerLogService pushTransferCustomerLogService;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 分片项目
        List<Integer> shardingItems = context.getShardingItems();
        // 总分片数
        int shardingTotalCount = context.getShardingTotalCount();
        int compensateTimes = 5;
        Long start = System.currentTimeMillis();
        log.warn("【转化数据同步客服补偿任务】调度开始");
        List<PushTransferCustomerLog> rows = pushTransferCustomerLogService.findListByStatusIs1(1, 200, shardingTotalCount, shardingItems);
        HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tempHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        tempHeaders.setAccept(Collections.singletonList(MediaType.ALL));
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        for (PushTransferCustomerLog customerLog : rows) {
            PushTransferCustomerLog updateLog = new PushTransferCustomerLog();
            updateLog.setId(customerLog.getId());
            updateLog.setCompensateTimes(customerLog.getCompensateTimes() + 1);
            // 检查补偿次数
            if (updateLog.getCompensateTimes() >= compensateTimes) {
                updateLog.setPushStatus(3);
            }
            postParameters.add("apiCode", customerLog.getApiCode());
            postParameters.add("jsonData", customerLog.getRequestBody());
            HttpEntity<MultiValueMap<String, Object>> stringHttpEntity = new HttpEntity<>(postParameters, tempHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(pushTransferUrl, stringHttpEntity, String.class);
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (ObjectUtils.isEmpty(responseEntity)) {
                String smg = String.format("%s : apiCode[%s];requestId:[%s]补偿失败！接口不能正常访问"
                        , LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), customerLog.getApiCode(), customerLog.getRequestId());
                alarmClient.sendAlarm(smg, "接口转化数据同步到智能客服失败", appName, secretKey,
                        Constants.sendCodeMap.get("sysError"));
                continue;
            }
            String body = responseEntity.getBody();
            int value = statusCode.value();
            JSONObject result = JSONObject.parseObject(body);
            String reasonPhrase = statusCode.getReasonPhrase();
            log.info("智能客服接口HttpStatus[code:{};reasonPhrase:{}]", value, reasonPhrase);
            String code = String.valueOf(result.get("code"));
            if (value == 200) {
                updateLog.setPushStatus(2);
                if (!"00".equals(code) && updateLog.getCompensateTimes() >= compensateTimes) {
                    updateLog.setPushStatus(4);
                }
                String smg = String.format("apiCode:[%s];requestId:[%s]补偿依然失败！" +
                        "\n接口返回http状态码[%d],http短语[%s];" +
                        "\n返回体[%s]", customerLog.getApiCode(), customerLog.getRequestId(), value, reasonPhrase, body);
                alarmClient.sendAlarm(smg, "接口转化数据同步到智能客服失败", appName, secretKey,
                        Constants.sendCodeMap.get("sysError"));
            }
            updateLog.setRequestBody(body);
            updateLog.setHttpStatus(value);
            updateLog.setHttpReasonPhrase(reasonPhrase);
            updateLog.setServiceCode(code);
            updateLog.setMessage(result.get("message") == null ? "" : result.get("message").toString());
            updateLog.setSwiftNumber(result.get("accessNumber") == null ? result.get("swiftNumber") == null
                    ? "" : result.get("swiftNumber").toString() : result.get("accessNumber").toString());
            pushTransferCustomerLogService.updateByPrimaryKeySelective(updateLog);
            postParameters.clear();
        }
        Long end = System.currentTimeMillis();
        log.warn("【转化数据同步客服补偿任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }
}
