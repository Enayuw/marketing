package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;

public interface PushDataService {
    Result pushDassData(Long id);

    Result pushDassTransferData(Long id);

    Result pushDassTransferIbu(Long id);

    Result pushSevenTransferData(Long id);

    Result pushHaierData();

    Result queryHaierData();

    /**
     * 目前该方法不适用当前需求
     * @param id
     * @return
     */
    @Deprecated
    Result<Boolean> pushHaierTransferData(Long id);

    /**
     * 单条推电销 a/b 一天推一条,true-->推;false-->不推
     * @param apiCode
     * @param custNum
     * @param status
     * @return
     */
    Boolean pushShDXSingleMutex(String apiCode, String custNum, String status, String userType);

    /**
     * 推送SftpToDb数据
     * @param localid
     * @return
     */
    Result<Boolean> pushSftpToDbData(Long localid);

    /**
     * 推送携程营销数据
     * @param data
     * @return
     */
    Result<Boolean> pushXieChengToDbData(String data);

    /**
     * 推送携程短信撞库数据
     * @param data
     * @return
     */
    void pushXieChengSmsCollidingToDbData(String data);

    /**
     * 推送携程短信撞库数据Version2
     */
    void pushXieChengSmsCollidingToDbDataVt(Long localId);

    String getHaierRequestId(String type);

    Boolean isPushDassWithCallGrade(String ruleLabel,String intentionGrade);

    String getStatusByGrade(String ruleLabel,String intentionGrade);


    /**
     * 海尔撞库数据推送
     *
     * @param localId 本地文件id
     * @author senyang.zheng
     * @date 2023/12/23
     */
    void pushHaierCollidingData(Long localId);

    /**
     * 推送携程短信撞库数据重试
     * @return
     */
    void retryPushXieChengSmsCollidingToDbData(Long localId);

    Result pushCsosDassData(Long id);

    Result pushUpdateDassData(Long id);

    Result pushWeiZhongDassData(Long id);

    /**
     * 推送特殊文件数据到Dass
     * @param id 文件ID
     * @param filePrefix 匹配的文件名前缀
     * @param prefixConfig 该前缀对应的配置
     * @return 推送结果
     */
    Result pushSpecialDassData(Long id, String filePrefix, JSONObject prefixConfig);

    /**
     * 推送动态分组文件数据到Dass（支持配置化分组字段）
     * @param id 文件ID
     * @param filePrefix 匹配的文件名前缀
     * @param prefixConfig 该前缀对应的配置（包含groupByField和mergeConfig）
     * @return 推送结果
     */
    Result pushDynamicGroupDassData(Long id, String filePrefix, JSONObject prefixConfig);
}
