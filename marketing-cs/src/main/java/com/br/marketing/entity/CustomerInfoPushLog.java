package com.br.marketing.entity;

import java.util.Date;

public class CustomerInfoPushLog {
    /**
     * 
     */
    private Long id;

    /**
     * 任务id
     */
    private Long mId;

    /**
     * 发送的唯一键（任务id+自增值）
     */
    private String batch;

    /**
     * 请求参数
     */
    private String param;

    /**
     * 推送条数
     */
    private Integer pushNum;

    /**
     * 结果值
     */
    private String resultContent;

    /**
     * http状态码
     */
    private String httpStatus;

    /**
     * 返回的状态码
     */
    private String code;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 
     */
    private String errorContent;

    /**
     * 00-成功，900001-程序错误，900002-公司不存在，900006-请求参数错误，900007-测试条数受限,900009-超出最大上传数量,900013-数据正在导入,900015-数据导入全部失败,900016-案件导入失败（部分成功部分失败）,900031-请求重复
     */
    private String realStauts;

    /**
     * 失败条数
     */
    private Integer failNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch == null ? null : batch.trim();
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param == null ? null : param.trim();
    }

    public Integer getPushNum() {
        return pushNum;
    }

    public void setPushNum(Integer pushNum) {
        this.pushNum = pushNum;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent == null ? null : resultContent.trim();
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus == null ? null : httpStatus.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getErrorContent() {
        return errorContent;
    }

    public void setErrorContent(String errorContent) {
        this.errorContent = errorContent == null ? null : errorContent.trim();
    }

    public String getRealStauts() {
        return realStauts;
    }

    public void setRealStauts(String realStauts) {
        this.realStauts = realStauts == null ? null : realStauts.trim();
    }

    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }
}