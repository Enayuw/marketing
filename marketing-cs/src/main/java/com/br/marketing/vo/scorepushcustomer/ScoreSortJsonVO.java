package com.br.marketing.vo.scorepushcustomer;

import lombok.Data;

@Data
public class ScoreSortJsonVO {
    private String sourceKey;
    private String mappingKey;
    private String sort;
    private Boolean first;
    private Integer dbNumber;
}
