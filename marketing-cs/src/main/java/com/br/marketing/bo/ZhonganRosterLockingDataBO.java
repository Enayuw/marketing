package com.br.marketing.bo;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhonganRosterLockingData;

/**
 * 组合属性 BO
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/16 14:51
 */
public class ZhonganRosterLockingDataBO {
    private ZhonganRosterLockingData data;
    private MarketingSyncUser syncUser;
    private String apiCode;
    private String tag;

    public ZhonganRosterLockingDataBO(ZhonganRosterLockingData data, MarketingSyncUser syncUser, String apiCode, String tag) {
        this.data = data;
        this.syncUser = syncUser;
        this.apiCode = apiCode;
        this.tag = tag;
    }

    public ZhonganRosterLockingDataBO() {
    }

    public ZhonganRosterLockingData getData() {
        return data;
    }

    public void setData(ZhonganRosterLockingData data) {
        this.data = data;
    }

    public MarketingSyncUser getSyncUser() {
        return syncUser;
    }

    public void setSyncUser(MarketingSyncUser syncUser) {
        this.syncUser = syncUser;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
