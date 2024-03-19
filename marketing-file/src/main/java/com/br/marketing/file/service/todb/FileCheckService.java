package com.br.marketing.file.service.todb;

import com.br.marketing.file.dto.FileContext;

/**
 * Created by Bairong on 2020/1/15.
 */
public interface FileCheckService {
    boolean checkDataFile(String path, String filename);

    boolean strategyIdCheck(String apiCode, String strategyId);

    boolean checkSmallDataFile(FileContext context);
}
