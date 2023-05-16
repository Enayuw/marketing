package com.br.marketing.monkey.job.didi;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.monkeydata.handle.didi.DidiCallRecordHandle;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/2/18 16:57
 */
@Component
@Slf4j
public class DidiCallingRecordJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DidiCallRecordHandle didiCallRecordHandle;

    private final static String DIDICOLLRECORDEXECTIME = "21:00:00";
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        List<String> sendDateLists = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParameter)) {
            sendDateLists = Splitter.on(",").splitToList(jobParameter);
            didiCallRecordHandle.pushDidiCallRecord(sendDateLists, "job");
        } else {
            if (!LocalTime.now().isBefore(getSendTime())) {
                sendDateLists.add(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
                didiCallRecordHandle.pushDidiCallRecord(sendDateLists, "job");
            }

        }
    }

    /**
     * 判断当前时间是否大于21点
     */
    private LocalTime getSendTime() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(marketingCommonConfig.getDidiCallRecordExecTime()==null?DIDICOLLRECORDEXECTIME:marketingCommonConfig.getDidiCallRecordExecTime(), timeFormat);
    }
}
