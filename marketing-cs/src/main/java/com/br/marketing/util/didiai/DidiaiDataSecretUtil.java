package com.br.marketing.util.didiai;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 滴滴 AI 按配置解析 AES 用 {@code dataSecret} 的工具类。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>从 {@link MarketingCommonConfig#getDidiaiDataSecretMap()} 按 {@code appKey} 取 {@code dataSecret}；</li>
 *   <li>未配置或该键为空时，回退为已解析的 {@code appSecret}，以兼容仅配置单密钥的历史环境（与对端约定了分离密钥后应在 Speed
 *       中单独配 {@code dataSecret}）。</li>
 * </ul>
 *
 * @author yueping.bai
 */
public final class DidiaiDataSecretUtil {

    private DidiaiDataSecretUtil() {}

    /**
     * 解析本请求用于 AES-128 的密钥源字符串。
     *
     * <p>参数说明：{@code appSecret} 为已通过 {@code appKey} 在 {@code didiaiAppSecretMap} / 固定表解析出的
     * 验签用密钥，作为 {@code dataSecret} 缺失时的回退；允许为 null（此时无回退，返回 null）。
     *
     * <p>返回值说明：用于 {@link DidiaiKeyUtil#toAes128KeyBytes(String)} 的字符串，优先为 {@code dataSecret}；否则为
     * {@code appSecret}；配置与入参均无法得到时返回 null。
     *
     * @param config    Speed 全量配置，不可为 null
     * @param appKey    请求头应用标识
     * @param appSecret 已解析的验签用密钥，作回退
     * @return AES 密钥源字符串，或 null
     */
    public static String resolveDataSecret(
            MarketingCommonConfig config, String appKey, String appSecret) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }
        Map<String, String> dataMap = config.getDidiaiDataSecretMap();
        if (dataMap != null) {
            String d = dataMap.get(appKey);
            if (StringUtils.isNotBlank(d)) {
                return d;
            }
        }
        return appSecret;
    }
}
