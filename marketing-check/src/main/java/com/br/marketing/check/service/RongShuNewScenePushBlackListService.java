package com.br.marketing.check.service;

/**
 * 榕树新场景：上传 + 转化三路数据扫描并推送外呼黑名单（blackData）。
 */
public interface RongShuNewScenePushBlackListService {

    /**
     * 按 Speed 配置的 apiCode 列表执行；单 apiCode 内依次处理上传 202、转化当天 applyResult=1、转化 T-N request_data。
     */
    void executePushBlackList();
}
