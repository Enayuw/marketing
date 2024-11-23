package com.br.marketing.dto.datagroup;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * apiCode
     */
    @ApiModelProperty(value = "apiCode")
    @NotEmpty
    private String apiCode;

    /**
     * 上传数据记录ID集合，多个,分割
     */
    @ApiModelProperty(value = "上传数据记录ID集合，多个,分割")
    @NotEmpty
    private String ids;


    /**
     * 分组规则json格式
     */
    @ApiModelProperty(value = "分组规则json格式")
    @NotEmpty
    private String groupRules;


    /**
     * 操作类型：0-新增，1-删除
     */
    @ApiModelProperty(value = "操作类型：0-新增，1-删除")
    private String operType;


}
