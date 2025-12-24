package com.br.marketing.enums.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileServerType {

    SFTP("sftp","sftp服务器"),
    FTP("ftp","ftp服务器"),
    LOCALDISK("localDisk","本地"),
    MINIO("minIO","minIO服务器"),

    ;

    private String serverType;

    private String desc;




}
