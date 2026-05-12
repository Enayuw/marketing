package com.br.marketing.monkey.config;

import com.br.marketing.common.utils.http.HttpBaseUrlHelper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 灵霄宝殿同程 apiCode 指派（tcapiCodeAssign）HTTP 配置。
 * 在 application-*.yml 的 {@code otherConfig.tcyr-lingxiao} 下维护（与 alarm、ningbo 等并列）。
 * Spring Boot 3.4+ 对 {@code @ConfigurationProperties} 的 prefix 要求全小写 kebab-case，{@code otherConfig} 含大写字母不合法，
 * 故本类使用 {@code @Value} 绑定，行为与原先一致。
 * 回调 marketing-inner 的地址在灵霄 roster-gods 配置 {@code otherConfig.marketing.tcyr-api-code-fill-url}，不由本服务传入。
 * 候选 apiCode 列表从 Speed {@link com.br.marketing.speedconfig.MarketingCommonConfig#getTcyrMatchCandidateApiCodes()} 读取；
 * 有权限选码的用户集合从 {@link com.br.marketing.speedconfig.MarketingCommonConfig#getTcyrApiCodeAssignAuthorizedUsers()} 读取并随 tcapiCodeAssign 请求体字段 {@code authorizedUsers} 透传灵霄；
 * HTTP 调用见 {@link com.br.marketing.client.middleheaven.MiddleHeavenTcyrApiCodeMatchClient}。
 */
@Data
@Component
public class LingxiaoTcyrProperties {

    /**
     * 为 false 时定时任务直接跳过；未配置时默认 false。
     */
    @Value("${otherConfig.tcyr-lingxiao.enabled:false}")
    private boolean enabled;

    /**
     * 灵霄宝殿 middle-heaven-roster-gods 服务根地址（与 middle-heaven-web 中 {@code api.rosterGods.baseUrl} 一致），不含末尾 /；
     * 可写完整 {@code http(s)://host:port}，或仅 {@code host:port}（启动时自动补 {@code http://}）。
     */
    @Value("${otherConfig.tcyr-lingxiao.base-url:http://middle-heaven-roster-gods}")
    private String baseUrl;

    @PostConstruct
    void normalizeBaseUrl() {
        this.baseUrl = HttpBaseUrlHelper.ensureHttpScheme(this.baseUrl);
    }

    /**
     * 接口路径（相对 base-url），默认与灵霄 {@code TcApiCodeAssignController} 一致：
     * {@code /open/dingtalk/api-code-card/tcapiCodeAssign}。
     */
    @Value("${otherConfig.tcyr-lingxiao.assign-path:/open/dingtalk/api-code-card/tcapiCodeAssign}")
    private String assignPath;

    /**
     * 可选：Bearer Token，按灵霄实际鉴权填写。
     */
    @Value("${otherConfig.tcyr-lingxiao.bearer-token:}")
    private String bearerToken;

    /**
     * 单次任务最多推送的 batch 数。
     */
    @Value("${otherConfig.tcyr-lingxiao.batch-limit:30}")
    private int batchLimit;

    @Value("${otherConfig.tcyr-lingxiao.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${otherConfig.tcyr-lingxiao.read-timeout-ms:15000}")
    private int readTimeoutMs;
}
