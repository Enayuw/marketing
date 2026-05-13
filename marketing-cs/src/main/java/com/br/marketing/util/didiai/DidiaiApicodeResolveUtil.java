package com.br.marketing.util.didiai;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 滴滴 AI 定制化上传：业务 apiCode、分表 cid 与 Drs 表后缀的解析工具（从 {@code MarketingCommonConfig} 拆出，避免配置类膨胀）。
 *
 * <p>配置字段 {@code didiaiAppkeyToApicodeMap}、{@code testApicodeList}、{@code didiaiApicodeToCidMap}
 * 由 Speed 绑定在 {@code MarketingCommonConfig}，本类仅承载无状态解析逻辑。
 *
 * @author yueping.bai
 */
public final class DidiaiApicodeResolveUtil {

    private DidiaiApicodeResolveUtil() {}

    /**
     * apiCode 解析结果封装类，区分成功/失败及失败原因。
     */
    public static class ApiCodeResolveResult {
        private final String apiCode;
        private final ResolveError error;

        private ApiCodeResolveResult(String apiCode, ResolveError error) {
            this.apiCode = apiCode;
            this.error = error;
        }

        public static ApiCodeResolveResult ok(String apiCode) {
            return new ApiCodeResolveResult(apiCode, null);
        }

        public static ApiCodeResolveResult fail(ResolveError error) {
            return new ApiCodeResolveResult(null, error);
        }

        public boolean isSuccess() {
            return error == null && apiCode != null;
        }

        public String getApiCode() {
            return apiCode;
        }

        public ResolveError getError() {
            return error;
        }
    }

    /**
     * 解析失败原因枚举。
     */
    public enum ResolveError {
        /** 根据 appKey 未能获取到对应的 apiCode。 */
        APICODE_NOT_FOUND,
        /** Test-ApiCode 请求头传入的 apiCode 不在配置白名单中。 */
        TEST_APICODE_NOT_IN_WHITELIST
    }

    /**
     * 解析有效的 apiCode（新版，支持 appKey 映射与 Test-ApiCode 白名单校验）。
     *
     * <p>解析优先级：
     * <ol>
     *   <li>若请求头包含 Test-ApiCode，校验其是否在 testApicodeList 白名单中，在则使用，不在则返回错误</li>
     *   <li>否则按 appKey 查 didiaiAppkeyToApicodeMap，找到则使用，未找到则返回错误</li>
     * </ol>
     *
     * @param testApiCodeHeader    请求头 Test-ApiCode（测试覆盖，优先级最高）
     * @param appKey               请求头 appKey
     * @param appkeyToApicodeMap   Speed 配置的 appKey → apiCode 映射
     * @param testApicodeList      测试 apiCode 白名单
     * @return 解析结果（含 apiCode 或错误类型）
     */
    public static ApiCodeResolveResult resolveEffectiveApiCode(
            String testApiCodeHeader,
            String appKey,
            Map<String, String> appkeyToApicodeMap,
            List<String> testApicodeList) {
        // 1. Test-ApiCode 优先（测试覆盖）
        if (StringUtils.isNotBlank(testApiCodeHeader)) {
            String testCode = testApiCodeHeader.trim();
            if (testApicodeList == null || !testApicodeList.contains(testCode)) {
                return ApiCodeResolveResult.fail(ResolveError.TEST_APICODE_NOT_IN_WHITELIST);
            }
            return ApiCodeResolveResult.ok(testCode);
        }
        // 2. 按 appKey 查 Map
        if (appkeyToApicodeMap != null && StringUtils.isNotBlank(appKey)) {
            String apiCode = appkeyToApicodeMap.get(appKey);
            if (StringUtils.isNotBlank(apiCode)) {
                return ApiCodeResolveResult.ok(apiCode);
            }
        }
        return ApiCodeResolveResult.fail(ResolveError.APICODE_NOT_FOUND);
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
