package com.br.marketing.monkey.enums.ningbo;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    DOWNLOAD(0, "下载"),
    UPLOAD(1, "上传"),
    ;

    private Integer code;

    private String description;

    TaskTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
