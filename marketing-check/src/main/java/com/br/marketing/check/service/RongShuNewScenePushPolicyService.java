package com.br.marketing.check.service;

/**
 * 榕树新场景自动化筛选决策推送（定时 Job 调用）。
 * <p>
 * 情况 1：转化表 request_data=T-1、if_apply=0、user_type ∈ {1,201,202,3}；情况 2：上传表 user_type=201、当日约 6 点批次窗口。
 * apiCode 列表与榕树新场景外呼黑名单共用 Speed 配置项 rongShuNewScenePushBlackListApiCodes。
 * </p>
 */
public interface RongShuNewScenePushPolicyService {

    void executePushPolicy();
}
