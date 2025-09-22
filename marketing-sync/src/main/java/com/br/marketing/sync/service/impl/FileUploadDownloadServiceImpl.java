package com.br.marketing.sync.service.impl;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.SftpUploadTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.SftpUploadTaskMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.sync.service.FileUploadDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileUploadDownloadServiceImpl implements FileUploadDownloadService {

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private SyncServiceImpl syncServiceImpl;

    @Resource
    private SftpUploadTaskMapper sftpUploadTaskMapper;


    @Override
    public void processUploadTask(SftpUploadTask uploadTask) {
        SyncConfig syncConfig = getSyncConfigByTask(uploadTask);
        if (Objects.isNull(syncConfig)) {
            // 配置不存在，更新为失败状态
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            return;
        }
        //文件上传
        boolean uploadResult = uploadSftp(uploadTask, syncConfig);
        
        // 根据上传结果更新任务状态
        if (uploadResult) {
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.SUCCESS.getCode());
            log.warn("文件上传成功，taskId: {}, fileName: {}", uploadTask.getId(), uploadTask.getFileName());
        } else {
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            log.warn("文件上传失败，taskId: {}, fileName: {}", uploadTask.getId(), uploadTask.getFileName());
        }
    }


    private boolean uploadSftp(SftpUploadTask uploadTask, SyncConfig syncConfig) {
        //获取内部sftp配置
        BaseFtpClient client = syncServiceImpl.getClient(syncConfig, true);
        
        // 处理路径中的日期替换
        String srcPath = replaceDateInPath(syncConfig.getSrcPath());
        
        String localPath = uploadTask.getLocalPath().concat(uploadTask.getFileName());

        try (InputStream inputStream = Files.newInputStream(Paths.get(localPath))) {
            client.mkdir(srcPath);
            client.uploadFile(inputStream, srcPath, uploadTask.getFileName());
            return true;
        } catch (Exception e) {
            log.error("上传文件出错，taskId: {}, fileName: {}, localPath: {}, srcPath: {}, error: {}",
                    uploadTask.getId(), uploadTask.getFileName(), localPath, srcPath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 替换路径中的日期占位符
     * @param path 原始路径
     * @return 替换后的路径
     */
    private String replaceDateInPath(String path) {
        // 获取当前日期
        String currentDate = DateHelper.getDateAddYyMmDd(0); // yyyyMMdd格式
        
        // 根据路径格式转换日期格式
        String formattedDate = currentDate;
        if (path.contains("yyyy-MM-dd")) {
            // 将yyyyMMdd格式转换为yyyy-MM-dd格式
            formattedDate = formatDate(currentDate, "yyyyMMdd", "yyyy-MM-dd");
            return path.replace("yyyy-MM-dd", formattedDate);
        } else if (path.contains("yyyyMMdd")) {
            return path.replace("yyyyMMdd", currentDate);
        }
        
        return path;
    }

    /**
     * 日期格式转换
     * @param date 日期字符串
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @return 转换后的日期字符串
     */
    private String formatDate(String date, String sourceFormat, String targetFormat) {
        try {
            SimpleDateFormat sourceFormatter = new SimpleDateFormat(sourceFormat);
            SimpleDateFormat targetFormatter = new SimpleDateFormat(targetFormat);
            return targetFormatter.format(sourceFormatter.parse(date));
        } catch (Exception e) {
            log.error("日期格式转换失败，date: {}, sourceFormat: {}, targetFormat: {}, error: {}",
                    date, sourceFormat, targetFormat, e.getMessage(), e);
            return date; // 转换失败时返回原始日期
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
            log.error("找到多个匹配的同步配置，apiCode: {}, dataType: {}, 配置数量: {}",
                    task.getApiCode(), DataTypeEnum.fromDescByValue(task.getDataType()), syncConfigs.size());
            return null;
        }

        SyncConfig syncConfig = syncConfigs.get(0);
        return syncConfig;
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
            return result > 0;

        } catch (Exception e) {
            log.error("更新任务状态异常，taskId: {}, status: {}, error: {}",
                    taskId, status, e.getMessage(), e);
            return false;
        }
    }

}
