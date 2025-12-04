package com.br.marketing.datarelayservice.service;

import com.br.marketing.dto.zhongyuan.ZhongYuanBaseResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName ZhongYuanUploadDataService
 * @Description 中原消金
 * @Author kongbx
 * @Date 2025/11/14 11:19
 */
public interface ZhongYuanUploadDataService {

    /**
     * 用户登录接口
     *
     * @param jsonData 请求JSON数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    ZhongYuanBaseResponse<?> login(String jsonData, HttpServletRequest request);

    /**
     * 外呼上报接口
     *
     * @param jsonData 请求JSON数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    ZhongYuanBaseResponse<?> batchTask(String jsonData, HttpServletRequest request);

    /**
     * 场景变量信息接口
     *
     * @param jsonData 请求JSON数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    ZhongYuanBaseResponse<?> sceneVariable(String jsonData, HttpServletRequest request);

    /**
     * 批量外呼任务状态修改接口
     *
     * @param jsonData 请求JSON数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    ZhongYuanBaseResponse<?> status(String jsonData, HttpServletRequest request);

    /**
     * 外呼任务场景变量修改接口
     *
     * @param jsonData 请求JSON数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    ZhongYuanBaseResponse<?> changeSceneVariable(String jsonData, HttpServletRequest request);
}
