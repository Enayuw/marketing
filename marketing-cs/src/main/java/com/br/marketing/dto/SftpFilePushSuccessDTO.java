package com.br.marketing.dto;

/**
 * sftpFile推送成功数据量
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/17 19:52
 */
public class SftpFilePushSuccessDTO {
    /**
     * 2022/11/17 19:56
     * 文件id
     */
    private Long localId;
    /**
     * 2022/11/17 19:56
     * 推送成功数据量
     */
    private int pushSum;

    public SftpFilePushSuccessDTO(Long localId, int pushSum) {
        this.localId = localId;
        this.pushSum = pushSum;
    }

    public SftpFilePushSuccessDTO() {
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public int getPushSum() {
        return pushSum;
    }

    public void setPushSum(int pushSum) {
        this.pushSum = pushSum;
    }
}
