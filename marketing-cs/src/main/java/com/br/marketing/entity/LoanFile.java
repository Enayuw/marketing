package com.br.marketing.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import java.util.List;

/**
 * Created by Bairong on 2019/10/18.
 */
@Data
public class LoanFile extends BaseRowModel {
    private Long id;
    private String apiCode;
    private String filePath;
    private String createTime;
    private String updateTime;
    private Integer status;
    private Integer type;
    @ExcelProperty(value = "批次号" ,index = 0)
    private String batchNumber;
    private String zipFileName;
    private String errorFile;
    @ExcelProperty(value = "应返回数据量" ,index = 1)
    private Integer expectedNum;
    @ExcelProperty(value = "文件实际数据量" ,index = 2)
    private Integer actualNum;
    @ExcelProperty(value = "实际返回文件数" ,index = 3)
    private Integer fileNum;
    @ExcelProperty(value = "文件总大小" ,index = 4)
    private String fileSize;
    @ExcelProperty(value = "上传时间" ,index = 5)
    private String uploadTime;
    private Integer zipStatus;
    private boolean skip;
    private String md5;
    private Integer dataType;
    private List<String> zipFileNames;
    private String statisticFilePath;
    private Integer scoreStatus;
    private String showTitle;
    private Integer pushType;
    private Integer pushStatus;
    private Integer indexNum;
    private String innerFtpPath;
    private String fileName;
    @Override
    public String toString() {
        return "LoanFile{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", filePath='" + filePath + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", batchNumber='" + batchNumber + '\'' +
                ", zipFileName='" + zipFileName + '\'' +
                ", errorFile='" + errorFile + '\'' +
                ", expectedNum=" + expectedNum +
                ", actualNum=" + actualNum +
                ", fileNum=" + fileNum +
                ", fileSize='" + fileSize + '\'' +
                ", uploadTime='" + uploadTime + '\'' +
                ", zipStatus=" + zipStatus +
                ", skip=" + skip +
                ", md5='" + md5 + '\'' +
                ", dataType='" + dataType + '\'' +
                ", showTitle='" + showTitle + '\'' +
                ", indexNum='" + indexNum + '\'' +
                ", innerFtpPath='" + innerFtpPath + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
