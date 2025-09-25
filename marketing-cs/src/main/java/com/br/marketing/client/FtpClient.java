package com.br.marketing.client;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SyncConfig;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.nio.file.Paths;
/**ftp客户端
 * @Author: Bairong
 * @Time: 2020/12/4 17:34
 * @Company：百融
 * @Description: 功能描述
 */
@Slf4j
public class FtpClient extends BaseFtpClient {
    /**
     * 字符集
     */
    private static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * 超时时间
     */
    private static final int DEFAULT_TIMEOUT = 60 * 1000;
    /**
     * ftpClient对象
     */
    private FTPClient ftp;
    /**
     * 初始化时ftp服务器路径
     */
    private  String ftpBasePath = "";

    public FtpClient(String hostName, int port, String userName, String password, String ftpBasePath) {
        super(hostName, port, userName, password);
        ftp = new FTPClient();
        ftp.setControlEncoding(DEFAULT_CHARSET);
        this.ftpBasePath=ftpBasePath;
        setTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
    }

    public FtpClient(SyncConfig loanSyncConfig, boolean isSrc) {
        super(loanSyncConfig, isSrc);
        ftp = new FTPClient();
        ftp.setControlEncoding(DEFAULT_CHARSET);
        setTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
    }
    @Override
    public boolean  connect() throws Exception {
        try {
            ftp.connect(hostName, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                disconnect();
                log.error("Can't find FTP server {}" , hostName);
                return false;
            }
            if (!ftp.login(userName, password)) {
                disconnect();
                log.error("Can't find FTP server {}" , hostName);
                return false;
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            initFtpBasePath();
        } catch (Exception e) {
            log.error("Can't find FTP server {}" + hostName,e);
            return false;
        }
        return true;
    }
    @Override
    public boolean isConnected() {
        return ftp.isConnected();
    }
    @Override
    public void disconnect() throws Exception {
        if (null != ftp && ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException ex) {
                log.error("disconnect error",ex);
            }
        }
    }
    /**
     * 判断路径是否存在
     *
     * @param remotePath
     * @return
     * @throws SftpException
     */
    public boolean isExist(String remotePath)  {
        boolean flag = false;
        try {
            if(ftp.changeWorkingDirectory(remotePath)){
                log.warn("存在路径：{},{}",remotePath,ftp.printWorkingDirectory());
                flag = true;
            }
        }catch (Exception e) {
            log.warn("目录不存在：{}",remotePath);
        }
        return flag;
    }

    /**
     * 判断文件是否存在
     *
     * @param remoteFile
     * @return
     */
    public boolean isExistFile(String remoteFile) {
        try {
            FTPFile[] files = ftp.listFiles(remoteFile);
            return files.length > 0 && !files[0].isDirectory();
        } catch (IOException e) {
            log.error("检查文件是否存在时出错", e);
            return false;
        }
    }

    @Override
    public InputStream getInputStream(String path, String fileName) throws IOException {
        log.warn("path:{},fileName:{}",path,fileName);
        String localPath= Constants.TMP_FILE_PATH;
        File tmpFile = new File(localPath + fileName);
        if(!tmpFile.getParentFile().exists()){
            tmpFile.getParentFile().mkdirs();
        }
        boolean download = download(path + fileName, tmpFile);
        InputStream input=null;
        if(download){
            input = java.nio.file.Files.newInputStream(Paths.get(localPath + fileName));
        }
        return input;
    }


