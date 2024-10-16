package com.br.marketing.vo.bi.param;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * BI报表配置请求参数
 *
 * @author dongshuo.he
 * @date 2024/09/18
 */
@Data
@ApiModel(value = "BI报表配置请求参数")
public class BiReportConfigParam {

    private static final long serialVersionUID = 2749250551270549133L;

    @NotNull(message = "报表apiCode不能为空")
    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @NotNull(message = "报表类型不能为空")
    @ApiModelProperty(value = "报表类型，必填字段")
    private String reportTypeName;

    @ApiModelProperty(value = "场景")
    private String userType;

    @ApiModelProperty(value = "报表统计日期")
    private String statisticDate;

    @ApiModelProperty(value = "自定义查询条件")
    private JSONObject condition;
}
