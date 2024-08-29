package com.br.marketing.vo.bi.param;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * BI报表查询参数
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "BI报表请求参数")
public class BiReportParam {

    @NotNull(message = "报表类型不能为空")
    @ApiModelProperty(value = "报表类型，必填字段")
    private String reportTypeName;

    @ApiModelProperty(value = "apiCode集合，做数据权限控制使用，可不传")
    private List<String> apiCodes;

    @ApiModelProperty(value = "自定义查询条件")
    private JSONObject condition;
}
