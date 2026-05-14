package com.br.marketing.datarelayservice.context;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.speedconfig.MarketingCommonConfig;

/**
 * 同程 relay 当前请求使用的「tcyr 服务配置」JSON（与 tcyrServerConfig 同结构：tcPublicKey、brPrivateKey、验签等）。
 * <p>标准入口在 Controller 中调用 {@link #setMain(MarketingCommonConfig)}；withoutSign 测试入口调用
 * {@link #setTestOrFallback(MarketingCommonConfig)}。Processor 中通过 {@link #resolve(MarketingCommonConfig)} 读取。</p>
 */
public final class TcRelayServerConfigContext {

    private static final ThreadLocal<JSONObject> HOLDER = new ThreadLocal<>();

    private TcRelayServerConfigContext() {
    }

    /**
     * 绑定正式 Speed：{@code tcyrServerConfig}。
     */
    public static void setMain(MarketingCommonConfig marketingCommonConfig) {
        HOLDER.set(marketingCommonConfig.getTcyrServerConfig());
    }

    /**
     * 绑定测试 Speed：{@code tcyrRelayTestServerConfig}；若为空则回退正式 {@code tcyrServerConfig}。
     */
    public static void setTestOrFallback(MarketingCommonConfig marketingCommonConfig) {
        JSONObject test = marketingCommonConfig.getTcyrRelayTestServerConfig();
        if (test != null && !test.isEmpty()) {
            HOLDER.set(test);
        } else {
            HOLDER.set(marketingCommonConfig.getTcyrServerConfig());
        }
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 若当前线程已绑定则使用绑定值，否则使用正式 {@code tcyrServerConfig}（兼容未显式 set 的调用）。
     */
    public static JSONObject resolve(MarketingCommonConfig marketingCommonConfig) {
        JSONObject local = HOLDER.get();
        if (local != null && !local.isEmpty()) {
            return local;
        }
        return marketingCommonConfig.getTcyrServerConfig();
    }
}
