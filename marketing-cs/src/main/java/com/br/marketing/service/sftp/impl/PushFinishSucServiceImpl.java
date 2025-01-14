package com.br.marketing.service.sftp.impl;

import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.sftp.PushFinishSucService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class PushFinishSucServiceImpl implements PushFinishSucService {
    @Autowired
    SyncConfigService syncConfigService;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Resource
    SyncConfigMapper loanSyncConfigMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Override
    public void pushFinish(Long fileId) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        StraHisFile file = straHisFileMapper.selectByPrimaryKey(fileId);
        if (!file.getSignFileStatus().equals(1)) {
            return;
        }
        SyncConfig config = new SyncConfig();
        config.setApiCode(file.getApiCode());
        config.setType(2);
        config.setDataType(DataTypeEnum.SCORE.getValue());
        config = loanSyncConfigMapper.queryConfigByConditaion(config);
        if (config == null) {
            log.error(String.format("apicode【%s】未配置sftp跑分文件路径", file.getApiCode()));
            return;
        }
        if (config.getCheckSuccess() == 1) {
            pushSuccess(file, sftpClient);
        }
    }

    private void pushSuccess(StraHisFile strafile, SftpClient sftpClient) {
        try {
            sftpClient.connect();
            String apiCode = strafile.getApiCode();
            String remotePath = "/UploadFiles/marketing/" + apiCode + "/output/" + DateHelper.getDateAddYyMmDd(0) + "/";
            String zipFileName = strafile.getZipfileName();
            String filePath = strafile.getFilePath();
            File file = new File(filePath + "/" + zipFileName);
            if (file.exists()) {
                String successFileName = zipFileName + ".success";
                File successFile = new File(syncConfigService.getPath() + "sftp_data/" + apiCode + "/" + successFileName);
                boolean newFile = true;
                if (!successFile.exists()) {
                    try {
                        newFile = successFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (newFile) {
                    log.warn("push success to sftp :{}", successFileName);
                    sftpClient.uploadFile(remotePath, successFileName, syncConfigService.getPath() + "sftp_data/" + apiCode + "/" + successFileName);
                }
            }
            sftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
