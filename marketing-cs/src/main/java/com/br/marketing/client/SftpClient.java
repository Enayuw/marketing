package com.br.marketing.client;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SyncConfig;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

/**
 * The type Sftp config.
 */
@Slf4j
public class SftpClient extends BaseFtpClient{

    private JSch jSch = null;
    private ChannelSftp sftp = null;
    private Channel channel = null;
    private Session session = null;
    /** 连接建立超时（毫秒） */
    private static final int CONNECT_TIMEOUT = 60 * 1000;
    /** 会话读写空闲超时（毫秒），超过该时间无数据读写则超时 */
    private static final int SESSION_TIMEOUT = 5 * 60 * 1000;

    /**
     * Instantiates a new Sftp config.
     *
     * @param hostName the host name
     * @param port     the port
     * @param userName the user name
     * @param password the password
     */
    public SftpClient(String hostName, int port, String userName, String password) {
        super(hostName,port,userName,password);
    }

    /**
     * Instantiates a new Sftp config.
     *
     * @param loanSyncConfig the loan sync bean
     * @param isSrc          the is src
     */
    public SftpClient(SyncConfig loanSyncConfig, boolean isSrc) {
        super(loanSyncConfig, isSrc);
    }
    /**
     * 连接登陆远程服务器
     *
     * @return
     */
    public boolean connect() throws Exception {
        try {
            jSch = new JSch();
            session = jSch.getSession(userName, hostName, port);
            session.setPassword(password);
            session.setConfig(this.getSshConfig());

            session.connect(CONNECT_TIMEOUT);

            session.setTimeout(SESSION_TIMEOUT);

            channel = session.openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT);

            sftp = (ChannelSftp) channel;
            log.debug("登陆成功:{} 欢迎：{}" , sftp.getServerVersion(),userName);
        } catch (JSchException e) {
            log.error("SSH方式连接FTP服务器时有JSchException异常!\r\n"+userName+","+hostName+":"+port,e);
            throw e;
        }
        return true;
    }

    /**
     * 关闭连接
     *
     * @throws Exception
     */
    public void disconnect() throws Exception {
        try {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
            if (channel.isConnected()) {
                channel.disconnect();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
            log.debug("退出成功:{}",userName);
        } catch (Exception e) {
            log.error("SSH方式断开连接异常!",e);
            throw e;
        }
    }

    /**
     * 是否连接
     * @return
     */
    public boolean isConnected(){
        return sftp.isConnected();
    }
    /**
     * 获取服务配置
     *
     * @return
     */
    private Properties getSshConfig()  {
        Properties sshConfig = null;
        sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        return sshConfig;
    }


    /**
     * 获取sftp上文件的字节流
     * @param path 文件路径
     * @param fileName 文件名称
     * @return 字节流
     * @throws SftpException
     */
    public InputStream getInputStream(String path, String fileName) throws Exception {
        InputStream inputStream=null;
        if(StringUtils.isEmpty(path)||StringUtils.isEmpty(fileName)){
            return inputStream;
        }
        sftp.cd(path);
        inputStream = sftp.get(fileName);
        return inputStream;
    }

    /**
     * 通过字节流上传文件到sftp
     * @param inputStream 字节流
     * @param path 文件路径
     * @param fileName 文件名称
     * @throws SftpException
     */
    public void uploadFile(InputStream inputStream,String path,String fileName) throws SftpException {
        sftp.cd(path);
        sftp.put(inputStream,fileName);
    }

    /**
     * 获取源目录下需要同步的文件名称和文件属性
     * @param srcPath 原路径
     * @param suffix 文件类型
     * @return 需要同步的文件名称和文件属性
     * @throws SftpException
     */
    public Map<String, SftpATTRS> listFiles(String srcPath, String suffix) {
        Map<String, SftpATTRS> ftpFileMap = new HashMap();
        if (!isExist(srcPath)) {
            return ftpFileMap;
        }
        try {
            Vector<ChannelSftp.LsEntry> sftpFile = sftp.ls(srcPath);
            Iterator<ChannelSftp.LsEntry> sftpFileNames = sftpFile.iterator();
            ChannelSftp.LsEntry isEntity;
            String fileName;
            while (sftpFileNames.hasNext()) {
                isEntity = sftpFileNames.next();
                SftpATTRS attrs = isEntity.getAttrs();
                fileName = isEntity.getFilename();
                String[] split = suffix.split(",");
                for (int i = 0; i < split.length; i++) {
                    if (fileName.endsWith(split[i])) {
                        ftpFileMap.put(fileName, attrs);
                        break;
                    }
                }
            }
        } catch (SftpException e) {
            log.error("srcPath:{}", srcPath, e);
        }
        return ftpFileMap;
    }

    /**
     * 获取源目录下需要同步的文件名称和文件属性
     * @param srcPath 原路径
     * @return 需要同步的文件名称和文件属性
     * @throws SftpException
     */
    public Map<String, SftpATTRS> listFiles(String srcPath) {
        Map<String, SftpATTRS> ftpFileMap = new HashMap();
        if (!isExist(srcPath)) {
            return ftpFileMap;
        }
        try {
            Vector<ChannelSftp.LsEntry> sftpFile = sftp.ls(srcPath);
            ChannelSftp.LsEntry isEntity;
            String fileName;
            Iterator<ChannelSftp.LsEntry> sftpFileNames = sftpFile.iterator();
            while (sftpFileNames.hasNext()) {
                isEntity = sftpFileNames.next();
                SftpATTRS attrs = isEntity.getAttrs();
                fileName = isEntity.getFilename();
                ftpFileMap.put(fileName, attrs);
            }
        } catch (SftpException e) {
            log.error("srcPath:{}", srcPath, e);
        }
        return ftpFileMap;
    }

    /**
     * 获取源目录下需要同步的文件名称和文件属性（自定义选择器）
     *
     * @param srcPath  源路径
     * @param selector 选择器
     * @throws SftpException
     */
    public void listFiles(String srcPath, ChannelSftp.LsEntrySelector selector) {
        try {
            sftp.ls(srcPath, selector);
        } catch (SftpException e) {
            log.error("srcPath:" + srcPath, e);
        }
    }

    /**
     * 获取源目录下需要同步的文件名称和文件属性（自定义选择器）
     *
     * @param srcPath 源路径
     * @param suffix  自定义扩展名
     * @return 远程文件信息
     * @throws SftpException
     */
    public Map<String, SftpATTRS> listFiles(String srcPath, String... suffix) {
        CopyOnWriteArrayList<ChannelSftp.LsEntry> vector = new CopyOnWriteArrayList<>();
        String currentDir = ".";
        listFiles(srcPath, (ChannelSftp.LsEntry entry) -> {
            if (suffix == null || suffix.length == 0) {
                vector.add(entry);
                return ChannelSftp.LsEntrySelector.CONTINUE;
            } else {
                String filename = entry.getFilename();
                if (currentDir.equals(filename) || (currentDir + currentDir).equals(filename)) {
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                }
                for (String s : suffix) {
                    if (filename.endsWith(s)) {
                        vector.add(entry);
                        return ChannelSftp.LsEntrySelector.CONTINUE;
                    }
                }
            }
            return ChannelSftp.LsEntrySelector.CONTINUE;
        });
        return vector.stream().collect(Collectors.toMap(ChannelSftp.LsEntry::getFilename, ChannelSftp.LsEntry::getAttrs));
    }

    public Map<String, SftpATTRS> listFiles(String srcPath, Set<String> fileNameSet) {
        CopyOnWriteArrayList<ChannelSftp.LsEntry> vector = new CopyOnWriteArrayList<>();
        String currentDir = ".";
        listFiles(srcPath, (ChannelSftp.LsEntry entry) -> {
            if (fileNameSet == null || fileNameSet.size() == 0) {
                vector.add(entry);
                return ChannelSftp.LsEntrySelector.CONTINUE;
            } else {
                String filename = entry.getFilename();
                if (currentDir.equals(filename) || (currentDir + currentDir).equals(filename)) {
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                }
                if (fileNameSet.contains(filename)) {
                    vector.add(entry);
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                }
            }
            return ChannelSftp.LsEntrySelector.CONTINUE;
        });
        return vector.stream().collect(Collectors.toMap(ChannelSftp.LsEntry::getFilename, ChannelSftp.LsEntry::getAttrs));
    }


    /**
     * 获取源目录下需要同步的文件名称和文件属性
     *
     * @param srcPath 原路径
     * @param suffix  文件类型
     * @return 对应文件类型的文件列表
     * @throws SftpException
     */
    public List<String> listFileName(String srcPath, String suffix) {
        List<String> list = new ArrayList<>();
        if (!isExist(srcPath)) {
            return list;
        }
        try {
            ChannelSftp.LsEntry isEntity;
            String fileName;
            Vector<ChannelSftp.LsEntry> sftpFile = sftp.ls(srcPath);
            Iterator<ChannelSftp.LsEntry> sftpFileNames = sftpFile.iterator();
            while (sftpFileNames.hasNext()) {
                isEntity = sftpFileNames.next();
                fileName = isEntity.getFilename();
                String[] split = suffix.split(",");
                for (int i = 0; i < split.length; i++) {
                    if (fileName.endsWith(split[i])) {
                        list.add(fileName);
                        break;
                    }
                }
            }
        } catch (SftpException e) {
            log.error("srcPath:{}", srcPath, e);
        }
        return list;
    }

    /**
     * 获取文件属性
     * @param filePath 文件路径
     * @return 文件属性
     * @throws SftpException
     */
    public SftpATTRS stats(String filePath) throws SftpException {
        return sftp.stat(filePath);
    }

    /**
     * 递归创建目录
     *
     * @param path 目录
     * @throws SftpException
     */
    public void mkdir(String path) throws Exception {
        log.warn("ftp mkdir {}", path);
        String[] split = path.split("/");
        StringBuilder realPath = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (StringUtils.isNotEmpty(s)) {
                realPath.append("/").append(s);
                if (!isExist(realPath.toString())) {
                    sftp.mkdir(realPath.toString());
                }
            }
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param remoteFile 文件
     * @return
     * @throws SftpException
     */
    public boolean isExistFile(String remoteFile)  {
        boolean flag = false;
        try {
            sftp.ls(remoteFile);
            log.debug("存在文件：{}",remoteFile);
            flag = true;
        }catch  (SftpException e) {
            if (e.id == SSH_FX_NO_SUCH_FILE) {
                log.warn("文件不存在：{}",remoteFile);
                return flag;
            }
            log.error("Unexpected exception during ls files on sftp: [{}:{}]", e.id, e.getMessage());
        }
        return flag;
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
            sftp.cd(remotePath);
            log.debug("存在路径：{}",remotePath);
           flag = true;
        }catch (Exception e) {
            log.warn("目录不存在：{}",remotePath);
        }
        return flag;
    }


    public void rename(String oldName,String newName) throws SftpException {
        sftp.rename(oldName,newName);
    }
    /**
     * 上传文件至远程sftp服务器
     * @param remotePath
     * @param remoteFilename
     * @param localFileName
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFilename, String localFileName) throws Exception {
        boolean success = false;
        log.warn("开始上传文件！");
        File localFile = new File(localFileName);
        try (InputStream fis = Files.newInputStream(Paths.get(localFile.getPath()))) {
            if (!isExist(remotePath)) {
                mkdir(remotePath);
            }
            sftp.cd(remotePath);
            sftp.put(fis, remoteFilename);
            success = true;
        } catch (SftpException e) {
            log.error("SftpException", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
        return success;
    }


    @Override
    public void uploadFileAndMk(InputStream inputStream, String path, String fileName) throws Exception {
    }

    /**
     * 下载远程sftp服务器文件
     *
     * @param remotePath
     * @param remoteFilename
     * @param localFilename
     * @return
     */
    public boolean downloadFile(String remotePath, String remoteFilename, String localFilename) {
        File localFile = new File(localFilename);
        boolean success = false;
        try (OutputStream output = Files.newOutputStream(Paths.get(localFile.getPath()))) {
            if (null != remotePath && !"".equals(remotePath.trim())) {
                sftp.cd(remotePath);
            }
            sftp.get(remoteFilename, output);
            success = true;
            log.info("成功接收文件,本地路径：{}", localFilename);
        } catch (SftpException e) {
            log.error("接收文件时有SftpException异常!", e);
        } catch (IOException e) {
            log.error("接收文件时有I/O异常!", e);
        }
        return success;
    }

    /**
     * 下载远程sftp服务器文件
     *
     * @param remotePath
     * @param remoteFilename
     * @param localFilename
     * @return File
     */
    public File downloadLocalFile(String remotePath, String remoteFilename, String localFilename, SftpATTRS attrs) {
        File localFile = new File(localFilename);
        if (localFile.exists() && localFile.isFile()) {
            long size = attrs.getSize();
            int mTime = attrs.getMTime();
            // 大小和最后修改时间相同，则为同一文件，不进行下载
            if (size == localFile.length() && mTime == (localFile.lastModified() / 1000)) {
                return localFile;
            }
        }
        try (OutputStream output = Files.newOutputStream(Paths.get(localFile.getPath()))) {
            if (null != remotePath && !"".equals(remotePath.trim())) {
                sftp.cd(remotePath);
            }
            sftp.get(remoteFilename, output);
            if (log.isInfoEnabled()) {
                log.info("成功接收文件,本地路径：{}", localFilename);
            }
        } catch (SftpException e) {
            log.error("接收文件时有SftpException异常!", e);
        } catch (IOException e) {
            log.error("接收文件时有I/O异常!" + e.getMessage(), e);
        }
        return localFile;
    }

    public File downloadLocalFile(String remotePath, String remoteFilename, String localFilename)
            throws SftpException, IOException {
        File localFile = new File(localFilename);
        if (localFile.exists() && localFile.isFile()) {
            SftpATTRS attrs = stats(remotePath.concat("/").concat(remoteFilename));
            long size = attrs.getSize();
            int mTime = attrs.getMTime();
            // 大小和最后修改时间相同，则为同一文件，不进行下载
            if (size == localFile.length() && mTime == (localFile.lastModified() / 1000)) {
                return localFile;
            }
        }
        try (OutputStream output = Files.newOutputStream(Paths.get(localFile.getPath()))) {
            if (null != remotePath && !"".equals(remotePath.trim())) {
                sftp.cd(remotePath);
            }
            sftp.get(remoteFilename, output);
            if (log.isInfoEnabled()) {
                log.info("成功接收文件,本地路径：{}", localFilename);
            }
        }
        return localFile;
    }
}
