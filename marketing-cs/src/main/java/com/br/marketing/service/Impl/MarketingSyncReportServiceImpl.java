package com.br.marketing.service.Impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
import com.br.common.mask.DataMask;
import com.br.common.mask.SensitiveType;
import com.br.common.util.DateUtils;
import com.br.common.validator.CellUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.eventtrack.EventTrackingCellReport;
import com.br.marketing.enums.ThirdPartnerDataPassBackTaskPushStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.MarketingSyncReportService;
import com.br.marketing.service.ValidityPeriodResendRecordService;
import com.br.marketing.service.eventtrack.EventTrackService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.MarketingSyncReportNumVO;
import com.br.marketing.vo.MarketingSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户上传数据统计报表
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/18 14:47
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/18 14:47
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Service
@Slf4j
public class MarketingSyncReportServiceImpl implements MarketingSyncReportService {

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private VariableDicMapper variableDicMapper;
    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Autowired
    MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Autowired
    ICompatibleService iCompatibleService;

    @Resource
    MarketingValidityChangeMapper changeMapper;

    @Resource
    ValidityPeriodResendRecordService recordService;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private PlatformTransactionManager platformTransactionManager;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private EventTrackService eventTrackService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private UploadDataFieldDictMapper uploadDataFieldDictMapper;

    @Resource
    private ThirdPartnerDataPassBackTaskMapper thirdPartnerDataPassBackTaskMapper;


    @Override
    public void syncReportProcess(String uploadDate, String jobName) {
        this.doSyncReportProcess(uploadDate, null, jobName);
    }

    /**
     * 上传数据统计报表流程
     *
     * @param uploadDate
     * @return
     */
    @Override
    public void syncReportProcess(String uploadDate) {
        this.doSyncReportProcess(uploadDate, null, null);
    }

    @Override
    public void syncReportProcessByApiCode(String uploadDate, String apiCode) {
        this.doSyncReportProcess(uploadDate, apiCode, null);
    }

