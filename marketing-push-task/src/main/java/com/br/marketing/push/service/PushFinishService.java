package com.br.marketing.push.service;

public interface PushFinishService {
    /**
     * 上传当日的finish文件到sftp
     * @param apiCode apiCode
     */
    void pushFinish(String apiCode);

    void pushFinish(Long fileId);
}
