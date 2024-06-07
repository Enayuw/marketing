package com.br.marketing.service.Impl.zhongbang;

import java.time.LocalDate;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 众邦
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-25 18:34
 */
public interface ZhongBangService {

    /**
     * 2023-08-25 18:35
     * 转化数据推送daas与外呼
     */
    void pushTransferToDaasRealTimeUserOneAndCustomer(String apiCode, ThreadPoolExecutor threadPool, String... dateTimeStr);


    /**
     * 2023-11-15 15:36
     * 查询文件并下载
     *
     * @param apiCode   apiCode
     * @param cid       cid
     * @param fileName  文件名称
     * @param tableHead 表头
     * @param filePath  文件路径
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @param threadPool 线程池
     * @return true 文件下载成功
     */
    boolean fileQueryAndDownload(String apiCode, String cid, String fileName
            , String tableHead, String filePath, String beginDate, String endDate, ThreadPoolExecutor threadPool);

    /**
     * 2024-05-09 10:36
     * 上传语音文件
     *
     * @param apiCode   apiCode
     * @param cid       cid
     * @param localDate 日期
     * @return true 文件下载成功
     */
    boolean voiceFileUpload(String apiCode, String cid, LocalDate localDate);
}
