package com.br.marketing.dto.zhijia;

import lombok.Data;

@Data
public class ZhiJiaCarInfoDTO {


    /**
     * 是否匹配成功，成功true，失败false
     */
    private Boolean isMatch;

    /**
     * 车牌
     */
    private Integer brandId;

    /**
     * 车系
     */
    private Integer seriesId;
    /**
     * 错误信息，未匹配返回错误信息
     */
    private String errorMsg;


}
