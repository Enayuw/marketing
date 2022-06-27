package com.br.marketing.check.job;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IApiToDbService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


@Component
@Slf4j
@Deprecated
public class ApiToDbByTimeJob extends AbstractSimpleElasticJob {

    @Autowired
    IApiToDbService iApiToDbService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        if(StringUtils.isNotBlank(jobParameter)) {
            try {
                String[] split = jobParameter.split(",");
                String apiCode = split[0];
                String userType = split[1];
                String startTime = split[2];
                String endTime = split[3];
                String scoreDate = split[4];
                if (StringUtils.isNotBlank(apiCode)
                        && StringUtils.isNotBlank(userType)
                        && StringUtils.isNotBlank(startTime)
                        && StringUtils.isNotBlank(endTime)
                ) {
                    LocalDateTime s = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime e = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDate.parse(scoreDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    HashMap<String, String> param = new HashMap<>();
                    param.put("apiCode", apiCode);
                    param.put("userType", userType);
                    param.put("startTime", startTime);
                    param.put("endTime", endTime);
                    param.put("scoreDate",scoreDate);
                    iApiToDbService.pushToDb(apiCode, param);
                }
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
            }
        }

    }
}
