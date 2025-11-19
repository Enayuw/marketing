package com.br.marketing.check.service.qifu;

import com.br.marketing.entity.BQifuUploadDataOriginal;

import java.util.List;

/**
 * 奇富查询外呼信息Service
 */
public interface QiFuQueryCallService {
    /**
     * 查询外呼信息
     */
    void queryCallMessage();

    void updateSelectStatus(List<BQifuUploadDataOriginal> dataList, Integer selectStatus);
}

