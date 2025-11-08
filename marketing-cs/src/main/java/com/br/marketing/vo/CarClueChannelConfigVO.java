package com.br.marketing.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName CarClueChannelConfigVO
 * @Author kongbx
 * @Date 2025/5/15 11:07
 */
@Data
public class CarClueChannelConfigVO {

    @Schema(description = "客户id(客户表中的主键id)")
    private Long id;

    @Schema(description = "易车KA拉取时间")
    private String pullDate;

    @Schema(description = "外呼意向等级配置")
    private JSONObject intentionConfig;

    @Schema(description = "数据清洗类型 0-手动执行 1-自动执行")
    private Integer cleanType;

    @Schema(description = "数据推送类型 0-手动执行 1-自动执行")
    private Integer pullType;

    @Schema(description = "操作人id")
    private Long optUserId;

    @Schema(description = "操作人账户名")
    private String optUserName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "")
    private Date updateTime;

    @Schema(description = "1-有效；9-无效")
    private Integer isDel;


}
