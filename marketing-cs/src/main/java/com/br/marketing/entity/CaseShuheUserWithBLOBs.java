package com.br.marketing.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.br.marketing.adapter.IToTransferSyncAdaptee;

import java.util.Date;

public class CaseShuheUserWithBLOBs extends CaseShuheUser implements IToTransferSyncAdaptee {
    /**
     * 业务字段
     */
    private String jsonData;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 预留字段2
     */
    private String reserveField2;

    public CaseShuheUserWithBLOBs() {
    }

    public CaseShuheUserWithBLOBs(String jsonData, String reserveField1, String reserveField2) {
        this.jsonData = jsonData;
        this.reserveField1 = reserveField1;
        this.reserveField2 = reserveField2;
    }

    public CaseShuheUserWithBLOBs(Long id, String apiCode, String custNum, String cell, String userType, String uploadDate, Date createTime, Date updateTime, String mobile, String biztype, String isBlack, String isTurn, String clcUsrFstLogTimAll, String clcUsrLstAppStaTim, String clcUsrIsoPhoTim, String clcUsrIsoIdtTim, String clcUsrIsoCrdTim, String clcUsrIsoInfTim, String clcUsrIsoAtoTim, String clcUsrAdtTimRcnLon, String clcUsrAdtLmtItr, String clcUsrFrtFqOrdTim, String clcUsrFstLndTimCshBtHl, Integer isTransfer, String errorInfo, String jsonData, String reserveField1, String reserveField2) {
        super(id, apiCode, custNum, cell, userType, uploadDate, createTime, updateTime, mobile, biztype, isBlack, isTurn, clcUsrFstLogTimAll, clcUsrLstAppStaTim, clcUsrIsoPhoTim, clcUsrIsoIdtTim, clcUsrIsoCrdTim, clcUsrIsoInfTim, clcUsrIsoAtoTim, clcUsrAdtTimRcnLon, clcUsrAdtLmtItr, clcUsrFrtFqOrdTim, clcUsrFstLndTimCshBtHl, isTransfer, errorInfo);
        this.jsonData = jsonData;
        this.reserveField1 = reserveField1;
        this.reserveField2 = reserveField2;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData == null ? null : jsonData.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2 == null ? null : reserveField2.trim();
    }

    @Override
    public String toString() {
        return "CaseShuheUserWithBLOBs{" +
                "jsonData='" + jsonData + '\'' +
                ", reserveField1='" + reserveField1 + '\'' +
                ", reserveField2='" + reserveField2 + '\'' +
                '}';
    }

    @Override
    public void adapteeRequest(MarketingTransferSyncUser transferSyncUser) {
        transferSyncUser.setApiCode(this.getApiCode());
        transferSyncUser.setCustNum(this.getCustNum());
        transferSyncUser.setUserType(this.getUserType());
        transferSyncUser.setLoginTime(this.getClcUsrFstLogTimAll());
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter includefilter = filters.addFilter();
        includefilter.addIncludes("isTurn"
                , "isBlack"
                , "clcUsrLstAppStaTim"
                , "clcUsrIsoPhoTim"
                , "clcUsrIsoIdtTim"
                , "clcUsrIsoCrdTim"
                , "clcUsrIsoInfTim"
        );
        String field1 = JSONObject.toJSONString(this, includefilter, SerializerFeature.WriteMapNullValue);
        transferSyncUser.setReserveField1(field1);
        transferSyncUser.setApplyTime(this.getClcUsrIsoAtoTim());
        transferSyncUser.setAuditTime(this.getClcUsrAdtTimRcnLon());
        transferSyncUser.setAuditAmount(this.getClcUsrAdtLmtItr());
        transferSyncUser.setApplyTime(this.getClcUsrFrtFqOrdTim());
        transferSyncUser.setLentTime(this.getClcUsrFstLndTimCshBtHl());
    }
}