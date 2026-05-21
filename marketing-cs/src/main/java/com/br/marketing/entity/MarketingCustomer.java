package com.br.marketing.entity;

import java.util.Date;

public class MarketingCustomer {
    /**
     *
     */
    private Long id;

    /**
     * 合作客户ID
     */
    private String cid;

    /**
     *
     */
    private String apiCode;

    /**
     * 备注
     */
    private String message;

    /**
     * incr 增量、all 全量、once 一次
     */
    private String type;

    /**
     * 并发数
     */
    private Integer threadNum;

    /**
     * 跑数时间 1实时跑，2 T+1
     */
    private Byte taskTime;

    /**
     * finish或success文件生成时间，默认为1实时,2表示定时,
     */
    private Byte finishDate;

    /**
     * 是否推送客服,1推送,0不推送
     */
    private Byte pushCustomer;

    /**
     * 是否校验黑名单,1校验,0不校验
     */
    private Byte checkBlackList;

    /**
     * 是否校验条数,1校验,0不校验
     */
    private Byte checkRedisNumber;

    /**
     * 是否记录日志,1记录,0不记录
     */
    private Byte saveLog;

    /**
     * 跑分顺序根据此字段倒序排序
     */
    private Byte sort;

    /**
     * 状态 1正常，0删除
     */
    private Byte status;

    /**
     * 扩展字段
     */
    private String extendConfigInfo;

    /**
     * 跑分批次进度 Redis 保留天数（库字段 expire_day，varchar 存数字如 10、30）
     */
    private String expireDay;

    /**
     * api推送并发数
     */
    private Integer pushThreadNum;

    /**
     * 跑分结果推送类型，0文件，1 api，默认支持文件推送
     */
    private Integer pushType;

    /**
     * 推送地址
     */
    private String pushUrl;

    /**
     * 合作客户名称
     */
    private String name;

    /**
     * 合作客户简称
     */
    private String shortName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否需要校验请求key值（0:不需要,1:需要,2:不需要(通用强校验),3:需要(通用强校验),4:不需要(通用弱校验),5:需要(通用弱校验)）
     */
    private Integer isCheck;

    /**
     * 是否计费（不计：0，计费：1）
     */
    private Integer isCharging;

    /**
     * 请求处理编码
     */
    private String requestCode;

    /**
     * 响应处理编码
     */
    private String responseCode;

    /**
     * 账号类型 0 测试 1 正式
     */
    private Integer accountType;

    /**
     * 账号状态：0禁用 1启用)
     */
    private String accountStatus;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 0:API 1:WEB 2:SFTP
     */
    private String transport;

    /**
     *
     */
    private Date officialTime;

    /**
     *
     */
    private String modifyUser;

    /**
     * 加密key
     */
    private String encryptionKey;

    /**
     * 解密key
     */
    private String decryptKey;

    /**
     * 流水号：全部默认v2
     */
    private String snVer;

    /**
     * 调用方式：1动态监控 2风险扫描与动态监控 3一次性查询 4定期全量监控
     */
    private String callMethod;

    /**
     * 文件加密方式 0 不加密 1 流加密 2 压缩加密
     */
    private String fileEncryptionMethods;

    /**
     * 文件加密算法 0 AES-128-CBC 1 AES-256-CBC
     */
    private String fileEncryptionAlgorithm;

    /**
     * 文件加密key
     */
    private String fileEncryptionKey;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 是否输出数据产品 0 否 1 是
     */
    private Integer isOutputDataProduct;

    /**
     * 0 内部用户,1 银行,2 非银行,3 催收用户,4 保险用户,5 其他
     */
    private String applyLoanType;

    /**
     * 客户类型
     */
    private String apiType;

    /**
     * 短信类别
     */
    private String smsCategory;

    /**
     * 一级部门
     */
    private String firstDepartment;

    /**
     * 二级部门
     */
    private String secondDepartment;

