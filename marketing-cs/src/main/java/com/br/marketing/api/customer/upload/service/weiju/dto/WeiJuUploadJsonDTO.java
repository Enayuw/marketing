package com.br.marketing.api.customer.upload.service.weiju.dto;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.dto.MarketingPreUserDTO;

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
public class WeiJuUploadJsonDTO extends BaseUploadDataAdaptee<MarketingPreUserDTO> {

    private static final long serialVersionUID = 8769826064634577808L;

    /**
     * 请求ID
     */
    private String traceId;

    /**
     * 执行批次号
     */
    private String executeBatchNo;


    /**
     * 营销⽤户名单 marketing:营销名单 complaint:客诉名单
     */
    private String pushType;

    /**
     * ⽤户类型：silence: 沉默⽤户；littleSilence: 较沉默⽤户；
     */
    private String operationUserType;

    /**
     * 场景：registedNoCredit:注册未授信； creditedNoWithdrawal:授信未提现； clearedNoReloan:结清未复贷；
     */
    private String scene;

    /**
     * 沉默天数分组(废弃)
     */
    @Deprecated
    private String silenceDaysGroup;


    /**
     * 沉默天数分组
     */
    private String operateGroup;


    /**
     * 结算分组
     */
    private String settleGroup;

    /**
     * 业务数据
     * 初始已知字段：
     * userId 用户id 必填
     * userType 分组 非必填
     * customName 客群分类 非必填
     * firstName 姓氏 非必填
     * gender 性别 非必填
     * age 年龄 非必填
     * cell 手机号-md5 必填
     * registerTime 注册日期 必填
     * auditTime 历史授信时间 yyyy-mm-dd 非必填
     * auditAmount 历史授信金额 非必填
     * lastboot 最近一次启动时间 非必填
     * lastloan 最近一次放款时间 非必填
     * lastamount 最近一次放款金额 非必填
     * lastsettle 最近结清时间 非必填
     */
    private JSONArray userInfoList;


    /**
     * 适应者请求
     *
     * @param apiCode  apiCode
     * @param jsonData json数据
     * @return {@link MarketingPreUserDTO }
     * @author senyang.zheng
     * @date 2024/10/23
     */
    @Override
    protected MarketingPreUserDTO adapteeRequest(String apiCode, String jsonData) {
        return new MarketingPreUserDTO();
    }
}
