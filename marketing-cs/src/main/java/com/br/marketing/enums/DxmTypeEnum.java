package com.br.marketing.enums;

/**
 * @ClassName DxmTypeEnum
 * @Description 度小满文件执行类型
 * @Author kongbx
 * @Date 2025/10/23 11:09
 */
public enum DxmTypeEnum {

    PULL_AND_UPLOAD(0,"拉取上传")
    ,PULL_AND_TRANSFER(1,"拉取转化")
    ,CALLBACK(2,"回调");

    DxmTypeEnum(Integer value,String desc) {
        this.value = value;
        this.desc=desc;
    }

    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
