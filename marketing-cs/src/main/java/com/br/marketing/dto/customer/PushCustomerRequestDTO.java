package com.br.marketing.dto.customer;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发送智能客服包装类
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/15 21:20
 */
@Data
public class PushCustomerRequestDTO implements Serializable {
    private static final long serialVersionUID = 4436352665936129980L;
    /**
     * 商户apiCode
     */
    private String apiCode;
    /**
     * 入参信息明细
     */
    private String jsonData;

    public PushCustomerRequestDTO(String apiCode, Integer transferStatus, List<MarketingTransferSyncUser> requestData) {
        this.apiCode = apiCode;
        this.jsonData = JSONObject.toJSONString(new JsonData(transferStatus, "addYiXinTransferData"
                , PushTransferToCustomerDTO.ListOf(requestData)
                , LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
//                , LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)), SerializerFeature.WriteMapNullValue);

    }

    public PushCustomerRequestDTO(String apiCode, Integer transferStatus,
                                  String method, List<MarketingTransferSyncUser> requestData) {
        this.apiCode = apiCode;
        this.jsonData = JSONObject.toJSONString(new JsonData(transferStatus, method
                , PushTransferToCustomerDTO.ListOf(requestData)
                , LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)));

    }

    public PushCustomerRequestDTO(String apiCode, Integer transferStatus,
                                  String method, List<MarketingTransferSyncUser> requestData, String importDate) {
        this.apiCode = apiCode;
        this.jsonData = JSONObject.toJSONString(new JsonData(transferStatus, method
                , PushTransferToCustomerDTO.ListOf(requestData), importDate));

    }

    /**
     * json 字符pojo
     */
    private static class JsonData {

        /**
         * 传送状态0：开始传送、1：传送中、2:传送结束
         */
        private Integer transferStatus;
        /**
         * 请求方法
         */
        private String method;
        /**
         * 请求数据明细(数据量<=200)
         */
        private List<PushTransferToCustomerDTO> requestData;
        /**
         * 导入时间（默认当天）yyyymmdd
         */
        private String importDate;

        public JsonData() {
        }

        public JsonData(Integer transferStatus, String method, List<PushTransferToCustomerDTO> requestData, String importDate) {
            this.transferStatus = transferStatus;
            this.method = method;
            this.requestData = requestData;
            this.importDate = importDate;
        }


        public Integer getTransferStatus() {
            return transferStatus;
        }

        public void setTransferStatus(Integer transferStatus) {
            this.transferStatus = transferStatus;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<PushTransferToCustomerDTO> getRequestData() {
            return requestData;
        }

        public void setRequestData(List<PushTransferToCustomerDTO> requestData) {
            this.requestData = requestData;
        }

        public String getImportDate() {
            return importDate;
        }

        public void setImportDate(String importDate) {
            this.importDate = importDate;
        }
    }

    /**
     * 客服同步接口与智能营销接口字段对应（标注为#的为扩展字段）
     * <p>
     * caseNum                  案件编号                                                         custNum                  唯一标识、必填；客户案件编号，可以和上传案件的案件编号进行关联
     * isRegister               是否注册1:是、0:否                                                ifRegister               是否注册
     * #registerChannel          注册渠道                                                         registerChannel          注册渠道
     * registerTime             注册时间yyyy-mm-dd hh:mm:ss                                      registerTime             注册时间yyyy-MM-dd hh:mm:ss:SSS
     * isImport                 是否进件1:是、0:否                                                ifApply                  是否进件
     * importTime               进件时间yyyy-mm-dd hh:mm:ss                                      applyDt                  进件时间yyyy-MM-dd hh:mm:ss:SSS
     * auditResult              审核结果4中类型：”DENY”、”WEAK_DENY”、”PASS”、”NULL”               applyResult              审批结果
     * creditAmount             授信金额                                                         auditAmount              授信总金额(元为单元，小数点后两位)
     * #isApplyLoan              是否申请放款 1:是、0:否                                           applyLoan                是否申请放款
     * isLoan                   是否放款 1:是、0:否                                               ifLent                   是否提现
     * loanTime                 放款时间yyyy-mm-dd hh:mm:ss                                      lentTime                 提现时间yyyy-MM-dd hh:mm:ss:SSS
     * loanAmount               放款金额                                                         lentAmount               提现金额(元为单元，小数点后两位)
     * registerNode             注册节点                                                         userType                 机构运营场景
     * #isApplyLargeAmount       是否申请大额提额 1:是、0:否                                        raiseLimit               是否申请大额提额
     * #applyLargeAmountTime     申请大额提额时间 yyyy-mm-dd hh:mm:ss                              raiseLimitTime           申请大额提额时间yyyy-MM-dd hh:mm:ss:SSS
     * #isPassApplyLargeAmount   申请大额提额是否通过 1:是、0:否                                    raiseLimitResult         申请大额提额是否通过
     * activity                 活动类型                                                         type                     转化节点
     * yiXinCreateTime          宜信明细创建时间yyyy-mm-dd hh:mm:ss                               insertTime               创建时间yyyy-MM-dd hh:mm:ss:SSS
     * #loanResult              放款状态                                                         loanResult              放款状态
     * <p>
     * <p>
     * 客服转化接口与智能运营转化接口字段映射对应
     */
    private static class PushTransferToCustomerDTO {
        /**
         * 案件编号
         */
        private String caseNum;
        /**
         * 是否注册:是、0:否
         */
        private String isRegister;
        /**
         * 注册渠道
         */
        private String registerChannel;
        /**
         * 注册时间yyyy-mm-dd hh:mm:ss
         */
        private String registerTime;
        /**
         * 是否进件1:是、0:否
         */
        private String isImport;
        /**
         * 进件时间yyyy-mm-dd hh:mm:ss
         */
        private String importTime;
        /**
         * 审核结果4中类型: ”DENY”、”WEAK_DENY”、”PASS”、”NULL”
         */
        private String auditResult;
        /**
         * 授信金额
         */
        private String creditAmount;
        /**
         * 是否申请放款  1:是、0:否
         */
        private String isApplyLoan;
        /**
         * 是否放款 1:是、0:否
         */
        private String isLoan;
        /**
         * 放款时间yyyy-mm-dd hh:mm:ss
         */
        private String loanTime;
        /**
         * 放款金额
         */
        private String loanAmount;
        /**
         * 注册节点
         */
        private String registerNode;
        /**
         * 是否申请大额提额
         */
        private String isApplyLargeAmount;
        /**
         * 申请大额提额时间 yyyy-mm-dd hh:mm:ss
         */
        private String applyLargeAmountTime;
        /**
         * 申请大额提额是否通过 1:是、0:否
         */
        private String isPassApplyLargeAmount;
        /**
         * 活动类型
         */
        private String activity;
        /**
         * 宜信明细创建时间yyyy-mm-dd hh:mm:ss
         */
        private String yiXinCreateTime;
        /**
         * 放款状态
         */
        private String loanResult;


        public static List<PushTransferToCustomerDTO> ListOf(List<MarketingTransferSyncUser> list) {
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return list.stream().map(transfer -> {
                String reserveField1 = transfer.getReserveField1();
                ReserveField1 reserveField2;
                try {
                    reserveField2 = StringUtils.isEmpty(reserveField1)
                            ? null : JSONObject.parseObject(reserveField1, ReserveField1.class);
                } catch (Exception exception) {
                    reserveField2 = null;
                }
                return new PushTransferToCustomerDTO(
                        transfer.getCustNum(),
                        transfer.getIfRegister(),
                        transfer.getRegisterTime(),
                        transfer.getIfApply(),
                        transfer.getApplyDt(),
                        StringUtils.isEmpty(transfer.getApplyResult()) ? "NULL" : ("0".equals(transfer.getApplyResult()) ? "DENY" : "1".equals(transfer.getApplyResult()) ? "PASS" : "NULL"),
                        transfer.getAuditAmount(),
                        transfer.getIfLent(),
                        transfer.getLentTime(),
                        transfer.getLentAmount(),
                        transfer.getUserType(),
                        transfer.getType(),
                        transfer.getInsertTime(),
                        reserveField2
                );
            }).collect(Collectors.toList());
        }


        public PushTransferToCustomerDTO() {
        }

        public PushTransferToCustomerDTO(String caseNum, String isRegister
                , String registerChannel, String registerTime, String isImport, String importTime, String auditResult
                , String creditAmount, String isApplyLoan, String isLoan, String loanTime, String loanAmount
                , String registerNode, String isApplyLargeAmount, String applyLargeAmountTime
                , String isPassApplyLargeAmount, String activity, String yiXinCreateTime, String loanResult) {
            this.caseNum = caseNum;
            this.isRegister = isRegister;
            this.registerChannel = registerChannel;
            this.registerTime = registerTime;
            this.isImport = isImport;
            this.importTime = importTime;
            this.auditResult = auditResult;
            this.creditAmount = creditAmount;
            this.isApplyLoan = isApplyLoan;
            this.isLoan = isLoan;
            this.loanTime = loanTime;
            this.loanAmount = loanAmount;
            this.registerNode = registerNode;
            this.isApplyLargeAmount = isApplyLargeAmount;
            this.applyLargeAmountTime = applyLargeAmountTime;
            this.isPassApplyLargeAmount = isPassApplyLargeAmount;
            this.activity = activity;
            this.yiXinCreateTime = yiXinCreateTime;
            this.loanResult = loanResult;
        }

        public PushTransferToCustomerDTO(String caseNum, String isRegister
                , String registerTime, String isImport, String importTime, String auditResult
                , String creditAmount, String isLoan, String loanTime, String loanAmount
                , String registerNode, String activity, String yiXinCreateTime, ReserveField1 reserveField1) {
            this.caseNum = caseNum;
            this.isRegister = isRegister;
            this.registerTime = registerTime;
            this.isImport = isImport;
            this.importTime = importTime;
            this.auditResult = auditResult;
            this.creditAmount = creditAmount;
            this.isLoan = isLoan;
            this.loanTime = loanTime;
            this.loanAmount = loanAmount;
            this.registerNode = registerNode;
            this.activity = activity;
            this.yiXinCreateTime = yiXinCreateTime;
            if (reserveField1 == null) {
                this.isApplyLoan = "";
                this.isApplyLargeAmount = "";
                this.applyLargeAmountTime = "";
                this.isPassApplyLargeAmount = "";
                this.loanResult = "";
            } else {
                this.isApplyLoan = reserveField1.getApplyLoan() == null ? "" : reserveField1.getApplyLoan();
                this.isApplyLargeAmount = reserveField1.getRaiseLimit() == null ? "" : reserveField1.getRaiseLimit();
                this.applyLargeAmountTime = reserveField1.getRaiseLimitTime() == null ? "" : reserveField1.getRaiseLimitTime();
                this.isPassApplyLargeAmount = reserveField1.getRaiseLimitResult() == null ? "" : reserveField1.getRaiseLimitResult();
                this.loanResult = reserveField1.getLoanResult() == null ? "" : reserveField1.getLoanResult();
                this.registerChannel = reserveField1.getRegisterChannel() == null ? "" : reserveField1.getRegisterChannel();
            }
        }

        public String getCaseNum() {
            return caseNum;
        }

        public void setCaseNum(String caseNum) {
            this.caseNum = caseNum;
        }

        public String getIsRegister() {
            return isRegister;
        }

        public void setIsRegister(String isRegister) {
            this.isRegister = isRegister;
        }

        public String getRegisterChannel() {
            return registerChannel;
        }

        public void setRegisterChannel(String registerChannel) {
            this.registerChannel = registerChannel;
        }

        public String getRegisterTime() {
            return registerTime;
        }

        public void setRegisterTime(String registerTime) {
            this.registerTime = registerTime;
        }

        public String getIsImport() {
            return isImport;
        }

        public void setIsImport(String isImport) {
            this.isImport = isImport;
        }

        public String getImportTime() {
            return importTime;
        }

        public void setImportTime(String importTime) {
            this.importTime = importTime;
        }

        public String getAuditResult() {
            return auditResult;
        }

        public void setAuditResult(String auditResult) {
            this.auditResult = auditResult;
        }

        public String getCreditAmount() {
            return creditAmount;
        }

        public void setCreditAmount(String creditAmount) {
            this.creditAmount = creditAmount;
        }

        public String getIsApplyLoan() {
            return isApplyLoan;
        }

        public void setIsApplyLoan(String isApplyLoan) {
            this.isApplyLoan = isApplyLoan;
        }

        public String getIsLoan() {
            return isLoan;
        }

        public void setIsLoan(String isLoan) {
            this.isLoan = isLoan;
        }

        public String getLoanTime() {
            return loanTime;
        }

        public void setLoanTime(String loanTime) {
            this.loanTime = loanTime;
        }

        public String getLoanAmount() {
            return loanAmount;
        }

        public void setLoanAmount(String loanAmount) {
            this.loanAmount = loanAmount;
        }

        public String getRegisterNode() {
            return registerNode;
        }

        public void setRegisterNode(String registerNode) {
            this.registerNode = registerNode;
        }

        public String getIsApplyLargeAmount() {
            return isApplyLargeAmount;
        }

        public void setIsApplyLargeAmount(String isApplyLargeAmount) {
            this.isApplyLargeAmount = isApplyLargeAmount;
        }

        public String getApplyLargeAmountTime() {
            return applyLargeAmountTime;
        }

        public void setApplyLargeAmountTime(String applyLargeAmountTime) {
            this.applyLargeAmountTime = applyLargeAmountTime;
        }

        public String getIsPassApplyLargeAmount() {
            return isPassApplyLargeAmount;
        }

        public void setIsPassApplyLargeAmount(String isPassApplyLargeAmount) {
            this.isPassApplyLargeAmount = isPassApplyLargeAmount;
        }

        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }

        public String getYiXinCreateTime() {
            return yiXinCreateTime;
        }

        public void setYiXinCreateTime(String yiXinCreateTime) {
            this.yiXinCreateTime = yiXinCreateTime;
        }

        public String getLoanResult() {
            return loanResult;
        }

        public void setLoanResult(String loanResult) {
            this.loanResult = loanResult;
        }

    }


    /**
     * 扩展字段1
     */
    private static class ReserveField1 {
        /**
         * 是否申请放款; 1是0否，同转化表“是否申请放款”
         */
        private String applyLoan;

        /**
         * 是否申请大额提额; 1是0否，同转化表“是否申请大额提额”
         */
        private String raiseLimit;
        /**
         * 申请大额提额时间 yyyy-MM-dd hh:mm:ss:SSS;同转化表“申请大额提额时间”
         */
        private String raiseLimitTime;
        /**
         * 申请大额提额是否通过;1通过0未通过同转化表“申请大额提额是否通过”
         */
        private String raiseLimitResult;
        /**
         * 放款状态; 同转化表“放款状态”
         */
        private String loanResult;
        /**
         * 注册渠道; 1是0否，同转化表“是否申请放款”
         */
        private String registerChannel;

        public ReserveField1(String applyLoan, String raiseLimit, String raiseLimitTime, String raiseLimitResult
                , String loanResult, String registerChannel) {
            this.applyLoan = applyLoan;
            this.raiseLimit = raiseLimit;
            this.raiseLimitTime = raiseLimitTime;
            this.raiseLimitResult = raiseLimitResult;
            this.loanResult = loanResult;
            this.registerChannel = registerChannel;
        }

        public ReserveField1() {
        }

        public String getApplyLoan() {
            return applyLoan;
        }

        public void setApplyLoan(String applyLoan) {
            this.applyLoan = applyLoan;
        }

        public String getRaiseLimit() {
            return raiseLimit;
        }

        public void setRaiseLimit(String raiseLimit) {
            this.raiseLimit = raiseLimit;
        }

        public String getRaiseLimitTime() {
            return raiseLimitTime;
        }

        public void setRaiseLimitTime(String raiseLimitTime) {
            this.raiseLimitTime = raiseLimitTime;
        }

        public String getRaiseLimitResult() {
            return raiseLimitResult;
        }

        public void setRaiseLimitResult(String raiseLimitResult) {
            this.raiseLimitResult = raiseLimitResult;
        }

        public String getLoanResult() {
            return loanResult;
        }

        public void setLoanResult(String loanResult) {
            this.loanResult = loanResult;
        }

        public String getRegisterChannel() {
            return registerChannel;
        }

        public void setRegisterChannel(String registerChannel) {
            this.registerChannel = registerChannel;
        }
    }
}
