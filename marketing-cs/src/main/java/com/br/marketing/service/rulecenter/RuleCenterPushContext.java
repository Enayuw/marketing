package com.br.marketing.service.rulecenter;


import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.StraHisFile;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 推送上下文
 */
@Data
public class RuleCenterPushContext {
    /**
     * 推送任务主信息
     */
    private CustomerInfoPushMain customerInfoPushMain;

    /**
     * 推送批次信息
     */
    private List<CustomerInfoPushBatch> customerInfoPushBatches;

    /**
     * 历史文件信息
     */
    private List<StraHisFile> straHisFiles;

    /**
     * 批次号列表
     */
    private List<String> batchNumbers;

    /**
     * 文件ID列表
     */
    private List<Long> fileIds;

    /**
     * 3K加密类型
     */
    private Integer encryptType;

    /**
     * 是否单分片处理
     */
    private Boolean singlePartition;

    /**
     * 分片数量
     */
    private Integer partitionCount;

    /**
     * 分片数据量
     */
    private Map<Integer, Integer> partitionDataCount = new HashMap<>();

    /**
     * ES线程池
     */
    private ThreadPoolExecutor esThreadPool;

    /**
     * 推送线程池
     */
    private ThreadPoolExecutor pushThreadPool;

    /**
     * 标签对象
     */
    private Object labelObject;

    /**
     * 是否使用ES标记
     */
    private Boolean markWithEsFlag;

    /**
     * 上传数据条件集合
     */
    private List<Map<String,String>> DataConditionList;

    /**
     * 扩展字段存储
     */
    private JSONObject extendDataJson;

}
