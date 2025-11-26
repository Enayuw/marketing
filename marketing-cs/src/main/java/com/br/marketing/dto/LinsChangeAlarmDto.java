package com.br.marketing.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LinsChangeAlarmDto {
    private String cardTitle;
    private Integer lineSupplierChangeCount = 0;
    private List<String> lineSupplierChangeList = new ArrayList<>();
}
