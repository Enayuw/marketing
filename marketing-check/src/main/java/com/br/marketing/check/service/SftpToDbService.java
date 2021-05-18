package com.br.marketing.check.service;

import com.br.marketing.client.SftpClient;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:05
 * @Company：百融
 * @Description: 功能描述
 */
public interface SftpToDbService {
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
     * @param finishName finish文件名称
     * @param batchNumber 批次号
     * @param lt 任务对象
     * @param sftpClient ftpUtil
     */
    void parsingFile(String key, String fileName, String localFilePath, String apiCode,
                     MerchantParam merchantParam, String finishName, String batchNumber, MarketingTask lt, SftpClient sftpClient);

    void parsingDeleteFile(String key, String fileName, String toString, String apiCode, MerchantParam merchantParam, String finishName, SftpClient sftpClient);
}
