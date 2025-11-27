package com.br.marketing.bridge.service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 众邦 AI
 *
 * @author zhen.li1
 * @dateTime 2025-11-14 10:34
 */
public interface ZhongBangAIService {


    /**
     * 2025-11-14 15:36
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




}
