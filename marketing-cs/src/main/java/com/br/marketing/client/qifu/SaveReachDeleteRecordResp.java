package com.br.marketing.client.qifu;

import com.br.marketing.client.qifu.enums.IsSucceedEnum;

/**
 * 保存触达删除记录响应
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-21 11:21
 */
public class SaveReachDeleteRecordResp extends BizData {

    private static final long serialVersionUID = -4242171297184554239L;
    /**
     * 2023-09-21 11:24
     * 操作是否成功  Y 成功 N 失败
     * 必填
     */
    private IsSucceedEnum isSucceed;
    /**
     * 2023-09-21 11:24
     * 提示信息 失败场景，原因
     * 必填
     */
    private String message;


    public SaveReachDeleteRecordResp(IsSucceedEnum isSucceed, String message) {
        this.isSucceed = isSucceed;
        this.message = message;
    }

    public SaveReachDeleteRecordResp() {
    }


    public IsSucceedEnum getIsSucceed() {
        return isSucceed;
    }

    public void setIsSucceed(IsSucceedEnum isSucceed) {
        this.isSucceed = isSucceed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SaveReachDeleteRecordResp{" +
                "isSucceed=" + isSucceed +
                ", message='" + message + '\'' +
                "} " + super.toString();
    }
}
