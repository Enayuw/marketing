package com.br.marketing.dto;

import com.br.marketing.entity.DataDistributeDetailLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataJoinLogDTO extends DataDistributeDetailLog {
    private Integer dataCode;
    private String dataMd5;
}
