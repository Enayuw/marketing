package com.br.marketing.datarelayservice.constant;

/**
 * 滴滴 AI 定制化上传在服务端使用的容量、条数与业务枚举类常量定义。
 *
 * <p>集中定义可避免魔法数分散在条件判断中，便于代码审查与后续改为可配置项。修改常量默认值时须评估对线上已有调用方
 * 批量大小与报文体积的影响。
 *
 * @author yueping.bai
 */
public final class DidiaiConstants {

    /**
     * 单次 HTTP 请求中允许承载的最大业务记录条数上限，超出则拒绝处理并返回批量过大错误码。
     */
    public static final int MAX_BATCH_SIZE = 2000;

    /**
     * 批量条数超过 {@link #MAX_BATCH_SIZE} 时写入响应 {@code errorMsg} 的固定中文提示，与枚举 {@code BATCH_TOO_LARGE} 保持一致。
     */
    public static final String BATCH_TOO_LARGE_MESSAGE =
            "批量条数超限，单次最多允许" + MAX_BATCH_SIZE + "条";

    /**
     * 解密后明文字符串允许占用的最大 UTF-8 字节长度，用于限制单次请求的内存占用，当前约为一兆字节。
     */
    public static final int MAX_PLAINTEXT_BYTES = 1024 * 1024;

    /**
     * 业务条线取值之一：表示贷后场景，与 properties 内 bizLine 及批次级 bizLine 取值保持一致。
     */
    public static final String BIZ_LINE_POST_LOAN = "POST_LOAN";

    /**
     * 业务条线取值之一：表示营销场景。
     */
    public static final String BIZ_LINE_MARKETING = "MARKETING";

    private DidiaiConstants() {}

    /**
     * 判断给定字符串是否为当前服务端允许的两类 bizLine 之一。
     *
     * @param bizLine 待判断的条线字符串，可为 null
     * @return 非 null 且等于贷后或营销常量之一时返回 true，否则 false
     */
    public static boolean isAllowedBizLine(String bizLine) {
        return BIZ_LINE_POST_LOAN.equals(bizLine) || BIZ_LINE_MARKETING.equals(bizLine);
    }
}
