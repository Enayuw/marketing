package com.br.marketing.file.service.filecheck;

import com.br.marketing.file.model.dto.FileContext;
import com.br.marketing.file.common.enums.ErrorFileTypeEnum;

/**
 * Created by Bairong on 2020/1/15.
 */
public interface FileCheckService {
    boolean checkDataFile(String path, String filename);

    boolean strategyIdCheck(String apiCode, String strategyId);

    boolean checkSmallDataFile(FileContext context);

    void errorDetail(FileContext context, String errorMessage, ErrorFileTypeEnum errorFileTypeEnum);

    boolean checkConfig(String field, String value, String apiCode, String value1);
}
