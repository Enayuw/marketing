package com.br.marketing.dto.mark;

import lombok.Data;

@Data
public class FlagDataCarryLogCell {

    private Long id;
    private String cellLog;
    private String cellSha256;
    private String flagRiskgroup;
}
