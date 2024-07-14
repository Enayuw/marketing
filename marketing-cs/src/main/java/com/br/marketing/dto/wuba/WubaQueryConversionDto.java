package com.br.marketing.dto.wuba;

import lombok.Data;

import java.util.Date;

@Data
public class WubaQueryConversionDto {

    private Integer batchType;
    private Integer queryStatus;
    private Date pushTimeStart;
    private Date pushTimeEnd;

}