package com.br.marketing.service.Impl;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.service.IFileActionService;
import com.google.common.base.Splitter;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Slf4j
@Service
public class FileActionServiceImpl implements IFileActionService {
    @Override
    public Result downFileBySftp(SftpClient client, String sourcePath, String targetPath,String fileName) {
        try {
            String sourceFilePath = sourcePath.concat(fileName);
            String targetFilePath = targetPath.concat(fileName);
            File dir = new File(targetPath);
            if (!dir.exists() || !dir.isDirectory()) {
                boolean mkdirs = dir.mkdirs();
                if (!mkdirs) {
                    log.error("创建文件夹失败-{}", targetPath);
                    return new Result()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage(String.format("创建路径失败:%s", targetPath));
                }
            }
            boolean b = client.downloadFile(sourcePath, fileName, targetFilePath);
            if (!b) {
                String msg = String.format("下载文件失败。源路径：%d；目的路径：%d；"
                        , sourceFilePath
                        , targetFilePath);
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
            }
            client.rename(sourceFilePath,sourceFilePath.concat(".bak"));
        } catch (SftpException e) {
            log.error(e.getMessage(),e);
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<List<String>> downSyncFileBySftp(SftpClient client, SyncConfig syncConfig, String targetPath) {
        List<String> names = new ArrayList<>();
        Integer code = ResultCode.SUCCESS.getValue();
        try {
            client.connect();
            String[] split = syncConfig.getSuffix().split(",");
            String fileSuffix = split[0];
            String fileSuccessSuffix = split[1];
            // 筛选SFTP对应目录下符合条件的所有文件
            Map<String, Set<String>> fileMap = listStpFile(syncConfig.getTargetPath(), client, syncConfig);
            for (String t : fileMap.keySet()) {
                String path = t;
                Set<String> fileNames = fileMap.get(t);
                for (String fileName : fileNames) {
                    String fileNameSuccess = fileName.concat(fileSuccessSuffix);
                    if(fileName.endsWith(fileSuffix)
                            && fileNames.contains(fileNameSuccess)){
                        downFileBySftp(client,t,targetPath,fileNameSuccess);
                        Result result = downFileBySftp(client, t, targetPath, fileName);
                        if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                            code = result.getCode();
                        }else{
                            names.add(fileName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            code = ResultCode.FAIL.getValue();
        }finally {
            try {
                client.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Result().setCode(code).setDate(names);
    }

    public Map<String, Set<String>> listStpFile(String path,SftpClient sftpClient, SyncConfig syncConfig) {
        Map<String, Set<String>> map = new HashMap<>();
        try {
            List<String> suffixs = Arrays.asList(syncConfig.getSuffix().split(","));
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(path);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if (attrs.isDir()) {
                    continue;
                } else {
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    long minutes = DateHelper.getDistanceMinutes(createFileTime);
                    if (minutes < 1) {
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理");
                        continue;
                    }
                    String[] names = fileName.split("\\.");
                    String name = ".".concat(names[names.length - 1]);
                    if (StringUtils.isNotEmpty(fileName) && (suffixs.contains(name))) {
                        Set<String> set = map.get(path);
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(path, set);
                        }
                        set.add(fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
        if(!map.isEmpty()){
            log.warn("map :{}", map);
        }
        return map;
    }
}
