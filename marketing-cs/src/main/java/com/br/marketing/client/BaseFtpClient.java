package com.br.marketing.client;
import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.SyncConfig;

import java.io.IOException;
import java.io.InputStream;

/**ftp客户端
 * @Author: Bairong
 * @Time: 2020/12/4 17:29
 * @Company：百融
 * @Description: 功能描述
 */
public abstract class BaseFtpClient {
    public String hostName ;
    public int port ;
    public String userName ;
    public String password ;
    public BaseFtpClient(){

    }

    public BaseFtpClient(String hostName, int port, String userName, String password) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }
    public BaseFtpClient(SyncConfig loanSyncConfig, boolean isSrc) {
        this.hostName = isSrc? loanSyncConfig.getSrcSftpHost() : loanSyncConfig.getTargetSftpHost();
        this.port =isSrc? loanSyncConfig.getSrcSftpPort() : loanSyncConfig.getTargetSftpPort();
        this.userName = isSrc? loanSyncConfig.getSrcSftpUser() : loanSyncConfig.getTargetSftpUser();
        this.password = isSrc? AESAlgorithmUtil.decrypt(loanSyncConfig.getSrcSftpPwd(), Constants.SFTP_P_SECRET_KEY)
                :AESAlgorithmUtil.decrypt(loanSyncConfig.getTargetSftpPwd(), Constants.SFTP_P_SECRET_KEY);
    }

    public static void main(String[] args) {
        String decrypt = AESAlgorithmUtil.decrypt(
                "9m3bbFRLa9OGe0NNAKFvfxjZDIEpd0j-bZokRLn_UeU"
                , Constants.SFTP_P_SECRET_KEY);
//        System.out.println(decrypt);
    }

    /**
     * 连接
     * @return 是否连接成功
     * @throws Exception
     */
    public abstract boolean connect() throws Exception;

    /**
     * 是否连接
     * @return
     */
    public abstract boolean isConnected();

    /**
     * 断开连接
     * @throws Exception
     */
    public abstract void disconnect() throws Exception;

    /**
     * 获取输入流
     * ftp获取输入流并操作完成之后需要先close流，并且调用ftpclient的completePendingCommand()方法
     * @param path 文件路径
     * @param fileName 文件名称
     * @return 输入流
     *
     */
    public abstract InputStream getInputStream(String path, String fileName)throws Exception;

    /**
     * 通过字节流上传文件到sftp
     * @param inputStream 字节流
     * @param path 文件路径
     * @param fileName 文件名称
     * @throws Exception
     */
    public abstract void uploadFile(InputStream inputStream,String path,String fileName)throws Exception;

    public abstract void uploadFileAndMk(InputStream inputStream,String path,String fileName) throws Exception;

    public abstract void mkdir(String realTargetPath) throws Exception;
}
