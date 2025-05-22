package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_sync_info
 * @author 
 */
@Data
public class MarketingSyncInfo implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 请求批次号
     */
    private String requestBatch;

    /**
     * 是否最后一次传输
     * @see com.br.marketing.common.constants.common.LastEnum
     */
    private Byte last;

    /**
     * 总数据量
     */
    private Long total;

    /**
     * 状态 1-进行中；2-全部成功；3-全部失败；4-部分成功
     */
    private Integer status;

    /**
     * 0:通用调用1:定制接口清洗后调用
     */
    private Integer dataSourceType;

    /**
     * 数据
     */
    private String jsonData;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 错误id
     */
    private Long errorId;

    /**
     * 数据实际条数
     */
    private Integer actualNum;

    /**
     * 1未同步；2已同步
     */
    private Integer isUpload;

    private static final long serialVersionUID = 1L;
}