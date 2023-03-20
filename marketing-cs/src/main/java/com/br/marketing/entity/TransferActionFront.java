package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * b_transfer_action_front
 *
 * @author
 */
@Data
public class TransferActionFront implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 任务状态 1-未执行；2-执行结束；
     */
    private Integer status;

    /**
     * 执行类型1-客服转化；2-电销
     */
    private Integer actionType;

    /**
     * 执行日期 格式 yyyy-MM-dd
     */
    private String actionData;

    /**
     * 有效标志，1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 入库日期
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}