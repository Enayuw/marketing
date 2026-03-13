package com.br.marketing.service.xyf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.entity.XyfSubmitRecordExample;
import com.br.marketing.enums.XyfSyncStatusEnum;
import com.br.marketing.mapper.XyfSubmitRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.xyf.XyfSyncDataCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 信用飞外呼数据上传清洗服务实现
 *
 * @Description 解析 plain_data，过滤后直接组装上传并推送（不再落库 b_xyf_submit_detail）
 * @Author system
 * @CreateTime 2025
 */
@Service
@Slf4j
public class XyfSyncDataCleanServiceImpl implements XyfSyncDataCleanService {

    private static final String TITLE = "【信用飞-上传数据清洗任务】";

    /** plain_data 顶层保留字段，除此以外的多传字段放入扩展字段1 */
    private static final Set<String> DATA_RESERVED = new HashSet<>(Arrays.asList(
            "corpCode", "accessToken", "strategyId", "batchDate", "batchId", "contactList"));

    /** contactList 单项保留字段，除此以外的多传字段放入扩展字段1；jobData 整键不放入，其内部字段会展开放入 */
    private static final Set<String> CONTACT_RESERVED = new HashSet<>(Arrays.asList(
            "prePhone", "phone", "productType", "jobId", "jobData"));

    @Resource
    private XyfSubmitRecordMapper xyfSubmitRecordMapper;

    @Resource
    private PushInfoService pushInfoService;

    @Override
    public List<XyfSubmitRecord> listWaitRecords() {
        XyfSubmitRecordExample example = new XyfSubmitRecordExample();
        example.createCriteria().andSyncStatusEqualTo(XyfSyncStatusEnum.SYNC_WAIT.getCode());
        example.setOrderByClause("id asc");
        return xyfSubmitRecordMapper.selectByExample(example);
    }

    /**
     * 处理单条 record：解析 plain_data → 过滤（phone/productType/jobId 非空）→ 组装上传数据（多传字段入扩展字段1）→ 直接推送
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRecord(XyfSubmitRecord record) {
        // 1. 更新为上传中
        updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNCING.getCode());
        String batchId = record.getBatchId();
        String plainData = record.getPlainData();
        if (StringUtils.isBlank(plainData)) {
            log.warn(TITLE + "batchId={} plain_data 为空，标记失败", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        // 2. 解析 plain_data，过滤并组装上传数据
        UploadDataDTO uploadDataDTO = parsePlainDataAndBuildUpload(record, plainData);
        if (uploadDataDTO == null) {
            log.warn(TITLE + "batchId={} 解析后无有效明细", batchId);
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            return;
        }
        // 3. 调用上传接口
        Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        if (pushResult != null && pushResult.isSuccess()) {
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_SUCCESS.getCode());
        } else {
            updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
        }
    }

    @Override
    public void updateRecordSyncStatus(Long recordId, int syncStatus) {
        XyfSubmitRecord up = new XyfSubmitRecord();
        up.setId(recordId);
        up.setSyncStatus(syncStatus);
        up.setUpdateTime(new Date());
        xyfSubmitRecordMapper.updateByPrimaryKeySelective(up);
    }

    /**
     * 解析 plain_data：过滤 phone/productType/jobId 为空的项；data 层与 contact 项多传字段均放入 reserveField1
     */
    private UploadDataDTO parsePlainDataAndBuildUpload(XyfSubmitRecord record, String plainData) {
        JSONObject dataRoot;
        try {
            dataRoot = JSON.parseObject(plainData);
        } catch (Exception e) {
            log.warn(TITLE + "batchId={} plain_data 解析失败", record.getBatchId(), e);
            return null;
        }
        if (dataRoot == null) {
            return null;
        }
        Object contactListObj = dataRoot.get("contactList");
        JSONArray contactArray = toJsonArray(contactListObj);
        if (contactArray == null || contactArray.isEmpty()) {
            return null;
        }
        int contactListTotal = contactArray.size();
        // data 层多传字段（除 corpCode、accessToken、strategyId、batchDate、batchId、contactList）
        JSONObject dataLevelExtras = new JSONObject();
        for (String key : dataRoot.keySet()) {
            if (!DATA_RESERVED.contains(key)) {
                dataLevelExtras.put(key, dataRoot.get(key));
            }
        }
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (int i = 0; i < contactArray.size(); i++) {
            JSONObject item = contactArray.getJSONObject(i);
            String phone = item.getString("phone");
            String productType = item.getString("productType");
            String jobId = item.getString("jobId");
            if (StringUtils.isBlank(phone) || StringUtils.isBlank(productType) || StringUtils.isBlank(jobId)) {
                continue;
            }
            MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
            dto.setCell(phone);
            dto.setCustNum(jobId);
            dto.setOperateType("6");
            String name = null;
            if (item.containsKey("jobData")) {
                Object jd = item.get("jobData");
                if (jd instanceof JSONObject) {
                    name = ((JSONObject) jd).getString("username");
                }
            }
            if (StringUtils.isBlank(name)) {
                name = item.getString("username");
            }
            dto.setName(name);
            JSONObject rf = new JSONObject();
            rf.put("strategyCode", record.getStrategyId());
            rf.put("userType", productType);
            rf.putAll(dataLevelExtras);
            // jobData 中所有字段洗到扩展字段
            if (item.containsKey("jobData")) {
                Object jd = item.get("jobData");
                if (jd instanceof JSONObject) {
                    JSONObject jobDataObj = (JSONObject) jd;
                    for (String key : jobDataObj.keySet()) {
                        rf.put(key, jobDataObj.get(key));
                    }
                }
            }
            for (String key : item.keySet()) {
                if (!CONTACT_RESERVED.contains(key)) {
                    rf.put(key, item.get(key));
                }
            }
            dto.setReserveField1(rf.toJSONString());
            syncUsers.add(dto);
        }
        if (syncUsers.isEmpty()) {
            return null;
        }
        if (syncUsers.size() < contactListTotal) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(),
                    "batchId=" + record.getBatchId() + " 存在被过滤明细，原始=" + contactListTotal + "，过滤后=" + syncUsers.size(), TITLE));
        }
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(record.getBatchDate());
        marketingPreUserDTO.setRequestId(record.getBatchId());
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(record.getApiCode());
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private static JSONArray toJsonArray(Object o) {
        if (o == null) {
            return new JSONArray();
        }
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        if (o instanceof String) {
            try {
                return JSON.parseArray((String) o);
            } catch (Exception e) {
                return new JSONArray();
            }
        }
        return new JSONArray();
    }
}
