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
     * @description 获取当天最新的跑分文件记录
     * @param apiCode
     * @return com.br.marketing.entity.StraHisFile
     * @author hedongshuo
     * @date 2025/2/21 12:59
     **/
    public StraHisFile getStraHisFile(String apiCode);

    /**
     * @param apiCode
     * @param batchNumber
     * @param id
     * @param cellLogs
     * @param esPageSize
     * @return List<MarketingHistory>
     * @description 查询es，获取跑分分值
     * @author hedongshuo
     * @date 2025/2/21 16:03
     **/
    public List<MarketingHistory> getScoreWithEs(String apiCode, String batchNumber, Long id, List<String> cellLogs, Integer esPageSize);

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

    public ThreadPoolExecutor getThreadPoolExecutor();

    public void threadPoolShutDown(ThreadPoolExecutor threadPool, String logPrefix);
}
