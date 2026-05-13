package com.br.marketing.monkey.service.ningbo;

import java.util.Date;

public interface NingBoBankDataService {

    void downloadFile(Date collectDate);

    void uploadFile(Date collectDate);
}
