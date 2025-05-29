package com.br.marketing.service;

/**
 * @author peng.kang
 * @description: 手机号收集
 * @date 2025/5/24 10:01
 */
public interface YunKeService {
    /**
    * @description:云客服务手机号收集
    */
    void phoneCollectByApiCode();
    /**
     * @description:云客服务机型获取
     */
    void getDeviceType();
}
