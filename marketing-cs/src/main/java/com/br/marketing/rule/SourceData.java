package com.br.marketing.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class SourceData extends InterfaceParams{
    /**
     * 原始数据id
     */
    @JsonIgnore
    private Long initId;
}
