package com.br.marketing.dto.tccpa;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FilePushTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    //配置：文件提取总量级
    private Integer extraNumTotal;

    //配置：文件最大量级
    private Integer extraNumSingle;

    //脚本查询：每个脚本的查询量级
    private List<FilePushTaskScriptNumDTO> scriptNumDTOS;

    //脚本查询：脚本量级总计
    private Integer scriptNum;

    //期望提取量级
    private Integer extraNumExp;

    //生成的文件信息
    private List<FilePushTaskFileDTO> files;

    //实际提取量级
    private Integer extraNumAct;

    //错误信息
    private String message;

    private Boolean onlyOk;

}