    /**
     * 无id关联的产品json
     */
    private String mealJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Byte getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Byte taskTime) {
        this.taskTime = taskTime;
    }

    public Byte getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Byte finishDate) {
        this.finishDate = finishDate;
    }

    public Byte getPushCustomer() {
        return pushCustomer;
    }

    public void setPushCustomer(Byte pushCustomer) {
        this.pushCustomer = pushCustomer;
    }

    public Byte getCheckBlackList() {
        return checkBlackList;
    }

    public void setCheckBlackList(Byte checkBlackList) {
        this.checkBlackList = checkBlackList;
    }

    public Byte getCheckRedisNumber() {
        return checkRedisNumber;
    }

    public void setCheckRedisNumber(Byte checkRedisNumber) {
        this.checkRedisNumber = checkRedisNumber;
    }

    public Byte getSaveLog() {
        return saveLog;
    }

    public void setSaveLog(Byte saveLog) {
        this.saveLog = saveLog;
    }

    public Byte getSort() {
        return sort;
    }

    public void setSort(Byte sort) {
        this.sort = sort;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getExtendConfigInfo() {
        return extendConfigInfo;
    }

    public void setExtendConfigInfo(String extendConfigInfo) {
        this.extendConfigInfo = extendConfigInfo == null ? null : extendConfigInfo.trim();
    }

    public String getExpireDay() {
        return expireDay;
    }

    public void setExpireDay(String expireDay) {
        this.expireDay = expireDay == null ? null : expireDay.trim();
    }

    public Integer getPushThreadNum() {
        return pushThreadNum;
    }

    public void setPushThreadNum(Integer pushThreadNum) {
        this.pushThreadNum = pushThreadNum;
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl == null ? null : pushUrl.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName == null ? null : shortName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(Integer isCheck) {
        this.isCheck = isCheck;
    }

    public Integer getIsCharging() {
        return isCharging;
    }

    public void setIsCharging(Integer isCharging) {
        this.isCharging = isCharging;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode == null ? null : requestCode.trim();
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode == null ? null : responseCode.trim();
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport ;
    }

    public Date getOfficialTime() {
        return officialTime;
    }

    public void setOfficialTime(Date officialTime) {
        this.officialTime = officialTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser == null ? null : modifyUser.trim();
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey == null ? null : encryptionKey.trim();
    }

    public String getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(String decryptKey) {
        this.decryptKey = decryptKey == null ? null : decryptKey.trim();
    }

    public String getSnVer() {
        return snVer;
    }

    public void setSnVer(String snVer) {
        this.snVer = snVer == null ? null : snVer.trim();
    }

    public String getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(String callMethod) {
        this.callMethod = callMethod;
    }

    public String getFileEncryptionMethods() {
        return fileEncryptionMethods;
    }

    public void setFileEncryptionMethods(String fileEncryptionMethods) {
        this.fileEncryptionMethods = fileEncryptionMethods;
    }

    public String getFileEncryptionAlgorithm() {
        return fileEncryptionAlgorithm;
    }

    public void setFileEncryptionAlgorithm(String fileEncryptionAlgorithm) {
        this.fileEncryptionAlgorithm = fileEncryptionAlgorithm;
    }

    public String getFileEncryptionKey() {
        return fileEncryptionKey;
    }

    public void setFileEncryptionKey(String fileEncryptionKey) {
        this.fileEncryptionKey = fileEncryptionKey == null ? null : fileEncryptionKey.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Integer getIsOutputDataProduct() {
        return isOutputDataProduct;
    }

    public void setIsOutputDataProduct(Integer isOutputDataProduct) {
        this.isOutputDataProduct = isOutputDataProduct;
    }

    public String getMealJson() {
        return mealJson;
    }

    public void setMealJson(String mealJson) {
        this.mealJson = mealJson == null ? null : mealJson.trim();
    }

    public String getApplyLoanType() {
        return applyLoanType;
    }

    public void setApplyLoanType(String applyLoanType) {
        this.applyLoanType = applyLoanType;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType == null ? null : apiType.trim();
    }

    public String getSmsCategory() {
        return smsCategory;
    }

    public void setSmsCategory(String smsCategory) {
        this.smsCategory = smsCategory == null ? null : smsCategory.trim();
    }

    public String getFirstDepartment() {
        return firstDepartment;
    }

    public void setFirstDepartment(String firstDepartment) {
        this.firstDepartment = firstDepartment == null ? null : firstDepartment.trim();
    }

    public String getSecondDepartment() {
        return secondDepartment;
    }

    public void setSecondDepartment(String secondDepartment) {
        this.secondDepartment = secondDepartment == null ? null : secondDepartment.trim();
    }
}