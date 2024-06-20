package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_clean_data_task
 * @author 
 */
@Data
public class MarketingCleanDataTask implements Serializable {
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 规则配置id
     */
    private Integer configId;

    /**
     * 文件id,多个用,分割
     */
    private String fileId;

    /**
     * 清洗类型：0上传，1转化
     */
    private Integer cleanType;

    /**
     * 试跑结果
     */
    private String testResult;

    /**
     * 清洗类型：0待清洗，1清洗中，2成功，3失败
     */
    private Integer cleanStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    private static final long serialVersionUID = 1L;
}