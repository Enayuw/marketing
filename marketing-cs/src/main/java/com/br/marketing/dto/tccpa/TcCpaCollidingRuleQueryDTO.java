package com.br.marketing.dto.tccpa;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleQueryDTO extends PageSearchDTO {

    @ApiModelProperty(value = "数据包名称")
    private String packageName;

    @ApiModelProperty(value = "启用禁用状态")
    private Integer enabled;

    @ApiModelProperty(value = "撞库日期开始")
    private String collidingDateBegin;

    @ApiModelProperty(value = "撞库日期结束")
    private String collidingDateEnd;

}
