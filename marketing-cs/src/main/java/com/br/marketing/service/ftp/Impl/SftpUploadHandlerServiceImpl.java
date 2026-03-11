package com.br.marketing.service.ftp.Impl;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.dto.SftpUploadTargetParamDTO;
import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.mapper.FileSyncTaskMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.ftp.SftpUploadHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SftpUploadHandlerServiceImpl implements SftpUploadHandlerService {

    @Resource
    private FileSyncTaskMapper fileSyncTaskMapper;

    @Resource
    private SyncConfigMapper syncConfigMapper;


    /**
     * 插入SFTP上传任务记录
     *
     * @param apiCode        商户编号
     * @param localPath      本地文件路径
     * @param fileName       文件名称
     * @param dataType       文件类型
     * @param postSqlProcess 后置SQL处理
     * @return 插入的任务ID
     */
    @Override
    public Result insertSftpUploadTask(String apiCode, String localPath, String fileName,
                                       Integer dataType, String postSqlProcess) {
        Result result = new Result().failure();
        try {
            FileSyncTask task = new FileSyncTask();
            task.setApiCode(apiCode);
            task.setLocalPath(localPath);
            task.setFileName(fileName);
            task.setDataType(dataType);
            task.setPostSqlProcess(postSqlProcess);
            task.setStatus(0); // 默认状态：0-待上传
            task.setCreateTime(new Date());
            // 插入记录
            int insertResult = fileSyncTaskMapper.insertSelective(task);
            if (insertResult > 0) {
                log.warn("成功插入SFTP上传任务，ID: {}, apiCode: {}, fileName: {}",
                        task.getId(), apiCode, fileName);
                return result.success();
            } else {
                log.error("插入SFTP上传任务失败，apiCode: {}, fileName: {}", apiCode, fileName);
                return result;
            }

        } catch (Exception e) {
            log.error("插入SFTP上传任务异常，apiCode: {}, fileName: {}, error: {}",
                    apiCode, fileName, e.getMessage(), e);
            return result;
        }
    }

    /**
     * 插入SFTP上传任务记录（含推送目标配置，pushTargetType=1 时不查配置）
     */
    @Override
    public Result insertSftpUploadTarget(SftpUploadTargetParamDTO param) {
        Result result = new Result().failure();
        if (param == null) {
            return result;
        }
        try {
            FileSyncTask task = new FileSyncTask();
            task.setApiCode(param.getApiCode());
            task.setLocalPath(param.getLocalPath());
            task.setFileName(param.getFileName());
            task.setDataType(param.getDataType());
            task.setPostSqlProcess(param.getPostSqlProcess());
            task.setStatus(0); // 默认状态：0-待上传
            task.setCreateTime(new Date());
            task.setPushTargetType(param.getPushTargetType());
            task.setTargetSftpHost(param.getTargetSftpHost());
            task.setTargetSftpPort(param.getTargetSftpPort());
            task.setTargetSftpUser(param.getTargetSftpUser());
            task.setTargetSftpPwd(param.getTargetSftpPwd());
            task.setTargetType(param.getTargetType());
            task.setTargetPath(param.getTargetPath());
            int insertResult = fileSyncTaskMapper.insertSelective(task);
            if (insertResult > 0) {
                log.warn("成功插入SFTP上传任务，ID: {}, apiCode: {}, fileName: {}",
                        task.getId(), param.getApiCode(), param.getFileName());
                return result.success();
            } else {
                log.error("插入SFTP上传任务失败，apiCode: {}, fileName: {}", param.getApiCode(), param.getFileName());
                return result;
            }
        } catch (Exception e) {
            log.error("插入SFTP上传任务异常，apiCode: {}, fileName: {}, error: {}",
                    param.getApiCode(), param.getFileName(), e.getMessage(), e);
            return result;
        }
    }

}
