package com.br.marketing.sync.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.sync.service.impl.MinioFileService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Component
@Slf4j
/**
 * @author:zhen.Li1
 * @Classname FileDownloadTaskMiNiOJob
 * @Description 文件下载MiNio任务JOB
 * @Date 2025/12/15
 */
public class FileDownloadTaskMiNiOJob extends AbstractSimpleElasticJob {

    @Resource
    private MinioFileService minioFileService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String parameter = context.getJobParameter();
        JSONObject jsonObject;
        if (JSON.isValid(parameter)) {
            jsonObject = JSONObject.parseObject(parameter);
        } else {
            jsonObject = new JSONObject();
        }
        if (Objects.isNull(jsonObject)) {
            return;
        }
        log.warn("minio开始执行下载任务");
        jsonObject.entrySet().forEach(entry -> {
            String targetPath = entry.getKey();
            String localPath = entry.getValue().toString();
            Boolean isSuccess = minioFileService.downloadFile(targetPath, localPath);
            if (isSuccess) {
                log.warn("minio下载文件完成,targetPath:{},localPath:{}", targetPath, localPath);
            }
        });

    }
}
