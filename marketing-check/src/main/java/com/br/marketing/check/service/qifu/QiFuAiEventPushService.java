package com.br.marketing.check.service.qifu;

import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface QiFuAiEventPushService {

    /**
     * 根据同步状态筛选数据
     *
     * @param syncStatus 同步状态
     * @return 数据集
     */
    List<DrsCustomizeUploadData> getDrsCustomizeUploadDataBySyncStatus(Integer syncStatus);

    /**
     * 根据serialNo在明细表中筛选最新一条数据
     *
     * @param serialNo serialNo
     * @return 最新一条数据
     */
    List<BQifuUploadDataOriginal> getQiFuUploadDataOriginalBySerialNo(String serialNo);



    void queryCallMessage(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList);
}
