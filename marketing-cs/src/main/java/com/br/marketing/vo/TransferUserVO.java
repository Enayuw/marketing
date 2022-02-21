package com.br.marketing.vo;

import lombok.Data;

@Data
public class TransferUserVO {
    private Long id;
    private String taskId;
    private String custNum;
    private String groupType;
    private String transformTime;
    private String reserveField1;
    private String reserveField2;
    private String createTime;
}
