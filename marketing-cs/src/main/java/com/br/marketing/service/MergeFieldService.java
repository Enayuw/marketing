package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * Merge Field interface
 * @Date 2024/12/5 21:31
 */
public interface MergeFieldService {

    /**
     * 合并上传明细表中字段和转化明细表中字段
     * @Date 2024/12/27 19:20
     * @param json 返回结果
     * @param transfer 转化明细对象
     * @param syncUser 上传明细对象
     * @param encryptionType 加密类型
     */
    void mergeUploadAndTransfer(JSONObject json, MarketingTransferSyncUser transfer, MarketingSyncUser syncUser
            , Integer encryptionType);
    /**
     * 将 转化明细表中时间格式的:SSS 给替换成""
     * @Date 2024/12/27 19:23
     * @param transfer 转化明细表对象
     */
    void formatSSSTransfer(MarketingTransferSyncUser transfer);

    /**
     * 处理三要素加密的方法
     * @Date 2024/12/27 19:21
     * @param content 待加密字段
     * @param encryptionType 加密类型
     * @return String 加密后的结果
     */
    String get3keyValue(String content, Integer encryptionType);
}
