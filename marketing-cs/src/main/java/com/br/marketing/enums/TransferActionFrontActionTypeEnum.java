package com.br.marketing.enums;

/**
 * b_transfer_action_front 转化数据预处理表中 action_type 字段的枚举
 * 数值是和apiCode绑定使用的，所以每个apiCode对数值的定义都不一样，所以没办法直接写出数值代表的含义
 * @Author yu.xia@brgroup.com
 * @Date 2024/7/24 19:45
 */
public enum TransferActionFrontActionTypeEnum {

    ZERO(0,"")
    ,ONE(1,"")
    ,TWO(2,"")
    ,THREE(3,"")
    ,FOUR(4,"")
    ,FIVE(5,"")
    ,SIX(6,"")
    ,SEVEN(7,"")
    ,EIGHT(8,"")
    ,NINE(9,"")
    ,TEN(10,"")
    ,ELEVEN(11,"")
    ,TWELVE(12,"")
    ,THIRTEEN(13,"")
    ,FOURTEEN(14,"")
    ,FIFTEEN(15,"")
    ,SIXTEEN(16,"")
    ;

    TransferActionFrontActionTypeEnum(Integer value, String desc) {
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
