package com.br.marketing.dto.msg.mq;

import java.io.Serializable;
import java.util.List;

/**
 * 接口数据信息
 *
 * @author Guo Zeqiang
 * @dateTime 2024-02-28 11:18
 */
public class ApiDataInfoDTO<T> implements Serializable {
    private static final long serialVersionUID = 3816179921837874894L;

    /**
     * 2024-02-28 11:24
     * 客户主键
     */
    private String cid;

    /**
     * 2024-02-28 11:24
     * 客户编码
     */
    private String apiCode;

    /**
     * 2024-02-28 11:24
     * 原始数据保存日志（info）表时间 yyyy-MM-ddThh:mm:ss
     */
    private String rawDataSaveTimeStr;

    /**
     * 2024-02-28 11:24
     * 原始数据保存日志（info）表日期 yyyy-MM-dd
     */
    private String rawDataSaveDateStr;

    /**
     * 2024-02-29 15:39
     * 请求批次
     */
    private String requestId;

    /**
     * 2024-02-29 15:50
     * 自定义数据
     */
    private List<T> argList;

    /**
     * 2024-02-28 13:34
     * 数据源
     */
    private MsgSourceEnum msgSource;


    public ApiDataInfoDTO() {
    }

    public ApiDataInfoDTO(String cid, String apiCode, String rawDataSaveTimeStr, String rawDataSaveDateStr, String requestId, List<T> argList) {
        this.cid = cid;
        this.apiCode = apiCode;
        this.rawDataSaveTimeStr = rawDataSaveTimeStr;
        this.rawDataSaveDateStr = rawDataSaveDateStr;
        this.requestId = requestId;
        this.argList = argList;
    }


    public ApiDataInfoDTO(String apiCode, String rawDataSaveTimeStr, String rawDataSaveDateStr, String requestId, List<T> argList) {
        this.apiCode = apiCode;
        this.rawDataSaveTimeStr = rawDataSaveTimeStr;
        this.rawDataSaveDateStr = rawDataSaveDateStr;
        this.requestId = requestId;
        this.argList = argList;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getRawDataSaveTimeStr() {
        return rawDataSaveTimeStr;
    }

    public void setRawDataSaveTimeStr(String rawDataSaveTimeStr) {
        this.rawDataSaveTimeStr = rawDataSaveTimeStr;
    }

    public String getRawDataSaveDateStr() {
        return rawDataSaveDateStr;
    }

    public void setRawDataSaveDateStr(String rawDataSaveDateStr) {
        this.rawDataSaveDateStr = rawDataSaveDateStr;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<T> getArgList() {
        return argList;
    }

    public void setArgList(List<T> argList) {
        this.argList = argList;
    }

    public MsgSourceEnum getMsgSource() {
        return msgSource;
    }

    private void setMsgSource(MsgSourceEnum msgSource) {
        this.msgSource = msgSource;
    }

    public ApiDataInfoDTO<T> addUploadMsgSource() {
        this.setMsgSource(MsgSourceEnum.UPLOAD);
        return this;
    }

    public ApiDataInfoDTO<T> addTransferMsgSource() {
        this.setMsgSource(MsgSourceEnum.TRANSFER);
        return this;
    }

    @Override
    public String toString() {
        return "ApiDataInfoDTO{" +
                "cid='" + cid + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", rawDataSaveTimeStr='" + rawDataSaveTimeStr + '\'' +
                ", rawDataSaveDateStr='" + rawDataSaveDateStr + '\'' +
                ", requestId='" + requestId + '\'' +
                ", argList=" + argList +
                ", msgSource=" + msgSource +
                '}';
    }

    /**
     * 2024-02-28 15:07
     * 消息来源枚举
     */
    public enum MsgSourceEnum {
        /**
         * 2024-02-28 15:08
         * 上传
         */
        UPLOAD(1),
        /**
         * 2024-02-28 15:08
         * 转化
         */
        TRANSFER(2);

        private int value;

        MsgSourceEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "MsgSourceEnum{" +
                    "value=" + value +
                    "} " + super.toString();
        }
    }

}
