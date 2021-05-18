package com.br.marketing.entity;

import lombok.Data;

@Data
public class ResultFileInfo {
    private String fileName;
    private long size;
    private String uploadTime;
    private boolean skip;
    private LoanFile loanFile;
    @Override
    public String toString() {
        return "ResultFileInfo{" +
                "fileName='" + fileName + '\'' +
                ", size=" + size +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }
}
