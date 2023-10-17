package com.br.marketing.dto.gume;

import java.io.Serializable;
import java.util.List;

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
     * 时间戳 + 五位以 上随机数_批次，必填
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
     */
    private List<bizData> data;

    public GuMeTransferJsonDTO(String sign, String requestId, String channelCode, List<bizData> data) {
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

    public List<bizData> getData() {
        return data;
    }

    public void setData(List<bizData> data) {
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

    /**
     * 2023-10-16 17:57
     * 业务数据
     */
    public static class bizData {
        /**
         * 2023-10-16 17:55
         * 分组
         * 非必填
         */
        private String group;
        /**
         * 2023-10-16 17:55
         * userId
         * 非必填
         */
        private String userId;
        /**
         * 2023-10-16 17:55
         * 注册日期
         * 必填
         */
        private String registrationDate;
        /**
         * 2023-10-16 17:55
         * 是否登录 1: 是 0: 否
         * 非必填
         */
        private String isLogin;
        /**
         * 2023-10-16 17:55
         * 登录时间 yyyy-mm-dd
         * 非必填
         */
        private String loginTime;
        /**
         * 2023-10-16 17:55
         * 是否申请授信 1: 是 0: 否
         * 非必填
         */
        private String isApplyCredit;
        /**
         * 2023-10-16 17:55
         * 申请授信时间 yyyy-mm-dd
         * 非必填
         */
        private String applyCreditTime;
        /**
         * 2023-10-16 17:55
         * 是否授信通过 1: 是 0: 否
         * 非必填
         */
        private String isCreditPass;
        /**
         * 2023-10-16 17:55
         * 授信通过时间 yyyy-mm-dd
         * 非必填
         */
        private String creditPassTime;
        /**
         * 2023-10-16 17:55
         * 授信金额
         * 非必填
         */
        private String creditAmount;
        /**
         * 2023-10-16 17:55
         * 是否申请提现 1: 是 0: 否
         * 非必填
         */
        private String isApplyWithdrawals;
        /**
         * 2023-10-16 17:55
         * 提现时间 yyyy-mm-dd
         * 非必填
         */
        private String withdrawalsTime;
        /**
         * 2023-10-16 17:55
         * 是否风控通过 1: 是 0: 否
         * 非必填
         */
        private String isRiskPass;
        /**
         * 2023-10-16 17:55
         * 风控通过金额
         * 非必填
         */
        private String riskPassAmount;
        /**
         * 2023-10-16 17:55
         * 放款日期 yyyy-mm-dd
         * 非必填
         */
        private String lendersDate;

        public bizData() {
        }

        public bizData(String group, String userId, String registrationDate, String isLogin, String loginTime
                , String isApplyCredit, String applyCreditTime, String isCreditPass, String creditPassTime
                , String creditAmount, String isApplyWithdrawals, String withdrawalsTime, String isRiskPass
                , String riskPassAmount, String lendersDate) {
            this.group = group;
            this.userId = userId;
            this.registrationDate = registrationDate;
            this.isLogin = isLogin;
            this.loginTime = loginTime;
            this.isApplyCredit = isApplyCredit;
            this.applyCreditTime = applyCreditTime;
            this.isCreditPass = isCreditPass;
            this.creditPassTime = creditPassTime;
            this.creditAmount = creditAmount;
            this.isApplyWithdrawals = isApplyWithdrawals;
            this.withdrawalsTime = withdrawalsTime;
            this.isRiskPass = isRiskPass;
            this.riskPassAmount = riskPassAmount;
            this.lendersDate = lendersDate;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRegistrationDate() {
            return registrationDate;
        }

        public void setRegistrationDate(String registrationDate) {
            this.registrationDate = registrationDate;
        }

        public String getIsLogin() {
            return isLogin;
        }

        public void setIsLogin(String isLogin) {
            this.isLogin = isLogin;
        }

        public String getLoginTime() {
            return loginTime;
        }

        public void setLoginTime(String loginTime) {
            this.loginTime = loginTime;
        }

        public String getIsApplyCredit() {
            return isApplyCredit;
        }

        public void setIsApplyCredit(String isApplyCredit) {
            this.isApplyCredit = isApplyCredit;
        }

        public String getApplyCreditTime() {
            return applyCreditTime;
        }

        public void setApplyCreditTime(String applyCreditTime) {
            this.applyCreditTime = applyCreditTime;
        }

        public String getIsCreditPass() {
            return isCreditPass;
        }

        public void setIsCreditPass(String isCreditPass) {
            this.isCreditPass = isCreditPass;
        }

        public String getCreditPassTime() {
            return creditPassTime;
        }

        public void setCreditPassTime(String creditPassTime) {
            this.creditPassTime = creditPassTime;
        }

        public String getCreditAmount() {
            return creditAmount;
        }

        public void setCreditAmount(String creditAmount) {
            this.creditAmount = creditAmount;
        }

        public String getIsApplyWithdrawals() {
            return isApplyWithdrawals;
        }

        public void setIsApplyWithdrawals(String isApplyWithdrawals) {
            this.isApplyWithdrawals = isApplyWithdrawals;
        }

        public String getWithdrawalsTime() {
            return withdrawalsTime;
        }

        public void setWithdrawalsTime(String withdrawalsTime) {
            this.withdrawalsTime = withdrawalsTime;
        }

        public String getIsRiskPass() {
            return isRiskPass;
        }

        public void setIsRiskPass(String isRiskPass) {
            this.isRiskPass = isRiskPass;
        }

        public String getRiskPassAmount() {
            return riskPassAmount;
        }

        public void setRiskPassAmount(String riskPassAmount) {
            this.riskPassAmount = riskPassAmount;
        }

        public String getLendersDate() {
            return lendersDate;
        }

        public void setLendersDate(String lendersDate) {
            this.lendersDate = lendersDate;
        }

        @Override
        public String toString() {
            return "bizData{" +
                    "group='" + group + '\'' +
                    ", userId='" + userId + '\'' +
                    ", registrationDate='" + registrationDate + '\'' +
                    ", isLogin='" + isLogin + '\'' +
                    ", loginTime='" + loginTime + '\'' +
                    ", isApplyCredit='" + isApplyCredit + '\'' +
                    ", applyCreditTime='" + applyCreditTime + '\'' +
                    ", isCreditPass='" + isCreditPass + '\'' +
                    ", creditPassTime='" + creditPassTime + '\'' +
                    ", creditAmount='" + creditAmount + '\'' +
                    ", isApplyWithdrawals='" + isApplyWithdrawals + '\'' +
                    ", withdrawalsTime='" + withdrawalsTime + '\'' +
                    ", isRiskPass='" + isRiskPass + '\'' +
                    ", riskPassAmount='" + riskPassAmount + '\'' +
                    ", lendersDate='" + lendersDate + '\'' +
                    '}';
        }
    }


}
