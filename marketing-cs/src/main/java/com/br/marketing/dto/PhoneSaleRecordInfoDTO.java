package com.br.marketing.dto;

import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Data
public class PhoneSaleRecordInfoDTO {
    private String apiCode;
    private Collection<String> custNums;
    private String transferType;
    private String startDate;
    private String endDate;
    private List<HashMap<String,String>> custNumAndApplets;
}
