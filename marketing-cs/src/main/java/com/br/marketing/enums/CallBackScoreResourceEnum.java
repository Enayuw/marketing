package com.br.marketing.enums;

public enum CallBackScoreResourceEnum {

    //捞取数据写入db或redis的线程数
    WriteDbThreadNumber("writeDbThreadNumber",10)
    //更新排序操作的线程数
    ,UpdateSortThreadNumber("updateSortThreadNumber",5)
    //推送客户的线程数
    ,PushCustomerThreadNumber("pushCustomerThreadNumber",5)
    //推送客户每页获取的数量
    ,PushCustomerDataPageNumber("pushCustomerDataPageNumber",1000)
    //推送众安获取taskId分页配置
    ,PushTaskPageByZhongBangNumber("pushTaskPageByZhongBangNumber",2000);

    CallBackScoreResourceEnum(String key,Integer value) {
        this.key = key;
        this.value=value;
    }

    private Integer value;

    private String key;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
