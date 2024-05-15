package com.br.marketing.dto;

import com.br.marketing.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;


public class PushInfoFilterDTO {

    @ApiModelProperty(value = "apicode")
    private String mApiCode;

    @ApiModelProperty(value = "推送日期开始时间")
    private String pushBeginTime;

    @ApiModelProperty(value = "推送日期结束时间")
    private String pushEndTime;

    @ApiModelProperty(value = "任务流水号")
    private String pushInfoId;

    @ApiModelProperty(value = "推送状态 1-执行中；2-执行成功；3-执行失败")
    private String mStatus;
    private List<String> mStatusList;

    @ApiModelProperty(value = "页号")
    private Integer current;

    @ApiModelProperty(value = "页大小")
    private Integer size;

    public List<String> getmStatusList() {
        if(mStatusList == null || mStatusList.size()<1){
            if(StringUtils.isNotBlank(mStatus)){
                List<String> mStatusListNew = new ArrayList<>();
                String[] split = mStatus.split(",");
                for(String item : split){
                    mStatusListNew.add(item);
                }
                this.mStatusList = mStatusListNew;
            }else{
                // do nothing
            }
        }
        return mStatusList;
    }

    public void setmStatusList(List<String> mStatusList) {
        this.mStatusList = mStatusList;
    }

    public String getmApiCode() {
        return mApiCode;
    }

    public void setmApiCode(String mApiCode) {
        this.mApiCode = mApiCode;
    }

    public String getPushBeginTime() {
        return pushBeginTime;
    }

    public void setPushBeginTime(String pushBeginTime) {
        this.pushBeginTime = pushBeginTime;
    }

    public String getPushEndTime() {
        return pushEndTime;
    }

    public void setPushEndTime(String pushEndTime) {
        this.pushEndTime = pushEndTime;
    }

    public String getPushInfoId() {
        return pushInfoId;
    }

    public void setPushInfoId(String pushInfoId) {
        this.pushInfoId = pushInfoId;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
