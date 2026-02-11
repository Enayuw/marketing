package com.br.marketing.service.xyf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.XyfSubmitDetail;
import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.entity.XyfSubmitRecordExample;
import com.br.marketing.enums.XyfSyncStatusEnum;
import com.br.marketing.mapper.XyfSubmitDetailMapper;
import com.br.marketing.mapper.XyfSubmitRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.xyf.XyfSyncDataCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 信用飞外呼数据上传清洗服务实现
 *
 * @Description 解析 contactList、jobId 去重、落库明细、组装上传并推送
 * @Author system
 * @CreateTime 2025
 */
@Service
@Slf4j
public class XyfSyncDataCleanServiceImpl implements XyfSyncDataCleanService {

    private static final String TITLE = "【信用飞-上传数据清洗任务】";

    @Resource
    private XyfSubmitRecordMapper xyfSubmitRecordMapper;

    @Resource
    private XyfSubmitDetailMapper xyfSubmitDetailMapper;

    @Resource
    private PushInfoService pushInfoService;

    /**
     * 查询 sync_status=未上传 的记录，按 id 升序
     */
    @Override
    public List<XyfSubmitRecord> listWaitRecords() {
        XyfSubmitRecordExample example = new XyfSubmitRecordExample();
        example.createCriteria().andSyncStatusEqualTo(XyfSyncStatusEnum.SYNC_WAIT.getCode());
        example.setOrderByClause("id asc");
        return xyfSubmitRecordMapper.selectByExample(example);
    }

    /**
     * 处理单条 record：更新为上传中 → 解析 contactList（jobId 去重）→ 组装上传数据 → 落库明细 → 推送 → 更新状态/总量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRecord(XyfSubmitRecord record, List<String> extendFields) {
        // 1. 更新为 1-上传中
        updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNCING.getCode());
        String batchId = record.getBatchId();
        String contactListStr = record.getContactList();
        if (contactListStr == null || contactListStr.trim().isEmpty()) {
            log.warn(TITLE + "batchId={} contactlist 为空，标记失败", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        // 2. 解析 contactList 为 detail（contactListTotal 为 contactList 原始条数，用于 record.total）
        ParseDetailResult parseResult = parseAndSaveDetails(record);
        List<XyfSubmitDetail> detailList = parseResult.detailList;
        int contactListTotal = parseResult.contactListTotal;
        // 3. 过滤异常数据，组装上传数据
        UploadDataDTO uploadDataDTO = buildSyncData(record, detailList, extendFields);
        long syncFailCount = detailList.stream()
                .filter(d -> d.getIsSync() == null || Constants.NO.equals(d.getIsSync()))
                .count();
        if (syncFailCount > 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(), "存在异常明细，请关注！", TITLE));
        }
        // 4. 插入明细数据
        xyfSubmitDetailMapper.batchSave(detailList);
        if (uploadDataDTO == null) {
            log.warn(TITLE + "batchId={} 解析后无有效明细", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        // 5. 调用上传接口
        Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        if (pushResult != null && pushResult.isSuccess()) {
            int syncTotal = (int) detailList.stream()
                    .filter(d -> d.getIsSync() != null && d.getIsSync() == Constants.YES)
                    .count();
            updateRecordSyncSuccess(record.getId(), contactListTotal, syncTotal);
        } else {
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
        }
    }

    /**
     * 仅更新 record 的 sync_status 与 update_time
     */
    @Override
    public void updateRecordSyncStatus(Long recordId, int syncStatus) {
        XyfSubmitRecord up = new XyfSubmitRecord();
        up.setId(recordId);
        up.setSyncStatus(syncStatus);
        up.setUpdateTime(new Date());
        xyfSubmitRecordMapper.updateByPrimaryKeySelective(up);
    }

