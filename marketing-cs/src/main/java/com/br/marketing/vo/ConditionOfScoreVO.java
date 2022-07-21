package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

public class ConditionOfScoreVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "条件")
    private String contentShow;

    public Long getId() {
        return id;
    }

    public ConditionOfScoreVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getContentShow() {
        return contentShow;
    }

    public ConditionOfScoreVO setContentShow(String contentShow) {
        this.contentShow = contentShow;
        return this;
    }
}
