package com.br.marketing.service.qifu;

import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.BQifuUploadDataOriginal;

import java.util.List;

/**
 * 奇富AI清洗Service（从b_qifu_upload_data_original表查询数据）
 */
public interface QiFuAiCleanService {
    /**
     * 从b_qifu_upload_data_original表查询数据并清洗组装调用上传接口入库
     */
    void aiCleanProcessFromOriginal();

    /**
     * 处理BQifuUploadDataOriginal数据的上传
     */
    void pushProcessForOriginal(List<BQifuUploadDataOriginal> dataList, String operateType);

    /**
     * 从实时卷查询结果构建 MarketingPreUserDetailDTO 列表（复用 buildNewListDto 逻辑）
     */
    List<MarketingPreUserDetailDTO> buildListFromCallRealTimeDetails(List<CallRealTimeDTO> dataDetails);
}
