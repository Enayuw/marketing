package com.br.marketing.dto.gume;

import com.alibaba.fastjson.JSONArray;

import java.io.Serializable;

/**
 * 数禾数据
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:14
 */
public class GuMeTransferJsonDTO implements Serializable {

    private static final long serialVersionUID = -690890886707455676L;

    /**
     * 2023-10-16 17:43
     * MD5(md5(requestId+channelCode))，每次都是 32 位大写，必填
     */
    private String sign;


    /**
     * 2023-10-16 17:43
     * 时间戳 + 五位以上随机数_批次，必填
     */
    private String requestId;


    /**
     * 2023-10-16 17:43
     * 渠道编码，必填
     */
    private String channelCode;

    /**
     * 2023-10-16 17:43
     * 业务数据，必填
     * 初始已知字段：
     * group 分组 非必填
     * userId userId 必填
     * registrationDate 注册日期 非必填
     * isLogin 是否登录 1: 是 0: 否 非必填
     * loginTime 登录时间 yyyy-mm-dd 非必填
     * isApplyCredit 是否申请授信 1: 是 0: 否 非必填
     * applyCreditTime 申请授信时间 yyyy-mm-dd 非必填
     * isCreditPass 是否授信通过 1: 是 0: 否 非必填
     * creditPassTime 授信通过时间 yyyy-mm-dd 非必填
     * creditAmount 授信金额 非必填
     * isApplyWithdrawals 是否申请提现 1: 是 0: 否 非必填
     * withdrawalsTime 提现时间 yyyy-mm-dd 非必填
     * isRiskPass 是否风控通过 1: 是 0: 否 非必填
     * riskPassAmount 风控通过金额 非必填
     * lendersDate 放款日期 yyyy-mm-dd 非必填
     */
    private JSONArray data;

    public GuMeTransferJsonDTO(String sign, String requestId, String channelCode, JSONArray data) {
        this.sign = sign;
        this.requestId = requestId;
        this.channelCode = channelCode;
        this.data = data;
    }

    public GuMeTransferJsonDTO() {
    }


    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GuMeTransferJsonDTO{" +
                "sign='" + sign + '\'' +
                ", requestId='" + requestId + '\'' +
                ", channelCode='" + channelCode + '\'' +
                ", data=" + data +
                '}';
    }
}
