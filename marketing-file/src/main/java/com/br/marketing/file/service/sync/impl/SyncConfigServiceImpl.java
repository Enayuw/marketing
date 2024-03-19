package com.br.marketing.file.service.sync.impl;

import com.br.common.util.AESAlgorithmUtil;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.file.FileApplication;
import com.br.marketing.file.service.sync.FileSyncService;
import com.br.marketing.file.service.sync.SyncConfigService;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
@Service
@Slf4j
public class SyncConfigServiceImpl implements SyncConfigService {

    @Resource
    SyncConfigMapper loanSyncConfigMapper;

    @Override
    public void insertConfig(SyncConfig loanSyncConfig) {
        String srcSftpPwd = loanSyncConfig.getSrcSftpPwd();
        String targetSftpPwd = loanSyncConfig.getTargetSftpPwd();
        String encryptSrcSftpPwd = AESAlgorithmUtil.encrypt(srcSftpPwd, Constants.SFTP_P_SECRET_KEY);
        String encryptTargetSftpPwd = AESAlgorithmUtil.encrypt(targetSftpPwd, Constants.SFTP_P_SECRET_KEY);
        loanSyncConfig.setSrcSftpPwd(encryptSrcSftpPwd);
        loanSyncConfig.setTargetSftpPwd(encryptTargetSftpPwd);
        log.warn("loanSyncConfig:{}",loanSyncConfig);
        loanSyncConfigMapper.insertConfig(loanSyncConfig);
    }

}
