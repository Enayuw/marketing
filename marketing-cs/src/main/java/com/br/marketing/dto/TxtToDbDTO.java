package com.br.marketing.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Data
public class TxtToDbDTO {

    private String cid;

    private String apiCode;

    private Long localId;

    private String content;

    private Integer line;

    private HashSet<String> fieldAll;

    private HashSet<String> fieldMust;

    private String dbName;

    private String errorMsg;

    private HashMap<Integer, String> address;

    private HashMap<Integer, String> extSetField;
}
