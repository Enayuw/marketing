package com.br.marketing.service;

import com.br.marketing.entity.LocalFile;

public interface DewuCollidingDataService {

    void collidingDataProcess(Long localFileIds);
    void collidingDataUploadSyncProcess();
}
