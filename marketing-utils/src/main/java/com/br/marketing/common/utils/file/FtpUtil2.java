package com.br.marketing.common.utils.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class FtpUtil2 {
    private FTPClient ftp = null;

    /**
     * @param path     上传到ftp服务器哪个路径下
     * @param addr     地址
     * @param port     端口号
     * @param username 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    public boolean connect(String path, String addr, int port,
                           String username, String password) throws Exception {
        boolean result = false;
        if (null == ftp) {
            ftp = new FTPClient();
        }
        int reply;
        ftp.connect(addr, port);
        ftp.login(username, password);
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            return result;
        }
        ftp.makeDirectory(path);
        ftp.changeWorkingDirectory(path);
        ftp.setBufferSize(1024 * 1024);
        ftp.enterLocalPassiveMode();
        result = true;
        return result;
    }

    public void change(String thisDay) throws IOException {
        ftp.makeDirectory(thisDay);
        ftp.changeWorkingDirectory(thisDay);
    }

    /**
     * 关闭ftp连接
     */
    public void closeFtp() {
        if (ftp != null && ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException e) {
                log.error("closeFtp error", e);
            }
        }
    }

    /**
     * @throws Exception
     */
    public void rename(String srcFname, String targetFname) throws Exception {
        log.info("rename srcFname:{},targetFname:{}", srcFname, targetFname);
        if (ftp != null) {
            try {
                ftp.rename(srcFname, targetFname);
            } catch (IOException e) {
                log.error("rename error", e);
            }
        } else {
            log.error("ftp is null error");
        }
    }

    /**
     * @param file 上传的文件或文件夹
     * @throws Exception
     */
    public boolean upload(File file) throws Exception {
        boolean flag = false;
        if (file.isDirectory()) {
            ftp.makeDirectory(file.getName());
            ftp.changeWorkingDirectory(file.getName());
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File file1 = new File(file.getPath() + "\\" + files[i]);
                if (file1.isDirectory()) {
                    upload(file1);
                    ftp.changeToParentDirectory();
                } else {
                    InputStream input = java.nio.file.Files.newInputStream(Paths.get(file.getPath() + "\\" + files[i]));
                    flag = ftp.storeFile(files[i], input);
                    if (!flag) {
                        log.error("上传文件失败{}", files[i]);
                    }
                    input.close();
                }
            }
        } else {
            InputStream input = java.nio.file.Files.newInputStream(file.toPath());
            flag = ftp.storeFile(file.getName(), input);
            if (!flag) {
                log.error("上传文件失败：{}", file.getName());
                //ftp.storeFile(file2.getName(), input);
            }
            input.close();
        }
        return flag;
    }

    public void deleteFile(String fileName) throws IOException {
        ftp.dele(fileName);
    }

    public boolean isExsits(String fileName) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftp.listFiles(fileName);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    public FTPFile[] listFiles() {
        FTPFile[] ftpFiles = null;
        try {
            ftpFiles = ftp.listFiles();
        } catch (IOException e) {
            log.error("listFiles error", e);
        }
        return ftpFiles;
    }


    /**
     * Description: 从FTP服务器下载文件
     *
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param tempFile
     * @return
     */
    public File downFile(String remotePath, String fileName, File tempFile) {
        try {
            // 转移到FTP服务器目录
            ftp.changeWorkingDirectory(remotePath);
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    OutputStream os = java.nio.file.Files.newOutputStream(tempFile.toPath());
                    ftp.retrieveFile(ff.getName(), os);
                    os.close();
                    return tempFile;
                }
            }
        } catch (IOException e) {
            log.error("downFile error", e);
        }
        return null;
    }

    public void changeWorkingDirectory(String remotePath) {
        // 转移到FTP服务器目录
        try {
            ftp.makeDirectory(remotePath);
            ftp.changeWorkingDirectory(remotePath);
        } catch (IOException e) {
            log.error("changeWorkingDirectory error", e);
        }
    }

    public String thisDay() {
        SimpleDateFormat toDay = new SimpleDateFormat("yyyyMMddHHmmss");
        String thisDay = toDay.format(new Date());
        return thisDay;
    }

    public FTPFile[] listFiles(String path, FTPFileFilter ftpFileFilter) {
        FTPFile[] ftpFiles = null;
        try {
            ftpFiles = ftp.listFiles(path, ftpFileFilter);
        } catch (IOException e) {
            log.error("listFiles error", e);
        }
        return ftpFiles;
    }
}
