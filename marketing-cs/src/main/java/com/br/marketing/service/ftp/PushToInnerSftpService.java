package com.br.marketing.service.ftp;

import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class PushToInnerSftpService {

    private final static String TITLE = "【推送内部SFTP】";

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Value("${innerSftp.uploadpath:00}")
    private String upLoadPath;

    public Result push(String fileName, String ftpRelativePath, String fileFullPath) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();
            String uploadPath = upLoadPath.concat(ftpRelativePath);
            String successFullPath = fileFullPath.concat(".success");
            String successFileName = fileName.concat(".success");
            File successFile = new File(successFullPath);
            if (!successFile.exists()) {
                successFile.createNewFile();
            }
            boolean b = sftpClient.uploadFile(uploadPath, fileName, fileFullPath);
            if (b) {
                boolean b1 = sftpClient.uploadFile(uploadPath, successFileName, successFullPath);
                if (!b1) {
                    log.error(TITLE + "上传success文件异常, fileName: {}", fileName);
                }
            }
        } catch (Exception e) {
            log.error(TITLE + "推送异常, fileName: {}", fileName, e);
            return new Result().failure();
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                try {
                    sftpClient.disconnect();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        return new Result().success();
    }
}
