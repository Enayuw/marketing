package com.br.marketing.service.sftp;

import com.br.marketing.entity.LoanFile;

/**
 * @Author: Bairong
 * @Time: 2020/11/20 15:30
 * @Company：百融
 * @Description: 压缩包文件校验
 */
public interface ZipFileCheckService {

    /**
     * 校验压缩包文件与源文件的大小
     * @param file 文件信息
     */
    public void zipFileCheck(LoanFile file);
}
