package com.br.marketing.check.service.qifu;

/**
 * 奇富AI清洗Service（从b_qifu_upload_data_original表查询数据）
 */
public interface QiFuAiCleanService {
    /**
     * 从b_qifu_upload_data_original表查询数据并清洗组装调用上传接口入库
     */
    void aiCleanProcessFromOriginal();

    void aiRealTimeCleanProcessFromOriginal();
}

