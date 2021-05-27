package com.br.marketing.es.bean;

import lombok.Data;


/**
 * 查询参数
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/25 17:02
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/25 17:02
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Data
public class QueryBaseBean {
    /**
     * apiCode
     */
    private String apiCode;
    /**
     * 多批次
     */
    private String batchNumbers;
    /**
     * 模型名称
     */
    private String modelCode;
    /**
     * 模型版本
     */
    private String modelVersion;
    /**
     * 分值区间
     */
    private String scoreRange;
    /**
     * 数量top值
     */
    private String amountTop;
    /**
     * 分页大小
     */
    private Integer pageSize;
    /**
     * 流水号
     * value：上一页最后一个流水号
     */
    private String hisPageSwiftNumber;
}
