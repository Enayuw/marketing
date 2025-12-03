package com.br.marketing.dto.tccpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaCollidingDataPackageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 包名称
     */
    private String packageName;

    /**
     * 跑分任务编号
     */
    private String batchNumbers;

    /**
     * 跑分条件
     */
    private String conditions;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 量级
     */
    private Integer magnitude;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 清洗状态 0-待清洗；1-清洗中；2-清洗完成
     */
    private Integer cleanStatus;

    /**
     * 删除状态 1-可用；2-删除中；9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
