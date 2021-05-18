package com.br.marketing.check.service;

/**
 * Created by Bairong on 2020/1/15.
 */
public interface FileCkeckServicce {
    boolean checkDataFile(String path, String filename);

    boolean strategyIdCheck(String apiCode, String strategyId);

    boolean checkSmallDataFile(String path, String filename,boolean flag,String batchNumber);
}
