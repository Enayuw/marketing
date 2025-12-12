package com.br.marketing.check.monitor;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * check服务Job开关守护任务
 * 扫描checkJobOnlineSwitch开关，如果开启超过30分钟则发送告警
 * 注意：此Job在monitor包下，不受JobSwitchAspect切面影响
 * 
 * @author system
 */
@Component
@Slf4j
public class CheckJobSwitchGuardJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 记录开关开启的时间戳（毫秒）
     * 如果为null或0，表示开关未开启或已关闭
     */
    private static Long switchOpenTime = null;

    /**
     * 告警阈值：30分钟（毫秒）
     */
    private static final long ALARM_THRESHOLD_MS = 30 * 60 * 1000L;

    /**
     * 获取开关开启时间
     */
    private static Long getSwitchOpenTime() {
        return switchOpenTime;
    }

    /**
     * 设置开关开启时间
     */
    private static void setSwitchOpenTime(Long time) {
        switchOpenTime = time;
    }

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            // 获取开关状态
            Boolean jobOnlineSwitch = marketingCommonConfig.getCheckJobOnlineSwitch();
            boolean isSwitchOpen = StringUtils.isNotEmpty(jobOnlineSwitch) && jobOnlineSwitch;

            if (isSwitchOpen) {
                // 开关已开启
                long currentTime = System.currentTimeMillis();
                
                // 如果之前没有记录开启时间，记录当前时间
                if (getSwitchOpenTime() == null) {
                    setSwitchOpenTime(currentTime);
                    log.info("检测到check服务Job开关已开启，开始计时");
                    return;
                }
                
                // 计算开关已开启的时长
                long openDuration = currentTime - getSwitchOpenTime();
                
                // 如果超过30分钟，发送告警
                if (openDuration >= ALARM_THRESHOLD_MS) {
                    long minutes = openDuration / (60 * 1000);
                    String alarmMessage = String.format(
                        "【重要告警】check服务Job上线开关(checkJobOnlineSwitch)已开启超过%d分钟！" +
                        "请及时关闭开关，避免影响定时任务执行。开关开启时间：%s",
                        minutes,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(getSwitchOpenTime()))
                    );
                    
                    log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.MARKETING_ERROR.getCode(),
                        alarmMessage,
                        "check服务Job开关超时告警"
                    ));
                } else {
                    long minutes = openDuration / (60 * 1000);
                    log.warn("check服务Job开关已开启{}分钟，未达到告警阈值", minutes);
                }
            } else {
                // 开关已关闭
                setSwitchOpenTime(null);
            }
        } catch (Exception e) {
            log.error("check服务Job开关守护任务执行异常", e);
        }
    }
}

