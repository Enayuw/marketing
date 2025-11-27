package com.br.marketing.service.Impl.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.MD5Utils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.SftpClient;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserAndCustomerTransferSoleFacade;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import com.jcraft.jsch.SftpException;
import com.zbank.file.bean.FileDownLoadInfo;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.UploadInfo;
import com.zbank.file.common.utils.Md5EncodeUtil;
import com.zbank.file.exception.SDKException;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
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

    @Resource
    private PushCustomerFileInfoMapper pushCustomerFileInfoMapper;

    @Resource
    private FileDbConfigMapper fileDbConfigMapper;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private ZhongbangVoiceFileDetailMapper zhongbangVoiceFileDetailMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");


    @Override
    public void pushTransferToDaasRealTimeUserOneAndCustomer(String apiCode, ThreadPoolExecutor threadPool
            , String... dateTimeStr) {
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
            , SyncUserValidityPeriodsBO bo) {
        ConversionData conversionData = new ConversionData();
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        conversionData.setDataId(transferSyncUser.getId().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
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
        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    /**
     * 2023-08-29 14:08
     * 动态调整线程大小
     */
    private void updatePoolSize(ThreadPoolExecutor threadPool) {
        int poolSize = marketingCommonConfig.getZhongBangTransferPushDaasThreadPoolSize();
        
        if (poolSize < 1) {
            throw new IllegalArgumentException();
        }
        
        synchronized (this) {
            int corePoolSize = threadPool.getCorePoolSize();
            if (corePoolSize != poolSize || threadPool.getMaximumPoolSize() != poolSize) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, poolSize);
            }
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
                    Map<String, SyncUserValidityPeriodsBO> validityPeriodMap =
                            transferDataValidityPeriodService.getValidityPeriodsByCustNum(
                                    custNumSet, apiCode, new Date());
                    if (CollectionUtils.isEmpty(validityPeriodMap)) {
                        return;
                    }
                    Set<String> cellSet = new HashSet<>();
                    validityPeriodMap.values().forEach((SyncUserValidityPeriodsBO userValidityPeriodsBO) -> {
                        List<MarketingSyncUser> syncUsers = userValidityPeriodsBO.getSyncUsers();
                        Set<String> set = syncUsers.stream().map(MarketingSyncUser::getCell).collect(Collectors.toSet());
                        cellSet.addAll(set);
                    });
                    int groupNo = v.getIntValue(groupNoKey);
                    Set<String> newCellSet = phoneSaleExtendService.groupRule(apiCode, day, cellSet
                            , groupNo);
                    List<MarketingTransferSyncUser> newTransferSyncUser = marketingTransferSyncUserMapper
                            .getTransferByCustNumOrderDatatikv_(dList.get(0).gettCid(), new ArrayList<>(custNumSet));
                    Map<String, MarketingTransferSyncUser> newTransferSyncUserMap = newTransferSyncUser.stream().collect(
                            Collectors.toMap(MarketingTransferSyncUser::getCustNum, Function.identity(), (v1, v2) -> v2));
                    List<DaasAndConversionData> list = new ArrayList<>();
                    for (MarketingTransferSyncUser transferSyncUser : dList) {
                        SyncUserValidityPeriodsBO bo = validityPeriodMap.get(transferSyncUser.getCustNum());
                        // 有效期判断
                        if (bo == null) {
                            continue;
                        }
                        // 最新的上传数据
                        MarketingSyncUser syncUser = bo.getSyncUsers().get(0);
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
            , Map<String, MarketingTransferSyncUser> newTransferSyncUserMap, SyncUserValidityPeriodsBO bo) {
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
    public boolean fileQueryAndDownload(String apiCode, String cid, String fileName
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
                                        // 缓存1M
                                        fileDownLoadInfo.getDestFile())), StandardCharsets.UTF_8), 1024 << 10))) {
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
        fileData.setDataFingerprint(MD5Utils.cell32(lineTxt).concat("_") + rowNum);
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

    @Override
    public boolean voiceFileUpload(String apiCode, String cid, LocalDate localDate) {
        boolean resultBool = true;
        int pageSize = 2000;
        int availableNumber = Runtime.getRuntime().availableProcessors();
        boolean bool = availableNumber > 25;
        int corePoolSize = (availableNumber / 2);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(bool ? 15 : corePoolSize, bool ? 25
                : (availableNumber + corePoolSize), new SynchronousQueue<>(), "br-zbank-voiceFile-file-upload");
        ThreadPoolExecutor threadPoolGet = BrExecutors.getThreadPool(corePoolSize, availableNumber
                , new SynchronousQueue<>(), "br-zbank-voiceFile-sftp-get");
        Map<String, JSONObject> zhongBangVoiceFileConfig = getVoiceFileConfig();
        ZonedDateTime zonedDateTime = localDate.atStartOfDay().atZone(ZoneId.systemDefault());
        Date startDate = Date.from(zonedDateTime.toInstant());
        Date endDate = Date.from(zonedDateTime.plusDays(1).toInstant());
        String dateStr = localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Set<Map.Entry<String, JSONObject>> entrySet = zhongBangVoiceFileConfig.entrySet();
        // 遍历文件配置信息
        for (Map.Entry<String, JSONObject> entry : entrySet) {
            JSONObject entryValue = entry.getValue();
            String fileType = entryValue.getString("fileType");
            String tableName = entry.getKey();
            List<FileDbConfig> fileDbConfigs = getFileDbConfig(apiCode, fileType, tableName);
            // 文件服务信息遍历
            for (FileDbConfig fileDbConfig : fileDbConfigs) {
                Long sftpConfigId = fileDbConfig.getSftpConfigId();
                SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(sftpConfigId);
                List<LocalFile> localFiles = getLocalFile(apiCode, fileType, startDate, endDate);
                resultBool = localFiles.size() > 0 && resultBool;
                // 明细文件信息
                for (LocalFile localFile : localFiles) {
                    String fileName = localFile.getFileName();
                    Long localFileId = localFile.getId();
                    int fileDetailsCount;
                    int fileInfoCount;
                    PushCustomerFileInfoExample exampleInfoCount = new PushCustomerFileInfoExample();
                    exampleInfoCount.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cid)
                            .andStatusEqualTo(1).andPushStatusIn(Arrays.asList(0, 3)).andLocalFileIdEqualTo(localFileId);
                    fileInfoCount = pushCustomerFileInfoMapper.countByExample(exampleInfoCount);
                    ZhongbangVoiceFileDetailExample exampleDetailCount = new ZhongbangVoiceFileDetailExample();
                    exampleDetailCount.createCriteria().andLocalIdEqualTo(localFileId).andStatusEqualTo(1)
                            .andApiCodeEqualTo(apiCode).andPushStatusEqualTo(0).andIsDeletedEqualTo(0);
                    fileDetailsCount = zhongbangVoiceFileDetailMapper.countByExample(exampleDetailCount);
                    if (fileInfoCount != fileDetailsCount) {
                        // 下载远程文件
                        resultBool = isFromSftpLocalDisk(localFile, syncConfig, dateStr, apiCode, cid
                                , pageSize, threadPoolGet, tableName) && resultBool;
                    }
                    fileInfoCount = pushCustomerFileInfoMapper.countByExample(exampleInfoCount);
                    if (fileInfoCount == fileDetailsCount) {
                        if (fileDetailsCount > 0) {
                            // 上传开始时间记录
                            updateLocalFilePushTime(localFileId);
                            // 文件上传
                            List<CompletableFuture<Boolean>> futures = uploadFile(
                                    cid, apiCode, pageSize, tableName, threadPool, localFile);
                            // 结果转换
                            resultBool = allOf(futures) && resultBool;
                        } else {
                            ZhongbangVoiceFileDetailExample countExample = new ZhongbangVoiceFileDetailExample();
                            exampleDetailCount.createCriteria().andLocalIdEqualTo(localFileId).andApiCodeEqualTo(apiCode)
                                    .andStatusEqualTo(1).andIsDeletedEqualTo(0);
                            fileDetailsCount = zhongbangVoiceFileDetailMapper.countByExample(countExample);
                            resultBool = fileDetailsCount > 0;
                        }
                    } else {
                        String msg = "众邦录音文件量级与明细量级不匹配，录音文件量级:" + fileInfoCount
                                + ",明细量级:" + fileDetailsCount + ",明细文件：" + fileName;
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(), msg
                                , "众邦录音文件量级与明细量级不匹配"));
                        resultBool = false;
                    }
                }
            }
        }
        return resultBool;
    }

    private Map<String, JSONObject> getVoiceFileConfig() {
        Map<String, JSONObject> zhongBangVoiceFileConfig = marketingCommonConfig.getZhongBangVoiceFileConfig();
        if (zhongBangVoiceFileConfig.isEmpty()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileType", "zhongbang_voice");
            jsonObject.put("uploadPoolSize", 5);
            jsonObject.put("getFilePoolSize", 5);
            zhongBangVoiceFileConfig.put("b_zhongbang_voice_file_detail", jsonObject);
        } else {
            JSONObject jsonObject = zhongBangVoiceFileConfig.get("b_zhongbang_voice_file_detail");
            if (jsonObject == null) {
                JSONObject jo = new JSONObject();
                jo.put("fileType", "zhongbang_voice");
                jo.put("uploadPoolSize", 5);
                jo.put("getFilePoolSize", 5);
                zhongBangVoiceFileConfig.put("b_zhongbang_voice_file_detail", jo);
            } else {
                if (!jsonObject.containsKey("fileType")) {
                    jsonObject.put("fileType", "zhongbang_voice");
                }
                if (!jsonObject.containsKey("uploadPoolSize")) {
                    jsonObject.put("uploadPoolSize", Runtime.getRuntime().availableProcessors());
                }
                if (!jsonObject.containsKey("getFilePoolSize")) {
                    jsonObject.put("getFilePoolSize", Runtime.getRuntime().availableProcessors());
                }
            }
        }
        return zhongBangVoiceFileConfig;
    }

    private List<FileDbConfig> getFileDbConfig(String apiCode, String fileType, String tableName) {
        FileDbConfigExample fileDbConfigExample = new FileDbConfigExample();
        fileDbConfigExample.createCriteria().andApiCodeEqualTo(apiCode).andDbNameEqualTo(tableName)
                .andDelEqualTo(1).andFileTypeEqualTo(fileType);
        return fileDbConfigMapper.selectByExample(fileDbConfigExample);
    }

    private List<LocalFile> getLocalFile(String apiCode, String fileType, Date startDate, Date endDate) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andStatusEqualTo("2").andCompleteEqualTo("1")
                .andApiCodeEqualTo(apiCode).andFileTypeEqualTo(fileType)
                .andCreateTimeGreaterThanOrEqualTo(startDate).andCreateTimeLessThan(endDate)
                .andActualNumberGreaterThan(0);
        return localFileMapper.selectByExample(localFileExample);
    }

    private void setThreadPool(final String tableName, String poolKey, ThreadPoolExecutor poolExecutor) {
        Map<String, JSONObject> voiceFileConfig = getVoiceFileConfig();
        int poolSize = voiceFileConfig.get(tableName).getIntValue(poolKey);
        if (poolSize > 0 && poolSize != poolExecutor.getCorePoolSize()) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(poolExecutor, poolSize);
        }
    }

    private void updateLocalFilePushTime(long localFileId) {
        LocalFile byPrimaryKey = localFileMapper.getByPrimaryKey(localFileId);
        if (byPrimaryKey.getPushStartTime() == null) {
            LocalFile localFileNew = new LocalFile();
            localFileNew.setId(localFileId);
            localFileNew.setPushStartTime(new Date());
            localFileMapper.updateByPrimaryKeySelective(localFileNew);
        }
    }

    /**
     * 2024-05-20 19:59
     * 下载远程文件到本地磁盘及文件信息保存
     */
    private boolean isFromSftpLocalDisk(LocalFile localFile, SyncConfig syncConfig, String dateStr
            , String apiCode, String cid, Integer pageSize, ThreadPoolExecutor threadPoolGet
            , final String tableName) {
        long localFileId = localFile.getId();
        String fileName = localFile.getFileName();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        String localPath = localFile.getLocalPath();
        String localDir = localPath.replaceAll(DateUtils.yyyyMMdd, dateStr).concat(fileName.replace(".txt", ""))
                .concat(File.separator).concat("voice_" + localFileId).concat(File.separator);
        String srcPath = syncConfig.getSrcPath().replaceAll(DateUtils.yyyyMMdd, dateStr);
        long maxId = 0;
        while (!Thread.currentThread().isInterrupted()) {
            ZhongbangVoiceFileDetailExample voiceFileDetailExample = new ZhongbangVoiceFileDetailExample();
            voiceFileDetailExample.createCriteria().andLocalIdEqualTo(localFileId).andStatusEqualTo(1)
                    .andApiCodeEqualTo(apiCode).andPushStatusEqualTo(0).andIsDeletedEqualTo(0)
                    .andIdGreaterThan(maxId);
            voiceFileDetailExample.setOrderByClause("id limit " + pageSize);
            List<ZhongbangVoiceFileDetail> fileDetails = zhongbangVoiceFileDetailMapper
                    .selectByExample(voiceFileDetailExample);
            if (fileDetails.isEmpty()) {
                break;
            }
            int size = fileDetails.size();
            maxId = fileDetails.get(size - 1).getId();
            setThreadPool(tableName, "getFilePoolSize", threadPoolGet);
            futures.add(CompletableFuture.supplyAsync(() -> {
                boolean bool = true;
                // 获得sftp连接
                SftpClient ftpClient = new SftpClient(syncConfig, true);
                try {
                    if (ftpClient.connect() && ftpClient.isConnected()) {
                        List<String> fileNameList = fileDetails.stream().map(ZhongbangVoiceFileDetail::getFileName)
                                .collect(Collectors.toList());
                        PushCustomerFileInfoExample example = new PushCustomerFileInfoExample();
                        example.createCriteria().andApiCodeEqualTo(apiCode)
                                .andCidEqualTo(cid).andFileNameIn(fileNameList).andLocalFileIdEqualTo(localFileId);
                        List<PushCustomerFileInfo> infoList = pushCustomerFileInfoMapper.selectByExample(example);
                        Map<String, PushCustomerFileInfo> fileInfoMap = infoList.stream().collect(Collectors.toMap(
                                PushCustomerFileInfo::getFileName, Function.identity()));
                        for (ZhongbangVoiceFileDetail detail : fileDetails) {
                            if (StringUtils.isBlank(detail.getFileName())) {
                                continue;
                            }
                            try {
                                File dir = new File(localDir);
                                if (!dir.exists() && !dir.mkdirs()) {
                                    log.error("众邦下载外呼录音文件，本地目录创建失败：{}", localDir);
                                    continue;
                                }
                                File file = ftpClient.downloadLocalFile(srcPath, detail.getFileName()
                                        , localDir.concat(detail.getFileName()));
                                String fileMd5 = Md5EncodeUtil.encode(file);
                                String parent = file.getParent();
                                long length = file.length();
                                PushCustomerFileInfo fileInfoOld = fileInfoMap.get(detail.getFileName());
                                if (fileInfoOld == null) {
                                    // 文件信息入库
                                    bool = saveInfo(cid, file, apiCode, parent, localDir, length, fileMd5, localFileId) && bool;
                                } else {
                                    updateInfo(fileInfoOld, fileMd5, file, parent, localDir, length);
                                }
                            } catch (SftpException | IOException | SDKException e) {
                                log.error(e.getMessage() + "录音文件：" + detail.getFileName(), e);
                                Thread.currentThread().interrupt();
                                bool = false;
                                if (e instanceof SftpException) {
                                    disconnect(ftpClient);
                                    try {
                                        ftpClient = new SftpClient(syncConfig, true);
                                        ftpClient.connect();
                                    } catch (Exception exception) {
                                        log.error(exception.getMessage());
                                    }
                                }
                            }
                        }
                    } else {
                        bool = false;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    bool = false;
                } finally {
                    disconnect(ftpClient);
                }
                return bool;
            }, threadPoolGet).exceptionally((Throwable throwable) -> {
                if (throwable != null) {
                    log.error(throwable.getMessage(), throwable);
                }
                return false;
            }));
            if (size < pageSize) {
                break;
            }
        }
        return allOf(futures);
    }

    private void disconnect(SftpClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error("众邦录音文件下载sftp关闭异常！" + e.getMessage(), e);
            }
        }
    }

    private boolean saveInfo(String cid, File file, String apiCode, String parent, String localDir
            , long length, String fileMd5, long localFileId) {
        // 文件信息入库
        PushCustomerFileInfo fileInfo = new PushCustomerFileInfo();
        Date lastModifiedDate = new Date(file.lastModified());
        fileInfo.setCid(cid);
        fileInfo.setApiCode(apiCode);
        fileInfo.setFileName(file.getName());
        fileInfo.setLastModifiedTime(lastModifiedDate);
        fileInfo.setLastModifiedDate(lastModifiedDate);
        fileInfo.setFileDirectory(parent == null ? localDir : parent);
        fileInfo.setFileSize(length);
        fileInfo.setCreateTime(new Date());
        fileInfo.setUpdateTime(fileInfo.getCreateTime());
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setLocalFileId(localFileId);
        // 待推送
        fileInfo.setPushStatus(0);
        try {
            pushCustomerFileInfoMapper.insertSelective(fileInfo);
            return true;
        } catch (Exception e) {
            log.error("众邦录音文件信息保存失败！" + e.getMessage(), e);
            return false;
        }
    }

    private void updateInfo(PushCustomerFileInfo fileInfoOld, String fileMd5, File file, String parent
            , String localDir, long length) {
        if (!fileInfoOld.getFileMd5().equals(fileMd5)) {
            PushCustomerFileInfo fileInfoUpdate = new PushCustomerFileInfo();
            Date lastModifiedDate = new Date(file.lastModified());
            fileInfoUpdate.setId(fileInfoOld.getId());
            fileInfoUpdate.setLastModifiedTime(lastModifiedDate);
            fileInfoUpdate.setLastModifiedDate(lastModifiedDate);
            fileInfoUpdate.setFileDirectory(parent == null ? localDir : parent);
            fileInfoUpdate.setFileSize(length);
            fileInfoUpdate.setUpdateTime(new Date());
            fileInfoUpdate.setFileMd5(fileMd5);
            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfoUpdate);
        }
    }

    private boolean allOf(List<CompletableFuture<Boolean>> futures) {
        try {
            return CompletableFuture.allOf(futures.toArray(
                    new CompletableFuture[0])).thenApply((Void v) -> {
                boolean b = true;
                for (CompletableFuture<Boolean> future : futures) {
                    try {
                        if (!future.get(1, TimeUnit.MINUTES) && b) {
                            b = false;
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.error(e.getMessage());
                        b = false;
                        Thread.currentThread().interrupt();
                    }
                }
                return b;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private List<CompletableFuture<Boolean>> uploadFile(String cid, String apiCode, int pageSize
            , final String tableName, ThreadPoolExecutor threadPool, LocalFile localFile) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        long localFileId = localFile.getId();
        long maxId = 0L;
        for (; true; ) {
            PushCustomerFileInfoExample exampleSelect = new PushCustomerFileInfoExample();
            exampleSelect.createCriteria().andApiCodeEqualTo(apiCode)
                    .andCidEqualTo(cid).andLocalFileIdEqualTo(localFileId)
                    .andStatusEqualTo(1).andPushStatusIn(Arrays.asList(0, 3)).andIdGreaterThan(maxId);
            exampleSelect.setOrderByClause("id limit " + pageSize);
            List<PushCustomerFileInfo> infoList = pushCustomerFileInfoMapper.selectByExample(exampleSelect);
            if (infoList.isEmpty()) {
                break;
            }
            int size = infoList.size();
            maxId = infoList.get(size - 1).getId();
            for (PushCustomerFileInfo fileInfo : infoList) {
                setThreadPool(tableName, "uploadPoolSize", threadPool);
                // 文件推送
                futures.add(CompletableFuture.supplyAsync(() -> {
                    File file = new File(fileInfo.getFileDirectory().concat(File.separator) + fileInfo.getFileName());
                    ZhongbangVoiceFileDetail voiceFileDetail = new ZhongbangVoiceFileDetail();
                    voiceFileDetail.setFileName(file.getName());
                    voiceFileDetail.setLocalId(localFileId);
                    fileInfo.setPushDate(new Date());
                    if (file.exists() && file.isFile()) {
                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                            UploadInfo uploadInfo = zBankClient.uploadInputStream(inputStream
                                    , file.getName(), file.length(), fileInfo.getFileMd5());
                            voiceFileDetail.setCustomerFileId(uploadInfo.getFileId());
                            voiceFileDetail.setPushStatus(1);
                            // 推送成功
                            fileInfo.setPushStatus(2);
                            fileInfo.setRemark("");
                        } catch (SDKException | IOException e) {
                            log.error(e.getMessage(), e);
                            fileInfo.setRemark(e.getMessage());
                            if (e instanceof IOException) {
                                // 文件异常
                                fileInfo.setStatus(2);
                            }
                            // 推送失败
                            fileInfo.setPushStatus(3);
                            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                            return false;
                        }
                    } else {
                        fileInfo.setStatus(2);
                        pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                        return false;
                    }
                    int i = pushCustomerFileInfoMapper.updateFileInfoAndFileDetailtikv_(fileInfo, voiceFileDetail);
                    if (i != 2) {
                        String msg = "众邦录音文件上传更新失败:文件：" + fileInfo.getFileName()
                                + ",明细文件：" + localFile.getFileName();
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode()
                                , msg, "众邦录音文件上传更新失败"));
                        return false;
                    }
                    return true;
                }, threadPool).exceptionally((Throwable throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                    }
                    return false;
                }));
            }
            if (size < pageSize) {
                break;
            }
        }
        return futures;
    }

}
