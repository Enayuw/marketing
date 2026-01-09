package com.br.marketing.entity;

import java.util.Date;
import lombok.Data;

@Data
public class DiDiV5CollidingData {
    /**
     * 主键id
     */
    private Long id;

    /**
     *
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 撞库时间
     */
    private Date collidingTime;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-未推送；1-推送中；2-推送失败；3-推送成功
     */
    private Integer pushStatus;

    /**
     * 状态 0-正常，1-非正常
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Integer createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}