package com.br.marketing.service.Impl;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.*;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.constants.common.LastEnum;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.*;
import com.br.marketing.dto.customer.PushCustomerRequestDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.PushTransferRobotaiLogService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PushRuleServiceImpl implements PushRuleService {

    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);

    private static HashMap<String, String> errorCodeHm;

    static {
        errorCodeHm = new HashMap();
        errorCodeHm.put("1001", "无客户编号");
        errorCodeHm.put("1002", "无grouptype或userType或cell");
        errorCodeHm.put("1003", "重复客户编号");
        errorCodeHm.put("1004", "重复电话");
        errorCodeHm.put("1005", "入库异常");
        errorCodeHm.put("1006", "参数过长");
    }

    @Autowired
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Autowired
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Autowired
    MarketingStrategyProductMapper marketingStrategyProductMapper;

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Autowired
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    DecodeClient decodeClient;

    @Resource
    private RestTemplate restTemplate;

    @Value("#{${api.pushTransfer.robotAi.tailor.apiCodeMap:{'7410787':true}}}")
    private Map<String, Boolean> tailorApiCodeMap;

    @Value("${api.pushTransfer.robotAi.robotOutboundUrl:'http://robotai-api-service/api/robotOutbound'}")
    private String robotOutboundUrl;

    @Resource
    private PushTransferCustomerLogMapper pushTransferCustomerLogMapper;

    @Resource
    private PushTransferRobotaiLogService pushTransferRobotaiLogService;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    @Override
    public Result<List<ScoreDetailVo>> getBatchInfos(CustomerBatchNumDTO dto) {
        Date date = addDay(dto.getScoreEndTime(), 1, "yyyy-MM-dd");
        dto.setScoreEndTime(DateUtils.format(date, "yyyy-MM-dd"));

        Date dateUpdate = addDay(dto.getUploadEndTime(), 1, "yyyy-MM-dd");
        dto.setUploadEndTime(DateUtils.format(dateUpdate, "yyyy-MM-dd"));
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchs(dto);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(scoreDetailVos);
    }

    @Override
    public Result<List<PushInfoDetailVO>> getPushInfos(RequestPushInfoDTO dto) {
        Date date = addDay(dto.getPushEndTime(), 1, "yyyy-MM-dd");
        dto.setPushEndTime(DateUtils.format(date, "yyyy-MM-dd"));
        List<PushInfoDetailVO> pushInfos = customerInfoPushMainMapper.getPushInfos(dto);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushInfos);
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

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    @Qualifier("apipool")
    ThreadPoolExecutor threadPoolExecutor;


    @Autowired
    @Qualifier("currentDbpool")
    ThreadPoolExecutor currentDbPoolExecutor;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    MarketingSyncErrorInfoMapper marketingSyncErrorInfoMapper;

    @Autowired
    MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Autowired
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    AlarmApiClient alarmApiClient;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    final String redisKey_apiCode_taskId = "marketing:preuser:";

    final String redisKeySoleNum = "sole:thread:num";

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    SoleStrategyService soleStrategyService;

    final static Byte customerStatus = Byte.valueOf("1");

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> pushCustomer(PushCustomerDTO dto) {

        /**
         * 先校验下 传过来的批次和 模型是否匹配
         * 推送mq
         */
        //region check
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }

        MarketingStrategyProductExample productExample = new MarketingStrategyProductExample();
        productExample.createCriteria().andFileIdIn(dto.getFileIdList())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingStrategyProduct> marketingStrategyProductsDb = marketingStrategyProductMapper.selectByExample(productExample);
        List<MarketingStrategyProduct> marketingStrategyProducts = marketingStrategyProductsDb.stream().filter(t ->
                dto.getProductName().equals(t.getProductName())
                        && dto.getProductVersion().equals(t.getProductVersion())).collect(Collectors.toList());
        if (marketingStrategyProducts.size() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次和所筛选的模型是否匹配");
        }

        Integer planNum = 0;
        if (dto.getMinTop() != null && dto.getMaxTop() != null) {
            planNum = dto.getMaxTop() - dto.getMinTop();
            if (planNum <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的top区间不合理");
            }
        } else if (dto.getMinTop() == null && dto.getMaxTop() == null) {

        } else {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的top区间不合理");
        }

        if (dto.getMinScore() != null && dto.getMaxScore() != null) {
            int scoreDvalue = dto.getMaxScore() - dto.getMinScore();
            if (scoreDvalue <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的分值区间不合理");
            }
        } else if (dto.getMinScore() == null && dto.getMaxScore() == null) {

        } else {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的分值区间不合理");
        }


        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(dto.getApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
        queryBaseBean.setFileIds(Joiner.on(",").join(dto.getFileIdList()));
        queryBaseBean.setModelCode(dto.getProductName());
        queryBaseBean.setModelVersion(dto.getProductVersion());
        if (dto.getMaxScore() != null && dto.getMinScore() != null) {
            queryBaseBean.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
        }
        if (dto.getMinTop() != null && dto.getMaxTop() != null) {
            queryBaseBean.setAmountTop(dto.getMinTop().toString().concat(",").concat(dto.getMaxTop().toString()));
        }
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        } else {
            if (planNum == 0) {
                planNum = total;
            }
        }

        //endregion

        //region insert db

        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(t -> t.getShowTitle()).collect(Collectors.toList());
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmModel(dto.getProductName());
        customerInfoPushMain.setmModelVersion(dto.getProductVersion());
        customerInfoPushMain.setmNumMin(dto.getMinTop());
        customerInfoPushMain.setmNumMax(dto.getMaxTop());
        customerInfoPushMain.setmScoreMin(dto.getMinScore());
        customerInfoPushMain.setmScoreMax(dto.getMaxScore());
        customerInfoPushMain.setmPlanNum(planNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        customerInfoPushMain.setmStatus(1);
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);

        marketingStrategyProducts.forEach(t -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
            customerInfoPushBatch.setmCusBatchNumber(t.getCusBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatch.setmFileId(t.getFileId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
        //endregion

        //region push mq
        producter.send("Marketing.Push.CustomerService", customerInfoPushMain.getId().toString());
        //endregion

        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> consumerPushCustomer(Long id) {
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);
        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }

        HashMap<Long, TaskExtendInfoVO> hsTaskExtend = new HashMap<>();
        List<TaskExtendInfoVO> extendInfosByFileIds = straHisFileMapper.getExtendInfosByFileIds(fileIds);
        extendInfosByFileIds.forEach(t -> {
            hsTaskExtend.put(t.getFileId(), t);
        });
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
        queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
        queryBaseBean.setModelCode(customerInfoPushMain.getmModel());
        queryBaseBean.setModelVersion(customerInfoPushMain.getmModelVersion());
        if (customerInfoPushMain.getmScoreMin() != null && customerInfoPushMain.getmScoreMax() != null) {
            queryBaseBean.setScoreRange(customerInfoPushMain.getmScoreMin().toString()
                    .concat(",").concat(customerInfoPushMain.getmScoreMax().toString()));
        }
        if (customerInfoPushMain.getmNumMin() != null && customerInfoPushMain.getmNumMax() != null) {
            queryBaseBean.setAmountTop(customerInfoPushMain.getmNumMin().toString()
                    .concat(",").concat(customerInfoPushMain.getmNumMax().toString()));
        }
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        //region push Intelligent Customer Service

        //调用es查询接口
        int minTop = (customerInfoPushMain.getmNumMin() == null) ? 0 : customerInfoPushMain.getmNumMin();
        int startPageYushu = minTop % 10000;
        Integer startPage = minTop / 10000 + (startPageYushu > 0 ? 1 : 0);
        String searchAfterStr = "";
        for (int i = 1; i <= startPage; i++) {

            if (i == startPage && startPageYushu > 0) {
                queryBaseBean.setPageSize(startPageYushu);
            } else {
                queryBaseBean.setPageSize(10000);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            String s = marketingHistoryEsService.builderMarketingWithSearchAfter(queryBaseBean);
            searchAfterStr = s;
        }
        Integer realTotalNum = 0;
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(2);
        int totalYuShu = total % 2000;
        int totalPage = total / 2000 + (totalYuShu > 0 ? 1 : 0);
        for (int i = 1; i <= totalPage; i++) {
            String sn = String.valueOf(i);
            if (i == totalPage && totalYuShu > 0) {
                queryBaseBean.setPageSize(totalYuShu);
            } else {
                queryBaseBean.setPageSize(2000);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            Integer realNum = marketingHistories.size();
            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            for (int k = 0; k < marketingHistories.size(); k++) {
                MarketingHistory marketingHistory = marketingHistories.get(k);
                if (k == (marketingHistories.size() - 1)) {
                    searchAfterStr = marketingHistory.getSearchAfter();
                }
                //人员信息
                PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
//                dto1.setCaseNumber("test_202106020100".concat("_").concat(String.valueOf(System.currentTimeMillis())));
                if (log.isInfoEnabled()) {
                    log.info("人员信息：cusnum:{};batchnumber:{}", marketingHistory.getCusNum(),
                            (StringUtils.isNotBlank(marketingHistory.getBatchNumber()) ? marketingHistory.getBatchNumber() : ""));
                }
                dto1.setCaseNumber(marketingHistory.getCusNum().concat("_").concat(marketingHistory.getBatchNumber()).concat("_")
                        .concat(String.valueOf(System.currentTimeMillis())));
                dto1.setPhone(marketingHistory.getCell());
                Optional<Product> first = marketingHistory.getProduct().stream().filter(t -> customerInfoPushMain.getmModel().equals(t.getCode())
                        && customerInfoPushMain.getmModelVersion().equals(t.getVersion())).findFirst();

                //人员的变量信息
                PushMarketingUserDetailVariablesDTO pushMarketingUserDetailVariablesDTO = new PushMarketingUserDetailVariablesDTO();
                pushMarketingUserDetailVariablesDTO.setScoreDate(marketingHistory.getRequestTime() == null ? "" : (DateUtils.format(
                        marketingHistory.getRequestTime(), "yyyy-MM-dd")));
                pushMarketingUserDetailVariablesDTO.setScoreName(customerInfoPushMain.getmModel());
                if (first.isPresent()) {
                    pushMarketingUserDetailVariablesDTO.setScore(String.valueOf(first.get().getScore()));
                }
                TaskExtendInfoVO taskExtendInfoVO = hsTaskExtend.get(Long.valueOf(marketingHistory.getFileId()));
                if (taskExtendInfoVO != null) {
                    pushMarketingUserDetailVariablesDTO.setUpdate(taskExtendInfoVO.getUploadTime());
                }
                pushMarketingUserDetailVariablesDTO.setTaskId(marketingHistory.getTaskId());
                pushMarketingUserDetailVariablesDTO.setGroupType(marketingHistory.getUserType());

                dto1.setVariables(pushMarketingUserDetailVariablesDTO);
                userDetailDTOS.add(dto1);
            }

            //推送任务基础信息
            PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
            pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
            pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
//                pushMarketingUserTaskInfoDTO.setStrategyCode("");
            pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + sn);
            PushMarketingExtendDataDTO extendDataDTO = new PushMarketingExtendDataDTO();
            extendDataDTO.setScoreName(customerInfoPushMain.getmModel());
            if (customerInfoPushMain.getmScoreMin() != null && customerInfoPushMain.getmScoreMax() != null) {
                extendDataDTO.setScoreRange(customerInfoPushMain.getmScoreMin().toString().concat(",")
                        .concat(customerInfoPushMain.getmScoreMax().toString()));
            }
            if (customerInfoPushMain.getmNumMin() != null && customerInfoPushMain.getmNumMax() != null) {
                extendDataDTO.setAmountTop(Convert.toStr(customerInfoPushMain.getmNumMin() - customerInfoPushMain.getmNumMax()));
            }
            extendDataDTO.setSampleTotal(sn);
            pushMarketingUserTaskInfoDTO.setExtendData(extendDataDTO);
            pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);

            //传输参数信息
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
            pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);

            Result<Integer> result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO, customerInfoPushMain.getId(),
                    pushMarketingUserTaskInfoDTO.getAccessNumber());
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                main.setmStatus(3);
            } else {
                realTotalNum += realNum;
            }
        }
        main.setmRealyNum(realTotalNum);
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        //endregion

        //region push mq
        producter.send("Marketing.Push.CustomerService.Search.Delay", customerInfoPushMain.getId().toString());
        //endregion

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    @Override
    public Result<Boolean> getCustomerStatus(Long mId) {

        CustomerInfoPushMain main = customerInfoPushMainMapper.selectByPrimaryKey(mId);

        CustomerInfoPushLogExample logExample = new CustomerInfoPushLogExample();
        logExample.createCriteria().andMIdEqualTo(mId).andRealStautsIn(Arrays.asList("1", "900013"));
        List<CustomerInfoPushLog> customerInfoPushLogs = customerInfoPushLogMapper.selectByExample(logExample);
        Boolean isContinue = Boolean.FALSE;
        for (CustomerInfoPushLog t : customerInfoPushLogs) {
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(main.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode("");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method", "uploadResult");
            jsonObject.put("accessNumber", t.getBatch());
            pushMarketingUserDTO.setJsonData(jsonObject);
            Result<String> userStatus = intelligentCustomerServiceClient.getUserStatus(pushMarketingUserDTO);
            if (ResultCode.SUCCESS.getValue().equals(userStatus.getCode())) {
                CustomerInfoPushLog updateLog = new CustomerInfoPushLog();
                updateLog.setId(t.getId());
                if ("900013".equals(userStatus.getData())) {
                    isContinue = Boolean.TRUE;
                }
                updateLog.setRealStauts(userStatus.getData());
                customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
            } else {
                isContinue = Boolean.TRUE;
            }
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue);
    }

    /**
     * 接受异步推送人员文本信息
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Override
    public Result insertMarketingPreUserText(String apiCode, String jsonData) {
        //region check
        long l1 = System.currentTimeMillis();
        RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
        dto.setApiCode(apiCode);
        try {
            dto.setJsonData(JSON.parseObject(jsonData, new TypeReference<MarketingPreUserDTO>() {
            }.getType()));
        } catch (JSONException ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        if (log.isInfoEnabled()) {
            log.info("反序列化耗时:{}", (System.currentTimeMillis() - l1));
        }
        if (dto.getJsonData() == null) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setCusBatch(dto.getJsonData().getTaskId());
        if (!StringUtils.isNotBlank(dto.getJsonData().getTaskId())) {
            throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
        }
        RuntimeDataContext.getData().setRequestBatch(dto.getJsonData().getRequestId());
        if (!StringUtils.isNotBlank(dto.getJsonData().getRequestId())) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        /**
         * 兼容旧逻辑,如果没传，则last=0，非最后一次，
         * */
        byte last = 0;
        String lastStr = dto.getJsonData().getLast();
        if (StringUtils.isNotBlank(lastStr)) {
            if (LastEnum.isLegal(lastStr)) {
                last = Byte.valueOf(lastStr);
            } else {
                throw new CommonException(MarketingErrorInfo.LAST_ERROR);
            }
        }
        /**
         * 兼容旧逻辑,如果没传，则total=0
         * */
        Long total = 0L;
        String totalStr = dto.getJsonData().getTotal();
        if (StringUtils.isNotBlank(totalStr)) {
            try {
                total = Long.valueOf(totalStr);
            } catch (NumberFormatException numberFormatException) {
                throw new CommonException(MarketingErrorInfo.TOTAL_ERROR);
            }
        }
        int size = dto.getJsonData().getDataItems().size();
        RuntimeDataContext.getData().setActualNum(size);
        if (size > 2000) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        if (log.isInfoEnabled()) {
            log.info("check耗时:{}", (System.currentTimeMillis() - l1));
        }
        //endregion
        long l = System.currentTimeMillis();
        try {
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            syncInfo.setApiCode(dto.getApiCode());
            syncInfo.setCusBatch(dto.getJsonData().getTaskId());
            syncInfo.setRequestBatch(dto.getJsonData().getRequestId());
            syncInfo.setLast(last);
            syncInfo.setTotal(total);
            syncInfo.setCreateTime(new Date());
            syncInfo.setJsonData(jsonData);
            syncInfo.setActualNum(size);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            if (log.isInfoEnabled()) {
                log.info("文本插入耗时:{}", (System.currentTimeMillis() - l));
            }
            long l3 = System.currentTimeMillis();
            producter.send("Marketing.PreUser.Receive", syncInfo.getId().toString());
            if (log.isInfoEnabled()) {
                log.info("MQ推送耗时:{}", (System.currentTimeMillis() - l3));
            }
        } catch (DuplicateKeyException keyException) {
            if (log.isInfoEnabled()) {
                log.error("文本插入耗时:{}", (System.currentTimeMillis() - l));
            }
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        } catch (Exception ex) {
            throw ex;
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    /**
     * 消费异步推送人员信息
     *
     * @param infoId
     * @return
     */
    @Override
    public Result<Boolean> insertMarketingPreUserSync(Long infoId) {
        String s = redisChgService.get(redisKeySoleNum);
        Integer soleNum = 20;
        if (StringUtils.isNotBlank(s)) {
            soleNum = Integer.valueOf(s);
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("去重线程数：%d", soleNum));
        }
        Boolean isContinue = Boolean.FALSE;
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        MarketingPreUserDTO dto = JSON.parseObject(marketingSyncInfo.getJsonData(), new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        //规则校验方式-isCheck
        MerchantParam merchantParam = null;
        String apiCode = marketingSyncInfo.getApiCode();
        Result<List<CustomerSoleRuleVO>> soleConfig = iRuleConfigService.getSoleConfig(apiCode);
        try {
            merchantParam = IceClient.getMerchantParam(apiCode);
        } catch (Exception e) {
            log.error("从用户中心请求用户信息出错--apiCode:{}--{}", apiCode, e);
        }
        Integer isCheck = 0;
        if (merchantParam != null) {
            isCheck = merchantParam.getIsCheck();
        }
        long l = System.currentTimeMillis();
        tableCreateService.createMarketingSyncUserTable(marketingSyncInfo.getApiCode());
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
            //此处会处理三种场景的数据
            //1 数禾、萨摩耶：只有groupType
            //2 宜信：既有groupType,又有userType
            //3 未来客户：只有userType
            String reserveField1Str = marketingPreUserDetailDTO.getReserveField1();
            ReserveField1DTO reserveField1 = null;
            if (StringUtils.isBlank(reserveField1Str)) {
                reserveField1 = new ReserveField1DTO();
                reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
            } else {
                try {
                    reserveField1 = JSON.parseObject(reserveField1Str, new TypeReference<ReserveField1DTO>() {
                    }.getType());
                    if (StringUtils.isBlank(reserveField1.getUserType())) {
                        reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
                    }
                } catch (JSONException ex) {
                    reserveField1 = new ReserveField1DTO();
                    reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
                    reserveField1.setExtStr(reserveField1Str);
                }
            }
            Integer finalIsCheck = isCheck;
            ReserveField1DTO finalReserveField = reserveField1;
            list.add(() -> {
                if (!StringUtils.isNotBlank(marketingPreUserDetailDTO.getCustNum())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setErrorCode("1001");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1001"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (!StringUtils.isNotBlank(finalReserveField.getUserType())
                        || !StringUtils.isNotBlank(marketingPreUserDetailDTO.getCell())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                    errorDetailVO.setErrorCode("1002");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1002"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                //解密、规则校验
                marketingPreUserDetailDTO.setStatus(MonitorTypeEnum.STATUS_1.getTypeCode());
                encodeMapping(marketingPreUserDetailDTO, "cell", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO, "id", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO, "name", finalIsCheck);
                Date nowData = new Date();
                String date = DateUtils.format(nowData, "yyyy-MM-dd HH:mm:ss");
                String appletDate = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd");
                String appletTime = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
                marketingSyncUser.setApiCode(apiCode);
                marketingSyncUser.setCusBatch(marketingSyncInfo.getCusBatch());
                marketingSyncUser.setRequestBatch(marketingSyncInfo.getRequestBatch());
                marketingSyncUser.setCustNum(marketingPreUserDetailDTO.getCustNum());
                marketingSyncUser.setIdCard(marketingPreUserDetailDTO.getId());
                marketingSyncUser.setName(marketingPreUserDetailDTO.getName());
                marketingSyncUser.setCell(marketingPreUserDetailDTO.getCell());
                marketingSyncUser.setGroupType(marketingPreUserDetailDTO.getGroupType());
                marketingSyncUser.setRegisterDate(marketingPreUserDetailDTO.getRegisterDate());
                marketingSyncUser.setReserveField1(JSON.toJSONString(finalReserveField));
                marketingSyncUser.setReserveField2(marketingPreUserDetailDTO.getReserveField2());
                marketingSyncUser.setCreateTime(nowData);
                marketingSyncUser.setUpdateTime(nowData);
                marketingSyncUser.setAppletDate(appletDate);
                marketingSyncUser.setStatus(marketingPreUserDetailDTO.getStatus());
                marketingSyncUser.setFailType(marketingPreUserDetailDTO.getFailType());
                marketingSyncUser.setAppletTime(marketingSyncInfo.getCreateTime());
                marketingSyncUser.setUserType(finalReserveField.getUserType());
                try {
                    Long st1 = System.currentTimeMillis();
                    Long et1 = null;
                    Long et2 = null;
                    marketingSyncUserMapper.insertMarketingSyncUser(marketingSyncUser);
                    et1 = System.currentTimeMillis() - st1;
                    if (ResultCode.SUCCESS.getValue().equals(soleConfig.getCode())) {
                        Long st2 = System.currentTimeMillis();
                        soleStrategyService.actionSole(soleConfig.getData(), marketingSyncUser);
                        et2 = System.currentTimeMillis() - st2;
                    }
                    if (log.isInfoEnabled()) {
                        log.info(String.format("去重数据：%d,数据入库和去重时间耗时：%d，数据去重时间：%d"
                                , marketingSyncUser.getId(), et1, et2));
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("IDX_taskId_custNum")) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1003");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1003"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    } else if (ex.getMessage().contains("uk_taskId_cell")) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1004");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1004"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    } else {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1005");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1005"));
                        log.error(ex.getMessage(), ex);
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        List<MarketingPreUserErrorDetailVO> errorBuild = new ArrayList<>();
        Integer errorSize = 0;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(soleNum, soleNum);
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures = null;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            threadPool.shutdown();
        }
        if (futures != null && !futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Result<MarketingPreUserErrorDetailVO> result = futures.get(i).get();
                    if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        errorSize++;
                        errorBuild.add(result.getData());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        MarketingSyncInfo updateSyncInfo = new MarketingSyncInfo();
        updateSyncInfo.setId(marketingSyncInfo.getId());
        updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
        Date nowData2 = new Date();
        if (errorSize == 0) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
        } else if (errorSize == futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(nowData2);
            errorInfo.setErrorInfo(JSON.toJSONString(errorBuild));
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(nowData2);
            errorInfo.setErrorInfo(JSON.toJSONString(errorBuild));
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        }
        marketingSyncInfoMapper.updateByPrimaryKeySelective(updateSyncInfo);
        if (log.isInfoEnabled()) {
            log.info("数据解析插入耗时:{}", (System.currentTimeMillis() - l));
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }

    @Override
    public Result insertTransferData(String apiCode, String jsonData) {
        //region check
        long l1 = System.currentTimeMillis();
        TransferDataDTO transferDataDTO = null;
        try {
            transferDataDTO = JSON.parseObject(jsonData, new TypeReference<TransferDataDTO>() {
            }.getType());
        } catch (JSONException ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        if (transferDataDTO == null) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setRequestBatch(transferDataDTO.getRequestId());
        if (!StringUtils.isNotBlank(transferDataDTO.getRequestId()) || transferDataDTO.getRequestId().length() > 100) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        int size = transferDataDTO.getDataItems().size();
        RuntimeDataContext.getData().setActualNum(size);
        if (size > 2000) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        //endregion
        long l = System.currentTimeMillis();
        try {
            MarketingTransferInfo transferInfo = new MarketingTransferInfo();
            transferInfo.setApiCode(apiCode);
            transferInfo.setRequestId(transferDataDTO.getRequestId());
            transferInfo.setOrgName(transferDataDTO.getOrgName());
            transferInfo.setCreateTime(new Date());
            transferInfo.setJsonData(jsonData);
            transferInfo.setActualNum(size);
            transferInfo.setLast(transferDataDTO.getLast());
            transferInfo.setTotal(transferDataDTO.getTotal());
            marketingTransferInfoMapper.insertSelective(transferInfo);
            producter.send("Marketing.Transfer.Receive", transferInfo.getId().toString());
        } catch (DuplicateKeyException keyException) {
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        } catch (Exception ex) {
            throw ex;
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    @Override
    public Result consumerTransferData(Long id) {
        Integer soleNum = 20;
        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        TransferDataDTO dto = JSON.parseObject(transferInfo.getJsonData(), new TypeReference<TransferDataDTO>() {
        }.getType());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).andStatusEqualTo(customerStatus);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() == 0) {
            throw new RuntimeException(String.format("该apicode:%s 没有维护cid信息,消费有问题", transferInfo.getApiCode()));
        }
        String cid = marketingCustomers.get(0).getCid();
        String tcid = cid.replaceFirst("-", "");
        tableCreateService.createMarketingTransferUserTable(tcid);
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            TransferDataItemDTO transferDataItemDTO = dto.getDataItems().get(i);
            list.add(() -> {
                if (!StringUtils.isNotBlank(transferDataItemDTO.getCustNum())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setErrorCode("1001");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1001"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (!StringUtils.isNotBlank(transferDataItemDTO.getUserType())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1002");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1002"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (transferDataItemDTO.getUserType().length() > 100) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1006");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1006"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                Date nowData = new Date();
                String requestDate = DateUtils.format(transferInfo.getCreateTime(), "yyyy-MM-dd");
                String requestTime = DateUtils.format(transferInfo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
                BeanUtils.copyProperties(transferDataItemDTO, transferSyncUser);
                transferSyncUser.setRequestId(transferInfo.getRequestId());
                transferSyncUser.setApiCode(transferInfo.getApiCode());
                transferSyncUser.setOrgName(transferInfo.getOrgName());
                transferSyncUser.setRequestData(requestDate);
                transferSyncUser.setRequestTime(requestTime);
                transferSyncUser.setCreateTime(nowData);
                transferSyncUser.setCid(cid);
                transferSyncUser.settCid(tcid);
                transferSyncUser.setRegisterTime(dateTimeComplet(transferDataItemDTO.getRegisterTime()));
                transferSyncUser.setLoginTime(dateTimeComplet(transferDataItemDTO.getLoginTime()));
                transferSyncUser.setApplyDt(dateTimeComplet(transferDataItemDTO.getApplyDt()));
                transferSyncUser.setApplyTime(dateTimeComplet(transferDataItemDTO.getApplyTime()));
                transferSyncUser.setRefuseTime(dateTimeComplet(transferDataItemDTO.getRefuseTime()));
                transferSyncUser.setAuditTime(dateTimeComplet(transferDataItemDTO.getAuditTime()));
                transferSyncUser.setLentTime(dateTimeComplet(transferDataItemDTO.getLentTime()));
                transferSyncUser.setSettleTime(dateTimeComplet(transferDataItemDTO.getSettleTime()));
                transferSyncUser.setTransformTime(dateTimeComplet(transferDataItemDTO.getTransformTime()));
                try {
                    marketingTransferSyncUserMapper.insertSelective(transferSyncUser);
                } catch (Exception ex) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1005");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1005"));
                    log.error(ex.getMessage(), ex);
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        List<MarketingPreUserErrorDetailVO> errorBuild = new ArrayList<>();
        Integer errorSize = 0;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(soleNum, soleNum);
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures = null;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            threadPool.shutdown();
        }
        if (futures != null && !futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Result<MarketingPreUserErrorDetailVO> result = futures.get(i).get();
                    if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        errorSize++;
                        errorBuild.add(result.getData());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        MarketingTransferInfo updateSyncInfo = new MarketingTransferInfo();
        updateSyncInfo.setId(id);
        updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
        if (errorSize == 0) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
        } else if (errorSize == futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
            updateSyncInfo.setErrorInfo(JSON.toJSONString(errorBuild));
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            updateSyncInfo.setErrorInfo(JSON.toJSONString(errorBuild));
        }
        marketingTransferInfoMapper.updateByPrimaryKeySelective(updateSyncInfo);
        if (updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success)
                || updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success_part)) {
            producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER, id.toString());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }


    private String dateTimeComplet(String data) {
        String res = "";
        try {
            if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":00:000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":00:00:000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{2}/\\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(" 00:00:00:000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else {
                res = data;
            }
        } catch (Exception ex) {
            res = data;
            log.error(ex.getMessage(), ex);
        }
        return res;
    }

    @Override
    public Result<MarketingTransferUserStatusVO> getTransferDataStatus(String apiCode, String requestId) {
        if (StringUtils.isBlank(requestId)) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        MarketingTransferInfoExample transferInfoExample = new MarketingTransferInfoExample();
        transferInfoExample.createCriteria().andRequestIdEqualTo(requestId).andApiCodeEqualTo(apiCode);
        List<MarketingTransferInfo> marketingTransferInfos = marketingTransferInfoMapper.selectByExample(transferInfoExample);
        if (marketingTransferInfos.size() <= 0) {
            throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
        }
        MarketingTransferInfo transferInfo = marketingTransferInfos.get(0);
        MarketingTransferUserStatusVO vo = new MarketingTransferUserStatusVO();
        vo.setApiCode(transferInfo.getApiCode());
        vo.setRequestId(transferInfo.getRequestId());
        vo.setStatus(transferInfo.getStatus());
        if (StringUtils.isNotBlank(transferInfo.getErrorInfo())) {
            List<MarketingPreUserErrorDetailVO> o = JSON.parseObject(transferInfo.getErrorInfo(), new TypeReference<List<MarketingPreUserErrorDetailVO>>() {
            }.getType());
            vo.setErrorInfo(o);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(vo).setMessage("成功");
    }

    final static Set groupTypeSaMoye;

    static {
        groupTypeSaMoye = new HashSet();
        groupTypeSaMoye.add("S01");
        groupTypeSaMoye.add("S02");
        groupTypeSaMoye.add("S04");
        groupTypeSaMoye.add("S06");
        groupTypeSaMoye.add("S08");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result insertBatchTransferUser(String apiCode, String jsonData) {
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(jsonData);
        } catch (Exception ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        String requestId = jsonObject.getString("requestId");
        RuntimeDataContext.getData().setRequestBatch(requestId);
        if (StringUtils.isBlank(requestId)) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        if (requestId.length() > 100) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() <= 0) {
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }
        String cid = marketingCustomers.get(0).getCid();
        if (StringUtils.isBlank(cid)) {
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }

        marketingSyncInfoMapper.createMarketingTransferTable("b_marketing_transfer_".concat(apiCode));
        Integer hasData = marketingSyncInfoMapper.selectTransfersByRequestId(apiCode, requestId);
        if (hasData > 0) {
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        }

        List<TransferUserVO> transfers = new ArrayList<>();
        try {
            transfers = JSON.parseObject(jsonObject.getString("dataItems"), new TypeReference<List<TransferUserVO>>() {
            }.getType());
        } catch (Exception ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setActualNum(transfers.size());
        if (transfers.size() > 100) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        if (transfers.size() == 0) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }


        StringBuilder sqlByTaskAndCustNum = new StringBuilder();
        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (int i = 0; i < transfers.size(); i++) {
//            StringBuilder sql = new StringBuilder();
            TransferUserVO transferUserVO = transfers.get(i);
            //region 校验参数
            if (StringUtils.isBlank(transferUserVO.getTaskId())) {
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if (transferUserVO.getTaskId().length() > 50) {
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if (StringUtils.isBlank(transferUserVO.getCustNum())) {
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if (transferUserVO.getCustNum().length() > 100) {
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if (StringUtils.isBlank(transferUserVO.getGroupType())) {
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            if (transferUserVO.getGroupType().length() > 100) {
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            //endregion

            //region 拼接sql
            Date parse = null;
            try {
                if (StringUtils.isNotBlank(transferUserVO.getTransformTime())) {
                    parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transferUserVO.getTransformTime());
                }
            } catch (Exception e) {
                throw new CommonException(MarketingErrorInfo.TIME_FORMAT_ERROR);
            }
            MarketingTransfer transfer = new MarketingTransfer();
            transfer.setApiCode(apiCode);
            transfer.setRequestId(requestId);
            transfer.setTaskId(transferUserVO.getTaskId());
            transfer.setCustNum(transferUserVO.getCustNum());
            transfer.setTransformTime(parse == null ? null : (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parse)));
            transfer.setCreateTime(new Date());
            transfer.setGroupType(transferUserVO.getGroupType());
            transfer.setReserveField1(transferUserVO.getReserveField1());
            transfer.setReserveField2(transferUserVO.getReserveField2());
            if (i == 0) {
                sqlByTaskAndCustNum.append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                        , transferUserVO.getTaskId()
                        , transferUserVO.getCustNum()));
            }
            if (i > 0) {
//                sql.append(",");
                sqlByTaskAndCustNum.append(" or ")
                        .append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                                , transferUserVO.getTaskId()
                                , transferUserVO.getCustNum()));
            }

            //endregion
            marketingSyncInfoMapper.insertTransfer(transfer);
            transferUserVO.setId(transfer.getId());
        }

//        if(StringUtils.isNotBlank(sql.toString())){
//            marketingSyncInfoMapper.insertBatchTransfer(sql.toString());
//        }
        HashMap<String, MarketingSyncUser> hmPreUser = new HashMap();
        if (StringUtils.isNotBlank(sqlByTaskAndCustNum.toString())) {
            List<MarketingSyncUser> preUserByTaskAndCust = marketingSyncInfoMapper.getPreUserByTaskAndCust(apiCode, sqlByTaskAndCustNum.toString());
            for (MarketingSyncUser marketingSyncUser : preUserByTaskAndCust) {
                hmPreUser.put(marketingSyncUser.getCusBatch()
                        .concat("_")
                        .concat(marketingSyncUser.getCustNum()), marketingSyncUser);
            }
        }
        //region 拼接客服接口参数
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        List<ConversionData> conversionDataList = new ArrayList<>();
        for (TransferUserVO transfer : transfers) {
            if(!(groupTypeSaMoye.contains(transfer.getGroupType())&&"1".equals(transfer.getReserveField1()))){
                continue;
            }
            ConversionData data = new ConversionData();
            data.setCaseNum(transfer.getCustNum());
            data.setInversionDate(transfer.getTransformTime());
            data.setInversionStatus("0");
            data.setInversionInfo(JSON.toJSONString(transfer));
            data.setTaskId(transfer.getTaskId());
            data.setPartnerProcessDate(nowDate);
            data.setGroupType(transfer.getGroupType());
            data.setCid(cid);
            MarketingSyncUser marketingSyncUser = hmPreUser.get(transfer.getTaskId()
                    .concat("_")
                    .concat(transfer.getCustNum()));
            if (marketingSyncUser != null) {
                data.setPhone(StringUtils.isBlank(marketingSyncUser.getFailType())
                        ? BrCipherMaker.getInstance().decode(marketingSyncUser.getCell())
                        : marketingSyncUser.getCell());
            }
            data.setDataId(transfer.getId().toString());
            conversionDataList.add(data);
        }
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setConversionData(conversionDataList);
        jsonDataDTO.setMethod("conversionData");
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(apiCode);
        robotOutboundDTO.setJsonData(jsonDataDTO);
        //endregion

        //todo 调用客服接口
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO, requestId);
        if (String.valueOf("9999").equals(transferRobotOutboundVO.getCode())) {
            throw new CommonException(MarketingErrorInfo.REQUEST_FAIL_ERROR, transferRobotOutboundVO.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 解密、规则校验
     *
     * @param user
     * @param isCheck
     * @return
     */
    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck) {
        String content = "";
        switch (type) {
            case "cell":
                content = StringUtils.isBlank(user.getCell()) ? "" : user.getCell();
                break;
            case "id":
                content = StringUtils.isBlank(user.getId()) ? "" : user.getId();
                break;
            case "name":
                content = StringUtils.isBlank(user.getName()) ? "" : user.getName();
                break;
        }
        if (DecodeClient.isMd5(content)) {
            //cell md5
            content = decodeClient.query(content, type, "md5", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (content.length() == 64) {
            //cell sha256
            content = decodeClient.query(content, type, "sha", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        }
        //明文规则校验
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotBlank(content) && "cell".equals(type)) {
            if (!userValidator.validatePhone(content)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setCell(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content) && "id".equals(type)) {
            if (!userValidator.validateId(content)) {
                user.setId(content);
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setId(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content) && "name".equals(type)) {
            if (!userValidator.validateName(content)) {
                user.setName(content);
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setName(BrCipherMaker.getInstance().encode(content));
        }
    }

    /**
     * 获取营销人员数据状态
     *
     * @param dto
     * @return
     */
    @Override
    public Result<MarketingPreUserSyncDetailVO> getMarketingPreUserSyncStatus(MarketingPreUserSyncStatusDTO dto) {
        Result<MarketingPreUserSyncDetailVO> marketingPreUserSyncDetailVOResult = new Result<>();
        MarketingPreUserSyncDetailVO vo = new MarketingPreUserSyncDetailVO();
        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
        syncInfoExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andCusBatchEqualTo(dto.getTaskId())
                .andRequestBatchEqualTo(dto.getRequestId());
        List<MarketingSyncInfo> marketingSyncInfos = marketingSyncInfoMapper.selectByExample(syncInfoExample);
        if (marketingSyncInfos.size() <= 0) {
            throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
        }
        MarketingSyncInfo syncInfo = marketingSyncInfos.get(0);
        vo.setApiCode(syncInfo.getApiCode());
        vo.setTaskId(syncInfo.getCusBatch());
        vo.setRequestId(syncInfo.getRequestBatch());
        vo.setStatus(syncInfo.getStatus());

        if (StatusConstants.MarketingPreUserStatus_fail.equals(syncInfo.getStatus())
                || StatusConstants.MarketingPreUserStatus_success_part.equals(syncInfo.getStatus())) {
            MarketingSyncErrorInfo errorInfo = marketingSyncErrorInfoMapper.selectByPrimaryKey(syncInfo.getErrorId());
            if (errorInfo != null) {
                List<MarketingPreUserErrorDetailVO> o = JSON.parseObject(errorInfo.getErrorInfo(), new TypeReference<List<MarketingPreUserErrorDetailVO>>() {
                }.getType());
                vo.setErrorInfo(o);
            }
        }
        switch (syncInfo.getStatus()) {
            case 1:
                marketingPreUserSyncDetailVOResult.setMessage("运行中");
                break;
            case 2:
                marketingPreUserSyncDetailVOResult.setMessage("全部成功");
                break;
            case 3:
                marketingPreUserSyncDetailVOResult.setMessage("全部失败");
                break;
            case 4:
                marketingPreUserSyncDetailVOResult.setMessage("部分成功");
                break;
            default:
        }
        return marketingPreUserSyncDetailVOResult.setCode(ResultCode.SUCCESS.getValue()).setDate(vo);
    }

    /**
     * 查询客户信息接口
     *
     * @param cid
     * @param custNum
     * @return
     */
    @Override
    public Result<MarketingSyncUser> queryCustInfo(String cid, String custNum) {
        Result<MarketingSyncUser> result = new Result<>();
        //校验
        if (StringUtils.isBlank(cid) && StringUtils.isBlank(custNum)) {
            return result.setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数缺失");
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andCidEqualTo(cid);
        List<MarketingCustomer> cList = marketingCustomerMapper.selectByExample(customerExample);
        if (cList != null && !cList.isEmpty()) {
            for (MarketingCustomer customer : cList) {
                String apiCode = customer.getApiCode();
                if (StringUtils.isNotBlank(apiCode)) {
                    MarketingSyncUser vo = marketingUserMapper.selectSyncUserByCustNum(apiCode, custNum);
                    if (vo != null) {
                        return result.setCode(ResultCode.SUCCESS.getValue()).setDate(vo).setMessage("成功");
                    }
                }
            }
        }
        return result.setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    //初始化一个ForkJoinPool
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Math.min(0x7fff, Runtime.getRuntime().availableProcessors()),
//            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                @Override
                public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                    ForkJoinWorkerThread workerThread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    workerThread.setName("br-push-forkJoin-pool-" + workerThread.getPoolIndex());
                    return workerThread;
                }
            },
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error("推送客服任务异常：任务线程:[{}]\n{}", t.getName(), e.getMessage(), e);
                }
            },
            // 队列模式，false 后人先出，true 先进先出
            false);


    @Override
    @Transactional
    public synchronized Result<Boolean> pushPersonalTransferData(Long infoId) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        try {
            // 传输标记
            final int transferStatus;
            // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
            List<MarketingTransferInfo> list = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(infoId);
            if (CollectionUtils.isEmpty(list)) {
                result.setDate(false);
                String smg = String.format("主键为[%s]的客户转化基础信息不存在,该信息直接消费,不再重放队列", infoId);
                log.error(smg);
                result.setMessage(smg);
                alarmClient.sendAlarm(smg, "接口转化数据同步到智能客服警告", appName, secretKey,
                        Constants.sendCodeMap.get("pushToCustomer"));
                return result;
            }
            MarketingTransferInfo info = list.get(0);
            String apiCode = info.getApiCode();
            Date createTime = ObjectUtils.isEmpty(info.getCreateTime()) ? new Date() : info.getCreateTime();
            result.setDate(true);
            if (!tailorApiCodeMap.getOrDefault(apiCode, false)) {
                try {
                    info.setId(infoId);
                    pushTransferData(info);
                    result.setDate(false);
                } catch (Exception e) {
                    String smg = String.format("主键[%d];apiCode[%s];requestId[%s]推送错误！\n%s", infoId, apiCode, info.getRequestId(), e.getMessage());
                    log.error(smg, e);
                    alarmClient.sendAlarm(smg, "接口转化(通用标准)数据同步到智能客服警告", appName, secretKey,
                            Constants.sendCodeMap.get("pushToCustomer"));
                }
                return result;
            }
            //        String yyyyMMdd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 格式化入库时间
            String yyyyMMdd = createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            // 如果是最后一次传
            if ("1".equals(info.getLast())) {
                transferStatus = 2;
            } else {
                transferStatus = 0;
            }
            String requestId = info.getRequestId();
            // 2 获取分表后缀
            String tcId = tableCreateService.getTcId(apiCode);
            // 3 获取转化数据,
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andRequestIdEqualTo(requestId);
            example.settCid(tcId);
            int page = 1;
            final int pageSize = 2000;
            final int retrySum = 2;
            List<PushTransferCustomerLog> logListAll = new ArrayList<>();
            label:
            for (; ; ) {
                PageHelper.startPage(page, pageSize);
                List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
                int size = transferList.size();
                PageInfo<MarketingTransferSyncUser> pageList = new PageInfo<>(transferList);
                // 总页数
                int pages = pageList.getPages();
                boolean b = true;
                // 处理开始标记
                switch (transferStatus) {
                    case 0:
                        if (size < 1) {
                            if (info.getActualNum() < 1) {
                                PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                        new PushCustomerRequestDTO(apiCode, transferStatus, null), 3, size);
                                pushTransferCustomerLog.setTransferStatus(transferStatus);
                                logListAll.add(pushTransferCustomerLog);
                                break label;
                            } else {
                                String smg = String.format("last[0];infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]转化未完成，未获取到转化数据"
                                        , infoId, apiCode, requestId, tcId, yyyyMMdd);
                                sendAlarm(smg);
                                return result;
                            }
                        }
                        b = asyncPush(transferList, logListAll);
                        break;
                    case 2:
                        if (page == pages) {
                            List<MarketingTransferSyncUser> listEnd;
                            if (size > 200) {
                                int len = (size - 200);
                                b = asyncPush(transferList.subList(0, len), logListAll);
                                listEnd = transferList.subList(len, size);
                            } else {
                                // 检查是否有开始标记
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "0,2");
                                if (countStatus > 0) {
                                    listEnd = transferList;
                                } else {
                                    // 检查转化信息表是否出现过last为0数据
                                    List<Long> ids = marketingTransferInfoMapper.countByApiCodAndLast(apiCode, createTime, "0");
                                    if (ids.size() == 0) {
                                        int len = size / 2;
                                        PushTransferCustomerLog pushLog = sendTransferDataToCustomer(
                                                new PushCustomerRequestDTO(apiCode, 0, transferList.subList(0, len)), retrySum, len);
                                        pushLog.setTransferStatus(0);
                                        logListAll.add(pushLog);
                                        listEnd = transferList.subList(len, size);
                                    } else {
                                        List<Long> infoIds = pushTransferCustomerLogMapper.findInfoIdListByCodeAndInfoTimeAndTransferStatus(apiCode, createTime, 0);
                                        if (infoIds.size() < ids.size()) {
                                            ids.removeAll(infoIds);
                                            listEnd = null;
                                            for (Long idf : ids) {
                                                pushPersonalTransferData(idf);
                                            }
                                        } else {
                                            listEnd = transferList;
                                        }
                                    }
                                }
                            }
                            if (listEnd != null) {
                                // 检查是否全部推送完成
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "1,3");
                                if (countStatus < 1) {
                                    PushTransferCustomerLog pushLog = sendTransferDataToCustomer(
                                            new PushCustomerRequestDTO(apiCode, transferStatus, listEnd), retrySum, listEnd.size());
                                    pushLog.setTransferStatus(transferStatus);
                                    logListAll.add(pushLog);
                                } else {
                                    PushCustomerRequestDTO pushCustomerRequestDTO = new PushCustomerRequestDTO(apiCode, transferStatus, listEnd);
                                    logListAll.add(new PushTransferCustomerLog(apiCode
                                            , pushCustomerRequestDTO.getJsonData()
                                            , listEnd.size()
                                            , 1
                                            , transferStatus
                                    ));
                                }
                            }
                        } else if (pages < 1) {
                            if (info.getActualNum() < 1) {
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "0,2");
                                if (countStatus > 0) {
                                    countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "1,3");
                                    if (countStatus < 1) {
                                        PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                                new PushCustomerRequestDTO(apiCode, transferStatus, null), 3, size);
                                        pushTransferCustomerLog.setTransferStatus(transferStatus);
                                        logListAll.add(pushTransferCustomerLog);
                                        break label;
                                    }
                                }
                                PushCustomerRequestDTO pushCustomerRequestDTO = new PushCustomerRequestDTO(apiCode, transferStatus, null);
                                logListAll.add(new PushTransferCustomerLog(apiCode
                                        , pushCustomerRequestDTO.getJsonData()
                                        , size
                                        , 1
                                        , transferStatus
                                ));
                            } else {
                                String smg = String.format("last[1]infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]转化未完成，未获取到转化数据"
                                        , infoId, apiCode, requestId, tcId, yyyyMMdd);
                                sendAlarm(smg);
                                return result;
                            }
                        } else {
                            b = asyncPush(transferList, logListAll);
                        }
                        break;
                    default:
                        log.error("未知的标记:{}", transferStatus);
                }
                if (!b) {
                    String smg = String.format("infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]中推送中线程任务失败"
                            , infoId, apiCode, requestId, tcId, yyyyMMdd);
                    sendAlarm(smg);
                    return result;
                }
                // 总记录数
                long total = pageList.getTotal();
                if ((pages == page && transferList.size() <= total) || transferList.size() == 0) {
                    break;
                }
                page++;
            }
            // 5 推送记录日志
            if (logListAll.size() > 0) {
                List<PushTransferCustomerLog> collect = logListAll.stream().peek(failLog -> {
                    failLog.setRequestId(requestId);
                    failLog.settCid(tcId);
                    failLog.setTransferInfoId(infoId);
                    failLog.setTransferInfoTime(createTime);
                }).collect(Collectors.toList());
                boolean bool = pushTransferCustomerLogMapper.bathInsert(collect);
                if (bool) {
                    log.info("推送客服数据已保存记录，本次保存[{}]", collect.size());
                } else {
                    result.setMessage("保存记录失败！");
                    String smg = String.format("保存推送记录失败：apiCode:{%s};requestId:{%s};tcId:{%s};infoId:{%d};失败数据量:{%d}"
                            , apiCode, requestId, tcId, infoId, collect.size());
                    sendAlarm(smg);
                    return result;
                }
            } else {
                return result;
            }
            result.setMessage("成功");
            result.setDate(false);
            return result;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(e.getMessage());
            sendAlarm(e.getMessage());
            return result;
        }
    }


    private boolean asyncPush(List<MarketingTransferSyncUser> transferSyncUserList, List<PushTransferCustomerLog> logList) throws Throwable {
        // 4 推送转化数据,每次200条，失败后重试3次，标记为同步中
        PushTransferDataToCustomerTask task = new PushTransferDataToCustomerTask(transferSyncUserList, 0, transferSyncUserList.size());
        List<PushTransferCustomerLog> logs = FORK_JOIN_POOL.invoke(task);
        logList.addAll(logs);
        boolean b = FORK_JOIN_POOL.awaitQuiescence(5, TimeUnit.SECONDS);
        if (b) {
            if (task.isCompletedAbnormally()) {
                Throwable exception = task.getException();
                if (exception != null) {
                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
                return false;
            }
            return true;
        }
        if (!task.isDone()) {
            task.isCancelled();
        }
        log.warn("等待任务超时，任务已经处理完成");
        return false;
    }


    private void sendAlarm(String smg) {
        log.warn(smg);
        alarmClient.sendAlarm(smg, "接口转化(私人订制)数据同步到智能客服警告", appName, secretKey,
                Constants.sendCodeMap.get("pushToCustomer"));
    }

    private void sendAlarm(String smg, String key) {
        log.warn(smg);
        alarmClient.sendAlarm(smg, "接口转化(私人订制)数据同步到智能客服警告", appName, secretKey,
                Constants.sendCodeMap.get("pushToCustomer"));
        redisChgService.incrBy(key, -1);
        redisChgService.expire(key, getKeyExpiration());
    }

    private synchronized Long getApiCodeCount(String key) {
        Long incr = redisChgService.incr(key);
        int keyExpiration = getKeyExpiration();
        redisChgService.expire(key, keyExpiration);
        return incr;
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }


    /**
     * 发送转化数据到客服
     *
     * @param requestDTO 数据集合
     * @param retrySum   指定最大重试次数，包括第一次执行，默认1次
     * @param rowSize    发送数据量
     * @return false 失败；true 成功； 失败需要写入失败日志，以便后续补发
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 17:51
     */
    private PushTransferCustomerLog sendTransferDataToCustomer(final PushCustomerRequestDTO requestDTO
            , int retrySum
            , final int rowSize) {
        int count = 1;
        if (retrySum < 1) {
            retrySum = 1;
        }
        final HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tempHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        tempHeaders.setAccept(Collections.singletonList(MediaType.ALL));
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("apiCode", requestDTO.getApiCode());
        postParameters.add("jsonData", requestDTO.getJsonData());
        final HttpEntity<MultiValueMap<String, Object>> stringHttpEntity = new HttpEntity<>(postParameters, tempHeaders);
        ResponseEntity<String> responseEntity = null;
        HttpStatus statusCode = null;
        String body;
        JSONObject result;
        String code;
        int value;
        do {
            log.info("########################第【{}/{}】次调用接口", count, retrySum);
            try {
                responseEntity = restTemplate.postForEntity(robotOutboundUrl
                        , stringHttpEntity, String.class);
                statusCode = responseEntity.getStatusCode();
                value = statusCode.value();
                body = responseEntity.getBody();
                result = JSONObject.parseObject(body);
                code = String.valueOf(result.get("code"));
                // 重试休眠
                TimeUnit.SECONDS.sleep(count < 4 ? count : 3);
            } catch (RestClientException | InterruptedException e) {
                value = -1;
                body = "";
                result = null;
                code = "";
                log.error(e.getMessage(), e);
            }
            count++;
        } while ((value != 200 || !"00".equals(code)) && count <= retrySum);
        int pushStatus = 0;
        if (ObjectUtils.isEmpty(responseEntity) || ObjectUtils.isEmpty(statusCode)) {
            String smg = String.format("%s : apiCode[%s]发送重试[%d]次后依然失败！接口不能正常访问"
                    , LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), requestDTO.getApiCode(), count - 1);
            alarmClient.sendAlarm(smg, "\n接口转化(私人订制)数据同步到智能客服失败", appName, secretKey,
                    Constants.sendCodeMap.get("pushToCustomer"));
            return new PushTransferCustomerLog(
                    requestDTO.getApiCode()
                    , requestDTO.getJsonData()
                    , rowSize
                    , 1
            );
        }
        String reasonPhrase = statusCode.getReasonPhrase();
        log.info("智能客服接口HttpStatus[code:{};reasonPhrase:{}]", value, reasonPhrase);
        if (value != 200 || !"00".equals(code)) {
            // 客服业务中出现的非正常状态码全部补偿
            pushStatus = 1;
            String smg = String.format("apiCode:[%s]发送重试[%d]次后依然失败！" +
                    "\n接口返回http状态码[%d],http短语[%s];" +
                    "\n应答消息[%s]", requestDTO.getApiCode(), count, value, reasonPhrase, body);
            alarmClient.sendAlarm(smg, "\n接口转化(私人订制)数据同步到智能客服失败", appName, secretKey,
                    Constants.sendCodeMap.get("pushToCustomer"));
        }
        return new PushTransferCustomerLog(
                requestDTO.getApiCode()
                , requestDTO.getJsonData()
                , body
                , code
                , result.get("message") == null ? "" : result.get("message").toString()
                , result.get("accessNumber") == null ? result.get("swiftNumber") == null
                ? "" : result.get("swiftNumber").toString() : result.get("accessNumber").toString()
                , rowSize
                , value
                , reasonPhrase
                , pushStatus
        );
    }


    // 处理任务
    private class PushTransferDataToCustomerTask extends RecursiveTask<List<PushTransferCustomerLog>> {

        private static final long serialVersionUID = 4930523513045970753L;
        private final List<MarketingTransferSyncUser> list;
        private final int start;
        private final int end;

        public PushTransferDataToCustomerTask(List<MarketingTransferSyncUser> list, int start, int end) {
            super();
            this.list = list;
            this.start = start;
            this.end = end;
        }

        @SneakyThrows
        @Override
        protected List<PushTransferCustomerLog> compute() {
            List<PushTransferCustomerLog> logList = new ArrayList<>();
            int threshold = 200;
            if ((end - start) <= threshold) {
                log.info("++++++++++++++++=====分段数据：【{}】-【{}】", start, end);
                List<MarketingTransferSyncUser> transferSyncUserList = list.subList(start, end);
                int transferStatus = 0;
                int retrySum = 2;
                PushTransferCustomerLog log = sendTransferDataToCustomer(
                        new PushCustomerRequestDTO(transferSyncUserList.get(0).getApiCode(), transferStatus, transferSyncUserList)
                        , retrySum, transferSyncUserList.size());
                log.setTransferStatus(transferStatus);
                logList.add(log);
            } else {
                int middle = (end + start) / 2;
                PushTransferDataToCustomerTask taskLeft = new PushTransferDataToCustomerTask(list, start, middle);
                PushTransferDataToCustomerTask taskRight = new PushTransferDataToCustomerTask(list, middle, end);
                invokeAll(taskLeft, taskRight);
                logList.addAll(taskLeft.join());
                logList.addAll(taskRight.join());
            }
            return logList;
        }
    }


    @Override
    public List<TransferRobotOutboundVO<UnsuccessfulData>> pushTransferData(MarketingTransferInfo transferInfo) {
        Assert.notNull(transferInfo, "转化信息不可为null");
        String apiCode = transferInfo.getApiCode();
        Assert.notNull(apiCode, "'apiCode'不可为null");
        String requestId = transferInfo.getRequestId();
        Assert.notNull(transferInfo, "'requestId'不可为null");
        String title = "接口转化(通用标准)数据同步到智能客服警告";
        // 1 获取分表后缀
        String tcId = tableCreateService.getTcId(apiCode);
        // 2 获取转化数据
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andRequestIdEqualTo(requestId);
        example.settCid(tcId);
        int page = 1;
        final int pageSize = 500;
        List<TransferRobotOutboundVO<UnsuccessfulData>> list = new ArrayList<>();
        for (; ; ) {
            PageHelper.startPage(page, pageSize);
            List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(transferList) && transferInfo.getActualNum() < 1) {
                String smg = String.format("转化信息为【apiCode:[%s],RequestId:[%s],infoId:[%s],tcId:[%s]】没有找到对应的转化数据，此消息不再放回队列！日期:%s", apiCode
                        , transferInfo.getRequestId(), transferInfo.getId(), tcId, DateUtils.getNowyyyy_MM_dd());
                alarmClient.sendAlarm(smg, title, appName, secretKey,
                        Constants.sendCodeMap.get("pushToCustomer"));
                PushTransferRobotaiLog robotaiLog = new PushTransferRobotaiLog(
                        transferInfo.getId()
                        , apiCode
                        , transferInfo.getRequestId()
                        , ""
                        , ""
                        , smg
                        , transferList.size()
                        , ""
                        , tcId
                );
                robotaiLog.setPushStatus(3);
                pushTransferRobotaiLogService.save(robotaiLog);
                break;
            }
            TransferRobotOutboundDTO robotOutboundDTO = getTransferRobotOutbound(transferInfo, transferList);
            TransferRobotOutboundVO<UnsuccessfulData> outboundVO = pushTransferData(robotOutboundDTO, transferInfo);
            if (!outboundVO.getAccessNumber().equals("-1")) {
                pushTransferRobotaiLogService.saveLog(transferInfo, robotOutboundDTO, outboundVO);
            }
            list.add(outboundVO);
            PageInfo<MarketingTransferSyncUser> pageInfo = new PageInfo<>(transferList);
            if (page == pageInfo.getPages() || transferList.size() == 0) {
                break;
            }
            page++;
        }
        return list;
    }

    @Override
    public TransferRobotOutboundVO<UnsuccessfulData> pushTransferData(TransferRobotOutboundDTO dto, MarketingTransferInfo transferInfo) {
        Assert.notNull(dto, String.format("转化数据不存在!\n转化信息[id=%d;apiCode=%s;requestId=%s]"
                , transferInfo.getId(), transferInfo.getApiCode(), transferInfo.getRequestId()));
        TransferRobotOutboundVO<UnsuccessfulData> outboundVO;
        try {
            outboundVO = robotaiApiServiceClient.pushRobotai(dto, transferInfo.getRequestId());
            if (outboundVO.getCode().equals("00")) {
                Object o = (outboundVO.getData());
                JSONObject object = JSON.parseObject(o.toString());
                Object unsuccessfulData = object.get("unsuccessfulData");
                JSONArray array = JSON.parseArray(unsuccessfulData.toString());
                if (array.size() < 1) {
                    outboundVO.setAccessNumber("-1");
                    return outboundVO;
                }
            } else if (outboundVO.getCode().equals("9999")) {
                outboundVO.setCode("");
                outboundVO.setAccessNumber("");
                outboundVO.setData(new UnsuccessfulData());
            } else {
                if (outboundVO.getAccessNumber() == null) {
                    outboundVO.setAccessNumber("");
                }
                if (outboundVO.getData() == null) {
                    outboundVO.setData(new UnsuccessfulData());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            outboundVO = new TransferRobotOutboundVO<>();
            outboundVO.setMessage(e.getMessage());
        }
        return outboundVO;
    }

    final static String hasTransfer = "1";

    final static String noHasTransfer = "0";

    @Override
    public TransferRobotOutboundDTO getTransferRobotOutbound(MarketingTransferInfo transferInfo
            , List<MarketingTransferSyncUser> transferList) {
        String title = "接口转化(通用标准)数据同步到智能客服警告";
        Assert.notNull(transferInfo, "转化信息不存在!");
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        String apiCode = transferInfo.getApiCode();
        if (CollectionUtils.isEmpty(transferList)) {
            String smg = String.format("apiCode:[%s],RequestId:[%s],id:[%s]信息不存在！日期:%s", apiCode
                    , transferInfo.getRequestId(), transferInfo.getId(), DateUtils.getNowyyyy_MM_dd());
            alarmClient.sendAlarm(smg, title, appName, secretKey,
                    Constants.sendCodeMap.get("pushToCustomer"));
            return null;
        }
        Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
        Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, syncUser -> syncUser
                , (v1, v2) -> StringUtils.isNotBlank(v2.getCell()) && !ObjectUtils.isEmpty(v2.getCreateTime())
                        && v2.getCreateTime().before(v1.getCreateTime()) ? v2 : v1));
        Assert.notNull(preUserByTask, "'MarketingSyncUser'不可为null");
        List<ConversionData> conversionDataArray = new ArrayList<>();
        transferList.forEach(transfer -> {
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(transfer.getId().toString());
            conversionData.setCid(transfer.getCid());
            conversionData.setCaseNum(transfer.getCustNum());
            conversionData.setGroupType(transfer.getUserType());
            conversionData.setInversionStatus(hasTransfer.equals(transfer.getIfTransform())
                    ?"0"
                    :(noHasTransfer.equals(transfer.getIfTransform())?"1":transfer.getIfTransform()));
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            conversionData.setPhone(map.containsKey(transfer.getCustNum())
                    ? BrCipherMaker.getInstance().decode(map.get(transfer.getCustNum()).getCell()) : "");
            TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
            BeanUtils.copyProperties(transfer, vo);
            conversionData.setInversionInfo(JSON.toJSONString(vo));
            conversionDataArray.add(conversionData);
        });
        robotOutboundDTO.setApiCode(apiCode);
        robotOutboundDTO.setJsonData(new TransferJsonDataDTO(conversionDataArray));
        return robotOutboundDTO;
    }
}
