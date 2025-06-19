package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    SCORE(1,"跑分文件")
    ,ERROR(2,"错误文件")
    ,DIANXIAO(3,"电销")
    ,QIQI(4,"七七撞库")
    ,TRANSFER(5,"转化文件")
    ,FILETODB(6,"入库文件")
    ,OFFLINESCORE(7,"离线跑分文件")
    ,MARKETINGDATA(8,"原始数据文件")
    ,DXTRANSFER(9,"转化过滤")
    ,DXIBU(10,"ibu电销")
    ,MARKETINGTRANSFERDATA(11,"转化清洗文件")
    ,MARKETINGUPLOADDATA(12,"上传清洗文件");
    private Integer value;
    private String desc;

    /**
     * 根据value值获取描述
     */
    public static String fromDescByValue(Integer value) {
        Optional<DataTypeEnum> first = Arrays.stream(DataTypeEnum.values()).filter(t -> value.equals(t.getValue())).findFirst();
        if (first.isPresent()) {
            return first.get().getDesc();
        } else {
            return null;
        }
    }
}
