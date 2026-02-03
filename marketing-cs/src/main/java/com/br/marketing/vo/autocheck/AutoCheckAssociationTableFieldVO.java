package com.br.marketing.vo.autocheck;

import lombok.Data;

import java.util.List;


@Data
public class AutoCheckAssociationTableFieldVO {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 对应字段
     */
    private List<FieldVO> fieldList;

    @Data
    public static class FieldVO {
        private String fieldName;
        private String fieldDesc;
    }
}
