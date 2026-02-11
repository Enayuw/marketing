package com.br.marketing.bridge.job.xyf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XyfSubmitDetail;
import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.entity.XyfSubmitRecordExample;
import com.br.marketing.enums.XyfSyncStatusEnum;
import com.br.marketing.mapper.XyfSubmitDetailMapper;
import com.br.marketing.mapper.XyfSubmitRecordMapper;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 信用飞外呼数据：解析 contactList 入库明细表，并调用 uploadClean 上传清洗
 */
@Component
@Slf4j
public class XyfSyncDataCleanJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【信用飞-上传数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XyfSubmitRecordMapper xyfSubmitRecordMapper;

    @Resource
    private XyfSubmitDetailMapper xyfSubmitDetailMapper;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE + "调度开始");
            action();
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    /**
     * 1) 查询 sync_status=未上传 的 b_xyf_submit_record
     * 2) 解析 contactlist 写入 b_xyf_submit_detail（幂等：batch_id+job_id 唯一）
     * 3) 按批次查明细，调用 generalDataCleanService.uploadClean
     * 4) 成功则更新 record.sync_status=上传成功，失败则=上传失败
     */
    private void action() {
        //1.查询需要上传的record
        XyfSubmitRecordExample example = new XyfSubmitRecordExample();
        example.createCriteria().andSyncStatusEqualTo(XyfSyncStatusEnum.SYNC_WAIT.getCode());
        example.setOrderByClause("id asc");
        List<XyfSubmitRecord> recordList = xyfSubmitRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }
        //2.获取需要映射的扩展字段
        List<String> extendFields = marketingCommonConfig.getXyfSyncExtendFields();
        for (XyfSubmitRecord record : recordList) {
            try {
                processRecord(record, extendFields);
            } catch (Exception e) {
                log.warn(TITLE + "处理批次失败 batchId={}, e={}", record.getBatchId(), e.getMessage(), e);
                updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            }
        }
    }

    private void processRecord(XyfSubmitRecord record, List<String> extendFields) throws Exception {
        //1.更新为1-上传中
        updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNCING.getCode());
        //2.解析contactList为detail，并保存
        String batchId = record.getBatchId();
        String contactListStr = record.getContactList();
        if (contactListStr == null || contactListStr.trim().isEmpty()) {
            log.warn(TITLE + "batchId={} contactlist 为空，标记失败", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        List<XyfSubmitDetail> detailList = parseAndSaveDetails(record);
        if (CollectionUtils.isEmpty(detailList)) {
            log.warn(TITLE + "batchId={} 解析后无有效明细", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        //3.只保留isSync = 1的detail
        List<XyfSubmitDetail> syncDetails = detailList.stream()
                .filter(d -> d.getIsSync() != null && d.getIsSync() == Constants.YES)
                .collect(Collectors.toList());
        if (syncDetails.size() < detailList.size()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(), "存在异常明细，请关注！", TITLE));
        }
        //4.组装上传数据，调用上传接口

        // 2. 明细转 List<JSONObject> 调用 uploadClean
        List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(detailList), JSONObject.class);
        Result<?> callResult = generalDataCleanService.uploadClean(jsonObjectList, record.getApiCode());
        log.warn(TITLE + "batchId={}, apiCode={}, uploadClean 结果 code={}, isSuccess={}, msg={}",
                batchId, record.getApiCode(), callResult != null ? callResult.getCode() : null,
                callResult != null && callResult.isSuccess(), callResult != null ? callResult.getMessage() : null);

        if (callResult != null && callResult.isSuccess()) {
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_SUCCESS.getCode());
        } else {
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
        }
    }

    /**
     * 解析 contactlist JSON 数组，写入 b_xyf_submit_detail，返回本批写入的明细列表（用于 uploadClean）
     */
    private List<XyfSubmitDetail> parseAndSaveDetails(XyfSubmitRecord record) {
        String batchId = record.getBatchId();
        List<XyfSubmitDetail> list = new ArrayList<>();
        String contactListStr = record.getContactList();
        if (contactListStr == null || contactListStr.trim().isEmpty()) {
            log.warn(TITLE + "batchId={} contactList为空！", batchId);
            return list;
        }
        List<XyfSubmitDetail> detailList = JSON.parseArray(contactListStr, XyfSubmitDetail.class);
        if (!CollectionUtils.isEmpty(detailList)) {
            detailList.forEach(d -> {
                d.setBatchId(batchId);
                // 仅当 phone、productType、jobId 均有值时 isSync=1，否则为 0
                boolean allPresent = StringUtils.isNotBlank(d.getPhone())
                        && StringUtils.isNotBlank(d.getProductType())
                        && StringUtils.isNotBlank(d.getJobId());
                d.setIsSync(allPresent ? Constants.YES : Constants.NO);
            });
            xyfSubmitDetailMapper.batchSave(detailList);
        }
        return list;
    }

    private void updateRecordSyncStatus(Long recordId, int syncStatus) {
        XyfSubmitRecord up = new XyfSubmitRecord();
        up.setId(recordId);
        up.setSyncStatus(syncStatus);
        up.setUpdateTime(new Date());
        xyfSubmitRecordMapper.updateByPrimaryKeySelective(up);
    }
}
