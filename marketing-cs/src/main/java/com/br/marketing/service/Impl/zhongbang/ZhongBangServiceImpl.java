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
import com.br.marketing.client.zbank.ZBankClient;
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
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserAndCustomerTransferSoleFacade;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.StreamDownLoadInfo;
import com.zbank.file.common.utils.Md5EncodeUtil;
import com.zbank.file.exception.SDKException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
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
    private ZBankClient zBankClient;

    @Resource
    private SyncConfigService syncConfigService;

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
                    pushWarnMessage(apiCode);
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

    private void pushWarnMessage(String apiCode) {
        String timeStr = "10:00:00";
        if (LocalTime.now().isAfter(LocalTime.parse(timeStr))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode()
                    , "众邦转化数据推送到daas(单条)与外呼，推送时间已过“10点”,任务将继续执行...,apiCode:" + apiCode
                    , "众邦转化数据推送daas(单条)与外呼告警"));
        }
    }

    @Override
    public boolean zhongBangFileQueryAndDownload(String apiCode, String cid, String fileName
            , String tableHead, String filePath, String beginDate, String endDate, ExecutorService executor) {
        // 2023-11-16 speed 控制文件名称，调度参数控制时间
        String okFileExtension = ".ok";
        String txtFileExtension = ".txt";
        String regex = "\\|@\\|";
        // 查询文件是否已创建完成
        List<FileInfo> okFiles = zBankClient.queryFileList(fileName, beginDate, endDate, 1);
        if (okFiles.size() > 0) {
            List<FileInfo> sortedOkFiles = sortedFileCreateTime(okFiles);
            for (FileInfo okFile : sortedOkFiles) {
                // 查询已经生成完成的文件
                List<FileInfo> infos = zBankClient.queryFileList(okFile.getFileName().replace(
                        okFileExtension, txtFileExtension), beginDate, endDate, 1);
                if (infos.size() > 0) {
                    List<FileInfo> sortedInfos = sortedFileCreateTime(infos);
                    String[] tableHeads = tableHead.split(regex);
                    int heads = tableHeads.length;
                    for (FileInfo fileInfo : sortedInfos) {
                        LocalFile localFile = selectLocalFile(apiCode, filePath, fileInfo);
                        boolean localFileExist = localFile == null;
                        LocalFile localFileNew = localFileExist ? saveLocalFile(cid, apiCode, filePath, fileInfo) : localFile;
                        // 下载生成的文件
                        StreamDownLoadInfo streamDownLoadInfo = zBankClient.downloadWholeFile(fileInfo);
                        fileInfo.setFileMd5(streamDownLoadInfo.getFileMd5());
                        InputStream inputStream = null;
                        InputStreamReader isr = null;
                        BufferedReader bufferedReader = null;
                        FileInputStream fis = null;
                        BufferedInputStream bis = null;
                        LocalFile localFileUpdate = new LocalFile();
                        localFileUpdate.setErrorActualNumber(0);
                        localFileUpdate.setPushNumber(0);
                        localFileUpdate.setId(localFileNew.getId());
                        try {
                            inputStream = streamDownLoadInfo.getInputStream();
                            if (!localFileExist) {
                                String path = filePath.concat(fileInfo.getFileMd5()).concat(File.separator).concat(fileInfo.getFileName());
                                File file = new File(path);
                                boolean bak = file.renameTo(new File(path.concat(".bak") + System.currentTimeMillis()));
                                if (!bak) {
                                    log.warn("众邦财富异常文件备份失败！path:{}", path);
                                }
                            }
                            File file = downLoadFile(inputStream, filePath, fileInfo);
                            String localMd5;
                            int errorSum = 0;
                            if (file == null) {
                                ByteBuffer buffer = null;
                                InputStream is1 = null;
                                InputStream is2 = null;
                                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                     WritableByteChannel writableByteChannel = Channels.newChannel(bos);
                                     ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)
                                ) {
                                    // 缓存1M
                                    buffer = ByteBuffer.allocate(1024 << 10);
                                    while (readableByteChannel.read(buffer) != -1 || buffer.position() > 0) {
                                        buffer.flip();
                                        writableByteChannel.write(buffer);
                                        buffer.compact();
                                    }
                                    is1 = new ByteArrayInputStream(bos.toByteArray());
                                    is2 = new ByteArrayInputStream(bos.toByteArray());
                                    isr = new InputStreamReader(new BufferedInputStream(is2), StandardCharsets.UTF_8);
                                    localMd5 = DigestUtils.md5Hex(is1);
                                } catch (IOException e) {
                                    log.error(e.getMessage(), e);
                                    localMd5 = "";
                                } finally {
                                    if (buffer != null) {
                                        buffer.clear();
                                    }
                                    closeable(is2, is1);
                                }
                            } else {
                                fis = new FileInputStream(file);
                                bis = new BufferedInputStream(fis);
                                isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
                                localMd5 = Md5EncodeUtil.encode(file);
                            }
                            if (checkFileMd5(localMd5, fileInfo) && isr != null) {
                                bufferedReader = new BufferedReader(isr);
                                LineNumberReader lineNumberReader = new LineNumberReader(bufferedReader);
                                String lineTxt;
                                List<PullCustomerFileData> fileDataList = new ArrayList<>();
                                List<Callable<Integer>> callables = new ArrayList<>();
                                while ((lineTxt = lineNumberReader.readLine()) != null) {
                                    fileDataList.add(newFileData(lineTxt, apiCode, tableHeads, heads, regex, localFileUpdate));
                                    if (saveFileData(fileDataList, 2000, localFile, callables)) {
                                        fileDataList = new ArrayList<>();
                                    }
                                }
                                saveFileData(fileDataList, 1, localFile, callables);
                                List<Future<Integer>> futures = executor.invokeAll(callables);
                                localFileUpdate.setPushNumber(lineNumberReader.getLineNumber());
                                localFileUpdate.setSrcPath(fileInfo.getFileMd5());
                                for (Future<Integer> future : futures) {
                                    try {
                                        errorSum += future.get(10, TimeUnit.SECONDS);
                                    } catch (ExecutionException | TimeoutException e) {
                                        localFileUpdate.setSrcPath(null);
                                        localFileUpdate.setComplete("3");
                                        localFileUpdate.setErrorActualNumber(localFileUpdate.getPushNumber() - errorSum);
                                        log.error(e.getMessage(), e);
                                    }
                                }
                            }
                            return errorSum == 0;
                        } catch (IOException | SDKException | InterruptedException e) {
                            log.error(e.getMessage(), e);
                            Thread.currentThread().interrupt();
                            return false;
                        } finally {
                            localFileUpdate.setStatus("2");
                            localFileUpdate.setPushEndTime(new Date());
                            if (localFileUpdate.getComplete() == null) {
                                localFileUpdate.setComplete("1");
                            }
                            localFileMapper.updateByPrimaryKeySelective(localFileUpdate);
                            try {
                                closeable(bufferedReader, isr, bis, fis, inputStream);
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                    try {
                        StreamDownLoadInfo streamDownLoadInfo = zBankClient.downloadWholeFile(okFile);
                        okFile.setFileMd5(streamDownLoadInfo.getFileMd5());
                        downLoadFile(streamDownLoadInfo.getInputStream(), filePath, okFile);
                    } catch (SDKException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                break;
            }
        }
        return false;
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
            , String apiCode, String[] tableHead, int heads, String regex, LocalFile localFile) {
        PullCustomerFileData fileData = new PullCustomerFileData();
        String[] rows;
        if (StringUtils.isBlank(lineTxt) || (rows = lineTxt.split(regex)).length != heads) {
            fileData.setDataStatus(2);
            localFile.setComplete("3");
            localFile.setErrorActualNumber(localFile.getErrorActualNumber() + 1);
        } else {
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < heads; i++) {
                jsonObject.put(tableHead[i], rows[i]);
            }
            fileData.setJsonData(jsonObject.toJSONString());
        }
        fileData.setFileData(lineTxt);
        fileData.setApiCode(apiCode);
        fileData.setDataFingerprint(MD5Utils.cell32(lineTxt));
        fileData.setDataStatus(1);
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
            , List<Callable<Integer>> callables) {
        if (fileDataList.size() >= saveSize) {
            callables.add(() -> {
                if (localFile != null) {
                    Set<String> dataFingerprintSet = pullCustomerFileDataMapper.getDataFingerprintSet(localFile.getId()
                            , fileDataList);
                    if (dataFingerprintSet.size() == fileDataList.size()) {
                        return 0;
                    }
                    fileDataList.removeIf(f -> dataFingerprintSet.contains(f.getDataFingerprint()));
                }
                int i = pullCustomerFileDataMapper.insertBatchSelective(fileDataList);
                if (i > 0) {
                    fileDataList.clear();
                    return 0;
                }
                return fileDataList.size();
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
                .andLocalPathLike(localPath);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        int size = localFiles.size();
        return size > 0 ? localFiles.get(0) : null;
    }


    /**
     * 2023-11-16 17:15
     * 下载
     */
    private File downLoadFile(InputStream inputStream, String filePath, FileInfo fileInfo) {
        File f = new File(filePath.concat(fileInfo.getFileMd5()).concat(File.separator).concat(fileInfo.getFileName()));
        File parentFile = f.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                log.error("众邦银行拉取文件目录创建失败，path:{},name:{}", parentFile.getAbsolutePath()
                        , fileInfo.getFileName());
                return null;
            }
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw");
             FileChannel channel = randomAccessFile.getChannel();
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)) {
            long position = 0;
            long fileSize = fileInfo.getFileSize().longValue();
            while (position < fileSize) {
                long count;
                position += channel.transferFrom(readableByteChannel, position
                        , (count = (position + (1024 << 10))) > fileSize ? fileSize : count);
                channel.force(false);
                log.warn("###众邦银行文件{}下载进度{}/{}：{}%", fileInfo.getFileName(), position, fileSize
                        , (position * 100 / fileSize));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return f;
    }

    private void closeable(Closeable... closeables) throws IOException {
        for (Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            closeable.close();
        }
    }


    /**
     * 2023-11-17 13:39
     * 文件信息排序，按创建时间降序，创建时间相同时按fileId降序
     */
    private static List<FileInfo> sortedFileCreateTime(List<FileInfo> files) {
        return files.stream().sorted(Comparator.comparing(FileInfo::getCreateTime)
                .thenComparing(FileInfo::getFileId).reversed()).collect(Collectors.toList());
    }


    /**
     * 比较文件md5
     *
     * @param localMd5 本地下载后的文件生成的md5值
     * @param fileInfo 服务端响应信息中的md5值
     * @return 一致返回true;
     */
    private boolean checkFileMd5(String localMd5, FileInfo fileInfo) throws SDKException {
        if (localMd5.equals(fileInfo.getFileMd5())) {
            return true;
        }
        log.error("众邦银行文件{}下载完成后md5不一致，resultMd5={}, localMd5={}", fileInfo.getFileName()
                , fileInfo.getFileMd5(), localMd5);
        return false;
    }


}
