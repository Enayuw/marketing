package com.br.marketing.service.ftp.Impl;

import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.entity.SftpUploadTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.mapper.SftpUploadTaskMapper;
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
    private SftpUploadTaskMapper sftpUploadTaskMapper;

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
    public void insertSftpUploadTask(String apiCode, String localPath, String fileName,
                                     Integer dataType, String postSqlProcess) {
        try {
            SftpUploadTask task = new SftpUploadTask();
            task.setApiCode(apiCode);
            task.setLocalPath(localPath);
            task.setFileName(fileName);
            task.setDataType(dataType);
            task.setPostSqlProcess(postSqlProcess);
            task.setStatus(0); // 默认状态：0-待上传
            task.setCreateTime(new Date());
            // 插入记录
            int result = sftpUploadTaskMapper.insertSelective(task);
            if (result > 0) {
                log.warn("成功插入SFTP上传任务，ID: {}, apiCode: {}, fileName: {}",
                        task.getId(), apiCode, fileName);
            } else {
                log.error("插入SFTP上传任务失败，apiCode: {}, fileName: {}", apiCode, fileName);
            }

        } catch (Exception e) {
            log.error("插入SFTP上传任务异常，apiCode: {}, fileName: {}, error: {}",
                    apiCode, fileName, e.getMessage(), e);
        }
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态：0-待上传，1-上传中，2-上传成功，3-上传失败
     * @return 更新结果
     */
    public boolean updateTaskStatus(Long taskId, Integer status) {
        try {
            SftpUploadTask task = new SftpUploadTask();
            task.setId(taskId);
            task.setStatus(status);
            task.setUpdateTime(new Date());

            int result = sftpUploadTaskMapper.updateByPrimaryKeySelective(task);

            if (result > 0) {
                log.info("成功更新任务状态，taskId: {}, status: {}", taskId, status);
                return true;
            } else {
                log.warn("更新任务状态失败，taskId: {}, status: {}", taskId, status);
                return false;
            }

        } catch (Exception e) {
            log.error("更新任务状态异常，taskId: {}, status: {}, error: {}",
                    taskId, status, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void processUploadTask(SftpUploadTask uploadTask) {
        SyncConfig syncConfig = getSyncConfigByTask(uploadTask);
        if(Objects.isNull(syncConfig)){
            return;
        }





    }


    /**
     * 根据上传任务查找对应的同步配置
     */
    private SyncConfig getSyncConfigByTask(SftpUploadTask task) {
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        criteria.andStatusEqualTo(1) // 状态有效
                .andApiCodeEqualTo(task.getApiCode()) // 匹配apiCode
                .andDataTypeEqualTo(task.getDataType()) // 匹配数据类型
                .andTypeEqualTo(2); // type=2表示上传任务

        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);

        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.error("未找到匹配的同步配置，apiCode: {}, dataType: {}",
                    task.getApiCode(), DataTypeEnum.fromDescByValue(task.getDataType()));
            return null;
        }

        if (syncConfigs.size() > 1) {
            log.warn("找到多个匹配的同步配置，apiCode: {}, dataType: {}, 配置数量: {}",
                    task.getApiCode(), DataTypeEnum.fromDescByValue(task.getDataType()), syncConfigs.size());
            return null;
        }

        SyncConfig syncConfig = syncConfigs.get(0);
        return syncConfig;
    }


}
