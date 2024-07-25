package com.br.marketing.client.zhijia.input;

import lombok.Data;

/**
 * @ClassName ReqAddZhiJiaClueDTO
 * @Description TODO
 * @Author kongbx
 * @Date 2024/7/10 17:17
 */
@Data
public class ReqAddZhiJiaClueDTO {

    /**
     * token验证
     */
    private String access_token;
    /**
     * mobile 手机号
     */
    private String mobile;
    /**
     * 加密手机号
     */
    private String mobilecode;
    /**
     * 城市id
     */
    private Integer cid;
    /**
     * 区县id
     */
    private Integer countyid;
    /**
     * 品牌id
     */
    private String brandid;
    /**
     * 车系id
     */
    private String seriesid;
    /**
     * 车型id
     */
    private String specid;
    /**
     * 首次上牌时间
     */
    private String firstregtime;
    /**
     * 车牌号前两位
     */
    private String platenum;
    /**
     * 行驶里程
     */
    private String mileage;
    /**
     * 合作方标识
     */
    private Integer appid;

}