    /**
     * 上传成功后更新 record：total、syncTotal、sync_status、update_time
     */
    private void updateRecordSyncSuccess(Long recordId, int total, int syncTotal) {
        XyfSubmitRecord up = new XyfSubmitRecord();
        up.setId(recordId);
        up.setTotal(total);
        up.setSyncTotal(syncTotal);
        up.setSyncStatus(XyfSyncStatusEnum.SYNC_SUCCESS.getCode());
        up.setUpdateTime(new Date());
        xyfSubmitRecordMapper.updateByPrimaryKeySelective(up);
    }

    /**
     * 按 isSync=1 的明细组装 MarketingPreUserDTO，extendFields 从 jobData 写入 reserveField1
     *
     * @return 组装好的上传 DTO，无有效明细时返回 null
     */
    private UploadDataDTO buildSyncData(XyfSubmitRecord record, List<XyfSubmitDetail> details, List<String> extendFields) {
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (XyfSubmitDetail detail : details) {
            if (!Constants.YES.equals(detail.getIsSync())) {
                continue;
            }
            JSONObject jobData = JSON.parseObject(detail.getJobData());
            if (jobData == null) {
                detail.setIsSync(Constants.NO);
                continue;
            }
            MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
            dto.setCell(detail.getPhone());
            dto.setName(jobData.getString("userName"));
            dto.setCustNum(detail.getJobId());
            dto.setOperateType("6");
            JSONObject rf = new JSONObject();
            rf.put("strategyCode", record.getStrategyId());
            rf.put("userType", detail.getProductType());
            if (!CollectionUtils.isEmpty(extendFields) && jobData != null) {
                for (String key : extendFields) {
                    if (jobData.containsKey(key)) {
                        rf.put(key, jobData.get(key));
                    }
                }
            }
            dto.setReserveField1(rf.toJSONString());
            syncUsers.add(dto);
        }
        if (CollectionUtils.isEmpty(syncUsers)) {
            return null;
        }
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(record.getBatchId());
        marketingPreUserDTO.setRequestId(record.getBatchId());
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(record.getApiCode());
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    /**
     * 解析 contactList JSON：jobId 去重、设置 batchId/isSync，返回明细列表及 contactList 原始条数
     */
    private ParseDetailResult parseAndSaveDetails(XyfSubmitRecord record) {
        String batchId = record.getBatchId();
        List<XyfSubmitDetail> list = new ArrayList<>();
        String contactListStr = record.getContactList();
        if (contactListStr == null || contactListStr.trim().isEmpty()) {
            log.warn(TITLE + "batchId={} contactList为空！", batchId);
            return new ParseDetailResult(list, 0);
        }
        list = JSON.parseArray(contactListStr, XyfSubmitDetail.class);
        if (list == null) {
            list = new ArrayList<>();
        }
        int contactListTotal = list.size();
        if (!CollectionUtils.isEmpty(list)) {
            List<XyfSubmitDetail> withJobId = list.stream()
                    .filter(d -> StringUtils.isNotBlank(d.getJobId()))
                    .collect(Collectors.toList());
            int beforeDedup = withJobId.size();
            list = new ArrayList<>(withJobId.stream()
                    .collect(Collectors.toMap(
                            XyfSubmitDetail::getJobId,
                            d -> d,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ))
                    .values());
            if (list.size() < beforeDedup) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(),
                        "batchId=" + batchId + " 存在重复 jobId，去重前=" + beforeDedup + "，去重后=" + list.size(), TITLE));
            }
            list.forEach(d -> {
                d.setBatchId(batchId);
                boolean allPresent = StringUtils.isNotBlank(d.getPhone())
                        && StringUtils.isNotBlank(d.getProductType())
                        && StringUtils.isNotBlank(d.getJobId());
                d.setIsSync(allPresent ? Constants.YES : Constants.NO);
            });
        }
        return new ParseDetailResult(list, contactListTotal);
    }

    /**
     * 解析结果：去重后的明细列表 + contactList 原始条数（用于 record.total）
     */
    private static class ParseDetailResult {
        final List<XyfSubmitDetail> detailList;
        final int contactListTotal;

        ParseDetailResult(List<XyfSubmitDetail> detailList, int contactListTotal) {
            this.detailList = detailList;
            this.contactListTotal = contactListTotal;
        }
    }
}
