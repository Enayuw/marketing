package com.br.marketing.service.tc;

/**
 * 同程 sync_record 开始清洗时，异步通知灵霄 gods。
 */
public interface TcyrDataCleanNotifyService {

    /**
     * 异步调用 gods {@code /open/dingtalk/api-code-card/tcDataCleanNotify}，仅传批次号；失败不影响清洗主流程。
     */
    void notifyAsync(String batchNo);
}
