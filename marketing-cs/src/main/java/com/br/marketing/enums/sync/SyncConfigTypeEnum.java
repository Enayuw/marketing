package com.br.marketing.enums.sync;

import lombok.Getter;

/**
 * 定制文件拉取类型
 *
 * @author zhen.Li1
 * @date 2026/01/21
 */
@Getter
public enum SyncConfigTypeEnum {


    DOWNLOAD_SYNC_TYPE("1", "下载同步类型"),

    UPLOAD_SYNC_TYPE("2", "上传同步类型");

    private String code;

    private String desc;

    SyncConfigTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
