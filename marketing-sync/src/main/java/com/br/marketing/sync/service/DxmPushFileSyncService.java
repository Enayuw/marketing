package com.br.marketing.sync.service;

/**
 * 度小满文件推送服务接口
 *
 * @ClassName DxmPushFileSyncService
 * @Description 推送内部SFTP上的文件到客户SFTP
 * @Author kongbx
 * @Date 2025/10/16 23:52
 */
public interface DxmPushFileSyncService {

    /**
     * 推送文件到客户SFTP
     * 
     * @param apiCode API编码，用于区分使用哪个配置
     */
    void pushToSftp(String apiCode);

}
