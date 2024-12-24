package com.br.marketing.service.clean.smy;

public interface SmyDataCleanService {
    void cleanCustomizedUploadData(String apiCode, String date);

    void cleanCustomizedTransferData(String apiCode, String date);
}
