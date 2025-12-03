package com.br.marketing.dto.tccpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrFailMsgSupplyDTO {

    private Integer failMsg;

    private Long magnitude;

    @JsonProperty("isSupply")
    private boolean isSupply;
}
