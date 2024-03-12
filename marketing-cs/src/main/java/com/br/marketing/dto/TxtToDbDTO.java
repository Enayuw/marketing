package com.br.marketing.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

@Data
public class TxtToDbDTO {

    private String cid;

    private String apiCode;

    private Long localId;

    /**
     * 该行文本内容
     */
    private String content;

    /**
     * 读取的文件行数
     */
    private Integer line;

    /**
     * key:line,value:content
     */
    private HashMap<Integer,String> datas;

    /**
     * 全部字段
     */
    private HashSet<String> fieldAll;

    /**
     * 驼峰字段映射下划线字段
     */
    private HashMap<String,String> fieldAllHm;

    /**
     * 必填字段
     */
    private HashSet<String> fieldMust;

    /**
     * 数据库表名
     */
    private String dbName;

    /**
     * 必填错误信息
     */
    private String errorMsg;

    /**
     * 数据位置对应的字段
     */
    private HashMap<Integer, String> address;

    /**
     * 扩展数据字段对应的位置
     */
    private HashMap<Integer, String> extSetField;
}
