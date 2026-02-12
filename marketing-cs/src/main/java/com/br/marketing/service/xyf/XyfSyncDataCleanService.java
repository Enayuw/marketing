package com.br.marketing.service.xyf;

import com.br.marketing.entity.XyfSubmitRecord;

import java.util.List;

/**
 * 信用飞外呼数据上传清洗服务
 *
 * @Description 解析 contactList、落库明细、组装上传数据并推送
 * @Author system
 * @CreateTime 2025
 */
public interface XyfSyncDataCleanService {

    /**
     * 查询 sync_status=未上传 的记录，按 id 升序
     *
     * @return 待上传的 record 列表，无则返回空列表
     */
    List<XyfSubmitRecord> listWaitRecords();

    /**
     * 处理单条 record：解析 plain_data、过滤（phone/productType/jobId 非空）、组装上传并推送，更新 record 状态
     */
    void processRecord(XyfSubmitRecord record);

    /**
     * 更新 record 同步状态（如处理异常时由调用方标记失败）
     *
     * @param recordId   记录主键
     * @param syncStatus 状态 0-未上传 1-上传中 2-上传成功 3-上传失败
     */
    void updateRecordSyncStatus(Long recordId, int syncStatus);
}
