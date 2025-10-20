package com.br.marketing.service.Impl;

import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.TransferFileTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SftpInnerServiceImpl {

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

    @Autowired
    TransferFileTaskMapper transferFileTaskMapper;

    public Result pushInnerSftp(TransferFileTask transferFileTask) {
        String endDate = transferFileTask.getStartDate();
        String apiCode = transferFileTask.getApiCode();

        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();
            String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? transferFileTask.getFileChildDir() + "/" : "";
            String uploadPath = upLoadPath.concat(apiCode).concat("/transferOutPut/").concat(childDir).concat(endDate);
            String fileAllPath = transferFileTask.getFilePath().concat(transferFileTask.getFileName());
            String successAllPath = fileAllPath.concat(".success");
            String successFileName = transferFileTask.getFileName().concat(".success");
            File successfile = new File(successAllPath);
            if (!successfile.exists()) {
                successfile.createNewFile();
            }
            boolean b = sftpClient.uploadFile(uploadPath, transferFileTask.getFileName(), fileAllPath);
            if (b) {
                boolean b1 = sftpClient.uploadFile(uploadPath, successFileName, successAllPath);
                if (!b1) {
                    log.error(String.format("上传success文件有问题 文件id：%s", transferFileTask.getId()));
                }
                TransferFileTask update = new TransferFileTask();
                update.setId(transferFileTask.getId());
                update.setStatus(3);
                update.setUpdateTime(new Date());
                transferFileTaskMapper.updateByPrimaryKeySelective(update);
            }
        } catch (Exception e) {
            log.error(String.format("推送转化文件到内部sftp错误 文件id：%d,错误：%s", transferFileTask.getId(), e.getMessage()), e);
            return new Result().setCode(ResultCode.FAIL.getValue());
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
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result pushInnerSftp(String innerPath, String outerPath, String fileName) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        String uploadPath = upLoadPath.concat(outerPath).concat(yyyyMMdd);
        String fileAllPath = innerPath.concat(fileName);
        String successAllPath = fileAllPath.concat(".success");
        String successFileName = fileName.concat(".success");
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();

            File successfile = new File(successAllPath);
            if (!successfile.exists()) {
                successfile.createNewFile();
            }
            boolean b = sftpClient.uploadFile(uploadPath, fileName, innerPath);
            if (b) {
                boolean b1 = sftpClient.uploadFile(uploadPath, successFileName, successAllPath);
                if (!b1) {
                    log.error(String.format("上传success文件有问题 文件路径：%s", successAllPath));
                }
            }
        } catch (Exception e) {
            log.error(String.format("推送转化文件到内部sftp错误 文件路径：%s,错误：%s", fileAllPath, e.getMessage()), e);
            return new Result().setCode(ResultCode.FAIL.getValue());
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
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result pushInnerSftp(String innerPath, String uploadPath, List<String> fileNames) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();
            for (String fileName : fileNames) {
                String fileAllPath = innerPath.concat(fileName);
                boolean isPush = sftpClient.uploadFile(uploadPath, fileName, fileAllPath);
                if (!isPush) {
                    log.error(String.format("文件传输有问题，文件全路径：%s", fileAllPath));
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
            }
        } catch (Exception e) {
            log.error(String.format("推送转化文件到内部sftp错误 文件路径：%s,错误：%s", innerPath, e.getMessage()), e);
            return new Result().setCode(ResultCode.FAIL.getValue());
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
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
