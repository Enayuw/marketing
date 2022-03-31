package com.br.marketing.dto;

import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Data
public class PhoneSaleRecordInfoDTO {
    private String apiCode;
    private Set<String> custNums;
    private String transferType;
    private String startDate;
    private String endDate;
}
