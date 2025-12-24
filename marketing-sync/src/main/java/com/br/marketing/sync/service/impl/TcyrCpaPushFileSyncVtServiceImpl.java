package com.br.marketing.sync.service.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaPushFileTaskVtMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.SyncApplication;
import com.br.marketing.sync.service.TcyrCpaPushFileSyncVtService;
import com.br.marketing.util.TimeUtils;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class TcyrCpaPushFileSyncVtServiceImpl implements TcyrCpaPushFileSyncVtService {

    private final static String TITLE = "【TCYR CPA自动化-推送文件数据同步】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcyrCpaPushFileTaskVtMapper tcyrCpaPushFileTaskVtMapper;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private SyncLogMapper syncLogMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Override
    public void fileSync(String pushDate) {
        String apiCode = marketingCommonConfig.getTcyrCpaApiCode();
        //1.查询今天是否有待同步文件任务
        TcyrCpaPushFileTaskVtExample taskExample = new TcyrCpaPushFileTaskVtExample();
        TcyrCpaPushFileTaskVtExample.Criteria criteria = taskExample.createCriteria();
        if (StringUtils.isEmpty(pushDate)) {
            criteria.andPushDateEqualTo(new Date());
        } else {
            criteria.andPushDateEqualTo(TimeUtils.parseStringToDate(pushDate));
        }
        criteria.andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(TcCpaPushFileTaskStatusEnum.STATUS_INNER_SFTP.getValue())
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        List<TcyrCpaPushFileTaskVt> tasks = tcyrCpaPushFileTaskVtMapper.selectByExample(taskExample);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        Date pushTime = tasks.get(0).getPushTime();
        if (new Date().compareTo(pushTime) < 0) {
            return;
        }

        TcyrCpaCollidingTaskExample example = new TcyrCpaCollidingTaskExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue())
                .andStatusEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_PUSHING.getValue());
        List<TcyrCpaCollidingTask> collidingTasks = tcyrCpaCollidingTaskMapper.selectByExample(example);

        for (TcyrCpaPushFileTaskVt task : tasks) {
            TcyrCpaPushFileTaskVt updateTask = new TcyrCpaPushFileTaskVt();
            try {
                updateTask.setId(task.getId());
                fileSyncProcess(task, updateTask);
                updateTask.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_OPE_SFTP.getValue());
                collidingTasks.forEach(collidingTask -> {
                    collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_COMPLETED.getValue());
                });
            } catch (Exception e) {
                collidingTasks.forEach(collidingTask -> {
                    collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_FAIL.getValue());
                });
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "sftp同步异常！", TITLE), e);
            }
            collidingTasks.forEach(collidingTask -> {
                tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(collidingTask);
            });
            tcyrCpaPushFileTaskVtMapper.updateByPrimaryKeySelective(updateTask);
        }


    }

    private void fileSyncProcess(TcyrCpaPushFileTaskVt task, TcyrCpaPushFileTaskVt updateTask) {
        //1.查询sftp配置
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria()
                .andApiCodeEqualTo(task.getApiCode())
                .andStatusEqualTo(SyncConfigTypeEnum.STATUS_USABLE.getValue())
                .andTypeEqualTo(SyncConfigCustomizedTypeEnum.TC_CPA_PUSH_FILE.getCode())
                .andDataTypeEqualTo(DataTypeEnum.TC_CPA_PUSH_FILE.getValue())
                .andCustomizedTypeEqualTo(SyncConfigCustomizedTypeEnum.TC_CPA_PUSH_FILE.getCode());
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "未找到sftp配置，请检查！", TITLE));
        }
        for (SyncConfig syncConfig : syncConfigs) {
            fillDate(syncConfig);
            updateTask.setOpeSftpPath(syncConfig.getTargetPath());
            Map<String, List<String>> listMap = listFile(syncConfig);
            if (MapUtils.isEmpty(listMap) || (listMap.containsKey("csv") && !listMap.containsKey("ok"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "未找到ok标识文件，请检查！", TITLE));
                return;
            }
            syncFile(syncConfig, listMap);
        }

    }

    private void fillDate(SyncConfig syncConfig) {
        String date = DateHelper.getDateAddYyMmDd(0);
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        syncConfig.setSrcPath(srcPath.replace("yyyyMMdd", date));
        syncConfig.setTargetPath(targetPath.replace("yyyyMMdd", date));
    }

    private void syncFile(SyncConfig syncConfig, Map<String, List<String>> listMap) {
        BaseFtpClient srcClient = getClient(syncConfig, true);
        BaseFtpClient targetClient = getClient(syncConfig, false);
        if (srcClient == null) {
            try {
                if (targetClient != null) {
                    targetClient.disconnect();
                }
            } catch (Exception ex) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "targetClient or srcClient disconnect, ex = " + ex.getMessage(), TITLE));
            }
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "targetClient or srcClient is null", TITLE));
            log.error("targetClient or srcClient is null");
            return;
        }
        if (!srcClient.isConnected()) {
            log.error("连接不可用 srcSftpClient.isConnected():{},targetSftpClient.isConnected():{}"
                    , srcClient.isConnected(), targetClient.isConnected());
            return;
        }
        SyncServiceImpl bean = SyncApplication.ac.getBean(SyncServiceImpl.class);
        List<String> csvList = listMap.get("csv");
        if (csvList != null) {
            for (String fileName : csvList) {
                bean.copyFile(syncConfig, fileName, srcClient, targetClient);
            }
        }
        List<String> okList = listMap.get("ok");
        if (okList != null) {
            for (String fileName : okList) {
                bean.copyFile(syncConfig, fileName, srcClient, targetClient);
            }
        }
        try {
            srcClient.disconnect();
            targetClient.disconnect();
        } catch (Exception e) {
            log.error("关闭sftp链接出错", e);
        }
    }


    private Map<String, List<String>> listFile(SyncConfig syncConfig) {
        Map<String, List<String>> resultMap = new HashMap<>();
        String apiCode = syncConfig.getApiCode();
        BaseFtpClient client = getClient(syncConfig, true);
        if (client == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "config is null", TITLE));
            return resultMap;
        }
        if (!client.isConnected()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "连接不可用 srcSftpClient.isConnected()：" + client.isConnected(), TITLE));
            return resultMap;
        }
        sftpFileList(resultMap, syncConfig, (SftpClient) client, apiCode);
        return resultMap;
    }

    private void sftpFileList(Map<String, List<String>> resultMap, SyncConfig loanSyncConfig, SftpClient client, String apiCode) {
        try {
            String srcPath = loanSyncConfig.getSrcPath();
            Map<String, SftpATTRS> map = client.listFiles(srcPath);
            log.warn("SFTP同步路径:{},该路径下文件有:{}个", srcPath, map.keySet().size());
            for (Map.Entry<String, SftpATTRS> entry : map.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                validateIsSync(createFileTime, fileName, apiCode, resultMap, loanSyncConfig);
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
    }

    private void validateIsSync(String createFileTime, String fileName, String apiCode, Map<String, List<String>> resultMap
            , SyncConfig syncConfig) {
        long minutes = DateHelper.getDistanceMinutes(createFileTime);
        if (minutes < 1) {
            log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, createFileTime);
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("apiCode", apiCode);
        params.put("fileName", fileName);
        params.put("srcPath", syncConfig.getSrcSftpHost().concat(":").concat(syncConfig.getSrcPath()));
        List<SyncLog> syncLogs = syncLogMapper.querySyncLog(params);
        if (CollectionUtils.isEmpty(syncLogs)) {
            String[] split = fileName.split("\\.");
            if (split.length > 1) {
                String suf = split[split.length - 1];
                List<String> list = resultMap.computeIfAbsent(suf, k -> new ArrayList<>());
                list.add(fileName);
            } else {
                log.warn("error fileName :{}", fileName);
            }
        }
    }

    /**
     * 获取sftp链接
     *
     * @param loanSyncConfig sftp配置信息
     * @param isSrc          是否为源地址账号
     * @return SftpClient
     */
    public BaseFtpClient getClient(SyncConfig loanSyncConfig, boolean isSrc) {
        BaseFtpClient client = null;
        if (isSrc) {
            if (Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getSrcType())) {
                client = new FtpClient(loanSyncConfig, isSrc);
            } else if (Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getSrcType())) {
                client = new SftpClient(loanSyncConfig, isSrc);
            }
        } else {
            if (Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getTargetType())) {
                client = new FtpClient(loanSyncConfig, isSrc);
            } else if (Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getTargetType())) {
                client = new SftpClient(loanSyncConfig, isSrc);
            }
        }
        try {
            if (client != null) {
                boolean connect = client.connect();
                if (!connect) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "登录sftp失败 src loanSyncConfig ：" + loanSyncConfig, TITLE));
                    return client;
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "Exception", TITLE), e);
        }
        return client;
    }
}
