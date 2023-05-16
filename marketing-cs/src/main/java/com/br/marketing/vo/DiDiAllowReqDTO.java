package com.br.marketing.vo;

import lombok.Data;

@Data
public class DiDiAllowReqDTO {
    private String mobile;

    private Long id;

    public DiDiAllowReqDTO(String mobile,Long id){
        this.mobile = mobile;
        this.id = id;
    }
}
