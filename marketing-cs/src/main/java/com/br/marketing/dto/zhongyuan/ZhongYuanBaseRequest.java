package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 中原消金公共请求基类
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class ZhongYuanBaseRequest<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水号
     */
    private String flowId;

    /**
     * 系统标识
     */
    private String sysId;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 渠道编号
     */
    private String channelNo;

    /**
     * 版本号
     */
    private String version;

    /**
     * 访问令牌（登录接口除外）
     */
    private String token;

    /**
     * 业务数据
     */
    private T data;
}

