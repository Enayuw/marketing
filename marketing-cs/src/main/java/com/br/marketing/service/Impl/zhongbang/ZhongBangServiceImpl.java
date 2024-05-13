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
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import com.br.marketing.mapper.PushCustomerFileInfoMapper;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserAndCustomerTransferSoleFacade;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import com.google.common.collect.Lists;
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
import java.util.function.BiFunction;
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
    public boolean voiceFileUpload(String apiCode, String cid, String beginDate, String endDate) {
        int availableNumber = Runtime.getRuntime().availableProcessors();
        boolean bool = availableNumber > 30;
        int corePoolSize = (availableNumber / 2);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(bool ? 15 : corePoolSize, bool ? 30
                : availableNumber + corePoolSize, new SynchronousQueue<>(), "br-zbank-voiceFile-file-upload");
        ThreadPoolExecutor mainPool = BrExecutors.getThreadPool(availableNumber / 2, availableNumber
                , "br-zbank-voiceFile-main");
        ThreadPoolExecutor apiPool = BrExecutors.getThreadPool(availableNumber / 2, availableNumber * 2
                , new SynchronousQueue<>(), "br-zbank-voiceFile-fileId-api");
        Date date = new Date();
        /* 2024-05-09 13:31
         * 录音文件目录可配置
         */
        String filePath = "E:\\项目文档\\智能营销中台\\众邦";
//        String filePath = marketingCommonConfig.getZhongBangUploadVoieFileDir();
        BiFunction<List<PushCustomerFileInfo>, Throwable, List<PushCustomerFileInfo>> handle = (fileInfoList, throwable) -> {
            if (throwable != null) {
                log.error(throwable.getMessage(), throwable);
            }
            return fileInfoList;
        };
        // 获取文件
        File directory = new File(filePath);
        if (directory.exists() && directory.isDirectory()) {
            File[] listFiles = directory.listFiles((dir, name) -> dir.isFile() && name.endsWith(".wav"));
            if (listFiles != null && (listFiles.length) > 0) {
                // 分段
                List<List<File>> fileListPartition = Lists.partition(Arrays.asList(listFiles), 2000);
                List<CompletableFuture<List<PushCustomerFileInfo>>> futures = new ArrayList<>();
                for (List<File> files : fileListPartition) {
                    // 查询重复数据
                    CompletableFuture<List<PushCustomerFileInfo>> listCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                .atStartOfDay().atZone(ZoneId.systemDefault());
                        Date dateStart = Date.from(zonedDateTime.toInstant());
                        Date dateEnd = Date.from(zonedDateTime.plusDays(1).toInstant());
                        Set<String> nameSet = files.stream().map(File::getName).collect(Collectors.toSet());
                        PushCustomerFileInfoExample example = new PushCustomerFileInfoExample();
                        example.createCriteria().andApiCodeEqualTo(apiCode)
                                .andCidEqualTo(cid).andNameIn(new ArrayList<>(nameSet))
                                .andCreateTimeGreaterThanOrEqualTo(dateStart)
                                .andCreateTimeLessThan(dateEnd);
                        return pushCustomerFileInfoMapper.selectByExample(example);
                    }, mainPool).exceptionally(throwable -> {
                        log.error(throwable.getMessage(), throwable);
                        return null;
                    });

                    // 失败数据补推
                    CompletableFuture<List<PushCustomerFileInfo>> failCompletableFuture =
                            listCompletableFuture.thenApplyAsync((fileInfoList) -> {
                                if (fileInfoList == null) {
                                    return null;
                                }
                                List<PushCustomerFileInfo> fileInfos = fileInfoList.stream().filter(
                                        fileInfo -> fileInfo.getPushStatus() == 3).collect(Collectors.toList());
                                List<PushCustomerFileInfo> updateFileInfoList = new ArrayList<>();
                                for (PushCustomerFileInfo fileInfo : fileInfos) {
                                    // 开始补推
                                    File file = new File(fileInfo.getFileDirectory().concat(File.separator).concat(fileInfo.getName()));
                                    if (file.exists()) {
                                        PushCustomerFileInfo newFileInfo = new PushCustomerFileInfo();
                                        newFileInfo.setId(fileInfo.getId());
                                        newFileInfo.setUpdateTime(new Date());
                                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                                            UploadInfo uploadInfo = zBankClient.uploadInputStream(inputStream
                                                    , fileInfo.getName(), fileInfo.getSize(), fileInfo.getFileMd5());
                                            newFileInfo.setFileId(uploadInfo.getFileId());
                                            updateFileInfoList.add(newFileInfo);
                                            // 推送正常
                                            newFileInfo.setPushStatus(2);
                                            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(newFileInfo);
                                        } catch (SDKException | IOException e) {
                                            newFileInfo.setRemark(e.getMessage());
                                            if (e instanceof IOException) {
                                                // 文件异常
                                                newFileInfo.setStatus(2);
                                                updateFileInfoList.add(newFileInfo);
                                                pushCustomerFileInfoMapper.updateByPrimaryKeySelective(newFileInfo);
                                            }
                                            log.error(e.getMessage(), e);
                                        }
                                    }
                                }
                                return updateFileInfoList;
                            }, threadPool).handle(handle);
                    futures.add(failCompletableFuture);

                    // 调用api接口
                    failCompletableFuture.thenApplyAsync(infoList -> {
                        // TODO: 2024-05-10 调用api接口
                        log.warn("异常数据补推,我要调用接口了{}", infoList);
                        return infoList;
                    }, apiPool).handle(handle);

                    // 已推送数据去重
                    CompletableFuture<List<File>> fileCompletableFuture = listCompletableFuture.thenApplyAsync((fileInfoList) -> {
                        if (fileInfoList == null) {
                            return null;
                        }
                        Set<String> names = fileInfoList.stream().map(PushCustomerFileInfo::getName).collect(Collectors.toSet());
                        return files.stream().filter(file -> !names.contains(file.getName())).collect(Collectors.toList());
                    }, mainPool).exceptionally(throwable -> {
                        log.error(throwable.getMessage(), throwable);
                        return null;
                    });

                    // 数据入库
                    CompletableFuture<Map<String, PushCustomerFileInfo>> saveThenApplyAsync =
                            fileCompletableFuture.thenApplyAsync(fileSaves -> {
                                Map<String, PushCustomerFileInfo> infoMap = new HashMap<>(2048);
                                for (File file : fileSaves) {
                                    // 文件信息入库
                                    PushCustomerFileInfo fileInfo = new PushCustomerFileInfo();
                                    Date lastModifiedDate = new Date(file.lastModified());
                                    String fileName = file.getName();
                                    String parent = file.getParent();
                                    long length = file.length();
                                    fileInfo.setCid(cid);
                                    fileInfo.setApiCode(apiCode);
                                    fileInfo.setName(fileName);
                                    fileInfo.setLastModifiedTime(lastModifiedDate);
                                    fileInfo.setLastModifiedDate(lastModifiedDate);
                                    fileInfo.setFileDirectory(parent == null ? filePath : parent);
                                    fileInfo.setSize(length);
                                    fileInfo.setCreateTime(new Date());
                                    fileInfo.setUpdateTime(fileInfo.getCreateTime());
                                    // 待推送
                                    fileInfo.setPushStatus(0);
                                    infoMap.put(fileName, fileInfo);
                                    pushCustomerFileInfoMapper.insertSelective(fileInfo);
                                }
                                return infoMap;
                            }, mainPool).handle((fileInfoMap, throwable) -> {
                                if (throwable != null) {
                                    log.error(throwable.getMessage(), throwable);
                                }
                                return fileInfoMap;
                            });

                    // 文件推送
                    CompletableFuture<Map<String, PushCustomerFileInfo>> pushThenApplyAsync =
                            fileCompletableFuture.thenApplyAsync(filePushs -> {
                                Map<String, PushCustomerFileInfo> infoMap = new HashMap<>(2048);
                                for (File file : filePushs) {
                                    PushCustomerFileInfo fileInfo = new PushCustomerFileInfo();
                                    fileInfo.setPushDate(date);
                                    if (file.exists() && file.isFile()) {
                                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                                            String fileMd5 = Md5EncodeUtil.encode(file);
                                            fileInfo.setFileMd5(fileMd5);
                                            UploadInfo uploadInfo = zBankClient.uploadInputStream(inputStream
                                                    , file.getName(), file.length(), fileMd5);
                                            fileInfo.setFileId(uploadInfo.getFileId());
                                            // 推送成功
                                            fileInfo.setPushStatus(2);
                                        } catch (SDKException | IOException e) {
                                            log.error(e.getMessage(), e);
                                            fileInfo.setRemark(e.getMessage());
                                            if (e instanceof IOException) {
                                                // 文件异常
                                                fileInfo.setStatus(2);
                                            }
                                            // 推送失败
                                            fileInfo.setPushStatus(3);
                                        }
                                    } else {
                                        fileInfo.setStatus(2);
                                    }
                                    infoMap.put(file.getName(), fileInfo);
                                }
                                return infoMap;
                            }, threadPool).handle((fileInfoMap, throwable) -> {
                                if (throwable != null) {
                                    log.error(throwable.getMessage(), throwable);
                                }
                                return fileInfoMap;
                            });

                    // 结果汇总
                    CompletableFuture<List<PushCustomerFileInfo>> future = saveThenApplyAsync.thenCombineAsync(
                            pushThenApplyAsync, (fileInfoMap, fileInfoMap2) -> {
                                List<PushCustomerFileInfo> infoList = new ArrayList<>();
                                if (fileInfoMap != null && fileInfoMap2 != null) {
                                    fileInfoMap.forEach((name, value) -> {
                                        PushCustomerFileInfo fileInfo = fileInfoMap2.get(name);
                                        if (value.getId() == null) {
                                            value.setStatus(fileInfo.getStatus());
                                            value.setFileId(fileInfo.getFileId());
                                            value.setPushStatus(fileInfo.getPushStatus());
                                            value.setPushDate(fileInfo.getPushDate());
                                            pushCustomerFileInfoMapper.insertSelective(value);
                                        } else {
                                            fileInfo.setId(value.getId());
                                            fileInfo.setUpdateTime(new Date());
                                            infoList.add(fileInfo);
                                            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                                        }
                                    });
                                } else if (fileInfoMap2 != null) {
                                    fileInfoMap2.forEach((name, value) -> {
                                        value.setName(name);
                                        value.setUpdateTime(new Date());
                                        infoList.add(value);
                                        pushCustomerFileInfoMapper.updateByPrimaryKeySelective(value);
                                    });
                                } else if (fileInfoMap != null) {
                                    fileInfoMap.forEach((name, value) -> {
                                        if (value.getId() == null) {
                                            pushCustomerFileInfoMapper.insertSelective(value);
                                        }
                                    });
                                }
                                return infoList;
                            }, mainPool).handle(handle);
                    futures.add(future);

                    // 调用api接口
                    future.thenApplyAsync(infoList -> {
                        // TODO: 2024-05-10 调用api接口
                        log.warn("我要调用接口了，{}", infoList);
                        return infoList;
                    }, apiPool).handle(handle);
                }
                // 结果转换
                try {
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply((v) -> {
                        for (CompletableFuture<List<PushCustomerFileInfo>> futureList : futures) {
                            try {
                                List<PushCustomerFileInfo> infoList = futureList.get(1, TimeUnit.MINUTES);
                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                log.error(e.getMessage());
                            }
                        }
                        return false;
                    }).get(1, TimeUnit.HOURS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error(e.getMessage(), e);
                }
                return false;
            }
        }
        return false;
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 1;
        });
        final CompletableFuture<Integer> future1 = future.thenApply((i) -> {
            return ++i;
        });

        final CompletableFuture<Integer> future2 = future1.thenApply((i) -> {
            int t = i / 0;
            return i + 10;
        });

        CompletableFuture<Integer> handle = future2.handle((integer, throwable) -> {
            if (throwable != null) {
                System.out.println("handle" + throwable.getMessage() + integer);
            }
            System.out.println("handle" + integer);
            return integer;
        });
        CompletableFuture<Boolean> handle23 = future2.handle((integer, throwable) -> {
            if (throwable != null) {
                System.out.println("handle" + throwable.getMessage() + integer);
            }
            System.out.println("handle" + integer);
            return false;
        });

        CompletableFuture<Integer> whenComplete = future2.whenComplete((integer, throwable) -> {
            if (throwable != null) {
                System.out.println("whenComplete" + throwable.getMessage() + integer);
            }
            System.out.println("whenComplete" + integer);
        });
        CompletableFuture<Integer> exceptionally = future2.exceptionally(throwable -> {
            System.out.println("exceptionally" + throwable.getMessage());
            return -32;
        });

        final CompletableFuture<Void> voidCompletableFuture = future2.thenAccept((i) -> {
            System.out.println(i);
        });
        final CompletableFuture<Void> voidCompletableFuture1 = handle.thenAccept((i) -> {
            System.out.println(i);
        });

        final CompletableFuture<Void> exceptionally1 = exceptionally.thenAccept((i) -> {
            System.out.println("exceptionally1:" + i);
        });

        System.out.println("exceptionally.get()" + exceptionally.get());
        CompletableFuture.allOf(future, future1, future2, voidCompletableFuture, voidCompletableFuture1, exceptionally1).join();


    }
}
