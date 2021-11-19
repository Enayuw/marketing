package com.br.marketing.dto;

import lombok.Data;

import java.util.HashMap;

@Data
public class TxtToDbDTO {

    private String cid;

    private String apiCode;

    private Long localId;

    private String content;

    private Integer line;

    private HashMap<Integer, String> address;

    private HashMap<Integer, String> extSetField;
}
