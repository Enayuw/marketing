package com.br.marketing.check.service;

import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.MerchantParam;

public interface DeleteMonitorService {
    /**
     * 从ftp下载压缩包文件
     * 解压zip文件
     * 校验txt文件名称
     * 读取txt文件内容
     * 解密txt文件中每一行的三要素
     * 将三要素信息写入到数据库
     * rename ftp的上文件名称
     * @param key 文件在ftp上的路径
     * @param fileName 压缩包文件名称
     * @param localFilePath 下载到本地的路径
     * @param apiCode 商户编号
     * @param merchantParam 商户配置
     * @param cusBatch 客户批次
     * @param ftp ftp
     */
    void parsingFile(String key, String fileName, String localFilePath, String apiCode, MerchantParam merchantParam,String cusBatch, FtpUtil2 ftp);
}
