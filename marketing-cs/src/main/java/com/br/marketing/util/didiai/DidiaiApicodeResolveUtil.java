package com.br.marketing.util.didiai;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 滴滴 AI 定制化上传：业务 apiCode、分表 cid 与 Drs 表后缀的解析工具（从 {@code MarketingCommonConfig} 拆出，避免配置类膨胀）。
 *
 * <p>配置字段 {@code didiaiApicode}、{@code didiaiApicodeToCidMap} 仍由 Speed 绑定在 {@code MarketingCommonConfig}，本类仅承载无状态解析逻辑。
 *
 * @author yueping.bai
 */
public final class DidiaiApicodeResolveUtil {

    private DidiaiApicodeResolveUtil() {}

    /**
     * 解析生效业务 apiCode：请求头 Test-ApiCode 非空则取其 trim，否则使用配置中的 didiaiApicode，再否则返回 {@code 7413678}。
     *
     * @param testApiCodeHeader Test-ApiCode 请求头值，可为空
     * @param didiaiApicode     Speed 配置的默认 apiCode，可为空
     * @return 非空 apiCode 字符串
     */
    public static String resolveEffectiveApiCode(String testApiCodeHeader, String didiaiApicode) {
        if (StringUtils.isNotBlank(testApiCodeHeader)) {
            return testApiCodeHeader.trim();
        }
        if (StringUtils.isNotBlank(didiaiApicode)) {
            return didiaiApicode.trim();
        }
        return "7413678";
    }

    /**
     * 根据生效 apiCode 解析分表 cid（无符号）；映射来自 Speed 配置，空配置时使用 {@link #apicodeToCidMapOrDefault(Map)} 默认值。
     *
     * @param effectiveApiCode   业务 apiCode
     * @param apicodeToCidMap    配置中的 apiCode→cid 映射，可为 null 或空
     * @return cid，如 9356；apiCode 为 null 或未映射时 null
     */
    public static String resolveCid(String effectiveApiCode, Map<String, String> apicodeToCidMap) {
        Map<String, String> map = apicodeToCidMapOrDefault(apicodeToCidMap);
        if (effectiveApiCode == null) {
            return null;
        }
        return map.get(effectiveApiCode);
    }

    /**
     * 将 cid 转为 Drs 动态分表后缀（与 Mapper 中 {@code b_drs_customize_upload_data${tCid}} 一致）。
     *
     * @param cid 无符号 cid，如 9356
     * @return 后缀如 {@code _9356}；cid 为空时 null
     */
    public static String cidToDrsTableSuffix(String cid) {
        if (cid == null) {
            return null;
        }
        String c = cid.trim();
        if (c.isEmpty()) {
            return null;
        }
        return "_" + c;
    }

    /**
     * 若 Speed 未配置或配置为空 Map，则返回默认 {@code 7413678 → 9356}；否则返回原映射引用。
     *
     * @param configured Speed 中的 {@code didiaiApicodeToCidMap}，可为 null
     * @return 非空、可用于查询的 Map
     */
    public static Map<String, String> apicodeToCidMapOrDefault(Map<String, String> configured) {
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        Map<String, String> defaults = new HashMap<>(2);
        defaults.put("7413678", "9356");
        return defaults;
    }
}
