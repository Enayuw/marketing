package com.br.marketing.innerapi.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.PushTransferRobotaiLog;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PushTransferRobotaiLogMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.PushTransferRobotaiLogService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用标准 接口转化推送客服失败记录补偿任务
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/11/05 17:48
 */
@Component
@Slf4j
public class TaskPushTransferToRobotaiJob extends AbstractSimpleElasticJob {

    @Resource
    private PushTransferRobotaiLogMapper pushTransferRobotaiLogMapper;


    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private PushRuleService ruleService;

    @Resource
    private PushTransferRobotaiLogService pushTransferRobotaiLogService;

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
        log.warn("通用标准【转化数据同步客服补偿任务】调度开始");
        PageHelper.startPage(1, 200);
        List<PushTransferRobotaiLog> rows = pushTransferRobotaiLogMapper.findListByStatusIs0(shardingTotalCount, shardingItems);
        String apiCode;
        for (PushTransferRobotaiLog robotaiLog : rows) {
            PushTransferRobotaiLog updateLog = new PushTransferRobotaiLog();
            updateLog.setId(robotaiLog.getId());
            updateLog.setCompensateTimes(robotaiLog.getCompensateTimes() + 1);
            // 检查补偿次数
            if (updateLog.getCompensateTimes() >= compensateTimes) {
                updateLog.setPushStatus(2);
            }
            apiCode = robotaiLog.getApiCode();
            TransferRobotOutboundVO<UnsuccessfulData> outboundVO = new TransferRobotOutboundVO<>();
            MarketingTransferInfo info = new MarketingTransferInfo();
            info.setApiCode(apiCode);
            info.setRequestId(robotaiLog.getRequestId());
            if (robotaiLog.getServiceCode().equals("00")) {
                JSONObject object = JSON.parseObject(robotaiLog.getResponseBody());
                if (StringUtils.isEmpty(object)) {
                    log.error("应答消息为空：{}", robotaiLog);
                    continue;
                }
                Object unsuccessfulData = object.get("unsuccessfulData");
                JSONArray array = JSONArray.parseArray(unsuccessfulData.toString());
                if (array.size() > 0) {
                    TransferRobotOutboundDTO outboundDTO = getTransferRobotOutbound(robotaiLog, info, array);
                    outboundVO = ruleService.pushTransferData(outboundDTO, info);
                    if (robotaiLog.getRowSize() > array.size()) {
                        if (!outboundVO.getAccessNumber().equals("-1")) {
                            info.setId(robotaiLog.getTransferInfoId());
                            pushTransferRobotaiLogService.saveLog(info, outboundDTO, outboundVO);
                            String smg = String.format("$$apiCode:[%s];requestId:[%s]补偿部分失败的数据依然失败或部分失败!已补偿[%d];" +
                                            "\n更新原记录为已补偿,并生成新记录！" +
                                            "\n业务返回状态码[%s];" +
                                            "\n业务应答消息[%s]", robotaiLog.getApiCode(), robotaiLog.getRequestId(), updateLog.getCompensateTimes()
                                    , outboundVO.getCode(), outboundVO.getMessage());
                            sendAlarm(smg);
                        } else {
                            String smg = String.format("@@apiCode:[%s];requestId:[%s]补偿成功,更新原记录为已补偿！已补偿[%d]" +
                                            "\n业务返回状态码[%s];" +
                                            "\n业务应答消息[%s]", robotaiLog.getApiCode(), robotaiLog.getRequestId(), updateLog.getCompensateTimes()
                                    , outboundVO.getCode(), outboundVO.getMessage());
                            sendAlarm(smg);
                        }
                        updateLog.setPushStatus(1);
                        pushTransferRobotaiLogMapper.updateByPrimaryKeySelective(updateLog);
                        continue;
                    } else {
                        updateLog.setRequestBody(JSON.toJSONString(outboundDTO));
                    }
                }
            } else if (StringUtils.isEmpty(robotaiLog.getServiceCode())) {
                outboundVO = ruleService.pushTransferData(JSON.parseObject(robotaiLog.getRequestBody(), new TypeReference<TransferRobotOutboundDTO>() {
                }), info);
            } else {
                JSONObject object = JSON.parseObject(robotaiLog.getResponseBody());
                Object unsuccessfulData = object.get("unsuccessfulData");
                JSONArray array = JSONArray.parseArray(unsuccessfulData.toString());
                if (array.size() > 0) {
//                    TransferRobotOutboundDTO outboundDTO1 = JSON.parseObject(robotaiLog.getRequestBody(), new TypeReference<TransferRobotOutboundDTO>() {
//                    });
                    TransferRobotOutboundDTO outboundDTO = getTransferRobotOutbound(robotaiLog, info, array);
//                    outboundDTO1.getJsonData().setConversionData(outboundDTO.getJsonData().getConversionData());
                    outboundVO = ruleService.pushTransferData(outboundDTO, info);
                    updateLog.setRequestBody(JSON.toJSONString(outboundDTO));
                }
            }
            updateLog.setResponseBody(JSON.toJSONString(outboundVO.getData()));
            if (outboundVO.getAccessNumber().equals("-1")) {
                updateLog.setPushStatus(1);
                String smg = String.format("@@apiCode:[%s];requestId:[%s]补偿成功！已补偿[%d]" +
                                "\n业务返回状态码[%s];" +
                                "\n业务应答消息[%s]", robotaiLog.getApiCode(), robotaiLog.getRequestId(), updateLog.getCompensateTimes()
                        , outboundVO.getCode(), outboundVO.getMessage());
                sendAlarm(smg);
            } else {
                String requestBody = updateLog.getRequestBody();
                String smg = String.format("$$apiCode:[%s];requestId:[%s]补偿依然失败或部分失败！已补偿[%d]" +
                                "\n业务返回状态码[%s];" +
                                "\n业务应答消息[%s]" +
                                "\n未成功数据情况:[%s]", robotaiLog.getApiCode(), robotaiLog.getRequestId(), updateLog.getCompensateTimes()
                        , outboundVO.getCode(), outboundVO.getMessage(),
                        requestBody.length() > 300 ? requestBody.substring(0, 300).concat("...") : requestBody);
                sendAlarm(smg);
            }
            updateLog.setMessage(outboundVO.getMessage());
            updateLog.setServiceCode(outboundVO.getCode());
            pushTransferRobotaiLogMapper.updateByPrimaryKeySelective(updateLog);
        }
        Long end = System.currentTimeMillis();
        log.warn("通用标准【转化数据同步客服补偿任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }

    private TransferRobotOutboundDTO getTransferRobotOutbound(PushTransferRobotaiLog robotaiLog, MarketingTransferInfo info, JSONArray array) {
        List<Long> dataIds = array.stream().map(obj -> {
            JSONObject js = (JSONObject) obj;
            return Long.parseLong(js.get("dataId").toString());
        }).collect(Collectors.toList());
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andIdIn(dataIds);
        example.settCid(robotaiLog.gettCid());
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        return ruleService.getTransferRobotOutbound(info, transferList);
    }

    private void sendAlarm(String smg) {
        String title = "接口转化(通用标准)数据同步到智能客服补偿任务警告";
        log.warn(smg);
        alarmClient.sendAlarm(smg, title, appName, secretKey,
                Constants.sendCodeMap.get("pushToCustomer"));
    }


}
