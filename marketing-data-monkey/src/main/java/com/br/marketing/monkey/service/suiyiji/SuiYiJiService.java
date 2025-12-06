package com.br.marketing.monkey.service.suiyiji;

import org.springframework.stereotype.Service;

@Service
public interface SuiYiJiService {

    void originalToUpload(String apiCode);

    void blackToUpload(String apiCode);

    /**
     * 重试部分成功的撞库数据文件
     * @param apiCode API编码
     */
    void retryPartialSuccessFiles(String apiCode);

    /**
     * 重试部分成功的黑名单文件
     * @param apiCode API编码
     */
    void retryPartialSuccessBlackFiles(String apiCode);

}