    /**
     * 下载ftp文件到本地上
     *
     * @param ftpFileName ftp文件路径名称
     * @param localFile   本地文件路径名称
     */
    public boolean download(String ftpFileName, File localFile)  {
        if(!localFile.getParentFile().exists()){
            localFile.getParentFile().mkdirs();
        }
        try (OutputStream out = new BufferedOutputStream( java.nio.file.Files.newOutputStream(localFile.toPath()));){
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFileName);
            if (fileInfoArray == null || fileInfoArray.length == 0) {
                log.error("File {} was not found on FTP server.",ftpFileName);
                return false;
            }

            FTPFile fileInfo = fileInfoArray[0];
//            if (fileInfo.getSize() > Integer.MAX_VALUE) {
//                log.error("File {}} is too large.",ftpFileName);
//                return false;
//            }

            if (!ftp.retrieveFile(ftpFileName, out)) {
                log.error("Error loading file {} from FTP server. Check FTP permissions and path.",ftpFileName);
                return false;
            }
            out.flush();
        }catch (Exception e){
            log.error("download error",e);
            return false;
        }
        return true;
    }
    @Override
    public void uploadFile(InputStream inputStream, String path, String fileName) throws Exception {
        try {
            if (!ftp.storeFile(path+fileName, inputStream)) {
                throw new IOException("Can't upload file '" + path+fileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            closeStream(inputStream);
        }
    }

    @Override
    public void uploadFileAndMk(InputStream inputStream, String path, String fileName) throws Exception {
        try {
            String tempPath = "";
            if(!ftp.changeWorkingDirectory(path)){
                String[] dirs = path.split("/");
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) {
                        continue;
                    }
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {  //进不去目录，说明该目录不存在
                        if (!ftp.makeDirectory(tempPath)) { //创建目录
                            //如果创建文件目录失败，则返回
                            throw new RuntimeException("创建目录失败 路径为："+tempPath);
                        } else {
                            //目录存在，则直接进入该目录
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }else{
                tempPath = path;
            }
            if (!tempPath.endsWith("/")) {
                tempPath = tempPath.concat("/");
            }
            if (!ftp.storeFile(tempPath+fileName, inputStream)) {
                throw new IOException("Can't upload file '" + fileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            closeStream(inputStream);
        }
    }

    @Override
    public void mkdir(String realTargetPath) throws IOException {
        log.info("ftp mkdir {}", realTargetPath);
        String[] split = realTargetPath.split("/");
        StringBuilder realPath = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (StringUtils.isNotEmpty(s)) {
                realPath.append("/").append(s);
                if (!isExist(realPath.toString())) {
                    log.info("ftp mkdir realPath {} ", realPath);
                    boolean b = ftp.makeDirectory(realPath.toString());
                    if (!b) {
                        log.warn("ftp mkdir {} error", realPath);
                    }
                }
            }
        }
    }

    /**
     * 连接ftp时保存刚登陆ftp时的路径
     */
    private void initFtpBasePath() throws IOException {
        if (StringUtils.isEmpty(ftpBasePath)) {
            ftpBasePath = ftp.printWorkingDirectory();
        }
    }
    /**
     * 设置超时时间
     * @param defaultTimeout 超时时间
     * @param connectTimeout 超时时间
     * @param dataTimeout    超时时间
     */
    private void setTimeout(int defaultTimeout, int connectTimeout, int dataTimeout) {
        ftp.setDefaultTimeout(defaultTimeout);
        ftp.setConnectTimeout(connectTimeout);
        ftp.setDataTimeout(dataTimeout);
    }
    /**
     * 关闭流
     * @param stream 流
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                log.error("closeStream error",ex);
            }
        }
    }

    public boolean completePendingCommand() throws IOException {
        boolean b = ftp.completePendingCommand();
        return b;
    }

    public  FTPFile[] listFiles(String path, FTPFileFilter ftpFileFilter) {
        FTPFile[] ftpFiles=null;
        try {
            ftpFiles = ftp.listFiles(path,ftpFileFilter);
        } catch (IOException e) {
            log.error("listFiles error",e);
        }
        return ftpFiles;
    }

    public FTPFile getFtpFile(String path,String fileName) throws IOException {
        ftp.changeWorkingDirectory(path);
        FTPFile[] ftpFiles = ftp.listFiles(fileName);
        FTPFile ftpFile = ftpFiles[0];
        return ftpFile;
    }

    public  boolean isExsits(String fileName)throws IOException{
        boolean flag = false;
        FTPFile[] ftpFileArr = ftp.listFiles(fileName);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }
    
    public  FTPFile[] listFiles(String path) {
        FTPFile[] ftpFiles=null;
        try {
            // 先检查目录是否存在
            if (!isExist(path)) {
                log.warn("【文件同步】FTP目录不存在: {}", path);
                // 返回空数组而不是null
                return new FTPFile[0];
            }
            
            // 目录存在，切换并列出文件
            if (ftp.changeWorkingDirectory(path)) {
                ftpFiles = ftp.listFiles();
                // 防止返回null
                if (ftpFiles == null) {
                    ftpFiles = new FTPFile[0];
                }
            } else {
                log.warn("【文件同步】无法切换到FTP目录: {}", path);
                return new FTPFile[0];
            }
        } catch (IOException e) {
            // 出错时返回空数组
            log.error("【文件同步】列出FTP目录文件时出错, path: {}", path, e);
            return new FTPFile[0];
        }
        return ftpFiles;
    }
}
