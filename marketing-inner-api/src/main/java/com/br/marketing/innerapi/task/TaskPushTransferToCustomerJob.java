package com.br.marketing.innerapi.task;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.PushTransferCustomerLog;
import com.br.marketing.service.PushTransferCustomerLogService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 私人订制 接口转化推送客服失败记录补偿任务
 * <p>
 * 重试情况：
 * 1. 网络问题，没有请求到客服接口
 * 容错机制：将本次接口请求数据直接保存到日志记录表。
 * 补偿调度任务触发时将保存的记录重新发起请求，并计算重试次数，成功接收后更新状态为已补偿。
 * <p>
 * 2. 客服返回非“00”状态的业务状态码
 * 容错机制：将本次请求及应答消息记录日志记录表。
 * 补偿调度任务触发时获取补偿状态中并标记为推送结束标志的数据；
 * 未找到时，查找补偿中标记为开始推送标记的记录，重新发送请求；
 * 找到时，根据记录查询状态为0的记录，优先重试状态为0的记录，最后确认没有状态为0的记录时，状态为2的记录重新发送；
 * 记录重试次数
 * 保证推送状态为0的数据都推送成功后再发送状态为2的数据
 * <p>
 * 注：所有请求都记录日志，调度任务默认为30分钟/次，默认重试5次
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 17:48
 */
//@Component
@Slf4j
@Deprecated
public class TaskPushTransferToCustomerJob extends AbstractSimpleElasticJob {


    @Resource
    private RestTemplate restTemplate;

    @Value("#{${api.pushTransfer.robotAi.tailor.apiCodeMap:{'7410787':true}}}")
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
        log.warn("私人订制【转化数据同步客服补偿任务】调度开始");
        List<PushTransferCustomerLog> logList;
        HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tempHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        tempHeaders.setAccept(Collections.singletonList(MediaType.ALL));
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
//        List<PushTransferCustomerLog> rows = pushTransferCustomerLogService.findListByStatusIs1(1, 200
//                , shardingTotalCount, shardingItems, 2);
        List<PushTransferCustomerLog> rows = pushTransferCustomerLogService.findListByStatusIs1(1, 200
                , LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE), 2);
        if (rows.size() < 1) {
            logList = pushTransferCustomerLogService.findListByStatusIs1(1, 200
                    , shardingTotalCount, shardingItems, 0);
            pushTransferCustomer(logList, compensateTimes, postParameters, tempHeaders);
        } else {
            for (PushTransferCustomerLog cLog : rows) {
                try {
                    logList = pushTransferCustomerLogService.findListByStatusAndCodeAndDate(1, 200, shardingTotalCount
                            , shardingItems, 0, cLog.getTransferInfoTime(), cLog.getApiCode(), 1);
                    if (logList.size() > 0) {
                        int i = pushTransferCustomer(logList, compensateTimes, postParameters, tempHeaders);
                        if (logList.size() == i) {
                            logList.clear();
                            logList.add(cLog);
                        } else {
                            continue;
                        }
                    } else {
                        logList = pushTransferCustomerLogService.findListByStatusAndCodeAndDate(1, 200, shardingTotalCount
                                , shardingItems, 0, cLog.getTransferInfoTime(), cLog.getApiCode(), 3);
                        if (logList.size() > 0) {
                            String smg = String.format("**apiCode:[%s];requestId:[%s]存在超出补偿次数的数据，结束标记请求等待中！记录主键[%d]" +
                                    "\n超出补偿请求数[%d];", cLog.getApiCode(), cLog.getRequestId(), cLog.getId(), logList.size());
                            sendAlarm(smg);
                            continue;
                        }
                        logList.add(cLog);
                    }
                    pushTransferCustomer(logList, compensateTimes, postParameters, tempHeaders);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    String smg = String.format("**apiCode:[%s];requestId:[%s]补偿任务自身出现错误！记录主键[%d]" +
                            "\n异常信息[%s];", cLog.getApiCode(), cLog.getRequestId(), cLog.getId(), e.getMessage());
                    sendAlarm(smg);
                }
            }
        }
        Long end = System.currentTimeMillis();
        log.warn("私人订制【转化数据同步客服补偿任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }


    private int pushTransferCustomer(List<PushTransferCustomerLog> logList
            , int compensateTimes, MultiValueMap<String, Object> postParameters, HttpHeaders tempHeaders) {
        String apiCode;
        int count = 0;
        for (PushTransferCustomerLog customerLog : logList) {
            try {
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
                        sendAlarm(smg);
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
                        String smg;
                        if ("900028".equals(code)) {
                            updateLog.setPushStatus(4);
                            smg = String.format("##apiCode:[%s];requestId:[%s]补偿失败,已补偿[%d]/共[%d],放弃补偿任务!原因：未配置资源方！" +
                                    "\n返回http状态码[%d],http短语[%s];" +
                                    "\n业务应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), compensateTimes, value, reasonPhrase, body);
                        } else if ("00".equals(code)) {
                            count++;
                            updateLog.setPushStatus(2);
                            smg = String.format("@@apiCode:[%s];requestId:[%s]补偿成功！已补偿[%d]/共[%d]" +
                                    "\n返回http状态码[%d],http短语[%s];" +
                                    "\n业务应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), compensateTimes, value, reasonPhrase, body);
                        } else {
                            smg = String.format("$$apiCode:[%s];requestId:[%s]补偿依然失败！已补偿[%d]/共[%d]" +
                                    "\n返回http状态码[%d],http短语[%s];" +
                                    "\n业务应答消息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), compensateTimes, value, reasonPhrase, body);
                        }
                        sendAlarm(smg);
                    }
                    updateLog.setHttpStatus(value);
                    updateLog.setHttpReasonPhrase(reasonPhrase);
                    updateLog.setServiceCode(code);
                    updateLog.setMessage(result.get("message") == null ? "" : result.get("message").toString());
                    updateLog.setSwiftNumber(result.get("accessNumber") == null ? result.get("swiftNumber") == null
                            ? "" : result.get("swiftNumber").toString() : result.get("accessNumber").toString());
                } catch (RestClientException e) {
                    String smg = String.format("$$apiCode:[%s];requestId:[%s]补偿依然失败！已补偿[%d]/共[%d],可能原因接口不可访问;" +
                            "\n异常信息[%s]", customerLog.getApiCode(), customerLog.getRequestId(), updateLog.getCompensateTimes(), compensateTimes, e.getMessage());
                    sendAlarm(smg);
                }
                updateLog.setUpdateTime(new Date());
                pushTransferCustomerLogService.updateByPrimaryKeySelective(updateLog);
                postParameters.clear();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                String smg = String.format("**apiCode:[%s];requestId:[%s]补偿任务异常！记录主键[%d]" +
                        "\n异常信息[%s];", customerLog.getApiCode(), customerLog.getRequestId(), customerLog.getId(), e.getMessage());
                sendAlarm(smg);
            } finally {
                postParameters.clear();
            }
        }
        return count;
    }

    private void sendAlarm(String smg) {
        String title = "接口转化(私人订制)数据同步到智能客服补偿任务警告";
        log.warn(smg);
        alarmClient.sendAlarm(smg, title, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
    }
}
