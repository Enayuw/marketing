package com.br.marketing.api.customer.service.guomei.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.customer.adapter.TransferDataAdaptee;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 国美数据
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:14
 */
public class GuMeTransferJsonDTO extends TransferDataAdaptee {

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
     * lendersAmount 放款金额 非必填
     */
    private JSONArray data;

    public GuMeTransferJsonDTO(String sign, String requestId, String channelCode, JSONArray data) {
        this.sign = sign;
        this.requestId = requestId;
        this.channelCode = channelCode;
        this.data = data;
    }

    public GuMeTransferJsonDTO(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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

    @Override
    protected TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode
            , TransferDataDTO<TransferDataItemDTO> transferDataDTO) {
        transferDataDTO.setRequestId(this.getRequestId().concat("_") + RandomStringUtils.randomNumeric(5));
        List<TransferDataItemDTO> objects = new ArrayList<>();
        JSONArray data = this.getData();
        int size = data.size();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            TransferDataItemDTO dto = new TransferDataItemDTO();
            dto.setApiCode(apiCode);
            dto.setCustNum(jsonObject.getString("userId"));
            jsonObject.remove("userId");
            // 2对应注册未授信
            // 3对应授信未动支
            String group = jsonObject.getString("group");
            dto.setUserType("注册未授信".equals(group) ? "2" : "授信未动支".equals(group) ? "3" : group);
            jsonObject.remove("group");
            dto.setRegisterTime(jsonObject.getString("registrationDate"));
            jsonObject.remove("registrationDate");
            String isLogin = jsonObject.getString("isLogin");
            dto.setIfLogin("是".equals(isLogin) ? "1" : "否".equals(isLogin) ? "0" : isLogin); //1是0否
            jsonObject.remove("isLogin");
            dto.setLoginTime(jsonObject.getString("loginTime"));
            jsonObject.remove("loginTime");
            String isApplyCredit = jsonObject.getString("isApplyCredit");
            dto.setIfApply("是".equals(isApplyCredit) ? "1" : "否".equals(isApplyCredit) ? "0" : isApplyCredit); //1是0否
            jsonObject.remove("isApplyCredit");
            dto.setApplyDt(jsonObject.getString("applyCreditTime"));
            jsonObject.remove("applyCreditTime");
            String isCreditPass = jsonObject.getString("isCreditPass");
            dto.setApplyResult("是".equals(isCreditPass) ? "1" : "否".equals(isCreditPass) ? "0" : isCreditPass); //1是0否
            jsonObject.remove("isCreditPass");
            dto.setAuditTime(jsonObject.getString("creditPassTime"));
            jsonObject.remove("creditPassTime");
            dto.setAuditAmount(jsonObject.getString("creditAmount")); //77777.77
            jsonObject.remove("creditAmount");
            String isRiskPass = jsonObject.getString("isRiskPass");
            dto.setIfLent("是".equals(isRiskPass) ? "1" : "否".equals(isRiskPass) ? "0" : isRiskPass); //1是0否
            jsonObject.remove("isRiskPass");
            dto.setLentTime(jsonObject.getString("lendersDate"));
            jsonObject.remove("lendersDate");
            dto.setLentAmount(jsonObject.getString("riskPassAmount"));
            jsonObject.remove("riskPassAmount");
            dto.setUnlentAmount(jsonObject.getString("lendersAmount"));
            jsonObject.remove("lendersAmount");
            // isApplyWithdrawals 1是0否
            String isApplyWithdrawals = jsonObject.getString("isApplyWithdrawals");
            jsonObject.put("isApplyWithdrawals", "是".equals(isApplyWithdrawals) ? "1"
                    : "否".equals(isApplyWithdrawals) ? "0" : isApplyWithdrawals);
            dto.setReserveField1(JSON.toJSONString(jsonObject));
            objects.add(dto);
        }
        transferDataDTO.setDataItems(objects);
        return transferDataDTO;
    }
}
