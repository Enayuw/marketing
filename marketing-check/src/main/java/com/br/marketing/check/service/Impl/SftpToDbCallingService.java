package com.br.marketing.check.service.Impl;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author guangchao.zhang
 * @Classname SftpToDbCallingService
 * @Description 首次拨打情况数据处理服务
 * @Date 2022/2/15 10:09 AM
 */
@Service
@Slf4j
public class SftpToDbCallingService {

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    /**
     * 将文件下载到本地
     *
     * @param context 文件上下文信息
     * @return 下载是否成功
     */
    public Boolean downLoadFile(FileContext context) {
        SftpClient client = (SftpClient) context.getBaseFtpClient();
        File dir = new File(context.getLocalTxtFilePath());
        if (!dir.exists() || !dir.isDirectory()) {
            boolean mkdir = dir.mkdirs();
            if (!mkdir) {
                log.error("创建文件夹失败-{}", context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb = new StringBuilder().append(context.getLocalTxtFilePath()).append(context.getTxtFileName());
        boolean download = client.downloadFile(context.getSftpZipFilePath(), context.getTxtFileName(), sb.toString());
        if (!download) {
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}", context.getSftpZipFilePath(), context.getTxtFileName());
            return false;
        }
        return true;
    }

    public void actionTxtFile(FileContext context, LocalFile localFile, List<String> baseHeads, Function<TxtToDbDTO, Result> fuc, SftpClient sftpClient) {
        String txtFilePathAndName = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        HashMap<Integer, String> address = getAddress(context, localFile, baseHeads, txtFilePathAndName);
        if (address != null) {
            doProcess(localFile, fuc, txtFilePathAndName, address, sftpClient);
        }
    }

    private HashMap<Integer, String> getAddress(FileContext context, LocalFile localFile, List<String> baseHeads, String txtFilePathAndName) {
        if (!checkFile(context, localFile, txtFilePathAndName)) {
            return checkAndGetHead(context, localFile, baseHeads, txtFilePathAndName);
        }
        return null;
    }

    private void doProcess(LocalFile localFile, Function<TxtToDbDTO, Result> fuc, String txtFilePathAndName, HashMap<Integer, String> address, SftpClient sftpClient) {
        long start = System.currentTimeMillis();
        Integer line = 1;
        AtomicInteger errorMark = new AtomicInteger(0);
        try (FileReader read = new FileReader(txtFilePathAndName);
             BufferedReader br = new BufferedReader(read);) {
            String row;
            // 创建线程池
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
            while ((row = br.readLine()) != null) {
                doThreadPoolProcess(localFile, fuc, address, line, errorMark, row, threadPool);
                line++;
            }
            //关闭线程池
            threadPool.shutdown();
            //当调用shutdown()方法后，并且所有提交的任务完成后返回为true;
            while (!threadPool.isTerminated()) ;
            log.info("所有线程都执行结束");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        if (log.isWarnEnabled()) {
            log.warn(String.format("数据入库时长:%d", end - start));
        }
        doProcessAfter(localFile, errorMark, line, sftpClient);
    }

    private void doThreadPoolProcess(LocalFile localFile, Function<TxtToDbDTO, Result> fuc, HashMap<Integer, String> address, Integer line, AtomicInteger errorMark, String row, ThreadPoolExecutor threadPool) {
        String trim = row.trim();
        TxtToDbDTO txtToDbDTO = new TxtToDbDTO();
        txtToDbDTO.setLine(line);
        txtToDbDTO.setApiCode(localFile.getApiCode());
        txtToDbDTO.setLocalId(localFile.getId());
        txtToDbDTO.setContent(trim);
        txtToDbDTO.setAddress(address);
        if (StringUtils.isNotEmpty(row) && StringUtils.isNotEmpty(trim)) {
            if (line > 1) {
                threadPool.submit(() -> {
                    Result apply = fuc.apply(txtToDbDTO);
                    if (!ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                        errorMark.getAndIncrement();
                    }
                });
            }
        }
    }

    private void doProcessAfter(LocalFile localFile, AtomicInteger errorMark, Integer line, SftpClient sftpClient) {
        LocalFile updateFile = new LocalFile();
        updateFile.setId(localFile.getId());
        updateFile.setActualNumber(line > 1 ? line - 2 : line);
        if (errorMark.get() > 0) {
            updateFile.setComplete("3");
        }
        localFileMapper.updateByPrimaryKeySelective(updateFile);
        doRenameFile(localFile, sftpClient);
        doSendEmailAlert(localFile, errorMark, updateFile);
    }
    private void doSendEmailAlert(LocalFile localFile, AtomicInteger errorMark, LocalFile updateFile) {
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("文件id：".concat(localFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat(localFile.getFileType()).concat("\r\n"))
                    .append("导入文件状态：".concat(errorMark.get() == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(updateFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(errorMark.toString()).concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "sftp数据上传", appName, secretKey,
                    Constants.sendCodeMap.get("uploadSuccess"));
            System.out.println(content);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void doRenameFile(LocalFile localFile, SftpClient sftpClient) {
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String srcPath = localFile.getSrcPath();
        String fileName = localFile.getFileName();
        String nameTxt = srcPath + "/" + fileName;
        String nameSuc = srcPath + "/" + fileName + ".success";
        String newNameTxt = nameTxt + ".bak";
        String newNameSuc = nameSuc + "_" + yyyyMMddHHmmss + ".bak";
        try {
            sftpClient.rename(nameTxt, newNameTxt);
            sftpClient.rename(nameSuc, newNameSuc);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<Integer, String> checkAndGetHead(FileContext context, LocalFile localFile, List<String> baseHeads, String txtFilePathAndName) {
        StringBuilder head = MyFileUtil.gethead(txtFilePathAndName);
        HashMap<Integer, String> address = new HashMap<>();
        Result hashMapResult = getHeadBase(head.toString(), address, baseHeads);
        if (!ResultCode.SUCCESS.getValue().equals(hashMapResult.getCode())) {
            log.error(String.format("%s 文件：%s", context.getTxtFileName(), hashMapResult.getMessage()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return null;
        }
        return address;
    }

    private boolean checkFile(FileContext context, LocalFile localFile, String txtFilePathAndName) {
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if (totalLines == 0) {
            log.error(String.format("%s 文件内容为空", context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return true;
        }
        return false;
    }

    public static Result getHeadBase(String head, HashMap<Integer, String> address, List<String> baseHeads) {
        List<String> heads = Splitter.on(",").splitToList(head);
        if (heads.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不存在");
        }
        if (!new HashSet<>(heads).containsAll(baseHeads)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("表头缺少必填字段");
        }
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if (org.apache.commons.lang.StringUtils.isBlank(s)) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不能有空字段");
            }
            address.put(i, s);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
