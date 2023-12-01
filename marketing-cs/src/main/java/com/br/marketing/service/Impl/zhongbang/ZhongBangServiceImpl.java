package com.br.marketing.service.Impl.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.MD5Utils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserAndCustomerTransferSoleFacade;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import com.zbank.file.bean.FileDownLoadInfo;
import com.zbank.file.bean.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务逻辑
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-25 18:37
 */
@Service
@Slf4j
public class ZhongBangServiceImpl implements ZhongBangService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private PhoneSaleExtendServiceImpl phoneSaleExtendService;

    @Resource
    private ArtificialRealTimeUserAndCustomerTransferSoleFacade artificialRealTimeUserAndCustomerTransferSoleFacade;

    @Resource
    private ZbankClient zBankClient;

    @Resource
    private PullCustomerFileDataMapper pullCustomerFileDataMapper;

    @Resource
    private LocalFileMapper localFileMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");


    @Override
    public void pushTransferToDaasRealTimeUserOneAndCustomer(String apiCode, ThreadPoolExecutor threadPool, String... dateTimeStr) {
        boolean bool = dateTimeStr.length > 1;
        int day = marketingCommonConfig.getZhongbangCellDistributeDay() - 1;
        String tcId = tableCreateService.getTcId(apiCode);
        LinkedHashMap<String, JSONObject> statusTypeMap = marketingCommonConfig.getZhongbangStatusTypeMap();
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        LocalDate now;
        if (bool) {
            now = LocalDateTime.parse(dateTimeStr[1]).toLocalDate();
        } else {
            now = LocalDate.parse(dateTimeStr[0], DateTimeFormatter.ISO_LOCAL_DATE);
        }
        example.createCriteria().andApiCodeEqualTo(apiCode).andRequestDataEqualTo(now.toString());
        int count = marketingTransferSyncUserMapper.countByExample(example);
        if (count < 1) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode()
                    , "众邦“" + now + "”未传输转化数据，不能推送数据到daas(单条)与外呼，如需重新推送需手动执行任务，apiCode:"
                            + apiCode + ";cid:" + tcId
                    , "众邦转化数据推送daas(单条)与外呼告警"));
            return;
        }
        LocalDate yesterdayDate = now.minusDays(1);
        String yesterdayStartTime = yesterdayDate.atStartOfDay().format(DATE_TIME_FORMATTER);
        String yesterdayEndTime = yesterdayDate.atTime(23, 59, 59, 999999999)
                .format(DATE_TIME_FORMATTER);
        String switchKey = "switch";
        String groupNoKey = "groupNo";
        String dxUserTypeKey = "dxUserType";
        statusTypeMap.forEach((k, v) -> {
            String sqlWhereClause;
            if (v.getBooleanValue(switchKey)) {
                switch (k) {
                    case "d":
                        example.clear();
                        MarketingTransferSyncUserExample.Criteria criteriaD = example.createCriteria();
                        if (bool) {
                            criteriaD.andRequestTimeBetween(dateTimeStr[0].replace("T", " ")
                                    , dateTimeStr[1].replace("T", " "));
                        } else {
                            criteriaD.andRequestDataEqualTo(dateTimeStr[0]);
                        }
                        criteriaD.andApplyResultEqualTo("1")
                                .andApplyTimeBetween(yesterdayStartTime, yesterdayEndTime)
                                .andApiCodeEqualTo(apiCode);
                        sqlWhereClause = " and (if_lent <> '1' or if_lent is null)";
                        markPackagePushDaas(example, threadPool, apiCode, k, v, groupNoKey, dxUserTypeKey
                                , day, sqlWhereClause);
                        break;
                    case "c":
                        example.clear();
                        MarketingTransferSyncUserExample.Criteria criteriaC = example.createCriteria();
                        if (bool) {
                            criteriaC.andRequestTimeBetween(dateTimeStr[0].replace("T", " ")
                                    , dateTimeStr[1].replace("T", " "));
                        } else {
                            criteriaC.andRequestDataEqualTo(dateTimeStr[0]);
                        }
                        criteriaC.andIfLoginEqualTo("1")
                                .andLoginTimeBetween(yesterdayStartTime, yesterdayEndTime)
                                .andApiCodeEqualTo(apiCode);
                        sqlWhereClause = " and (if_apply <> '1' or if_apply is null)";
                        markPackagePushDaas(example, threadPool, apiCode, k, v, groupNoKey, dxUserTypeKey
                                , day, sqlWhereClause);
                        break;
                    default:
                }
            }
        });
    }

    /**
     * 2023-08-28 9:51
     * 组装daas记录信息
     */
    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transferSyncUser
            , MarketingSyncUser syncUser, String status, String dxUserType, int groupNo) {
        PhoneSaleExtendInfo info = new PhoneSaleExtendInfo();
        info.setApiCode(transferSyncUser.getApiCode());
        info.setCustNum(transferSyncUser.getCustNum());
        info.setAppletDate(transferSyncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        info.setAppletTime(transferSyncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DATE_TIME_FORMATTER));
        info.setTaskId(syncUser.getCusBatch());
        info.setStatus(status);
        info.setPStatus(1);
        info.setCreateTime(new Date());
        info.setUpdateTime(info.getCreateTime());
        info.setType(transferSyncUser.getType());
        info.setPushDxTime(new Date());
        info.setSourceId(transferSyncUser.getId());
        info.setCell(syncUser.getCell());
        info.setDxUserType(dxUserType);
        info.setGroupNo(groupNo);
        info.setUserType(transferSyncUser.getUserType());
        return info;
    }

    /**
     * 2023-08-28 9:52
     * 组装推送daas信息
     */
    private DassSingleImportDataDTO packageDassSingleImportDataDTO(MarketingTransferSyncUser transferSyncUser
            , MarketingSyncUser syncUser, String dxUserType, Map<String, MarketingTransferSyncUser> newTransferSyncUserMap) {
        String phone = BrCipherMaker.getInstance().decode(syncUser.getCell());
        DassSingleImportDataDTO singleImportDataDTO = new DassSingleImportDataDTO();
        String reserveField1 = syncUser.getReserveField1();
        String firstName = null;
        if (StringUtils.isNotBlank(reserveField1)) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                firstName = jsonObject.getString("firstName");
                if (StringUtils.isNotBlank(firstName)) {
                    firstName = firstName.replaceAll("\\*", "");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        MarketingTransferSyncUser newSyncUser = newTransferSyncUserMap.get(transferSyncUser.getCustNum());
        if (newSyncUser == null) {
            newSyncUser = transferSyncUser;
        }
        singleImportDataDTO.setName(StringUtils.isNotBlank(firstName) ? firstName : "1");
        singleImportDataDTO.setOrgname("zhongbang");
        singleImportDataDTO.setPhone(phone);
        singleImportDataDTO.setUserType(dxUserType);
        singleImportDataDTO.setSource("33");
        singleImportDataDTO.setUid(transferSyncUser.getCustNum());
        singleImportDataDTO.setRegisterTime(replaceZero(newSyncUser.getRegisterTime(), transferSyncUser.getRegisterTime()));
        singleImportDataDTO.setLoginTime(replaceZero(newSyncUser.getLoginTime(), transferSyncUser.getLoginTime()));
        singleImportDataDTO.setAuditTime(replaceZero(newSyncUser.getAuditTime(), transferSyncUser.getAuditTime()));
        singleImportDataDTO.setAuditAmount(newSyncUser.getAuditAmount());
        String idCard = BrCipherMaker.getInstance().decode(syncUser.getIdCard());
        if (StringUtils.isNotBlank(idCard)) {
            singleImportDataDTO.setGender(com.br.marketing.common.utils.StringUtils.getGenderByIdCard(idCard));
        }
        return singleImportDataDTO;
    }

    /**
     * 2023-08-29 9:31
     * 替换0
     */
    private String replaceZero(String s1, String s2) {
        return StringUtils.isBlank(s1) ? (StringUtils.isBlank(s2) ? s2 : s2.replace(":000", ""))
                : s1.replace(":000", "");
    }

    /**
     * 2023-08-28 9:52
     * 组装推送daas信息
     */
    private ConversionData packageConversionData(MarketingTransferSyncUser transferSyncUser
            , SyncUserValidityPeriodBO bo) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transferSyncUser.getId().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setCid(transferSyncUser.getCid());
        conversionData.setCaseNum(transferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transferSyncUser.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transferSyncUser.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setInversionStatus("0");
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transferSyncUser.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        // 有效期设置
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    /**
     * 2023-08-29 14:08
     * 动态调整线程大小
     */
    private synchronized void updatePoolSize(ThreadPoolExecutor threadPool) {
        int poolSize = marketingCommonConfig.getZhongBangTransferPushDaasThreadPoolSize();
        int corePoolSize = threadPool.getCorePoolSize();
        if (corePoolSize != poolSize || threadPool.getMaximumPoolSize() != poolSize) {
            threadPool.setMaximumPoolSize(poolSize);
            threadPool.setCorePoolSize(poolSize);
        }
        if (poolSize < 1) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 2023-08-29 10:06
     * 删除id条件
     */
    private void updateExamplePage(MarketingTransferSyncUserExample example, int pageNo, int pageSize) {
        example.setOrderByClause(" request_time limit " + pageNo * pageSize + "," + pageSize);
    }

    /**
     * 2023-08-29 10:06
     * 更新查询条件
     */
    private void checkActiveCount(ThreadPoolExecutor threadPool) {
        int activeCount = 0;
        do {
            try {
                activeCount = threadPool.getActiveCount();
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
        } while (activeCount != 0);
    }

    /**
     * 2023-08-28 9:47
     * 过滤数据
     * 推送daas及外呼
     */
    private void markPackagePushDaas(MarketingTransferSyncUserExample example
            , ThreadPoolExecutor threadPool
            , String apiCode
            , String status
            , JSONObject v
            , String groupNoKey
            , String dxUserTypeKey
            , int day
            , String sqlWhereClause) {
        int pageNo = 0;
        int pageSize = 2000;
        updateExamplePage(example, pageNo, pageSize);
        for (; ; ) {
            List<MarketingTransferSyncUser> dList = marketingTransferSyncUserMapper.selectByExampleSql(example
                    , sqlWhereClause);
            if (CollectionUtils.isEmpty(dList)) {
                break;
            }
            updatePoolSize(threadPool);
            Set<String> custNumSet = dList.stream().map(MarketingTransferSyncUser::getCustNum)
                    .collect(Collectors.toSet());
            threadPool.execute(() -> {
                try {
                    updatePoolSize(threadPool);
                    Map<String, SyncUserValidityPeriodBO> validityPeriodMap =
                            transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(
                                    custNumSet, apiCode, new Date());
                    if (CollectionUtils.isEmpty(validityPeriodMap)) {
                        return;
                    }
                    Set<String> cellSet = validityPeriodMap.values().stream().map(
                            m -> m.getSyncUser().getCell()).collect(Collectors.toSet());
                    int groupNo = v.getIntValue(groupNoKey);
                    Set<String> newCellSet = phoneSaleExtendService.groupRule(apiCode, day, cellSet
                            , groupNo);
                    List<MarketingTransferSyncUser> newTransferSyncUser = marketingTransferSyncUserMapper
                            .getTransferByCustNumOrderDatatikv_(dList.get(0).gettCid(), new ArrayList<>(custNumSet));
                    Map<String, MarketingTransferSyncUser> newTransferSyncUserMap = newTransferSyncUser.stream().collect(
                            Collectors.toMap(MarketingTransferSyncUser::getCustNum, Function.identity(), (v1, v2) -> v2));
                    List<DaasAndConversionData> list = new ArrayList<>();
                    for (MarketingTransferSyncUser transferSyncUser : dList) {
                        SyncUserValidityPeriodBO bo = validityPeriodMap.get(transferSyncUser.getCustNum());
                        // 有效期判断
                        if (bo == null) {
                            continue;
                        }
                        // 最新的上传数据
                        MarketingSyncUser syncUser = bo.getSyncUser();
                        String cell = syncUser.getCell();
                        if (!newCellSet.contains(cell)) {
                            continue;
                        }
                        String dxUserType = v.getString(dxUserTypeKey);
                        list.add(buildDaasAndConversionData(transferSyncUser, syncUser, status, dxUserType
                                , groupNo, newTransferSyncUserMap, bo));
                    }
                    ProcessHandlerContext context = new ProcessHandlerContext();
                    context.setApiCode(apiCode);
                    artificialRealTimeUserAndCustomerTransferSoleFacade.call(list, context);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
            int size = dList.size();
            if (size < pageSize) {
                break;
            }
            pageNo++;
            updateExamplePage(example, pageNo, pageSize);
        }
        checkActiveCount(threadPool);
    }

    private DaasAndConversionData buildDaasAndConversionData(MarketingTransferSyncUser transferSyncUser
            , MarketingSyncUser syncUser, String status, String dxUserType, int groupNo
            , Map<String, MarketingTransferSyncUser> newTransferSyncUserMap, SyncUserValidityPeriodBO bo) {
        DassSingleImportAdapSoleDTO soleDTO = new DassSingleImportAdapSoleDTO();
        // 电销本地推送记录
        PhoneSaleExtendInfo info = packagePhoneSaleExtendInfo(
                transferSyncUser, syncUser, status, dxUserType, groupNo);
        // 电销
        DassSingleImportDataDTO dassImportDataDTO = packageDassSingleImportDataDTO(
                transferSyncUser, syncUser, dxUserType, newTransferSyncUserMap);
        soleDTO.setDassSingleImportDataDTO(dassImportDataDTO);
        // 外呼
        ConversionData conversionData = packageConversionData(transferSyncUser, bo);
        RealTimeUserDataSoleDTO dto = new RealTimeUserDataSoleDTO();
        dto.setPhoneSaleExtendInfo(info);
        dto.setDassSingleImportAdapDTO(soleDTO);
        dto.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);
        dto.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        dto.setSoleType(1);
        DaasAndConversionData data = new DaasAndConversionData();
        data.setConversionData(conversionData);
        data.setRealTimeUserDataSoleDTO(dto);
        return data;
    }

    @Override
    public boolean zhongBangFileQueryAndDownload(String apiCode, String cid, String fileName
            , String tableHead, String filePath, String beginDate, String endDate, ThreadPoolExecutor threadPool) {
        // 2023-11-16 speed 控制文件名称，调度参数控制时间
        String okFileExtension = ".ok";
        String txtFileExtension = ".txt";
        String regex = "\\|@\\|";
        int maxSaveSize = 30;
        // 查询文件是否已创建完成
        List<FileInfo> okFiles = zBankClient.queryFileList(fileName, beginDate, endDate, 1);
        if (okFiles.size() > 0) {
            List<FileInfo> sortedOkFiles = sortedFileCreateTime(okFiles);
            for (FileInfo okFile : sortedOkFiles) {
                // 查询已经生成完成的文件
                String txtFileName = okFile.getFileName().replace(okFileExtension, txtFileExtension);
                List<FileInfo> infos = zBankClient.queryFileList(txtFileName, beginDate, endDate, 1);
                if (infos.size() > 0) {
                    List<FileInfo> sortedInfos = sortedFileCreateTime(infos);
                    String[] tableHeads = tableHead.split(regex);
                    int heads = tableHeads.length;
                    for (FileInfo fileInfo : sortedInfos) {
                        log.warn("众邦财富({})文件{}开始下载FileId:{}...", apiCode, fileInfo.getFileName(), fileInfo.getFileId());
                        String txtFilePath = filePath.concat(txtFileExtension);
                        if (mkdirPath(txtFilePath, apiCode, fileName)) {
                            return false;
                        }
                        long startTime = System.currentTimeMillis();
                        // 下载生成的文件
                        FileDownLoadInfo fileDownLoadInfo = zBankClient.downLoadSplitFileMergeInLocal(fileInfo, txtFilePath);
                        long endTime = System.currentTimeMillis();
                        if (fileDownLoadInfo == null || fileDownLoadInfo.getDestFile() == null) {
                            log.error("众邦财富({})文件{}下载失败！耗时：{}ms", apiCode, txtFileName, endTime - startTime);
                            return false;
                        }
                        log.warn("众邦财富({})文件{}下载完成，FileMd5:{},FileId:{},文件大小:{},耗时：{}ms"
                                , apiCode, fileDownLoadInfo.getFileName(), fileDownLoadInfo.getFileMd5()
                                , fileDownLoadInfo.getFileId(), fileDownLoadInfo.getFileSize(), endTime - startTime);
                        LocalFile localFile = selectLocalFile(apiCode, txtFilePath, fileInfo);
                        boolean localFileExist = localFile == null;
                        LocalFile localFileNew = localFileExist ? saveLocalFile(cid, apiCode, txtFilePath, fileInfo) : localFile;
                        fileInfo.setFileMd5(fileDownLoadInfo.getFileMd5());
                        LocalFile localFileUpdate = new LocalFile();
                        localFileUpdate.setErrorActualNumber(0);
                        localFileUpdate.setPushNumber(0);
                        localFileUpdate.setActualNumber(0);
                        localFileUpdate.setId(localFileNew.getId());
                        localFileUpdate.setLocalPath(fileDownLoadInfo.getDestFile().getParent());
                        try (LineNumberReader lineNumberReader = new LineNumberReader(new BufferedReader(
                                new InputStreamReader(new BufferedInputStream(new FileInputStream(
                                        fileDownLoadInfo.getDestFile())), StandardCharsets.UTF_8)))) {
                            AtomicInteger errorSum = new AtomicInteger(0);
                            String lineTxt;
                            List<PullCustomerFileData> fileDataList = new ArrayList<>();
                            int rowNum = 1;
                            while ((lineTxt = lineNumberReader.readLine()) != null) {
                                fileDataList.add(newFileData(lineTxt, apiCode, tableHeads, heads, regex, localFileUpdate
                                        , rowNum));
                                rowNum++;
                                if (saveFileData(fileDataList, maxSaveSize, localFile, threadPool, errorSum)) {
                                    fileDataList = new ArrayList<>();
                                }
                            }
                            int lineNumber = lineNumberReader.getLineNumber();
                            localFileUpdate.setActualNumber(lineNumber);
                            saveFileData(fileDataList, 1, localFile, threadPool, errorSum);
                            localFileUpdate.setSrcPath(fileInfo.getFileMd5());
                            isCompletedByTaskCount(threadPool, fileInfo.getFileName());
                            int sum;
                            if ((sum = errorSum.get()) == 0) {
                                return true;
                            }
                            log.error("众邦财富({})文件{}入库大量失败或入库异常！失败量：{}", apiCode, txtFileName, sum);
                            return false;
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            return false;
                        } finally {
                            localFileUpdate.setStatus("2");
                            localFileUpdate.setPushEndTime(new Date());
                            if (localFileUpdate.getComplete() == null) {
                                localFileUpdate.setComplete("1");
                            }
                            setNumber(apiCode, localFileUpdate);
                            localFileMapper.updateByPrimaryKeySelective(localFileUpdate);
                            String okFilePath = filePath.concat(okFileExtension);
                            if (!mkdirPath(okFilePath, apiCode, okFile.getFileName())) {
                                zBankClient.downLoadSplitFileMergeInLocal(okFile, okFilePath);
                            }
                        }
                    }
                } else {
                    log.warn("众邦财富({})在{}~{}时间段内没有查询到txt文件{}", apiCode, beginDate, endDate, txtFileName);
                }
                break;
            }
        } else {
            log.warn("众邦财富({})在{}~{}时间段内没有查询到ok文件{}", apiCode, beginDate, endDate, fileName);
        }
        return false;
    }

    private void isCompletedByTaskCount(ThreadPoolExecutor threadPool, String fileName) {
        int count = 0;
        while (threadPool.getTaskCount() != threadPool.getCompletedTaskCount() && count < 12) {
            log.warn("众邦财富文件{}批量入库未完成，计划执行的任务总数{},完成执行任务的总数{}，当前工作线程数{}，最大线程数{}" +
                            "，等待入库线程执行完。。。",
                    fileName, threadPool.getTaskCount()
                    , threadPool.getCompletedTaskCount()
                    , threadPool.getActiveCount()
                    , threadPool.getMaximumPoolSize());
            count++;
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                break;
            }
        }
    }

    /**
     * 2023-11-22 10:56
     * 实际入库量
     */
    private void setNumber(String apiCode, LocalFile localFileUpdate) {
        PullCustomerFileDataExample example = new PullCustomerFileDataExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andLocalFileIdEqualTo(localFileUpdate.getId())
                .andDataStatusEqualTo(1);
        int i = pullCustomerFileDataMapper.countByExample(example);
        int num = localFileUpdate.getActualNumber() - localFileUpdate.getErrorActualNumber();
        localFileUpdate.setPushNumber(Math.max(num, i));
    }

    /**
     * 2023-11-20 9:49
     * 保存本地文件记录
     */
    private LocalFile saveLocalFile(String cid, String apiCode, String localPath, FileInfo fileInfo) {
        LocalFile localFile = new LocalFile();
        localFile.setApiCode(apiCode);
        localFile.setCid(cid);
        localFile.setPushStartTime(new Date());
        localFile.setFileName(fileInfo.getFileName());
        localFile.setLocalPath(localPath);
        localFile.setStatus("1");
        localFile.setComplete("3");
        localFile.setPushStatus("0");
        localFile.setActualNumber(0);
        localFile.setPushNumber(0);
        localFile.setErrorActualNumber(0);
        // 众邦财富
        localFile.setFileType("zhongbang_caifu");
        localFile.setCreateTime(new Date());
        localFile.setUpdateTime(localFile.getCreateTime());
        localFile.setPushStartTime(localFile.getCreateTime());
        localFileMapper.insertSelective(localFile);
        return localFile;
    }

    /**
     * 2023-11-20 9:48
     * 创建文件数据日志
     */
    private PullCustomerFileData newFileData(String lineTxt
            , String apiCode, String[] tableHead, int heads, String regex, LocalFile localFile, int rowNum) {
        PullCustomerFileData fileData = new PullCustomerFileData();
        String[] rows;
        if (StringUtils.isBlank(lineTxt) || (rows = lineTxt.split(regex)).length != heads) {
            fileData.setDataStatus(2);
            localFile.setComplete(StringUtils.isBlank(lineTxt) ? null : "3");
            localFile.setErrorActualNumber(localFile.getErrorActualNumber() + 1);
        } else {
            try {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < heads; i++) {
                    jsonObject.put(tableHead[i], rows[i]);
                }
                fileData.setJsonData(jsonObject.toJSONString());
            } catch (Exception ignored) {
            }
            fileData.setDataStatus(1);
        }
        fileData.setFileData(lineTxt);
        fileData.setApiCode(apiCode);
        fileData.setDataFingerprint(MD5Utils.cell32(lineTxt.concat("_") + rowNum));
        fileData.setLocalFileId(localFile.getId());
        fileData.setCreateDate(LocalDate.now().toString());
        fileData.setCreateTime(new Date());
        fileData.setUpdateTime(fileData.getCreateTime());
        return fileData;
    }

    /**
     * 2023-11-20 9:47
     * 批量保存
     */
    private boolean saveFileData(List<PullCustomerFileData> fileDataList, int saveSize, LocalFile localFile
            , ThreadPoolExecutor threadPool, AtomicInteger errorSum) {
        if (fileDataList.size() >= saveSize) {
            threadPool.execute(() -> {
                if (localFile != null) {
                    Set<String> dataFingerprintSet = pullCustomerFileDataMapper.getDataFingerprintSet(localFile.getId()
                            , fileDataList);
                    if (dataFingerprintSet.size() == fileDataList.size()) {
                        return;
                    }
                    fileDataList.removeIf(f -> dataFingerprintSet.contains(f.getDataFingerprint()));
                }
                if (fileDataList.size() > 0) {
                    int i = pullCustomerFileDataMapper.insertBatchSelective(fileDataList);
                    if (i > 0) {
                        fileDataList.clear();
                        return;
                    }
                    log.error("众邦财富数据入库失败！localFile:{},数据指纹集合{}"
                            , fileDataList.get(0).getLocalFileId()
                            , fileDataList.stream().map(PullCustomerFileData::getDataFingerprint).toArray());
                    errorSum.addAndGet(fileDataList.size());
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 2023-11-20 9:48
     * 查询本地文件记录
     */
    private LocalFile selectLocalFile(String apiCode, String localPath, FileInfo fileInfo) {
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andFileTypeEqualTo("zhongbang_caifu")
                .andApiCodeEqualTo(apiCode)
                .andFileNameEqualTo(fileInfo.getFileName())
                .andCompleteEqualTo("3")
                .andPushStatusEqualTo("0")
                .andSrcPathIsNull()
                .andPushNumberEqualTo(0)
                .andErrorActualNumberEqualTo(0)
                .andLocalPathEqualTo(localPath);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        int size = localFiles.size();
        return size > 0 ? localFiles.get(0) : null;
    }

    /**
     * 2023-12-01 12:22
     * true 创建失败
     */
    private boolean mkdirPath(String path, String apiCode, String fileName) {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("众邦财富({})拉取文件目录创建失败，path:{},name:{}", apiCode, path, fileName);
                return true;
            }
        }
        return false;
    }


    /**
     * 2023-11-17 13:39
     * 文件信息排序，按创建时间降序，创建时间相同时按fileId降序
     */
    private static List<FileInfo> sortedFileCreateTime(List<FileInfo> files) {
        return files.stream().sorted(Comparator.comparing(FileInfo::getCreateTime)
                .thenComparing(FileInfo::getFileId).reversed()).collect(Collectors.toList());
    }


}
