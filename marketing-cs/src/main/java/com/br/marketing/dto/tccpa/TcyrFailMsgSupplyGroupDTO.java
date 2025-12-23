package com.br.marketing.dto.tccpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrFailMsgSupplyGroupDTO {

    private String releaseTime;

    private List<TcyrFailMsgSupplyDTO> supplyInfo;
}
