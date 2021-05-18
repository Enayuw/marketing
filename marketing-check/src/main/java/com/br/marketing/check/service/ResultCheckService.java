package com.br.marketing.check.service;

public interface ResultCheckService {
    /**
     * 校验ftp上的文件与本地磁盘上的文件是否一致
     * 校验内容包括：大小、文件个数、文件行数
     * @param apiCode apiCode
     */
    void taskResultCheck(String apiCode);
}
