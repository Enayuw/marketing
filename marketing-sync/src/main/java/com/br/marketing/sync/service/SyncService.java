package com.br.marketing.sync.service;

import com.br.marketing.entity.SyncConfig;

/**
 * 文件同步接口类
 */
public interface SyncService {
    /**
     * 从客户的sfpt目录同步客户上传的文件到存量监控内部账号对应目录下
     * 1.只同步配置的文件类型
     * 2.按apiCode、文件名称、文件生成时间判断是否需要同步，如果按这三个字段从同步历史中能查询到结果，则不再同步
     * 3.按是否校验success文件、是否校验finish文件判断是否立即同步
     * 4.同步完成后需要对比两个目录下的文件大小是否一致
     */
    void getFromSftp();

    /**
     * 从存量监控sftp内部账号目录同步结果结果文件到对应的客户sftp目录
     * 1.只同步配置的文件类型
     * 2.按apiCode、文件名称、文件生成时间判断是否需要同步，如果按这三个字段从同步历史中能查询到结果，则不再同步
     * 3.按是否校验success文件、是否校验finish文件判断是否立即同步
     * 4.同步完成后需要对比两个目录下的文件大小是否一致
     */
    void putToSftp();

    void insertConfig(SyncConfig loanSyncConfig);
}
