package com.br.marketing.check.service.Impl;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

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
        String sb = context.getLocalTxtFilePath() + context.getTxtFileName();
        boolean download = client.downloadFile(context.getSftpZipFilePath(), context.getTxtFileName(), sb);
        if (!download) {
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}", context.getSftpZipFilePath(), context.getTxtFileName());
            return false;
        }
        return true;
    }

    /**
     * 处理文件主程序
     * @param context 文件上下文
     * @param localFile 本地文件对象
     * @param baseHeads 基础字段
     * @param fuc 回调方法
     * @param sftpClient sftp 客户端
     */
    public void actionTxtFile(FileContext context, LocalFile localFile, List<String> baseHeads, Function<TxtToDbDTO, Result> fuc, SftpClient sftpClient) {
        String txtFilePathAndName = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        HashMap<Integer, String> address = getAddress(context, localFile, baseHeads, txtFilePathAndName);
        if (address != null) {
            doRenameFile(localFile, sftpClient);
            doProcess(localFile, fuc, txtFilePathAndName, address, sftpClient);
        }
    }

    /**
     * 获取文件字段头信息
     * @param context 文件上下文
     * @param localFile 本地文件对象
     * @param baseHeads 基础字段
     * @param txtFilePathAndName sftp路径
     * @return 文件字段头
     */
    private HashMap<Integer, String> getAddress(FileContext context, LocalFile localFile, List<String> baseHeads, String txtFilePathAndName) {
        if (checkFile(context, localFile, txtFilePathAndName)) {
            return checkAndGetHead(context, localFile, baseHeads, txtFilePathAndName);
        }
        return null;
    }

    /**
     * 多线程处理逻辑
     * @param localFile 本地文件
     * @param fuc 回调方法
     * @param txtFilePathAndName sftp 路径
     * @param address 文件字段头
     * @param sftpClient sftp 客户端
     */
    private void doProcess(LocalFile localFile, Function<TxtToDbDTO, Result> fuc, String txtFilePathAndName, HashMap<Integer, String> address, SftpClient sftpClient) {
        long start = System.currentTimeMillis();
        Integer line = 1;
        AtomicInteger errorMark = new AtomicInteger(0);
        try {
            FileReader read = new FileReader(txtFilePathAndName);
            BufferedReader br = new BufferedReader(read);
            String row;
            Integer haloSaveDataThreadNum = marketingCommonConfig.getHaloSaveDataThreadNum();
            log.warn("哈啰回调落库线程数：{}",haloSaveDataThreadNum);
            // 创建线程池
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(haloSaveDataThreadNum, haloSaveDataThreadNum);
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
        doProcessAfter(localFile, errorMark, line);
    }

    /**
     * 线程池处理方法
     * @param localFile 本地文件对象
     * @param fuc 回调方法
     * @param address 基础字段头
     * @param line 处理行数
     * @param errorMark 错误条数计数
     * @param row 文件行
     * @param threadPool 线程池对象
     */
    private void doThreadPoolProcess(LocalFile localFile, Function<TxtToDbDTO, Result> fuc, HashMap<Integer, String> address, Integer line, AtomicInteger errorMark, String row, ThreadPoolExecutor threadPool) {
        TxtToDbDTO txtToDbDTO = new TxtToDbDTO();
        txtToDbDTO.setLine(line);
        txtToDbDTO.setApiCode(localFile.getApiCode());
        txtToDbDTO.setLocalId(localFile.getId());
        txtToDbDTO.setContent(row.trim());
        txtToDbDTO.setAddress(address);
        if (StringUtils.isNotEmpty(row) && StringUtils.isNotEmpty(row.trim())) {
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

    /**
     * 主程后处理程序
     * @param localFile 本地文件对象
     * @param errorMark 错误计数
     * @param line 行数
     * @param sftpClient sftp 客户端
     */
    private void doProcessAfter(LocalFile localFile, AtomicInteger errorMark, Integer line) {
        LocalFile updateFile = new LocalFile();
        updateFile.setId(localFile.getId());
        updateFile.setActualNumber(line > 1 ? line - 2 : line);
        if (errorMark.get() > 0) {
            updateFile.setComplete("3");
        }
        updateFile.setErrorActualNumber(Integer.valueOf(errorMark.toString()));
        localFileMapper.updateByPrimaryKeySelective(updateFile);

        afterProcessSendEmailAlert(localFile, errorMark, updateFile);
    }

    /**
     * 邮件方法
     * @param localFile 本地文件对象
     * @param errorMark 错误计数
     * @param updateFile 更新文件日志对象
     */
    private void afterProcessSendEmailAlert(LocalFile localFile, AtomicInteger errorMark, LocalFile updateFile) {
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("文件id：".concat(localFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat(localFile.getFileType()).concat("\r\n"))
                    .append("导入文件状态：".concat(errorMark.get() == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(updateFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(errorMark.toString()).concat("\r\n"));
            sendEmailAlert(content);
            System.out.println(content);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void sendEmailAlert(StringBuilder content) {
        alarmClient.sendAlarm(content.toString(), "----拨打回调数据sftp数据上传", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
    }

    /**
     * 文件重命名方法
     *
     * @param localFile  本地文件对象
     * @param sftpClient sftp对象
     */
    private void doRenameFile(LocalFile localFile, SftpClient sftpClient) {
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String srcPath = localFile.getSrcPath();
        String fileName = localFile.getFileName();
        String nameTxt = srcPath + "/" + fileName;
        String nameSuc = srcPath + "/" + fileName + ".success";
        String newNameTxt = nameTxt +"_"+ yyyyMMddHHmmss + ".bak";
        String newNameSuc = nameSuc + "_" + yyyyMMddHHmmss + ".bak";
        try {
            sftpClient.rename(nameTxt, newNameTxt);
            sftpClient.rename(nameSuc, newNameSuc);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件头
     * @param context 文件上下文
     * @param localFile 本地文件对象
     * @param baseHeads 基础字段
     * @param txtFilePathAndName sftp 路径
     * @return
     */
    private HashMap<Integer, String> checkAndGetHead(FileContext context, LocalFile localFile, List<String> baseHeads, String txtFilePathAndName) {
        StringBuilder head = MyFileUtil.gethead(txtFilePathAndName);
        HashMap<Integer, String> address = new HashMap<>(16);
        assert head != null;
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

    /**
     * 文件内容检查
     * @param context 文件上下文
     * @param localFile 本地文件
     * @param txtFilePathAndName 文件路径
     * @return 是否通过
     */
    private boolean checkFile(FileContext context, LocalFile localFile, String txtFilePathAndName) {
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if (totalLines == 0) {
            log.error(String.format("%s 文件内容为空", context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            sendEmailAlert(new StringBuilder(String.format("%s 文件内容为空", context.getTxtFileName())));
            return false;
        }
        return true;
    }

    /**
     * 文件头字段检查
     * @param head 文件字段
     * @param address 字段容器
     * @param baseHeads 基础字段
     * @return 结果返回
     */
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
