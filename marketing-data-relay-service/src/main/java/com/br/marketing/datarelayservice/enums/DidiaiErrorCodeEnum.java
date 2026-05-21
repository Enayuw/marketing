package com.br.marketing.datarelayservice.enums;

import com.br.marketing.datarelayservice.constant.DidiaiConstants;

/**
 * 滴滴 AI 定制化上传接口在营销中转服务侧使用的数字错误码与对应中文描述。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>成功时 errorCode 为 0，与对端统一响应格式中的 errorCode 字段对齐；</li>
 *   <li>4 万段为业务与协议类可预期错误，便于网关与监控按区间告警；</li>
 *   <li>5 万段为未分类的系统异常，用于兜底。</li>
 * </ul>
 *
 * <p>各枚举常量含义见字段注释，接入层应优先返回明确错误码而非笼统失败。
 *
 * @author yueping.bai
 */
public enum DidiaiErrorCodeEnum {

    /** 处理成功，可与对端约定成功分支逻辑。 */
    SUCCESS(0, "成功"),

    /** 未捕获的运行时异常或依赖调用失败等，需结合日志排查。 */
    UNKNOWN_ERROR(50000, "系统异常"),

    /** 创建定制化上传分表失败。 */
    CREATE_DRS_TABLE_FAILED(50001, "创建分表失败"),

    /** 写入定制化上传汇总表失败。 */
    PERSIST_DRS_ROW_FAILED(50002, "写入汇总表失败"),

    /** 请求头 appKey 在本地配置 apps 列表中找不到对应 appSecret。 */
    UNKNOWN_APP(40001, "未知应用或 appKey 未配置"),

    /** 滴滴相关 JSON 配置整体缺失或结构不完整，无法完成安全校验。 */
    CONFIG_MISSING(40002, "服务端配置缺失"),

    /** 缺少请求头 appKey。 */
    MISSING_APP_KEY(40010, "缺少请求头 appKey"),

    /** 缺少请求头 timestamp。 */
    MISSING_TIMESTAMP(40011, "缺少请求头 timestamp"),

    /** timestamp 头存在但格式非法（无法解析为 long）。 */
    BAD_TIMESTAMP(40012, "timestamp 非法"),

    /** 缺少请求头 sign。 */
    MISSING_SIGN(40013, "缺少请求头 sign"),

    /** apiCode 映射不到 cid，无法确定分表。 */
    CID_NOT_CONFIGURED(40014, "未配置 apiCode 对应 cid"),

    /** 请求体缺少密文字段（data/cipherText/cipher 均为空）。 */
    MISSING_CIPHER(40015, "缺少密文字段 data、cipherText 或 cipher"),

    /** AES 解密失败，常见原因为密钥、IV、密文与对端不一致。 */
    DECRYPT_FAILED(40003, "解密失败"),

    /** HMAC 验签未通过，常见原因为 params 与对端序列化不一致或时间戳拼接不一致。 */
    SIGN_FAILED(40004, "验签失败"),

    /** 请求时间戳与服务器当前时间差超过配置的允许分钟数。 */
    TIMESTAMP_EXPIRED(40005, "请求已过期"),

    /** 解密后字符串无法解析为合法 JSON，或结构不符合本接口约定。 */
    JSON_INVALID(40006, "JSON 解析失败"),

    /** 解密后明文为空。 */
    PLAINTEXT_EMPTY(40016, "明文为空"),

    /** 批量数据为空。 */
    BATCH_EMPTY(40017, "批量数据为空"),

    /** 批量条数超过本服务配置的上限（见 {@link DidiaiConstants#MAX_BATCH_SIZE}）。 */
    BATCH_TOO_LARGE(40007, DidiaiConstants.BATCH_TOO_LARGE_MESSAGE),

    /** 解密后明文字节长度超过本服务配置的单次上限。 */
    BODY_TOO_LARGE(40008, "请求体过大"),

    /** 单条记录缺少必填字段。 */
    RECORD_FIELD_MISSING(40018, "业务参数缺失"),

    /** bizLine 取值不合法。 */
    BIZ_LINE_INVALID(40019, "bizLine 非法"),

    /** 模拟上传未开启（联调专用接口）。 */
    SIM_DISABLED(40020, "模拟上传未开启"),

    /** 单条记录 properties 中缺少非空的 userType（映射至 reserveField1.userType）。 */
    USER_TYPE_MISSING(40021, "缺少 properties.userType"),

    /** 根据 appKey 未能获取到对应的 apiCode。 */
    APICODE_NOT_FOUND(40022, "未获取到对应的 apiCode"),

    /** Test-ApiCode 请求头传入的 apiCode 不在配置白名单中。 */
    TEST_APICODE_NOT_IN_WHITELIST(40023, "测试 apiCode 不在配置白名单中，请检查"),

    /** 单条记录 properties 中缺少非空的 uid（映射至 custNum）。 */
    UID_MISSING(40024, "缺少必传字段uid，请检查！");

    private final int code;
    private final String message;

    /**
     * 构造单个错误码枚举常量。
     *
     * @param code    数字错误码，写入 HTTP 响应 JSON 的 errorCode 字段
     * @param message 中文描述，写入 errorMsg 字段
     */
    DidiaiErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 返回当前枚举项对应的数字错误码。
     *
     * @return 错误码整型值
     */
    public int getCode() {
        return code;
    }

    /**
     * 返回当前枚举项对应的中文错误描述。
     *
     * @return 错误描述字符串
     */
    public String getMessage() {
        return message;
    }
}
