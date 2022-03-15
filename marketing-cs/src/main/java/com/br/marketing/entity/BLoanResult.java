package com.br.marketing.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * Created by Bairong on 2020/6/9.
 */
@Data
public class BLoanResult extends BaseRowModel {
    private int id;
    private String apiCode;
    @ExcelProperty(value = "文件名称" ,index = 1)
    private String fileName;
    @ExcelProperty(value = "文件大小" ,index = 1)
    private String fileSize;
    @ExcelProperty(value = "文件实际数据量" ,index = 1)
    private int actualLines;
    @ExcelProperty(value = "应返回数据量" ,index = 1)
    private int expectedLines;
    @ExcelProperty(value = "上传时间" ,index = 1)
    private String uploadTime;
    private String createTime;

    @Override
    public String toString() {
        return "BLoanResult{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", actualLines=" + actualLines +
                ", expectedLines=" + expectedLines +
                ", uploadTime='" + uploadTime + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }

}
