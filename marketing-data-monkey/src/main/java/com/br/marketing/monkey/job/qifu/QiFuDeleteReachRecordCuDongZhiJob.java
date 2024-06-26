package com.br.marketing.monkey.job.qifu;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLog;
import com.br.marketing.entity.RetryMainLogExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.QifuSaveReachDeleteRecordApiPushLogMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.qifu.DeleteReachRecordHandler;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * D20240622促动支用户删除-3710139（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=166635171
 *
 * @author lixiang
 * @dateTime 2024-06-27 19:22
 */
@Component
@Slf4j
public class QiFuDeleteReachRecordCuDongZhiJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【奇富删除触达记录促动支】";

    @Resource
    private DeleteReachRecordHandler deleteReachRecordHandler;

    @Resource
    private QifuSaveReachDeleteRecordApiPushLogMapper qifuSaveReachDeleteRecordApiPushLogMapper;

    @Resource
    private RetryMainLogMapper retryMainLogMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Value("${api.qifu.isProxy:true}")
    private boolean isProxy;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private JobManager jobManager;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE + "调度开始");
            processAction(shardingContext);
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.error(TITLE + "调度异常", e);
        }
    }

    /**
     * 2023-09-27 11:02
     * parameter格式：apiCode:yyyy-MM-dd或apiCode
     * eg1: 3710053:2023-09-22
     * eg2: 7491630
     */
    public void processAction(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        String parameter = shardingContext.getJobParameter();
        String apiCode;
        LocalDate now = LocalDate.now();
        String actionDate = now.toString();
        String deleteDate = now.minusDays(marketingCommonConfig.getQiFuDeleteReachRecordDay()).toString();
        if (StringUtils.isBlank(parameter)) {
            apiCode = "3710139";
        } else {
            String[] params = parameter.split(":");
            if (params.length == 1) {
                apiCode = params[0];
            } else {
                apiCode = params[0];
                deleteDate = params[1];
            }
        }

        Page2Condition<QifuSaveReachDeleteRecordApiPushLog> data = new Page2Condition<>();
        data.setPageIndex(0);
        data.setPageSize(2000);
        QifuSaveReachDeleteRecordApiPushLog pushLog = new QifuSaveReachDeleteRecordApiPushLog();
        data.setParam(pushLog);
        pushLog.setApiCode(apiCode);
        pushLog.setSyncAppletDate(deleteDate);
        pushLog.setPushDate(actionDate);

        // actionFront
        int actionType = JobManager.ActionTypeEnum.QIFU_DELETE_REACH_RECORD.getActionType();
        TransferActionFront actionFront = jobManager.getFrontData(apiCode, actionDate, actionType, null);
        if (actionFront != null) {
            if (2 == actionFront.getStatus()) {
                pushLog.setStatus(0);
                deleteReachRecordHandler.action(data);
                log.warn(TITLE + "该任务今日已经推送, apiCode:{}, deleteDate:{}", apiCode, deleteDate);
            } else {
                log.warn(TITLE + "该任务今日已经已有任务在运行, apiCode:{}, deleteDate:{}", apiCode, deleteDate);
                return;
            }
        } else {
            actionFront = jobManager.saveFront(apiCode, now.toString(), actionType);
            if (actionFront.getId() == null) {
                log.warn(TITLE+ "执行记录添加失败, apiCode:{}, actionDate:{}", apiCode, actionDate);
                return;
            }
            pushLog.setStatus(1);
            deleteReachRecordHandler.action(data);
            jobManager.updateFrontDataStatus(actionFront.getId(), 2);
        }

        // 检查异常数据发送告警
        String accessToken = marketingCommonConfig.getQiFuDingDingAccessToken();
        if (StringUtils.isNotBlank(accessToken) && isRetry(now)) {
            errorStatistics(apiCode, actionDate, accessToken, marketingCommonConfig.getQiFuDingDingSecret());
        }
        long end = System.currentTimeMillis();
        log.warn(TITLE + "调度结束, apiCode:{}, deleteDate:{}, 耗时:{}", apiCode, deleteDate, end - start);
    }

    /**
     * 2023-09-27 18:08
     * 检查是否还存在重试数据
     */
    private boolean isRetry(LocalDate now) {
        RetryMainLogExample example = new RetryMainLogExample();
        example.createCriteria()
                .andRetryMethodEqualTo("callDeleteReachRecordCuDongZhi")
                .andRetryStatusEqualTo(1)
                .andRetryServiceEqualTo("com.br.marketing.strategy.MethodRetryHandlerService")
                .andCreateTimeBetween(Date.from(now.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));
        int i = retryMainLogMapper.countByExample(example);
        return i == 0;
    }

    /**
     * 2023-09-27 18:08
     * 错误信息告警
     */
    private void errorStatistics(String apiCode, String dateStr, String accessToken, String secret) {
        List<QifuSaveReachDeleteRecordApiPushLog> apiErrorStatistics =
                qifuSaveReachDeleteRecordApiPushLogMapper.getApiErrorStatistics(apiCode, dateStr);
        List<QifuSaveReachDeleteRecordApiPushLog> qiFuBizErrorStatistics =
                qifuSaveReachDeleteRecordApiPushLogMapper.getQiFuBizErrorStatistics(apiCode, dateStr);
        List<QifuSaveReachDeleteRecordApiPushLog> bizErrorStatistics =
                qifuSaveReachDeleteRecordApiPushLogMapper.getBizErrorStatistics(apiCode, dateStr);
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        String title = "奇富删除触达记录促动支推送异常信息";
        markdown.setTitle(title);
        StringBuilder sb = new StringBuilder("# 奇富【删除触达记录促动支】接口推送异常信息\n");
        if (CollectionUtils.isEmpty(apiErrorStatistics) && CollectionUtils.isEmpty(qiFuBizErrorStatistics)
                && CollectionUtils.isEmpty(bizErrorStatistics)) {
            return;
        }
        if (!CollectionUtils.isEmpty(apiErrorStatistics)) {
            sb.append("## api错误信息:\n");
            sb.append("|状态|返回码|返回码描述|入库日期|推送日期|量级|\n");
            sb.append("|:----:|:----:|:----:|:----:|:----:|:----:|\n");
            for (QifuSaveReachDeleteRecordApiPushLog apiErrorStatistic : apiErrorStatistics) {
                sb.append("|");
                sb.append(apiErrorStatistic.getRespFlag()).append("|");
                sb.append(apiErrorStatistic.getRespCode()).append("|");
                sb.append(apiErrorStatistic.getRespMsg()).append("|");
                sb.append(apiErrorStatistic.getSyncAppletDate()).append("|");
                sb.append(apiErrorStatistic.getPushDate()).append("|");
                sb.append(apiErrorStatistic.getStatus());
                sb.append("|\n");
            }
        }
        if (!CollectionUtils.isEmpty(qiFuBizErrorStatistics)) {
            sb.append("## 响应业务错误信息:\n");
            sb.append("|返回码|返回码描述|入库日期|推送日期|量级|\n");
            sb.append("|:----:|:----:|:----:|:----:|:----:|\n");
            for (QifuSaveReachDeleteRecordApiPushLog apiErrorStatistic : qiFuBizErrorStatistics) {
                sb.append("|");
                sb.append(apiErrorStatistic.getQifuIsSucceed()).append("|");
                sb.append(apiErrorStatistic.getQifuMessage()).append("|");
                sb.append(apiErrorStatistic.getSyncAppletDate()).append("|");
                sb.append(apiErrorStatistic.getPushDate()).append("|");
                sb.append(apiErrorStatistic.getStatus());
                sb.append("|\n");
            }
        }
        if (!CollectionUtils.isEmpty(bizErrorStatistics)) {
            sb.append("## 推送逻辑错误信息:\n");
            sb.append("|错误描述|入库日期|推送日期|量级|\n");
            sb.append("|:----:|:----:|:----:|:----:|\n");
            for (QifuSaveReachDeleteRecordApiPushLog apiErrorStatistic : bizErrorStatistics) {
                sb.append("|");
                sb.append(apiErrorStatistic.getErrorMsg()).append("|");
                sb.append(apiErrorStatistic.getSyncAppletDate()).append("|");
                sb.append(apiErrorStatistic.getPushDate()).append("|");
                sb.append(apiErrorStatistic.getStatus());
                sb.append("|\n");
            }
        }
        String text = sb.toString();
        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), text, title));
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingMarkdownMessage, isProxy);
    }
}
