package com.br.marketing.dto;

/**
 * sftpFile推送数据量
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
     * 数据量
     */
    private int number;

    public SftpFilePushSuccessDTO(Long localId, int number) {
        this.localId = localId;
        this.number = number;
    }

    public SftpFilePushSuccessDTO() {
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
