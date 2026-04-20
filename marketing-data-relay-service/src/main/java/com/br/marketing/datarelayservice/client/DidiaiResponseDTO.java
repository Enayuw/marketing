package com.br.marketing.datarelayservice.client;

import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;

import java.io.Serializable;

/**
 * 滴滴 AI 上传接口的统一 HTTP 响应体，与对端约定的 errorCode、errorMsg、data 结构一致。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>成功时 errorCode 为 0，errorMsg 为成功文案，data 内携带 requestId 与字符串形式的 status；
 *   <li>失败时 errorCode 为非 0，errorMsg 为失败原因，data 可为空；
 *   <li>提供静态工厂方法 ok 与 fail，减少 Controller 与 Service 中的样板赋值代码。
 * </ul>
 *
 * @author yueping.bai
 */
public class DidiaiResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int errorCode;
    private String errorMsg;
    private DidiaiDataBody data;

    /**
     * 构造表示处理成功的响应对象，并填充 data 节点。
     *
     * <p>参数说明：requestId 建议为批次内首条业务 requestId 或与对端约定的批次标识；status 为字符串，
     * 常见取值为 true 或 false 的英文小写，与对端文档保持一致。
     *
     * <p>返回值说明：已设置 errorCode 为成功枚举、errorMsg 为成功文案、data 非空的响应实例。
     *
     * @param requestId 写入 data.requestId 的字符串
     * @param status      写入 data.status 的字符串
     * @return 成功响应 DTO
     */
    public static DidiaiResponseDTO ok(String requestId, String status) {
        DidiaiResponseDTO dto = new DidiaiResponseDTO();
        dto.setErrorCode(DidiaiErrorCodeEnum.SUCCESS.getCode());
        dto.setErrorMsg(DidiaiErrorCodeEnum.SUCCESS.getMessage());
        DidiaiDataBody body = new DidiaiDataBody();
        body.setRequestId(requestId);
        body.setStatus(status);
        dto.setData(body);
        return dto;
    }

    /**
     * 构造表示处理失败的响应对象，不设置 data 节点。
     *
     * <p>参数说明：errorCode 为业务或协议错误码；errorMsg 为人类可读说明，可直接展示给调用方或写入日志。
     *
     * <p>返回值说明：仅含错误码与错误信息的响应实例，data 字段为 null。
     *
     * @param errorCode 非零错误码
     * @param errorMsg  错误描述
     * @return 失败响应 DTO
     */
    public static DidiaiResponseDTO fail(int errorCode, String errorMsg) {
        DidiaiResponseDTO dto = new DidiaiResponseDTO();
        dto.setErrorCode(errorCode);
        dto.setErrorMsg(errorMsg);
        return dto;
    }

    /**
     * 获取响应中的数字错误码。
     *
     * @return errorCode 字段值
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 设置响应中的数字错误码。
     *
     * @param errorCode 错误码
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取响应中的错误说明文案。
     *
     * @return errorMsg 字段值
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 设置响应中的错误说明文案。
     *
     * @param errorMsg 错误说明
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 获取成功分支下的 data 载荷对象，失败时可能为 null。
     *
     * @return data 节点，可能为 null
     */
    public DidiaiDataBody getData() {
        return data;
    }

    /**
     * 设置 data 载荷对象。
     *
     * @param data 成功时的业务载荷，失败时可传 null
     */
    public void setData(DidiaiDataBody data) {
        this.data = data;
    }

    /**
     * 表示对端统一响应中 data 对象的字段集合，仅包含 requestId 与 status 两个字符串字段。
     *
     * <p>功能说明：与 JSON 中的 data 对象一一对应，便于 Jackson 或 Fastjson 序列化为目标 JSON 结构。
     *
     * @author yueping.bai
     */
    public static class DidiaiDataBody implements Serializable {

        private static final long serialVersionUID = 1L;

        private String requestId;
        private String status;

        /**
         * 获取对账或幂等关联用的请求标识字符串。
         *
         * @return requestId
         */
        public String getRequestId() {
            return requestId;
        }

        /**
         * 设置 requestId 字段。
         *
         * @param requestId 请求标识
         */
        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        /**
         * 获取业务处理是否成功的字符串形态标记。
         *
         * @return status 字段，如 true 或 false 的字符串
         */
        public String getStatus() {
            return status;
        }

        /**
         * 设置 status 字段。
         *
         * @param status 字符串状态
         */
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
