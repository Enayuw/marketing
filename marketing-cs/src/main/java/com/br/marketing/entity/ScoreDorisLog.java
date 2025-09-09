package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_score_doris_log
 * @author :zhen.Li1
 */
@Data
public class ScoreDorisLog implements Serializable {

    private Long id;

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 跑分批次号
     */
    private String batchNumber;

    /**
     * 同步状态 1-同步中；2-已同步
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}