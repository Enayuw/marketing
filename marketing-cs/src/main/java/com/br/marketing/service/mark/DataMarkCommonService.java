package com.br.marketing.service.mark;

import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.es.bean.MarketingHistory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 数据打标公共接口
 * @author hedongshuo
 * @date 2025/2/21 12:58
 **/
public interface DataMarkCommonService {

    /**
     * @param apiCode
     * @param scoreDate yyyy-MM-dd
     * @return com.br.marketing.entity.StraHisFile
     * @description 获取当天最新的跑分文件记录
     * @author hedongshuo
     * @date 2025/2/21 12:59
     **/
    public StraHisFile getStraHisFile(String apiCode, String scoreDate);

    /**
     * @param apiCode
     * @param batchNumber
     * @param id
     * @param cellLogs
     * @param esPageSize
     * @param isPlainText
     * @return List<MarketingHistory>
     * @description 查询es，获取跑分分值
     * @author hedongshuo
     * @date 2025/2/21 16:03
     **/
    public List<MarketingHistory> getScoreWithEs(String apiCode, String batchNumber, Long id,
                                                 List<String> cellLogs, Integer esPageSize, Boolean isPlainText,
                                                 List<StraHisFile> straHisFiles);

    /**
     * @description 根据标记类型
     * @param apiCode
     * @param markType
     * @return java.util.List<com.br.marketing.entity.DataMarkConfig>
     * @author hedongshuo
     * @date 2025/2/21 17:48
     **/
    public List<DataMarkConfig> getMarkConfigs(String apiCode, Integer markType);

    public Boolean isMatch(Map<String, Object> scoreMap, String condition);

    public ThreadPoolExecutor getThreadPoolExecutor(Boolean isUsedByEs);

    public void modifyCorePoolSize(ThreadPoolExecutor poolExecutor, Boolean isUsedByEs);

    public void threadPoolShutDown(ThreadPoolExecutor threadPool, String logPrefix);
}
