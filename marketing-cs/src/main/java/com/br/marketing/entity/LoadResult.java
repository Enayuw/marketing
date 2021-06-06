package com.br.marketing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Bairong on 2020/4/7.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoadResult {
    private Integer id;
    private String apiCode;
    private String cusBatch;
    private String fileName;
    private String message;
    private String status;
    private String batchNumber;
    private Integer actualNumber;
    private Integer taskNumber;
    private String type;



    public LoadResult(String apiCode, String cusBatch, String fileName, String message, String status, String batchNumber,
                      Integer actualNumber, Integer taskNumber, String type) {
        this.apiCode = apiCode;
        this.cusBatch = cusBatch;
        this.fileName = fileName;
        this.message = message;
        this.status = status;
        this.batchNumber = batchNumber;
        this.actualNumber = actualNumber;
        this.taskNumber = taskNumber;
        this.type = type;
    }

    @Override
    public String toString() {
        return "LoadResult{" +
                "apiCode='" + apiCode + '\'' +
                ", cusBatch='" + cusBatch + '\'' +
                ", fileName='" + fileName + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", actualNumber=" + actualNumber +
                ", taskNumber=" + taskNumber +
                ", type='" + type + '\'' +
                '}';
    }
}
