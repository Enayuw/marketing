package com.br.marketing.config.biz;

import com.br.marketing.client.middleheaven.MiddleHeavenTcyrApiCodeMatchClient;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.common.utils.http.HttpBaseUrlHelper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 灵霄宝殿 middle-heaven-roster-gods 同程 HTTP 配置（{@code otherConfig.tcyr-lingxiao}）。
 * <ul>
 *   <li>{@link MiddleHeavenTcyrApiCodeMatchClient#postTcApiCodeAssign} → {@link #assignPath}</li>
 *   <li>{@link MiddleHeavenTcyrApiCodeMatchClient#postTcDataCleanNotify} → {@link #cleanNotifyPath}</li>
 * </ul>
 * 共用 {@link #baseUrl}、{@link #bearerToken}、超时；仅路径与开关字段不同。
 */
@Data
@Component
public class LingxiaoTcyrProperties {

    /**
     * tcapiCodeAssign 定时任务开关（monkey）；未配置默认 false。
     */
    @Value("${otherConfig.tcyr-lingxiao.enabled:false}")
    private boolean enabled;

    @Value("${otherConfig.tcyr-lingxiao.base-url:http://middle-heaven-roster-gods}")
    private String baseUrl;

    @Value("${otherConfig.tcyr-lingxiao.assign-path:/open/dingtalk/api-code-card/tcapiCodeAssign}")
    private String assignPath;

    @Value("${otherConfig.tcyr-lingxiao.clean-notify-path:/open/dingtalk/api-code-card/tcDataCleanNotify}")
    private String cleanNotifyPath;

    @Value("${otherConfig.tcyr-lingxiao.bearer-token:}")
    private String bearerToken;

    @Value("${otherConfig.tcyr-lingxiao.batch-limit:30}")
    private int batchLimit;

    @Value("${otherConfig.tcyr-lingxiao.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${otherConfig.tcyr-lingxiao.read-timeout-ms:15000}")
    private int readTimeoutMs;

    /**
     * quickDeal record 开始清洗时是否异步调用 {@link #cleanNotifyPath}。
     */
    @Value("${otherConfig.tcyr-lingxiao.clean-notify-enabled:true}")
    private boolean cleanNotifyEnabled;

    @PostConstruct
    void normalizeBaseUrl() {
        this.baseUrl = HttpBaseUrlHelper.ensureHttpScheme(this.baseUrl);
    }
}
