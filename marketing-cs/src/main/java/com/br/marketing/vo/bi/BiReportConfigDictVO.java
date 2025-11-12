package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * BI报表 VO
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiReportConfigDictVO {

    @Schema(description = "字典key")
    private String dictKey;
    @Schema(description = "客户编号")
    private String apiCode;
    @Schema(description = "字典值")
    private String dictValue;
    @Schema(description = "字典描述")
    @JsonProperty(value = "dictDesc")
    private String dictDesc;
    @Schema(description = "创建时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Schema(description = "修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
