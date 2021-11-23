package com.br.marketing.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 日志收集
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/3 14:22
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/3 14:22
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Data
public class MarketingInfoLog {
    /**
     * 上传类型 sync 客户上传数据信息、transfer 客户转化数据请求
     */
    @JSONField(name = "upload_type")
    private String uploadType;
    /**
     * 商户编号
     */
    @JSONField(name = "api_code")
    private String apiCode;
    /**
     * 客户批次号
     */
    @JSONField(name = "cus_batch")
    private String cusBatch;
    /**
     * 请求批次号
     */
    @JSONField(name = "request_batch")
    private String requestBatch;
    /**
     * 请求json
     */
    @JSONField(name = "json_data")
    private String jsonData;
    /**
     * 响应json
     */
    @JSONField(name = "response_json")
    private String responseJson;
    /**
     * 请求IP
     */
    @JSONField(name = "request_ip")
    private String requestIp;
    /**
     * 请求时间
     */
    @JSONField(name = "start_time")
    private long startTime;
    /**
     * 响应时间
     */
    @JSONField(name = "end_time")
    private long endTime;
    /**
     * 响应编码
     */
    @JSONField(name = "response_code")
    private String responseCode;
    /**
     * 查询耗时
     */
    @JSONField(name = "cost_time")
    private long costTime;
    /**
     * 数据实际条数
     */
    @JSONField(name = "actual_num")
    private Integer actualNum;
    /**
     * 备注
     */
    private String remark;
}