    public void doSyncReportProcess(String uploadDate, String apiCodes, String jobName) {
        long l = System.currentTimeMillis();
        Integer uploadThreadNum = marketingCommonConfig.getSyncReportThreadConfig().getInteger("upload");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(uploadThreadNum, uploadThreadNum);
        List<Customer> customers = new ArrayList<>();
        if (apiCodes != null) {
            //1.获取所有客户
            customers.add(customerMapper.getCustomerByApiCode(apiCodes));
        } else {
            customers = customerMapper.getAllCustomerByResentlySyncInfotikv_();
        }
        Map<String, Set<String>> userTypeMap = getUserTypeMap();
        CountDownLatch countDownLatch = new CountDownLatch(customers.size());
        for (Customer customer : customers) {
            if (StringUtils.isNoneBlank(jobName)) {
                Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(), jobName);
                if (!action) {
                    countDownLatch.countDown();
                    continue;
                }
            }
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getSyncReportThreadConfig().getInteger("upload"));
            threadPool.submit(() -> {
                try {
                    if (AuthShowProductor.NORMAL.getCode().equals(customer.getStatus())) {
                        String apiCode = customer.getApiCode();
                        log.warn("开始执行上传数据统计报表任务,apiCode={},uploadDate={}", apiCode, uploadDate);
                        //3.组装数据
                        Set<String> userTypeList = userTypeMap.getOrDefault(apiCode, Collections.emptySet());
                        //获取场景
                        if (!userTypeList.isEmpty()) {
                            long start = System.currentTimeMillis();
                            for (String userType : userTypeList) {
                                String createStartDate = uploadDate;
                                String createEndDate = LocalDate.parse(uploadDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        .plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                String appletDateStart = LocalDate.parse(uploadDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        .minusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                List<String> appletDateList = syncReportMapper.getAppletDate(apiCode, userType, createStartDate, createEndDate
                                        , appletDateStart);
                                for (String appletDate : appletDateList) {
                                    //统计上传记录关键信息
                                    Map<String, Object> uploadMagnStatInfo = syncReportMapper.selectUploadMagnStatInfotiflash_(apiCode, userType, appletDate);
                                    Integer normalNum = Integer.parseInt(uploadMagnStatInfo.get("normalNum").toString());
                                    if (normalNum == 0) {
                                        continue;
                                    }
                                    Integer duplicateRemovalNum = Integer.parseInt(uploadMagnStatInfo.get("duplicateRemovalNum").toString());
                                    Date appletBeginTime = DateHelper.parseDate(uploadMagnStatInfo.get("appletBeginTime").toString());
                                    Date appletEndTime = DateHelper.parseDate(uploadMagnStatInfo.get("appletEndTime").toString());
                                    MarketingSyncReport modifyReport = new MarketingSyncReport();
                                    //统计reserve_field1的key集合
                                   /* HashSet<String> keySet = new HashSet<>();
                                    try {
                                        List<String> keysList = syncReportMapper.selectUploadExtendKeystikv_(apiCode, userType, appletDate);
                                        keysList.forEach((String key) -> {
                                            if (StringUtils.isEmpty(key)) {
                                                return;
                                            }
                                            List<String> fieldList = Arrays.asList(key.trim().substring(1, key.length() - 2).replace
                                                    ("\"", "").replaceAll("\\s+", "").split(","));
                                            keySet.addAll(fieldList);
                                        });
                                        modifyReport.setReserveField1Key(String.join(",", keySet));
                                    } catch (Exception e) {
                                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                                                "上传数据统计扩展字段key异常"), e);
                                    }*/
                                    //数据正常入库条数
                                    modifyReport.setNormalNum(normalNum);
                                    //去重后数据量
                                    modifyReport.setDuplicateRemovalNum(duplicateRemovalNum);
                                    //上传结束时间
                                    modifyReport.setAppletEndTime(appletEndTime);
                                    MarketingSyncReport report = selectMarketingSyncReport(apiCode, userType, appletDate);
                                    //判断是否更新
                                    if (report == null) {
                                        //新增
                                        modifyReport.setAppletBeginTime(appletBeginTime);
                                        //场景
                                        modifyReport.setUserType(userType);
                                        //上传日期
                                        modifyReport.setAppletDate(appletDate);
                                        //客户编号
                                        modifyReport.setCid(customer.getCid());
                                        //apiCode
                                        modifyReport.setApiCode(apiCode);
                                        //客户名称
                                        modifyReport.setShortName(customer.getShortName());
                                        modifyReport.setCreateTime(new Date());
                                        log.warn("新增上传数据统计：{}", JSON.toJSONString(modifyReport));
                                        syncReportMapper.insert(modifyReport);
                                    } else {
                                        modifyReport.setId(report.getId());
                                        log.warn("编辑上传数据统计：{}", JSON.toJSONString(modifyReport));
                                        syncReportMapper.modifyReportById(modifyReport);
                                    }
                                }
                            }
                            log.warn("上传记录更新耗时：{}s", (System.currentTimeMillis() - start) / 1000);
                        }
                    }
                } catch (Exception e) {
                    log.error("程序执行上传数据统计报表任务异常，apiCode={}", customer.getApiCode(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        // 等待线程执行完毕
        try {
            countDownLatch.await();
            //关闭线程池
            threadPool.shutdown();
            log.warn("上传记录-同步记录操作执行完成，耗时{}s", (System.currentTimeMillis() - l) / 1000);
        } catch (InterruptedException e) {
            log.error("countDownLatch 线程执行异常", e);
        }
        //插入扩展字段key统计表
       /* try {
            handlerDataFieldDict(uploadDate);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "扩展字段添加失败"), e);
        }*/

    }

    private void handlerDataFieldDict(String uploadDate) {
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andAppletDateEqualTo(uploadDate).andReserveField1KeyIsNotNull();
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        Map<String, List<MarketingSyncReport>> reportMap =
                reportList.stream().collect(Collectors.groupingBy(MarketingSyncReport::getApiCode));
        reportMap.forEach((String apiCode, List<MarketingSyncReport> reports) -> {
            String cId = tableCreateService.getCId(apiCode);
            Set<String> keySet = new HashSet<>();
            reports.forEach((MarketingSyncReport report) -> {
                List<String> fieldList = Arrays.asList(report.getReserveField1Key().split(","));
                keySet.addAll(fieldList);
            });
            UploadDataFieldDictExample dictExample = new UploadDataFieldDictExample();
            dictExample.createCriteria().andApiCodeEqualTo(apiCode);
            List<UploadDataFieldDict> dataFieldDictList = uploadDataFieldDictMapper.selectByExample(dictExample);
            if (CollectionUtils.isEmpty(dataFieldDictList)) {
                UploadDataFieldDict dataFieldDict = new UploadDataFieldDict();
                dataFieldDict.setApiCode(apiCode);
                dataFieldDict.setCid(cId);
                dataFieldDict.setReserveField1Key(String.join(",", keySet));
                dataFieldDict.setCreateTime(new Date());
                dataFieldDict.setUpdateTime(new Date());
                uploadDataFieldDictMapper.insertSelective(dataFieldDict);
            } else {
                UploadDataFieldDict updateField = dataFieldDictList.get(0);
                List<String> fieldList = Arrays.asList(updateField.getReserveField1Key().split(","));
                keySet.addAll(fieldList);
                updateField.setReserveField1Key(String.join(",", keySet));
                uploadDataFieldDictMapper.updateByPrimaryKeySelective(updateField);

            }
        });

    }

    /**
     * 根据参数获取上传统计数据
     *
     * @param apiCode
     * @param userType
     * @param uploadDate
     * @return
     */
    private MarketingSyncReport selectMarketingSyncReport(String apiCode, String userType, String uploadDate) {
        MarketingSyncReportExample report = new MarketingSyncReportExample();
        report.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andUserTypeEqualTo(userType)
                .andAppletDateEqualTo(uploadDate);
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(report);
        if (reportList != null && !reportList.isEmpty()) {
            return reportList.get(0);
        }
        return null;
    }

    /**
     * 获取数据上传时间
     *
     * @param apiCode
     * @param userType
     * @return
     */
    private String getAppletTime(String apiCode, String userType, String uploadDate, Boolean flag) {
        if (flag) {
            return syncReportMapper.uploadSyncMinAppletTimetiflash_(apiCode, userType, uploadDate);
        } else {
            return syncReportMapper.uploadSyncMaxAppletTimetiflash_(apiCode, userType, uploadDate);
        }
    }

    /**
     * 获取数据条数
     *
     * @param apiCode
     * @param userType
     * @return
     */
    private Integer getUploadNum(String apiCode, String userType, String uploadDate, Boolean flag) {
        if (flag) {
            return syncReportMapper.uploadSyncCounttiflash_(apiCode, userType, uploadDate, AuthShowProductor.NO_NORMAL.getCode());
        } else {
            return syncReportMapper.uploadSyncCounttiflash_(apiCode, userType, uploadDate, AuthShowProductor.NORMAL.getCode());
        }
    }

    /**
     * 获取所有场景
     *
     * @return
     */
    private Map<String, Set<String>> getUserTypeMap() {
        VariableDicExample dic = new VariableDicExample();
        dic.createCriteria().andIsDelEqualTo(1);
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        return dicList.parallelStream().collect(Collectors.groupingBy(VariableDic::getApiCode
                , Collectors.mapping(VariableDic::getFieldValue, Collectors.toSet())));
    }


    /**
     * 获取场景
     *
     * @param apiCode
     * @return
     */
    private List<String> getUserTypeList(String apiCode) {
        VariableDicExample dic = new VariableDicExample();
        dic.createCriteria().andApiCodeEqualTo(apiCode);
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        List<String> userTypeList = new ArrayList<>();
        if (dicList != null && !dicList.isEmpty()) {
            userTypeList = dicList.stream().map(v -> v.getFieldValue()).collect(Collectors.toList());
        }
        return userTypeList;
    }

    @Override
    public PageResultReturn getReportList(int current, int size, String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes,
                                          String userTypes) {

        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")) {
            cidOrName = cidOrName.replace("_", "\\_");
        }

        List<String> apiCodeList = new ArrayList<>();
        List<String> userTypeList = new ArrayList<>();
        if (apiCodes != null && !"".equals(apiCodes)) {
            String[] split = apiCodes.split(",");
            for (String item : split) {
                apiCodeList.add(item);
            }
        }
        if (userTypes != null && !"".equals(userTypes)) {
            String[] split = userTypes.split(",");
            for (String item : split) {
                userTypeList.add(item);
            }
        }
        Map params = new HashMap();
        params.put("cidOrName", cidOrName);
        params.put("appletTimeStart", appletTimeStart);
        params.put("appletTimeEnd", appletTimeEnd);
        params.put("apiCodeList", apiCodeList);
        params.put("userTypeList", userTypeList);

        PageHelper.startPage(current, size);
        List<MarketingSyncReportVO> list = syncReportMapper.selectList(params);
        for (MarketingSyncReportVO marketingSyncReportVO : list) {
            String apiCode = marketingSyncReportVO.getApiCode();
            String userType = marketingSyncReportVO.getUserType();
            String appletDate = marketingSyncReportVO.getAppletDate();
            MarketingDataValidConfig validDate = changeMapper.getValidDate(apiCode, userType, appletDate);
            if (ObjectUtil.isNotEmpty(validDate)) {
                marketingSyncReportVO.setValidStartDate(validDate.getValidStartDate());
                marketingSyncReportVO.setValidEndDate(validDate.getValidEndDate());
            } else {
                log.warn("该apiCode={} , userType={} , appletDate={}维度不存在有效期起止时间", apiCode, userType, appletDate);
            }

        }

        return PageResultReturn.setPageResult(list, current, size);
    }

    @Override
    public Map getReportListTotal(String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes) {
        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")) {
            cidOrName = cidOrName.replace("_", "\\_");
        }

        List<String> apiCodeList = new ArrayList<>();
        List<String> userTypeList = new ArrayList<>();
        if (apiCodes != null && !"".equals(apiCodes)) {
            String[] split = apiCodes.split(",");
            for (String item : split) {
                apiCodeList.add(item);
            }
        }
        if (userTypes != null && !"".equals(userTypes)) {
            String[] split = userTypes.split(",");
            for (String item : split) {
                userTypeList.add(item);
            }
        }
        Map params = new HashMap();
        params.put("cidOrName", cidOrName);
        params.put("appletTimeStart", appletTimeStart);
        params.put("appletTimeEnd", appletTimeEnd);
        params.put("apiCodeList", apiCodeList);
        params.put("userTypeList", userTypeList);

        Map map = new HashMap();
        Long normalNumTotal = 0L;
        Long duplicateRemovalNumTotal = 0L;
        List<MarketingSyncReportNumVO> listTotal = syncReportMapper.getReportListTotaltiflash_(params);
        if (!CollectionUtils.isEmpty(listTotal)) {
            //数据正常入库条数
            normalNumTotal = listTotal.stream().collect(Collectors.summingLong(MarketingSyncReportNumVO::getNormalNumTotal));
            //去重后数据量
            duplicateRemovalNumTotal = listTotal.stream().collect(Collectors.summingLong(MarketingSyncReportNumVO::getDuplicateRemovalNumTotal));
        }
        map.put("normalNumTotal", normalNumTotal);
        map.put("duplicateRemovalNumTotal", duplicateRemovalNumTotal);
        return map;
    }

    @Override
    public JSONObject getReportByCell(String cidOrName, String appletTimeStart, String appletTimeEnd
            , String apiCodes, String userTypes, String cell, String orderField, String descField) {
        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        String decodeCell = "";
        decodeCell = BrCipherMaker.getInstance().decode(cell);
        // 1. 明文 cell 需要log加密
        if (CellUtils.isValidateCell(decodeCell)) {
            // do nothing
        } else if (DecodeGrpcClient.isMd5(decodeCell)) {
            decodeCell = RpcClientProxy.decode(decodeCell, "cell", "md5", "");
        } else {
            decodeCell = RpcClientProxy.decode(decodeCell, "cell", "sha", "");
        }
        if (StringUtils.isBlank(decodeCell)) {
            decodeCell = cell;
        }
        decodeCell = DataMask.mask(decodeCell, SensitiveType.LogMask, "");
        List<String> apiCodeList = transformStringToListByComma(apiCodes);
        List<String> userTypeList = transformStringToListByComma(userTypes);
        // 2. 通过 apiCodes 获取客户信息,并将结果填充到响应中
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andStatusEqualTo((byte) 1).andApiCodeIn(apiCodeList);
        List<MarketingCustomer> list = marketingCustomerMapper.selectByExample(example);
        Map<String, MarketingCustomer> customerMap = list.stream()
                .collect(Collectors.toMap(MarketingCustomer::getApiCode, Function.identity()));
        // 发送日志记录
        packageAndSendEventTrack(cidOrName, appletTimeStart, appletTimeEnd, apiCodes
                , userTypes, cell, customerMap, apiCodeList);
        // 3. 根据 apiCodes,cell 查询结果
        JSONObject result = new JSONObject();
        List<MarketingSyncUserCell> syncUserListAllApiCode = new ArrayList<>();
        for (int i = 0; i < apiCodeList.size(); i++) {
            String apiCode = apiCodeList.get(i);
            try {
                List<MarketingSyncUserCell> syncUsersList = marketingSyncUserMapper.selectSyncUserByCelltikv_(appletTimeStart
                        , appletTimeEnd, apiCode, userTypeList, decodeCell, orderField, descField);
                syncUserListAllApiCode.addAll(syncUsersList);
            } catch (Exception e) {
                if (e.getMessage().contains("doesn't exist")) {
                    log.warn("手机号查询表不存在cidOrName:{}-appletTimeStart:{}-appletTimeEnd:{}-apiCode:{}" +
                                    "-userTypes:{}-cell:{}-orderField:{}-descField:{}"
                            , cidOrName, appletTimeStart, appletTimeEnd, apiCode, userTypes, cell, orderField, descField);
                } else {
                    log.error("cidOrName:{}-appletTimeStart:{}-appletTimeEnd:{}-apiCode:{}" +
                                    "-userTypes:{}-cell:{}-orderField:{}-descField:{}-手机号查询异常--"
                            , cidOrName, appletTimeStart, appletTimeEnd, apiCode, userTypes, cell, orderField, descField, e);
                }
            }
        }
        syncUserListAllApiCode.stream().forEach((MarketingSyncUserCell c) -> {
            String apiCode = c.getApiCode();
            MarketingCustomer marketingCustomer = customerMap.get(apiCode);
            c.setCid(marketingCustomer.getCid());
            c.setShortName(marketingCustomer.getShortName());
        });
        List<MarketingSyncUserCell> collect = syncUserListAllApiCode.stream()
                .sorted(Comparator.comparing(MarketingSyncUserCell::getAppletDate).reversed())
                .collect(Collectors.toList());
        result.put("records", JSON.toJSON(collect));
        JSONObject countObject = new JSONObject();
        // 单独计算全部数据的统计总数
        Long normalNumTotal = syncUserListAllApiCode.stream().mapToLong(MarketingSyncUserCell::getNormalNum).sum();
        Long duplicateRemovalNumTotal = syncUserListAllApiCode.stream().mapToLong(MarketingSyncUserCell::getDuplicateRemovalNum).sum();
        countObject.put("normalNumTotal", normalNumTotal);
        countObject.put("duplicateRemovalNumTotal", duplicateRemovalNumTotal);
        result.put("totals", countObject);
        return result;
    }

    /**
     * 对含有逗号的String类型进行分割转换成List<String>
     *
     * @param params 含有逗号的String参数
     * @return List<String>
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/18 10:37
     */
    public List<String> transformStringToListByComma(String params) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotBlank(params)) {
            String[] split = params.split(",");
            for (String item : split) {
                list.add(item);
            }
        }
        return list;
    }

    public void packageAndSendEventTrack(String cidOrName, String appletTimeStart, String appletTimeEnd
            , String apiCodes, String userTypes, String cell, Map<String, MarketingCustomer> customerMap, List<String> apiCodeList) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            EventTrackingCellReport cellReport = new EventTrackingCellReport();
            cellReport.setCell(cell);
            cellReport.setCreateTime(new Date());
            cellReport.setUpdateTime(new Date());
            cellReport.setIsDelete(0);
            cellReport.setUserId(userDetail.getId().toString());
            cellReport.setUserName(userDetail.getUserName());
            cellReport.setRealName(userDetail.getRealName());
            JSONObject param = new JSONObject();
            param.put("cidOrName", cidOrName);
            MarketingCustomer marketingCustomer = customerMap.get(apiCodeList.get(0));
            if (null != marketingCustomer) {
                param.put("cid", marketingCustomer.getCid());
                param.put("shortName", marketingCustomer.getShortName());
            }
            param.put("appletTimeStart", appletTimeStart);
            param.put("appletTimeEnd", appletTimeEnd);
            param.put("apiCodes", apiCodes);
            param.put("userTypes", userTypes);
            param.put("cell", cell);
            cellReport.setRequestParam(param.toJSONString());
            eventTrackService.insertSync(cellReport);
        } catch (Exception e) {
            log.error("cidOrName[{}]appletTimeStart[{}]appletTimeEnd[{}]apiCodes[{}]userTypes[{}]cell[{}]--"
                    , cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes, cell, e);
        }
    }

    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public void deleteReportByAppletDate(String mes) {
        JSONObject jsonObject = JSON.parseObject(mes);
        JSONArray dataArray = jsonObject.getJSONArray("dataArray");
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataJson = dataArray.getJSONObject(i);
                String apiCode = dataJson.getString("apiCode");
                String appletDate = dataJson.getString("appletDate");
                syncReportMapper.deleteByAppletDate(apiCode, appletDate);
            }
        }

    }

    @Override
    public boolean updateById(List<Long> ids, String validStartDate, String validEndDate) {
        try {
            if (ids.isEmpty()) {
                return true;
            }
            List<String> list = new ArrayList<>();
            List<Long> validIds = new ArrayList<>();
            String str = "";
            Map<String, String> apiCodeMappingConfig = marketingCommonConfig.getThirdPartnerApiCodeMappingConfig();
            List<String> apiCodes = apiCodeMappingConfig.values().stream().collect(Collectors.toList());
            for (Long id : ids) {
                MarketingSyncReportVO reportVO = syncReportMapper.selectById(id);
                String apiCode = reportVO.getApiCode();
                String userType = reportVO.getUserType();
                String appletDate = reportVO.getAppletDate();
                MarketingDataValidConfig data = syncReportMapper.selectValidData(apiCode, userType, appletDate);
                if (ObjectUtil.isEmpty(data)) {
                    log.warn("apiCode={},userType={},appletDate={}没有相应的有效期数据", apiCode, userType, appletDate);
                    continue;
                }
                validIds.add(data.getId());
                MarketingDataValidConfig newData = new MarketingDataValidConfig();
                newData.setId(data.getId());
                newData.setValidStartDate(validStartDate);
                newData.setValidEndDate(validEndDate);
                entityOptService.writeOptLog(data.getId(), newData, data);

                str = apiCode + userType;
                if (!list.contains(str)) {
                    list.add(str);
                    log.warn("开始重推, apiCode={}, userType={}, id={}", apiCode, userType, data.getId());
                    recordService.saveRecord(apiCode, userType, data.getId());
                }
                if (apiCodes.contains(apiCode)) {
                    ThirdPartnerDataPassBackTask task = new ThirdPartnerDataPassBackTask();
                    task.setApiCode(apiCode);
                    task.setAppletDate(appletDate);
                    task.setUserType(userType);
                    task.setValidStartDate(validStartDate);
                    task.setValidEndDate(validEndDate);
                    task.setExtend("");
                    thirdPartnerDataPassBackTaskMapper.insertSelective(task);
                }
            }
            log.warn("更新有效期的ids, validIds={}", validIds);
            if (!validIds.isEmpty()) {
                validStartDate = DateUtils.format(addDay(validStartDate, 0, "yyyy-MM-dd"), "yyyy-MM-dd");
                validEndDate = DateUtils.format(addDay(validEndDate, 0, "yyyy-MM-dd"), "yyyy-MM-dd");
                marketingDataValidConfigMapper.updateBatchById(validIds, validStartDate, validEndDate);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("更新有效期报错, msg={}", e.getMessage());
            return false;
        }
    }

    @Override
    public Result<Boolean> nearRealtimeDataCountFragmentsStatis(String dataCountFragmentsMgs) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        boolean statisSwitch = !marketingCommonConfig.getUploadAndTransferDataRealtimeStatisSwitch();
        if (StringUtils.isBlank(dataCountFragmentsMgs) || statisSwitch) {
            return result;
        }
        ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO = JSONObject.parseObject(dataCountFragmentsMgs
                , new TypeReference<ApiDataInfoDTO<UserTypeCollectionDTO>>() {
                }.getType());
        String apiCode = apiDataInfoDTO.getApiCode();
        if (StringUtils.isBlank(apiCode)) {
            log.error("上传未获取到apiCode，消息内容：{}", dataCountFragmentsMgs);
            return result;
        }
        MarketingCustomer customer = marketingCustomerService.getCacheCustomerByApiCode(apiCode);
        String cId = StringUtils.isNotBlank(apiDataInfoDTO.getCid()) ? apiDataInfoDTO.getCid() : customer.getCid();
        if (StringUtils.isBlank(cId)) {
            log.error("上传未获取到cid，消息内容：{}", dataCountFragmentsMgs);
            return result;
        }
        LocalDateTime rawDataSaveTime = LocalDateTime.parse(apiDataInfoDTO.getRawDataSaveTimeStr()
                , DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDate rawDataSaveDate = rawDataSaveTime.toLocalDate();
        String rawDataSaveDateStr = rawDataSaveDate.toString();
        String yyyymmdd = rawDataSaveDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String requestId = apiDataInfoDTO.getRequestId();
        StringBuilder redisKey = new StringBuilder(RedisKeyConstant.ASYNC_COUNT);
        redisKey.append(cId).append(":").append(apiCode).append(":").append(yyyymmdd).append(":")
                .append(apiDataInfoDTO.getMsgSource()).append(":");
        if (apiDataInfoDTO.uploadMsgSource()) {
            Set<String> userTypeSet = CollectionUtils.isEmpty(apiDataInfoDTO.getArgList()) ? null
                    : apiDataInfoDTO.getArgList().stream().map(UserTypeCollectionDTO::getUserType).collect(Collectors.toSet());
            List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.selectSyncUserByRequestBatchList(apiCode
                    , requestId, userTypeSet, rawDataSaveDateStr);
            if (CollectionUtils.isEmpty(syncUserList)) {
                return result;
            }
            Map<String, MarketingSyncReport> userTypeMap = new HashMap<>();
            for (MarketingSyncUser syncUser : syncUserList) {
                String userType = syncUser.getUserType();
                int n = syncUser.getStatus() == 1 && (syncUser.getIsRepeat() == 1 || syncUser.getIsRepeat() == 2) ? 1 : 0;
                MarketingSyncReport syncReport = userTypeMap.getOrDefault(userType, new MarketingSyncReport());
                syncReport.setId(syncUser.getId());
                if (syncReport.getUserType() == null) {
                    userTypeMap.put(syncUser.getUserType(), syncReport);
                    syncReport.setApiCode(apiCode);
                    syncReport.setCid(cId);
                    syncReport.setUserType(userType);
                    syncReport.setAppletDate(rawDataSaveDateStr);
                    syncReport.setAppletBeginTime(syncUser.getCreateTime());
                    syncReport.setAppletEndTime(syncReport.getAppletBeginTime());
                    syncReport.setNormalNum(1);
                    syncReport.setDuplicateRemovalNum(n);
                    syncReport.setShortName(customer.getShortName());
                } else {
                    syncReport.setNormalNum(syncReport.getNormalNum() + 1);
                    syncReport.setDuplicateRemovalNum(syncReport.getDuplicateRemovalNum() + n);
                    syncReport.setAppletEndTime(syncUser.getCreateTime());
                }
            }
            TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
            List<String> hashKeys = new ArrayList<>();
            try {
                for (Map.Entry<String, MarketingSyncReport> entry : userTypeMap.entrySet()) {
                    String userType = entry.getKey();
                    MarketingSyncReport syncReport = entry.getValue();
                    String hKey = redisKey + userType;
                    hashKeys.add(hKey);
                    String lockKey = hKey + ":lock";
                    String lockValue = apiDataInfoDTO.getRawDataSaveTimeStr() + syncReport.getId();
                    syncReport.setId(null);
                    try {
                        redisChgService.lock(lockKey, lockValue);
                        // 上锁
                        Map<String, Object> cacheMap = redisChgService.hgetall(hKey);
                        Map<String, String> jsonObject = null;
                        boolean cacheBool = CollectionUtils.isEmpty(cacheMap);
                        if (cacheBool) {
                            // 缓存不存在
                            MarketingSyncReportExample example = new MarketingSyncReportExample();
                            example.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cId)
                                    .andUserTypeEqualTo(userType).andAppletDateEqualTo(rawDataSaveDateStr);
                            List<MarketingSyncReport> syncReports = syncReportMapper.selectNumberByExample(example);
                            if (CollectionUtils.isEmpty(syncReports)) {
                                // 未持久化
                                syncReport.setCreateTime(new Date());
                                syncReport.setUpdateTime(syncReport.getCreateTime());
                                int i = syncReportMapper.insertSelective(syncReport);
                                if (i > 0 && syncReport.getId() != null) {
                                    MarketingSyncReport report = new MarketingSyncReport();
                                    report.setId(syncReport.getId());
                                    report.setNormalNum(syncReport.getNormalNum());
                                    report.setDuplicateRemovalNum(syncReport.getDuplicateRemovalNum());
                                    report.setAppletBeginTime(syncReport.getAppletBeginTime());
                                    report.setAppletEndTime(syncReport.getAppletEndTime());
                                    redisChgService.hmset(hKey, JSONObject.parseObject(JSON.toJSONString(report)
                                            , new TypeReference<Map<String, String>>() {
                                            }));
                                    redisChgService.unlock(lockKey, lockValue);
                                    redisChgService.expire(hKey, RandomUtils.nextInt(300, 1800));
                                    continue;
                                }
                            } else {
                                // 已持久化
                                MarketingSyncReport syncReportOld = syncReports.get(0);
                                jsonObject = syncReportSummary(syncReport, syncReportOld, false);
                            }
                        } else {
                            // 缓存
                            MarketingSyncReport cacheSyncReport = JSONObject.parseObject(JSON.toJSONString(cacheMap)
                                    , new TypeReference<MarketingSyncReport>() {
                                    });
                            jsonObject = syncReportSummary(syncReport, cacheSyncReport, true);
                        }
                        int i = syncReportMapper.updateByPrimaryKeySelective(syncReport);
                        if (i > 0 && jsonObject != null) {
                            redisChgService.hmset(hKey, jsonObject);
                            if (cacheBool) {
                                redisChgService.expire(hKey, RandomUtils.nextInt(1800, 3600));
                            }
                        } else {
                            redisChgService.del(hKey);
                        }
                    } finally {
                        redisChgService.unlock(lockKey, lockValue);
                    }
                }
                platformTransactionManager.commit(transaction);
            } catch (Exception e) {
                log.error(e.getMessage() + "\n" + dataCountFragmentsMgs, e);
                platformTransactionManager.rollback(transaction);
                delSyncReportHashKey(hashKeys);
                result.setCode(ResultCode.FAIL.getValue());
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException interruptedException) {
                    log.warn(interruptedException.getMessage(), interruptedException);
                    Thread.currentThread().interrupt();
                }
            }
        }
        return result;
    }

    /**
     * 2024-03-21 17:03
     * 批量删除hash key
     *
     * @param hashSyncReportKeys key
     */
    private void delSyncReportHashKey(List<String> hashSyncReportKeys) {
        String[] keys = hashSyncReportKeys.toArray(new String[0]);
        try {
            long count = redisChgService.del(keys);
            if (count != keys.length) {
                log.warn("转化数据统计清理redis主键部分失败，共:{}；删除:{}；keys:{}"
                        , keys.length, count, Arrays.toString(keys));
                hashSyncReportKeys.forEach((String key) -> {
                    try {
                        redisChgService.del(key);
                    } catch (Exception exception) {
                        log.warn(exception.getMessage(), exception);
                    }
                });
            }
        } catch (Exception exception) {
            log.error(exception + "\n转化数据统计清理redis主键失败:" + Arrays.toString(keys), exception);
        }
    }

    /**
     * 2024-03-12 15:01
     * 汇总数据
     *
     * @param syncReport    目标记录
     * @param syncReportOld 历史记录
     * @return key filed; value value
     */
    private Map<String, String> syncReportSummary(MarketingSyncReport syncReport, MarketingSyncReport syncReportOld
            , boolean cacheBool) {
        String cacheString;
        syncReport.setUserType(null);
        syncReport.setCid(null);
        syncReport.setAppletDate(null);
        syncReport.setApiCode(null);
        syncReport.setCreateTime(null);
        syncReport.setShortName(null);
        syncReport.setRemark(null);
        syncReport.setNormalNum(syncReportOld.getNormalNum() + syncReport.getNormalNum());
        syncReport.setDuplicateRemovalNum(syncReportOld.getDuplicateRemovalNum() + syncReport.getDuplicateRemovalNum());
        boolean beginBool = (syncReportOld.getAppletBeginTime().before(syncReport.getAppletBeginTime())
                || syncReportOld.getAppletBeginTime().equals(syncReport.getAppletBeginTime()));
        boolean endBool = (syncReportOld.getAppletEndTime().after(syncReport.getAppletEndTime())
                || syncReportOld.getAppletEndTime().equals(syncReport.getAppletEndTime()));
        if (cacheBool) {
            syncReport.setAppletBeginTime(beginBool ? null : syncReport.getAppletBeginTime());
            syncReport.setAppletEndTime(endBool ? null : syncReport.getAppletEndTime());
            cacheString = JSON.toJSONString(syncReport);
            syncReport.setId(syncReportOld.getId());
        } else {
            syncReport.setAppletBeginTime(beginBool ? syncReportOld.getAppletBeginTime() : syncReport.getAppletBeginTime());
            syncReport.setAppletEndTime(endBool ? syncReportOld.getAppletEndTime() : syncReport.getAppletEndTime());
            syncReport.setId(syncReportOld.getId());
            cacheString = JSON.toJSONString(syncReport);
            if (beginBool) {
                syncReport.setAppletBeginTime(null);
            }
            if (endBool) {
                syncReport.setAppletEndTime(null);
            }
        }
        Map<String, String> stringMap = JSONObject.parseObject(cacheString, new TypeReference<Map<String, String>>() {
        });
        syncReport.setUpdateTime(new Date());
        return stringMap;
    }

    @Override
    public List<String> getLastMonthDataDates(String apiCode) {
        return syncReportMapper.getLastMonthDataDates(apiCode);
    }

    @Override
    public void exportData(String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes,
                           String userTypes,Integer selectType,String selectExportIds, HttpServletResponse response){
        try {
            String yyyyMMdd = new SimpleDateFormat(DateHelper.SHORT_DATE_FORMAT).format(new Date());
            String encodeFileName  = URLEncoder.encode("数据导出"+ yyyyMMdd +".txt",StandardCharsets.UTF_8.toString());
            response.setContentType("text/plain; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodeFileName);
            ServletOutputStream out = response.getOutputStream();
            try(OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                // 写入UTF-8 BOM，确保Excel等软件正确识别编码
                out.write(0xEF);
                out.write(0xBB);
                out.write(0xBF);
                writer.append("上传日期").append(",").append("客户编号").append(",").append("APIcode").append(",")
                        .append("客户名称").append(",").append("场景").append(",").append("数据正常入库条数").append(",")
                        .append("去重后数据量").append(",").append("创建时间").append(",").append("上传开始时间").append(",")
                        .append("上传结束时间").append(",").append("数据生效时间").append(",").append("数据失效时间").append("\r\n");

                fillExportData(cidOrName,appletTimeStart,appletTimeEnd,apiCodes,userTypes,selectType,selectExportIds,writer,out);
            }catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SYNC_REPORT_EXPORT_SERVICEERROR.getCode(), e.getMessage()), e);
            }
        } catch (IOException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SYNC_REPORT_EXPORT_SERVICEERROR.getCode(), e.getMessage()), e);
        }
    }

    private void fillExportData(String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes,
                                Integer selectType, String selectExportIds, OutputStreamWriter writer, ServletOutputStream out)  throws IOException{
            if (selectType == 1) {
                if (StringUtils.isNotEmpty(appletTimeEnd)) {
                    appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
                }
                if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")) {
                    cidOrName = cidOrName.replace("_", "\\_");
                }
                List<String> apiCodeList = new ArrayList<>();
                List<String> userTypeList = new ArrayList<>();
                if (apiCodes != null && !"".equals(apiCodes)) {
                    String[] split = apiCodes.split(",");
                    for (String item : split) {
                        apiCodeList.add(item);
                    }
                }
                if (userTypes != null && !"".equals(userTypes)) {
                    String[] split = userTypes.split(",");
                    for (String item : split) {
                        userTypeList.add(item);
                    }
                }
                Map params = new HashMap();
                params.put("cidOrName", cidOrName);
                params.put("appletTimeStart", appletTimeStart);
                params.put("appletTimeEnd", appletTimeEnd);
                params.put("apiCodeList", apiCodeList);
                params.put("userTypeList", userTypeList);
                Integer pageNum = 1;
                Integer pageSize = marketingCommonConfig.getSyncReportExportPageSize()==null?10000:marketingCommonConfig.getSyncReportExportPageSize();
                while (true) {
                    PageHelper.startPage(pageNum, pageSize,false);
                    List<MarketingSyncReportVO> list = syncReportMapper.selectExportDataList(params);
                    Integer endLineStatus = list.size() < pageSize ? 1 : 0;
                    exportAppendListData(list,writer,endLineStatus);
                    if (CollectionUtils.isEmpty(list) || list.size() < pageSize) {
                        writer.flush();
                        out.flush();
                        break;
                    }
                    pageNum++;
                    writer.flush();
                    out.flush();
                }
            } else {
                List<Long> selectIdList = new ArrayList<>();
                if(StringUtils.isNotEmpty(selectExportIds)){
                    String[] split = selectExportIds.split(",");
                    for (String item : split) {
                        selectIdList.add(Long.parseLong(item));
                    }
                    if (!CollectionUtils.isEmpty(selectIdList)) {
                        List<MarketingSyncReportVO> list = syncReportMapper.selectByIdList(selectIdList);
                        exportAppendListData(list,writer,1);
                    }
                }
                writer.flush();
                out.flush();
            }
    }

    private void exportAppendListData(List<MarketingSyncReportVO> list, OutputStreamWriter writer,Integer endLineStatus) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            MarketingSyncReportVO marketingSyncReportVO = list.get(i);
            writer.append(safeToString(marketingSyncReportVO.getAppletDate())).append(",")
                    .append(safeToString(marketingSyncReportVO.getCid())).append(",")
                    .append(safeToString(marketingSyncReportVO.getApiCode())).append(",")
                    .append(safeToString(marketingSyncReportVO.getShortName())).append(",")
                    .append(safeToString(marketingSyncReportVO.getUserType())).append(",")
                    .append(safeToString(marketingSyncReportVO.getNormalNum())).append(",")
                    .append(safeToString(marketingSyncReportVO.getDuplicateRemovalNum())).append(",")
                    .append(safeToString(marketingSyncReportVO.getCreateTime())).append(",")
                    .append(safeToString(marketingSyncReportVO.getAppletBeginTime())).append(",")
                    .append(safeToString(marketingSyncReportVO.getAppletEndTime())).append(",")
                    .append(safeToString(marketingSyncReportVO.getValidStartDate())).append(",")
                    .append(safeToString(marketingSyncReportVO.getValidEndDate()));
            if (i == list.size() - 1) {
                if (endLineStatus == 0) {
                     writer.append("\r\n");
                }
            }else{
                writer.append("\r\n");
            }
        }
    }

    private String safeToString(Object value) {
        return value != null ? value.toString() : "";
    }

}
