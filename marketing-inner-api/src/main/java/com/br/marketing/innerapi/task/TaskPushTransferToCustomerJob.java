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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 接口转化推送客服失败记录补偿任务
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 17:48
 */
@Component
@Slf4j
public class TaskPushTransferToCustomerJob extends AbstractSimpleElasticJob {


    @Resource
    private RestTemplate restTemplate;

    @Value("#{${api.pushTransfer.robotAi.tailor.apiCodeMap:'7410787:true'}}")
    private Map<String, Boolean> tailorApiCodeMap;

    @Value("${api.pushTransfer.robotAi.robotOutboundUrl:'http://robotai-api-service/api/robotOutbound'}")
    private String robotOutboundUrl;

    @Resource
    private PushTransferCustomerLogService pushTransferCustomerLogService;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 分片项目
        List<Integer> shardingItems = context.getShardingItems();
        // 总分片数
        int shardingTotalCount = context.getShardingTotalCount();
        // 设置最大重试次数
        int compensateTimes = StringUtils.isEmpty(context.getJobParameter()) ? 5 : Integer.parseInt(context.getJobParameter());
        Long start = System.currentTimeMillis();
        log.warn("【转化数据同步客服补偿任务】调度开始");
        List<PushTransferCustomerLog> rows = pushTransferCustomerLogService.findListByStatusIs1(1, 200, shardingTotalCount, shardingItems);
        HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tempHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        tempHeaders.setAccept(Collections.singletonList(MediaType.ALL));
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        String apiCode;
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
            apiCode = customerLog.getApiCode();
            if (!tailorApiCodeMap.getOrDefault(apiCode, false)) {
                continue;
            }
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(robotOutboundUrl, stringHttpEntity, String.class);
                HttpStatus statusCode = responseEntity.getStatusCode();
                if (ObjectUtils.isEmpty(responseEntity)) {
                    String smg = String.format("%s : apiCode[%s];requestId:[%s]补偿失败！接口不能正常访问"
                            , LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), customerLog.getApiCode(), customerLog.getRequestId());
                    alarmClient.sendAlarm(smg, "接口转化数据同步到智能客服失败", appName, secretKey,
                            Constants.sendCodeMap.get("pushToCustomer"));
                    continue;
                }
                String body = responseEntity.getBody();
                updateLog.setResponseBody(body);
                int value = statusCode.value();
                JSONObject result = JSONObject.parseObject(body);
                String reasonPhrase = statusCode.getReasonPhrase();
                log.info("智能客服接口HttpStatus[code:{};reasonPhrase:{}]", value, reasonPhrase);
                String code = String.valueOf(result.get("code"));
                if (value == 200) {
                    if ("900028".equals(code)) {
                        updateLog.setPushStatus(4);
                        String smg = String.format("##apiCode:[%s];requestId:[%s]补偿失败,已补偿[%d],放弃补偿任务!原因：未配置资源方！" +
                                "\n接口返回http状态码[%d],http短语[%s];" +
                                "\n应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), value, reasonPhrase, body);
                        log.warn(smg);
                        alarmClient.sendAlarm(smg, "接口转化数据补偿同步到智能客服失败", appName, secretKey,
                                Constants.sendCodeMap.get("pushToCustomer"));
                    } else if ("00".equals(code)) {
                        updateLog.setPushStatus(2);
                        log.info(String.format("@@apiCode:[%s];requestId:[%s]补偿成功！已补偿[%d]" +
                                "\n接口返回http状态码[%d],http短语[%s];" +
                                "\n应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), value, reasonPhrase, body));
                    } else {
                        String smg = String.format("$$apiCode:[%s];requestId:[%s]补偿依然失败！已补偿[%d]" +
                                "\n接口返回http状态码[%d],http短语[%s];" +
                                "\n应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), value, reasonPhrase, body);
                        log.error(smg);
                        alarmClient.sendAlarm(smg, "接口转化数据补偿同步到智能客服失败", appName, secretKey,
                                Constants.sendCodeMap.get("pushToCustomer"));
                    }
                }
                updateLog.setHttpStatus(value);
                updateLog.setHttpReasonPhrase(reasonPhrase);
                updateLog.setServiceCode(code);
                updateLog.setMessage(result.get("message") == null ? "" : result.get("message").toString());
                updateLog.setSwiftNumber(result.get("accessNumber") == null ? result.get("swiftNumber") == null
                        ? "" : result.get("swiftNumber").toString() : result.get("accessNumber").toString());
            } catch (RestClientException e) {
                String smg = String.format("$$apiCode:[%s];requestId:[%s]补偿依然失败！已补偿[%d],可能原因接口不可访问;" +
                        "\n异常信息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), e.getMessage());
                log.error(smg);
                alarmClient.sendAlarm(smg, "接口转化数据补偿同步到智能客服失败", appName, secretKey,
                        Constants.sendCodeMap.get("pushToCustomer"));
            }
            pushTransferCustomerLogService.updateByPrimaryKeySelective(updateLog);
            postParameters.clear();
        }
        Long end = System.currentTimeMillis();
        log.warn("【转化数据同步客服补偿任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }
}
