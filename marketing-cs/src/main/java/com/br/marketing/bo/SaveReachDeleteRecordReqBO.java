package com.br.marketing.bo;

import com.br.marketing.client.qifu.SaveReachDeleteRecordReq;
import com.br.marketing.mapper.QifuSaveReachDeleteRecordApiPushLogMapper;

/**
 * 奇富保存触达删除记录业务封装
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-25 21:10
 */
public class SaveReachDeleteRecordReqBO {

    private SaveReachDeleteRecordReq req;
    private Long logId;
    private String apiCode;
    private String appletDate;
    private QifuSaveReachDeleteRecordApiPushLogMapper mapper;

    public SaveReachDeleteRecordReqBO(SaveReachDeleteRecordReq req, Long logId, String apiCode, String appletDate, QifuSaveReachDeleteRecordApiPushLogMapper mapper) {
        this.req = req;
        this.logId = logId;
        this.apiCode = apiCode;
        this.appletDate = appletDate;
        this.mapper = mapper;
    }

    public SaveReachDeleteRecordReqBO() {
    }

    public SaveReachDeleteRecordReq getReq() {
        return req;
    }

    public void setReq(SaveReachDeleteRecordReq req) {
        this.req = req;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate;
    }

    public QifuSaveReachDeleteRecordApiPushLogMapper getMapper() {
        return mapper;
    }

    public void setMapper(QifuSaveReachDeleteRecordApiPushLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String toString() {
        return "SaveReachDeleteRecordReqBO{" +
                "req=" + req +
                ", logId=" + logId +
                ", apiCode='" + apiCode + '\'' +
                ", appletDate='" + appletDate + '\'' +
                ", mapper=" + mapper +
                '}';
    }
}
