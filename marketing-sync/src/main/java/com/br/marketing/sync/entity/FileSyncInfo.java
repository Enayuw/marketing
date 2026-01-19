package com.br.marketing.sync.entity;

import com.br.marketing.entity.SyncConfig;
import lombok.Data;

/**
 * 文件同步信息
 */
@Data
public class FileSyncInfo {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件路径（源路径）
     */
    private String filePath;
    
    /**
     * 文件创建/修改时间
     */
    private String createTime;
    
    /**
     * 文件大小
     */
    private long fileSize;
    
    /**
     * 文件后缀：txt, csv, zip, success, finish, no_suffix
     */
    private String suffix;
    
    /**
     * 日期（用于checkFinishSuccess校验，格式：yyyyMMdd）
     */
    private String date;
    
    /**
     * 同步配置（同步阶段使用）
     */
    private SyncConfig config;


    
    /**
     * 构造函数 - 完整参数
     */
    public FileSyncInfo(String fileName, String filePath, String createTime, long fileSize, 
                        String suffix, SyncConfig config) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.createTime = createTime;
        this.fileSize = fileSize;
        this.suffix = suffix;
        this.config = config;
    }

}
