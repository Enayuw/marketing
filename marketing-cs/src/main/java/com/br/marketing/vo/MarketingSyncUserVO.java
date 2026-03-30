package com.br.marketing.vo;

import com.br.marketing.entity.MarketingSyncUser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MarketingSyncUserVO {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String apiCode;
    private String cusBatch;
    private String requestBatch;
    private String custNum;
    private String idCard;
    private String name;
    private String cell;
    private String cellMd5;
    private String cellSha256;
    private String cellOriginal;
    private String idCardOriginal;
    private String nameOriginal;
    private String groupType;
    private String userType;
    private String operateType;
    private String registerDate;
    private String reserveField1;
    private String reserveField2;
    private String createTime;
    private String updateTime;
    private String appletDate;
    private Integer status;
    private String failType;
    private String appletTime;
    private Integer isTask;
    private String taskTime;
    private Integer isRepeat;

    public static MarketingSyncUserVO fromEntity(MarketingSyncUser user) {
        if (user == null) {
            return null;
        }
        MarketingSyncUserVO vo = new MarketingSyncUserVO();
        vo.setApiCode(user.getApiCode());
        vo.setCusBatch(user.getCusBatch());
        vo.setRequestBatch(user.getRequestBatch());
        vo.setCustNum(user.getCustNum());
        vo.setIdCard(user.getIdCard());
        vo.setName(user.getName());
        vo.setCell(user.getCell());
        vo.setCellMd5(user.getCellMd5());
        vo.setCellSha256(user.getCellSha256());
        vo.setCellOriginal(user.getCellOriginal());
        vo.setIdCardOriginal(user.getIdCardOriginal());
        vo.setNameOriginal(user.getNameOriginal());
        vo.setGroupType(user.getGroupType());
        vo.setUserType(user.getUserType());
        vo.setOperateType(user.getOperateType());
        vo.setRegisterDate(user.getRegisterDate());
        vo.setReserveField1(user.getReserveField1());
        vo.setReserveField2(user.getReserveField2());
        vo.setCreateTime(formatDate(user.getCreateTime()));
        vo.setUpdateTime(formatDate(user.getUpdateTime()));
        vo.setAppletDate(user.getAppletDate());
        vo.setStatus(user.getStatus());
        vo.setFailType(user.getFailType());
        vo.setAppletTime(formatDate(user.getAppletTime()));
        vo.setIsTask(user.getIsTask());
        vo.setTaskTime(formatDate(user.getTaskTime()));
        vo.setIsRepeat(user.getIsRepeat());
        return vo;
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch;
    }

    public String getRequestBatch() {
        return requestBatch;
    }

    public void setRequestBatch(String requestBatch) {
        this.requestBatch = requestBatch;
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getCellMd5() {
        return cellMd5;
    }

    public void setCellMd5(String cellMd5) {
        this.cellMd5 = cellMd5;
    }

    public String getCellSha256() {
        return cellSha256;
    }

    public void setCellSha256(String cellSha256) {
        this.cellSha256 = cellSha256;
    }

    public String getCellOriginal() {
        return cellOriginal;
    }

    public void setCellOriginal(String cellOriginal) {
        this.cellOriginal = cellOriginal;
    }

    public String getIdCardOriginal() {
        return idCardOriginal;
    }

    public void setIdCardOriginal(String idCardOriginal) {
        this.idCardOriginal = idCardOriginal;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1;
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType;
    }

    public String getAppletTime() {
        return appletTime;
    }

    public void setAppletTime(String appletTime) {
        this.appletTime = appletTime;
    }

    public Integer getIsTask() {
        return isTask;
    }

    public void setIsTask(Integer isTask) {
        this.isTask = isTask;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public Integer getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Integer isRepeat) {
        this.isRepeat = isRepeat;
    }

}
