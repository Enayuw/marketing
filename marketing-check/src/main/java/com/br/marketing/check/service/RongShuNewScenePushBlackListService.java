package com.br.marketing.check.service;

/**
 * 榕树新场景：定时 Job 侧「上传 + 转化 T-N」两路外呼黑名单推送（blackData）。
 * <p>转化 applyResult=1（T 日永久拉黑）不在此接口，见实时或其它模块。</p>
 */
public interface RongShuNewScenePushBlackListService {

    /**
     * 按 Speed {@code rongShuNewScenePushBlackListApiCodes} 遍历 apiCode；
     * 每个 apiCode：上传 userType=202（当天 applet_date）→ 转化 {@code request_data}=CURDATE()-N（无 apply 条件）。
     */
    void executePushBlackList();
}
