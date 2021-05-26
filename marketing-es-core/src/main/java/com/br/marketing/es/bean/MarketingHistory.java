package com.br.marketing.es.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 营销平台ES
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/20 16:42
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/20 16:42
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketingHistory implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * apiCode
     */
    @JSONField(name = "api_code")
    private String apiCode;
    /**
     * 身份证
     */
    @JSONField(name = "id_card")
    private String idCard;
    /**
     * 手机号
     */
    private String cell;
    /**
     * 姓名
     */
    private String name;
    /**
     * 上传日期
     */
    @JSONField(name = "request_time", format = "yyyy-MM-dd")
    private Date requestTime;
    /**
     * 批次号
     */
    @JSONField(name = "batch_number")
    private String batchNumber;
    /**
     * 流水号
     */
    @JSONField(name = "swift_number")
    private String swiftNumber;
    /**
     * 客戶编号
     */
    @JSONField(name = "cus_num")
    private String cusNum;
    /**
     * 策略编号
     */
    @JSONField(name = "strategy_id")
    private String strategyId;
    /**
     * 策略版本
     */
    private String version;
    /**
     * 策略版本
     */
    private List<Product> product;

}
