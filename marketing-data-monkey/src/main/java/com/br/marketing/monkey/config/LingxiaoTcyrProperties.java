package com.br.marketing.monkey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 灵霄宝殿同程 apiCode 匹配（tcapiCodeMatchOutPut）HTTP 配置。
 * 在 application-*.yml 的 {@code otherConfig.tcyr-lingxiao} 下补齐 base-url、path 等。
 * 候选 apiCode 列表从 Speed {@link com.br.marketing.speedconfig.MarketingCommonConfig#getTcyrMatchCandidateApiCodes()} 读取；
 * HTTP 调用见 {@link com.br.marketing.client.middleheaven.MiddleHeavenTcyrApiCodeMatchClient}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "otherConfig.tcyr-lingxiao")
public class LingxiaoTcyrProperties {

    /**
     * 为 false 时定时任务直接跳过（默认关闭，避免未配置地址时误调）。
     */
    private boolean enabled = false;

    /**
     * 灵霄宝殿服务根地址，不含末尾路径，例如 https://lingxiao.example.com
     */
    private String baseUrl = "";

    /**
     * 接口路径，默认与需求文档一致；若网关前缀不同可改。
     */
    private String matchOutPutPath = "/tcapiCodeMatchOutPut";

    /**
     * 可选：Bearer Token，按灵霄实际鉴权填写。
     */
    private String bearerToken = "";

    /**
     * 单次任务最多推送的 batch 数。
     */
    private int batchLimit = 30;

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 15000;
}
