package com.br.marketing.dto.datagroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * 数据分组配置DTO
 *
 * @author zhen.li1
 * @dateTime 2024/11/07 20:49
 */
@Data
public class DataGroupConfgDTO {

    @Schema(description = "主键id")
    private Long id;

    /**
     * apiCode
     */
    @Schema(description = "apiCode")
    @NotEmpty
    private String apiCode;

    /**
     * 上传数据记录ID集合，多个,分割
     */
    @Schema(description = "上传数据记录ID集合，多个,分割")
    @NotEmpty
    private String ids;


    /**
     * 分组规则json格式
     */
    @Schema(description = "分组规则json格式")
    @NotEmpty
    private String groupRules;


    /**
     * 操作类型：0-新增，1-删除
     */
    @Schema(description = "操作类型：0-新增，1-删除")
    private String operType;


}
