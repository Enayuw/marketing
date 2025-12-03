package com.br.marketing.monkey.service.qifu;

import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface QiFuAiEventPushService {

    void assembleRealTimeUploadDataOriginal();

    /**
     * 根据同步状态筛选数据
     *
     * @param syncStatus 同步状态
     * @return 数据集
     */
    List<DrsCustomizeUploadData> getDrsCustomizeUploadDataBySyncStatus(Integer syncStatus, Long minId, Integer pageSize);

    /**
     * 根据serialNo在明细表中筛选最新一条数据
     *
     * @param serialNo serialNo
     * @return 最新一条数据
     */
    List<BQifuUploadDataOriginal> getQiFuUploadDataOriginalBySerialNo(String serialNo);

    void updateSyncStatusById(String id, Integer syncStatus);

    void insertRealTimeData(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList);

    List<BQifuUploadDataOriginal> queryCallMessage(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList);

    /**
     * 在事务中处理批次数据：插入数据 + 更新状态
     *
     * @param resultList 待插入的数据列表
     * @param drsCustomizeUploadData 待更新状态的数据
     */
    void processBatchData(List<BQifuUploadDataOriginal> resultList,
                                       DrsCustomizeUploadData drsCustomizeUploadData);
}
