package com.br.marketing.datarelayservice.didiai;

import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil.ResolveError;

/**
 * 滴滴 AI 上传响应构建工具类。
 *
 * <p>提供各类响应对象和 DTO 的构建方法，封装错误码与错误消息的组装逻辑。
 * 本类为无状态工具类，仅包含静态方法，不允许实例化。
 *
 * @author yueping.bai
 */
public final class DidiaiResponseUtils {

    private DidiaiResponseUtils() {}

    /**
     * 根据 apiCode 解析失败原因构造对应的错误响应。
     *
     * @param error 解析失败原因枚举
     * @return 包含对应错误码和错误信息的响应
     */
    public static DidiaiResponseDTO buildApiCodeErrorResponse(ResolveError error) {
        if (error == ResolveError.TEST_APICODE_NOT_IN_WHITELIST) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.TEST_APICODE_NOT_IN_WHITELIST.getCode(),
                    DidiaiErrorCodeEnum.TEST_APICODE_NOT_IN_WHITELIST.getMessage());
        }
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.APICODE_NOT_FOUND.getCode(),
                DidiaiErrorCodeEnum.APICODE_NOT_FOUND.getMessage());
    }

    /**
     * 构造 cid 未配置的错误响应。
     *
     * <p>返回包含错误码和提示信息的响应，提示用户在 didiaiApicodeToCidMap 中补充配置。
     *
     * @param effectiveApiCode 未找到 cid 映射的 apiCode
     * @return cid 未配置的错误响应
     */
    public static DidiaiResponseDTO buildCidNotConfiguredResponse(String effectiveApiCode) {
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.CID_NOT_CONFIGURED.getCode(),
                DidiaiErrorCodeEnum.CID_NOT_CONFIGURED.getMessage()
                        + "，请在 didiaiApicodeToCidMap 中补充: "
                        + effectiveApiCode);
    }

    /**
     * 将裸密文字符串包装为 DidiaiEncryptedRequestDTO 对象。
     *
     * <p>将请求体中直接传入的 Base64 密文字符串设置到 DTO 的 data 字段，
     * 保持与 Service 层的接口契约一致。
     *
     * @param rawCipherText 裸密文字符串
     * @return 包含密文的 DTO 对象
     */
    public static DidiaiEncryptedRequestDTO buildEncryptedRequestDTO(String rawCipherText) {
        DidiaiEncryptedRequestDTO dto = new DidiaiEncryptedRequestDTO();
        dto.setData(rawCipherText);
        return dto;
    }
}
