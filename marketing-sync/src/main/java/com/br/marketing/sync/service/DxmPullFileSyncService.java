package com.br.marketing.sync.service;

/**
 * 度小满文件同步服务接口
 * 
 * @ClassName DxmPullFileSyncService
 * @Description 拉取客户SFTP上的CSV文件，解密第一列手机号，生成新文件并上传到内部SFTP
 * @Author kongbx
 * @Date 2025/10/16 21:02
 */
public interface DxmPullFileSyncService {

    /**
     * 从客户SFTP拉取文件并处理
     */
    void getFromSftp(String apiCode);

}
