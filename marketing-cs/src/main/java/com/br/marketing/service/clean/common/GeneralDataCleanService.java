package com.br.marketing.service.clean.common;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import java.util.List;

/**
 * @description 通用数据清洗
 * 可支持上传、转化，可支持不同客户的定制接口的上传和转化清洗
 * @author hedongshuo
 * @date 2025/4/24 11:40
 **/
public interface GeneralDataCleanService {

    /**
     * @description common上传清洗
     * @param data
     * @param apiCode
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/4/24 11:45
     **/
    Result uploadClean(List<JSONObject> data ,String apiCode) throws NoSuchFieldException;

    /**
     * @description common转化清洗
     * @param data
     * @param apiCode
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/4/24 11:45
     **/
    Result transferClean(List<JSONObject> data, String apiCode);

    /**
     * @description bizAction上传清洗
     * @param data
     * @param apiCode
     * @param bizAction
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/4/24 13:37
     **/
    Result uploadClean(List<JSONObject> data, String apiCode, String bizAction) throws NoSuchFieldException;

    /**
     * @description bizAction转化清洗
     * @param data
     * @param apiCode
     * @param bizAction
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/4/24 13:37
     **/
    Result transferClean(List<JSONObject> data, String apiCode, String bizAction);

}
