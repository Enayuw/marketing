package com.br.marketing.monkey.service.syj;

import org.springframework.stereotype.Service;

@Service
public interface SuiYiJiService {

    void originalToUpload(String apiCode);

    void blackToUpload(String apiCode);

}
