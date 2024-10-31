package com.br.marketing.api.customer.black.service.guomei.dto;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.customer.black.adapter.BaseBlackDataAdaptee;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 国美定制上传数据json
 *
 * @author senyang.zheng
 * @date 2024/08/06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class GuoMeiBlackJsonDTO extends BaseBlackDataAdaptee<TransferDataDTO<TransferDataItemDTO>> {

    private static final long serialVersionUID = 3764405936555885180L;


    /**
     * 流水号
     */
    private String requestId;

    /**
     * 机构编码
     */
    private String institutionCode;

    /**
     * 自定义参数
     */
    private Boolean endFlag;

    /**
     * 业务数据，必填 初始已知字段： userId 用户id 必填 userType 分组 非必填 customName 客群分类 非必填 firstName 姓氏 非必填 gender 性别 非必填 age 年龄 非必填 cell 手机号-md5 必填 registerTime 注册日期
     * 必填 auditTime 历史授信时间 yyyy-mm-dd 非必填 auditAmount 历史授信金额 非必填 lastboot 最近一次启动时间 非必填 lastloan 最近一次放款时间 非必填 lastamount 最近一次放款金额 非必填 lastsettle 最近结清时间
     * 非必填
     */
    private JSONArray userList;


    @Override
    protected TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode, String jsonData) {
        return new TransferDataDTO<>();
    }
}
