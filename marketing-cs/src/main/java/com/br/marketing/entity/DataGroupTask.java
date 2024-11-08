package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_data_group_task
 * @author 
 */
@Data
public class DataGroupTask implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 配置id
     */
    private Long configId;

    /**
     * 分组字段
     */
    private String groupFiled;

    /**
     * 字段分组规则，json类型
     */
    private String groupRule;

    /**
     * 状态 0-待开始；1-进行中；2-已完成；3-处理失败
     */
    private Integer status;

    /**
     * 操作类型 0为新增字段，1为删除字段
     */
    private Integer operType;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}