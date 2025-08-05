package com.br.marketing.entity;

import java.util.Date;
import lombok.Data;

@Data
public class ZhongAnCollidingDataLog {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String apiCode;

    /**
     * 数据来源类型：C&S-已接通&已发短信，C&NS=已接通&未发短信，NC&S-未接通&发短信，S-只发短信，NC-未接通
     */
    private String dataSourceType;

    /**
     * 场景
     */
    private String userType;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 短信发送状态：0-失败，1-成功
     */
    private Integer smsSendStatus;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;

    /**
     * 上报日期
     */
    private String reportDate;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}