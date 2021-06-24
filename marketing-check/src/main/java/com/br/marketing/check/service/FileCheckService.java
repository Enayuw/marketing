package com.br.marketing.check.service;

import com.br.marketing.check.dto.FileContext;

/**
 * Created by Bairong on 2020/1/15.
 */
public interface FileCheckService {
    boolean checkDataFile(String path, String filename);

    boolean strategyIdCheck(String apiCode, String strategyId);

    boolean checkSmallDataFile(FileContext context);
}
