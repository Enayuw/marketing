package com.br.marketing.entity;

import lombok.Data;

@Data
public class SyncLog {
    private int id;
    private String apiCode;
    private String srcPath;
    private String targetPath;
    private String fileName;
    private String createFileTime;
    private String startTime;
    private String endTime;
    private String fileSize;
    private String createTime;
    private String updateTime;
    private int status;

    @Override
    public String toString() {
        return "LoanSyncLog{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", srcPath='" + srcPath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", createFileTime='" + createFileTime + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", status=" + status +
                '}';
    }
}
