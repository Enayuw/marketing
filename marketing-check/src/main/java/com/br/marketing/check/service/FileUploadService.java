package com.br.marketing.check.service;

import com.br.marketing.common.utils.FtpUtil;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;

public interface FileUploadService {
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
     * @param ftpUtil ftpUtil
     */
    void parsingFile(String key, String fileName, String localFilePath, String apiCode,
                     MerchantParam merchantParam, String finishName, String batchNumber, MarketingTask lt, FtpUtil ftpUtil);
}
