package com.br.marketing.datarelayservice.didiai;

import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * 滴滴 AI 上传请求参数校验工具类。
 *
 * <p>提供请求头参数的非空校验等功能，校验失败时返回对应的错误响应对象。
 * 本类为无状态工具类，仅包含静态方法，不允许实例化。
 *
 * @author yueping.bai
 */
public final class DidiaiValidationUtils {

    private DidiaiValidationUtils() {}

    /**
     * 校验必填的请求头参数。
     *
     * <p>依次检查 appKey、timestamp、sign 是否为空或空白字符串。
     * 任一参数缺失时返回对应的错误响应；全部通过时返回 null。
     *
     * @param appKey       应用标识
     * @param timestampStr 时间戳字符串
     * @param sign         签名
     * @return 校验失败时返回错误响应；校验通过时返回 null
     */
    public static DidiaiResponseDTO validateRequiredHeaders(
            String appKey, String timestampStr, String sign) {
        if (StringUtils.isBlank(appKey)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getCode(),
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getMessage());
        }
        if (StringUtils.isBlank(timestampStr)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getCode(),
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getMessage());
        }
        if (StringUtils.isBlank(sign)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_SIGN.getCode(),
                    DidiaiErrorCodeEnum.MISSING_SIGN.getMessage());
        }
        return null;
    }
}
