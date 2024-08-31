package com.br.marketing.dto.zhijia;

import lombok.Data;

@Data
public class CityCountyDataDTO {


    /**
     * 是否匹配成功，成功true，失败false
     */
    private Boolean isMatch;

    /**
     * 城市ID
     */
    private Integer cId;

    /**
     * 区县ID
     */
    private Integer countyId;
    /**
     * 错误信息，未匹配返回错误信息
     */
    private String errorMsg;


}
