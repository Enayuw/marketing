package com.br.marketing.monkey.job.zhongbang.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.monkey.enums.zhongbangai.PushFileStatusEnum;
import com.br.marketing.monkey.service.zhongbang.ZhongBangAIVoiceService;
import com.br.marketing.service.Impl.JobManager;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;


/**
 * 众邦AI录音明细上传
 * 技术文档地址：https://c.100credit.cn/pages/viewpage.action?pageId=227798379
 *
 * @Author: zhen.Li
 * @Date: 2025-11-20
 */
@Component
@Slf4j
public class ZhongBangAIPushVoiceDetailJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongBangAIVoiceService zhongBangAIVoiceService;

    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = "3740001";
        String parameter = context.getJobParameter();
        if (StringUtils.isNotEmpty(parameter)) {
            apiCode = parameter;
        }
        LocalDate localDate = LocalDate.now();
        ZonedDateTime zonedDateTime = localDate.atStartOfDay().atZone(ZoneId.systemDefault());
        Date startDate = Date.from(zonedDateTime.toInstant());
        Date endDate = Date.from(zonedDateTime.plusDays(1).toInstant());
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andPushStatusIn(Lists.newArrayList(String.valueOf(PushFileStatusEnum.RUNNING.getCode()),
                String.valueOf(PushFileStatusEnum.PUSH_ERROR.getCode())))
                .andApiCodeEqualTo(apiCode).andFileTypeEqualTo(SftpFileTypeEnum.ZHONGBANG_AI_VOICE.getValue())
                .andCreateTimeGreaterThanOrEqualTo(startDate).andCreateTimeLessThan(endDate);
        //查询推送录音完成的文件
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        if (CollectionUtils.isEmpty(localFileList)) {
            log.warn("众邦AI推送录音明细不满足开始条件或已完成");
            return;
        }
        zhongBangAIVoiceService.voiceAIFileUploadDetail(localFileList.get(0), localDate);
    }
}
