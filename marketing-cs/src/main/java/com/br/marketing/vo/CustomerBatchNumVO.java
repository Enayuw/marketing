package com.br.marketing.vo;

import java.util.List;
import java.util.Set;

import com.br.marketing.dto.ScoreTimeDTO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomerBatchNumVO extends BaseAuthPermissionData {

    @ApiModelProperty(value = "跑分时间区间，多段")
    private List<ScoreTimeDTO> scoreTimeList;

    @ApiModelProperty(value = "场景集合")
    private Set<String> userTypeSet;

    private Integer current;

    private Integer size;

}
