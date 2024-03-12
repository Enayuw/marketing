package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.FileDbConfig;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class SftpToDbByCommonService {

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;

    @Autowired
    RabbitMqProducter producter;
    /**
     * The File ckeck servicce.
     */
    @Resource
    FileCheckServiceImpl fileCheckService;
    /**
     * The Redis chg service.
     */
    @Resource
    RedisChgService redisChgService;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private static String phoneReg = "^([\\+]*[0-9]+)$";

    private final static Integer SPLITSIZE = 5000;


    /**
     * 下载文件
     *
     * @param context
     * @return
     */
    public Boolean dowloadFile(FileContext context) {
        String localFilePath = context.getLocalTxtFilePath();
        String zipFileName = context.getTxtFileName();
        SftpClient client = (SftpClient) context.getBaseFtpClient();
        File dir = new File(localFilePath);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                log.error("创建文件夹失败-{}", context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb = new StringBuilder().append(localFilePath).append(zipFileName);
        boolean download = client.downloadFile(context.getSftpZipFilePath(), zipFileName, sb.toString());
        if (!download) {
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}", context.getSftpZipFilePath(), zipFileName);
            return false;
        }
        return true;
    }

    public Boolean actionTxtFile(FileContext context, LocalFile localFile, List<String> baseHeads, String routKey, Function<TxtToDbDTO, Result> fuc) {
        return actionTxtFile(context, localFile, baseHeads, routKey, fuc, null);
    }

    /**
     * 处理文件
     *
     * @param context
     * @param localFile
     * @param baseHeads
     * @param routKey
     * @param fuc
     * @return
     */
    public Boolean actionTxtFile(FileContext context, LocalFile localFile, List<String> baseHeads, String routKey, Function<TxtToDbDTO, Result> fuc, Function<LocalFile, Result> datafuc) {
        String txtFilePathAndName = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        StringBuilder head;
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if (totalLines == 0) {
            log.error(String.format("%s 文件内容为空", context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }
        head = MyFileUtil.gethead(txtFilePathAndName);

        HashMap<Integer, String> address = new HashMap<>();
        HashMap<Integer, String> extSetField = new HashMap<>();
        Result hashMapResult = SftpToDbUtils.statisticsHeadByCommon(head.toString(), address, extSetField, baseHeads);
        if (!ResultCode.SUCCESS.getValue().equals(hashMapResult.getCode())) {
            log.error(String.format("%s 文件：%s", context.getTxtFileName(), hashMapResult.getMessage()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }

        long start = System.currentTimeMillis();

        String filepath = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        AtomicInteger errorMark = new AtomicInteger(0);
        try (
                FileReader read = new FileReader(filepath);
                BufferedReader br = new BufferedReader(read);) {
            String row;
            Integer line = 1;
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                TxtToDbDTO txtToDbDTO = new TxtToDbDTO();
                txtToDbDTO.setLine(line);
                txtToDbDTO.setApiCode(localFile.getApiCode());
                txtToDbDTO.setLocalId(localFile.getId());
                txtToDbDTO.setContent(trim);
                txtToDbDTO.setAddress(address);
                txtToDbDTO.setExtSetField(extSetField);
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
                line++;
            }
            /**
             * 等待所有任务都执行完成
             **/
            threadPool.shutdown();
            while (true) {
                if (threadPool.isTerminated()) {
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                }
            }
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setActualNumber(line > 1 ? line - 2 : line);
            localFile.setActualNumber(updateFile.getActualNumber());
            if (errorMark.get() > 0) {
                updateFile.setComplete("3");
            }

            if (datafuc != null) {
                Result apply = datafuc.apply(localFile);
                if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                    if (apply.getData() instanceof Integer) {
                        errorMark.getAndAdd((Integer) apply.getData());
                    }
                }
            }

            updateFile.setErrorActualNumber(errorMark.get());
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            producter.send(routKey, localFile.getId().toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        if (log.isWarnEnabled()) {
            log.warn(String.format("数据入库时长:%d", end - start));
        }
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("文件id：".concat(localFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat(localFile.getFileType()).concat("\r\n"))
                    .append("导入文件状态：".concat(errorMark.get() == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(localFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(String.valueOf(errorMark.get())).concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "sftp数据上传", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return true;
    }


    /**
     * 根据配置文件入库
     *
     * @param context
     * @param localFile
     * @param fileDbConfig
     * @param fuc
     * @return
     */
    public Boolean actionTxtFile(FileContext context, LocalFile localFile, FileDbConfig fileDbConfig, Function<TxtToDbDTO, Result> fuc) {
        String txtFilePathAndName = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        //region 文件校验
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if (totalLines == 0) {
            log.error(String.format("%s 文件内容为空", context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }
        //endregion
        //region 文件表头处理
        StringBuilder head = MyFileUtil.gethead(txtFilePathAndName);
        ArrayList<String> fieldAll = new ArrayList<>();
        ArrayList<String> fieldMust = new ArrayList<>();
        setHead(fieldAll, fieldMust, fileDbConfig.getDbFields());
        HashSet<String> fieldAllSet = new HashSet<>();
        HashMap<String, String> fieldAllHm = new HashMap<>();
        HashSet<String> fieldMustSet = new HashSet<>();
        StringBuilder errorMsg = new StringBuilder();
        fieldAll.forEach(t -> {
            fieldAllSet.add(t);
            fieldAllHm.put(t, StringUtils.humpToLine2(t));
        });
        fieldMust.forEach(t -> {
            errorMsg.append(String.format("%s不能为空;", t));
            fieldMustSet.add(t);
        });
        // 数据坐标
        HashMap<Integer, String> address = new HashMap<>();
        // 扩展字段数据坐标
        HashMap<Integer, String> extSetField = new HashMap<>();
        Result hashMapResult = SftpToDbUtils.statisticsHeadByCommon(head.toString(), address, extSetField, fieldMust);
        if (!ResultCode.SUCCESS.getValue().equals(hashMapResult.getCode())) {
            log.error(String.format("%s 文件：%s", context.getTxtFileName(), hashMapResult.getMessage()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }
        //endregion
        //region 写入数据库
        long start = System.currentTimeMillis();

        String filepath = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        AtomicInteger errorMark = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        try (
                FileReader read = new FileReader(filepath);
                BufferedReader br = new BufferedReader(read);) {
            Integer line = 1;
            Integer threadNum = 20;
            if (marketingCommonConfig.getThreadNumSftpToDbByCommon() != null && marketingCommonConfig.getThreadNumSftpToDbByCommon() > 0) {
                threadNum = marketingCommonConfig.getThreadNumSftpToDbByCommon();
            }
            Integer dataNum = 50;
            if (marketingCommonConfig.getDataNumSftpToDbByCommon() != null && marketingCommonConfig.getDataNumSftpToDbByCommon() > 0) {
                dataNum = marketingCommonConfig.getDataNumSftpToDbByCommon();
            }
            log.warn("SftpToDbByCommonJob入库线程数：" + threadNum);
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
            Integer hasNum = 0;
            HashMap<Integer,String> datasHp = new HashMap<>();
            Boolean readFile = Boolean.TRUE;
            while (readFile) {
                String row = br.readLine();
                if(line == 1){
                    line++;
                    continue;
                }
                if(row == null){
                    readFile = Boolean.FALSE;
                }else{
                    String trim = row.trim();
                    if (StringUtils.isNotEmpty(row) && StringUtils.isNotEmpty(trim)) {
                        datasHp.put(line,trim);
                        hasNum++;
                    }
                }
                if((!readFile && datasHp.size()>0) || dataNum==hasNum){
                    TxtToDbDTO txtToDbDTO = new TxtToDbDTO();
                    HashMap<Integer, String> threadDatas = new HashMap<>();
                    threadDatas.putAll(datasHp);
                    txtToDbDTO.setDatas(threadDatas);
                    txtToDbDTO.setApiCode(localFile.getApiCode());
                    txtToDbDTO.setLocalId(localFile.getId());
                    txtToDbDTO.setAddress(address);
                    txtToDbDTO.setFieldAll(fieldAllSet);
                    txtToDbDTO.setFieldAllHm(fieldAllHm);
                    txtToDbDTO.setFieldMust(fieldMustSet);
                    txtToDbDTO.setErrorMsg(errorMsg.toString());
                    txtToDbDTO.setExtSetField(extSetField);
                    txtToDbDTO.setDbName(fileDbConfig.getDbName());
                    threadPool.submit(() -> {
                        Result apply = fuc.apply(txtToDbDTO);
                        if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                            JSONObject jsonObject = JSON.parseObject(apply.getMessage());
                            errorMark.getAndAdd(jsonObject.getInteger("errorNum"));
                            success.getAndAdd(jsonObject.getInteger("successNum"));
                        }
                    });
                    hasNum = 0;
                    datasHp.clear();
                }
                if(row !=null){
                    line++;
                }
            }
            /**
             * 等待所有任务都执行完成
             **/
            threadPool.shutdown();
            while (true) {
                if (threadPool.isTerminated()) {
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                }
            }
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setActualNumber(line > 1 ? line - 2 : line);
            localFile.setActualNumber(updateFile.getActualNumber());
            if (errorMark.get() > 0) {
                updateFile.setComplete("3");
            }
            updateFile.setErrorActualNumber(errorMark.get());
            updateFile.setStatus("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            if (StringUtils.isNotBlank(fileDbConfig.getRouteKey())) {
                producter.send(fileDbConfig.getRouteKey(), localFile.getId().toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //endregion
        long end = System.currentTimeMillis();
        if (log.isWarnEnabled()) {
            log.warn(String.format("数据入库时长:%d", end - start));
        }
        //region 提示
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("文件id：".concat(localFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat(localFile.getFileType()).concat("\r\n"))
                    .append("导入文件状态：".concat(errorMark.get() == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(localFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(String.valueOf(errorMark.get())).concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "sftp数据上传", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        //endregion
        return true;
    }

    /**
     * 校验配置
     *
     * @param fileDbConfig
     * @return
     */
    public Result<String> checkFileDbconfig(FileDbConfig fileDbConfig) {
        if (StringUtils.isBlank(fileDbConfig.getDbName())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("落表文件配置表名为空");
        }
        if (StringUtils.isBlank(fileDbConfig.getInnerPath())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("落表文件配置路径为空");
        }
        if (StringUtils.isBlank(fileDbConfig.getDbFields())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("落表文件配置字段为空");
        }
        ArrayList<String> fieldAll = new ArrayList<>();
        ArrayList<String> fieldMust = new ArrayList<>();
        setHead(fieldAll, fieldMust, fileDbConfig.getDbFields());
        if (fieldAll.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("落表文件配置的字段为空");
        }
        if (fieldMust.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("落表文件配置的必填字段为空");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }


    public void setHead(List<String> fieldAll, List<String> fieldMust, String fieldStre) {
        List<String> fields = Splitter.on(",").splitToList(fieldStre);
        fields.forEach(t -> {
            List<String> item = Splitter.on(":").splitToList(t);
            if (item.get(1).equals("1")) {
                fieldMust.add(item.get(0));
            }
            fieldAll.add(item.get(0));
        });
    }
}
