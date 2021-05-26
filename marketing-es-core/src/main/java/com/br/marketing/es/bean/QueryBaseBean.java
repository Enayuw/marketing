package com.br.marketing.es.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

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
    @JSONField(name = "api_code")
    private String apiCode;
    /**
     * 跑分日期时间间隔
     */
    private Date taskStartTime;
    private Date taskEndTime;
    /**
     * 批次号
     */
    @JSONField(name = "batch_number")
    private String batchNumber;
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
}
