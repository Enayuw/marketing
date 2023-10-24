package com.br.marketing.entity;

import lombok.Data;

/**
 * 携程上报实体
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.entity
 * @Description: 携程上报实体
 * @CreateTime: 2022-07-18 20 :07
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Data
public class ThirdAdOuterReq {

    private String appId;

    private String timestamp;

    private String source;

    private String clickId;

    private String actionType;

    private String mktMode;

    private String mktChannel;

    private String mktProductNo;

    private String deviceInfo;

    public ThirdAdOuterReq(String timestamp,String source,String clickId,String actionType,String deviceInfo){
        this.timestamp = timestamp;
        this.source = source;
        this.clickId = clickId;
        this.actionType = actionType;
        this.deviceInfo = deviceInfo;
    }

    public ThirdAdOuterReq(String timestamp,String source,String clickId,String actionType,String deviceInfo
            ,String mktMode,String mktChannel,String mktProductNo,String appId){
        this.timestamp = timestamp;
        this.source = source;
        this.clickId = clickId;
        this.actionType = actionType;
        this.deviceInfo = deviceInfo;
        this.mktMode = mktMode;
        this.mktChannel = mktChannel;
        this.mktProductNo = mktProductNo;
        this.appId = appId;
    }
}
