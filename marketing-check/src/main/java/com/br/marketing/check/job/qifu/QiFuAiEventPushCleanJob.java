package com.br.marketing.check.job.qifu;


import com.alibaba.fastjson2.JSONObject;
import com.br.marketing.check.service.qifu.QiFuAiEventPushService;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.EventPushData;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName QiFuAiEventPushCleanJob
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Component
public class QiFuAiEventPushCleanJob extends AbstractSimpleElasticJob {

    private static final Logger logger = LoggerFactory.getLogger(QiFuAiEventPushCleanJob.class);

    @Resource
    private QiFuAiEventPushService qiFuAiEventPushService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {

        //查找未同步的事件推送数据sync_status = 0
        List<DrsCustomizeUploadData> drsCustomizeUploadDataList = qiFuAiEventPushService.getDrsCustomizeUploadDataBySyncStatus(0);
        if (CollectionUtils.isEmpty(drsCustomizeUploadDataList)) {
            logger.warn("不存在未处理的事件推送数据");
        } else {
            List<BQifuUploadDataOriginal> uploadDataOriginalList = assembleRealTimeUploadDataOriginal(drsCustomizeUploadDataList);


        }

    }

    public List<BQifuUploadDataOriginal> assembleRealTimeUploadDataOriginal(List<DrsCustomizeUploadData> drsCustomizeUploadDataList) {
        //解析事件推送接口原始数据
        List<BQifuUploadDataOriginal> resultList = new ArrayList<>();
        for (DrsCustomizeUploadData drsCustomizeUploadData : drsCustomizeUploadDataList) {

            JSONObject jsonObject = JSONObject.parseObject(drsCustomizeUploadData.getRequestJsonData());
            List<EventPushData> eventPushDataList = jsonObject.getJSONArray("eventList").toJavaList(EventPushData.class);

            if (eventPushDataList != null && !CollectionUtils.isEmpty(eventPushDataList)) {
                for (EventPushData eventPushData : eventPushDataList) {
                    String serialNo = eventPushData.getSerialNo();

                    //根据serialNo查询明细表
                    List<BQifuUploadDataOriginal> uploadDataOriginalList = qiFuAiEventPushService.getQiFuUploadDataOriginalBySerialNo(serialNo);
                    if (!CollectionUtils.isEmpty(uploadDataOriginalList)) {
                        BQifuUploadDataOriginal bqifuUploadDataOriginal = uploadDataOriginalList.get(0);
                        bqifuUploadDataOriginal.setEventType(eventPushData.getEventType());
                        bqifuUploadDataOriginal.setSerialNo(serialNo);
                        bqifuUploadDataOriginal.setTemplateNo(eventPushData.getTemplateNo());
                        bqifuUploadDataOriginal.setFlowNo(eventPushData.getFlowNo());
                        resultList.add(bqifuUploadDataOriginal);
                    }
                }
            }
        }
        qiFuAiEventPushService.queryCallMessage(resultList);
        return resultList;
    }
}

