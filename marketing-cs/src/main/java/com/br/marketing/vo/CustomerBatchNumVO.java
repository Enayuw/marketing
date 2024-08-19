package com.br.marketing.vo;

import com.br.marketing.dto.ScoreTimeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
public class CustomerBatchNumVO {

    @ApiModelProperty(value = "商户编号集合")
    @NotNull(message = "商户编个号不能为空")
    private Set<String> apiCodeSet;

    @ApiModelProperty(value = "跑分时间区间，多段")
    private List<ScoreTimeDTO> scoreTimeList;

    @ApiModelProperty(value = "场景集合")
    private Set<String> userTypeSet;

    private Integer current;

    private Integer size;


}
