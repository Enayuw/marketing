package com.br.marketing.vo.datagroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class DataGroupConfigVO {


    /**
     *
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @Schema(description = "apiCode")
    private String apiCode;


    /**
     * 分组规则，json类型
     */
    @Schema(description = "分组规则，json类型")
    private String groupRules;

    /**
     * 上传记录统计Id集合
     */
    @Schema(description = "上传记录统计Id集合")
    private String uploadReportId;

    /**
     * 1-有效；9-无效
     */
    @Schema(description = "1-有效；9-无效")
    private Integer isDel;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;


}
