package com.br.marketing.service.Impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.*;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.constants.common.LastEnum;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.dto.*;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.vo.*;
import com.google.common.base.Joiner;

import java.util.*;

import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushRuleService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class PushRuleServiceImpl implements PushRuleService {

    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);

    private static HashMap<String,String> errorCodeHm;

    static {
        errorCodeHm = new HashMap();
        errorCodeHm.put("1001","无客户编号");
        errorCodeHm.put("1002","无grouptype或userType或cell");
        errorCodeHm.put("1003","重复客户编号");
        errorCodeHm.put("1004","重复电话");
        errorCodeHm.put("1005","入库异常");
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
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    DecodeClient decodeClient;

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
    AlarmApiClient alarmApiClient;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    final String redisKey_apiCode_taskId = "marketing:preuser:";

    final static String marketingPreUserTable = "b_marketing_sync_";

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

        StraHisFileExample straHisFileExample= new StraHisFileExample();
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

        HashMap<Long,TaskExtendInfoVO> hsTaskExtend = new HashMap<>();
        List<TaskExtendInfoVO> extendInfosByFileIds = straHisFileMapper.getExtendInfosByFileIds(fileIds);
        extendInfosByFileIds.forEach(t->{
            hsTaskExtend.put(t.getFileId(),t);
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
                if(taskExtendInfoVO !=null){
                    pushMarketingUserDetailVariablesDTO.setUpdate(taskExtendInfoVO.getUploadTime());
                    pushMarketingUserDetailVariablesDTO.setTaskId(taskExtendInfoVO.getCusTaskId());
                    pushMarketingUserDetailVariablesDTO.setGroupType(taskExtendInfoVO.getGroupType());
                }

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
        if (!StringUtils.isNotBlank(dto.getJsonData().getTaskId())) {
            throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
        }
        if (!StringUtils.isNotBlank(dto.getJsonData().getRequestId())) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        /**
         * 兼容旧逻辑,如果没传，则last=0，非最后一次，
         * */
        byte last = 0;
        String lastStr = dto.getJsonData().getLast();
        if (StringUtils.isNotBlank(lastStr)) {
            if(LastEnum.isLegal(lastStr)) {
                last = Byte.valueOf(dto.getJsonData().getLast());
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
                total = Long.valueOf(dto.getJsonData().getTotal());
            } catch (NumberFormatException numberFormatException) {
                throw new CommonException(MarketingErrorInfo.TOTAL_ERROR);
            }
        }
        int size = dto.getJsonData().getDataItems().size();
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
        Boolean isContinue = Boolean.FALSE;
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        MarketingPreUserDTO dto = JSON.parseObject(marketingSyncInfo.getJsonData(), new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        //规则校验方式-isCheck
        MerchantParam merchantParam = null;
        String apiCode = marketingSyncInfo.getApiCode();
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
        marketingUserMapper.createMarketingPreUserTable(marketingPreUserTable.concat(marketingSyncInfo.getApiCode()));
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
            //此处会处理三种场景的数据
            //1 数禾、萨摩耶：只有groupType
            //2 宜信：既有groupType,又有userType
            //3 未来客户：只有userType
            ReserveField1DTO reserveField1 = marketingPreUserDetailDTO.getReserveField1();
            if (null == reserveField1) {
                reserveField1 = new ReserveField1DTO();
                reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
            } else {
                if (StringUtils.isBlank(reserveField1.getUserType())) {
                    reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
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
                encodeMapping(marketingPreUserDetailDTO,"cell", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO,"id", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO,"name", finalIsCheck);
                String date = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                String appletDate = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd");
                String dataStr = String.format("( '%s','%s','%s','%s','%s','%s','%s','%s', '%s','%s' ,'%s' ,'%s' ,'%s','%s','%s','%s',%s)"
                        , marketingSyncInfo.getApiCode(), marketingSyncInfo.getCusBatch()
                        , marketingSyncInfo.getRequestBatch(), marketingPreUserDetailDTO.getCustNum()
                        , marketingPreUserDetailDTO.getCell()
                        , StringUtils.isBlank(marketingPreUserDetailDTO.getId())?"":marketingPreUserDetailDTO.getId()
                        , StringUtils.isBlank(marketingPreUserDetailDTO.getName())?"":marketingPreUserDetailDTO.getName()
                        , marketingPreUserDetailDTO.getGroupType()
                        , finalReserveField.getUserType()
                        , marketingPreUserDetailDTO.getRegisterDate()
                        , JSON.toJSONString(marketingPreUserDetailDTO.getReserveField1())
                        , marketingPreUserDetailDTO.getReserveField2()
                        , date, date, appletDate,
                        marketingPreUserDetailDTO.getFailType() == null ? "" : marketingPreUserDetailDTO.getFailType(),
                        marketingPreUserDetailDTO.getStatus());
                try {
                    marketingUserMapper.insertBatchMarketingPreUserByDatas(marketingSyncInfo.getApiCode(), dataStr);
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
                        log.error(ex.getMessage(),ex);
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        List<MarketingPreUserErrorDetailVO> errorBuild = new ArrayList<>();
        Integer errorSize = 0;
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures = null;
        try {
            futures = currentDbPoolExecutor.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
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
                    log.error(e.getMessage(),e);
                }
            }
        }
        MarketingSyncInfo updateSyncInfo = new MarketingSyncInfo();
        updateSyncInfo.setId(marketingSyncInfo.getId());
        updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
        if (errorSize == 0) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
        } else if (errorSize == futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(new Date());
            errorInfo.setErrorInfo(JSON.toJSONString(errorBuild));
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(new Date());
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result insertBatchTransferUser(String apiCode, String jsonData){
        JSONObject jsonObject =null;
        try {
            jsonObject = JSON.parseObject(jsonData);
        }catch (Exception ex){
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        String requestId = jsonObject.getString("requestId");
        if(StringUtils.isBlank(requestId)){
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        if(requestId.length()>100){
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if(marketingCustomers.size()<=0){
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }
        String cid = marketingCustomers.get(0).getCid();
        if(StringUtils.isBlank(cid)){
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }

        marketingSyncInfoMapper.createMarketingTransferTable("b_marketing_transfer_".concat(apiCode));
        Integer hasData = marketingSyncInfoMapper.selectTransfersByRequestId(apiCode, requestId);
        if(hasData>0){
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        }

        List<TransferUserVO> transfers = new ArrayList<>();
        try {
            transfers = JSON.parseObject(jsonObject.getString("dataItems"), new TypeReference<List<TransferUserVO>>() {
            }.getType());
        }catch (Exception ex){
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        if(transfers.size()>100){
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        if(transfers.size()==0){
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }


        StringBuilder sqlByTaskAndCustNum = new StringBuilder();
        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (int i = 0; i < transfers.size(); i++) {
//            StringBuilder sql = new StringBuilder();
            TransferUserVO transferUserVO = transfers.get(i);
            //region 校验参数
            if(StringUtils.isBlank(transferUserVO.getTaskId())){
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if(transferUserVO.getTaskId().length()>50){
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if(StringUtils.isBlank(transferUserVO.getCustNum())){
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if(transferUserVO.getCustNum().length()>100){
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if(StringUtils.isBlank(transferUserVO.getGroupType())){
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            if(transferUserVO.getGroupType().length()>100){
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            //endregion

            //region 拼接sql
            Date parse = null;
            try {
                if(StringUtils.isNotBlank(transferUserVO.getTransformTime())) {
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
            transfer.setTransformTime(parse==null?null:(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parse)));
            transfer.setCreateTime(new Date());
            transfer.setGroupType(transferUserVO.getGroupType());
            transfer.setReserveField1(transferUserVO.getReserveField1());
            transfer.setReserveField2(transferUserVO.getReserveField2());
            if(i==0){
                sqlByTaskAndCustNum.append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                        ,transferUserVO.getTaskId()
                        ,transferUserVO.getCustNum()));
            }
            if(i>0){
//                sql.append(",");
                sqlByTaskAndCustNum.append(" or ")
                        .append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                                ,transferUserVO.getTaskId()
                                ,transferUserVO.getCustNum()));
            }

            //endregion
            marketingSyncInfoMapper.insertTransfer(transfer);
            transferUserVO.setId(transfer.getId());
        }

//        if(StringUtils.isNotBlank(sql.toString())){
//            marketingSyncInfoMapper.insertBatchTransfer(sql.toString());
//        }
        HashMap<String,MarketingSyncUser> hmPreUser = new HashMap();
        if(StringUtils.isNotBlank(sqlByTaskAndCustNum.toString())){
            List<MarketingSyncUser> preUserByTaskAndCust = marketingSyncInfoMapper.getPreUserByTaskAndCust(apiCode, sqlByTaskAndCustNum.toString());
            for (MarketingSyncUser marketingSyncUser : preUserByTaskAndCust) {
                hmPreUser.put(marketingSyncUser.getCusBatch()
                        .concat("_")
                        .concat(marketingSyncUser.getCustNum()),marketingSyncUser);
            }
        }
        //region 拼接客服接口参数
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        List<ConversionData> conversionDataList = new ArrayList<>();
        for (TransferUserVO transfer : transfers) {
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
            if(marketingSyncUser!=null){
                data.setPhone(StringUtils.isBlank(marketingSyncUser.getFailType())
                        ?BrCipherMaker.getInstance().decode(marketingSyncUser.getCell())
                        :marketingSyncUser.getCell());
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
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO,requestId);
        if(String.valueOf("9999").equals(transferRobotOutboundVO.getCode())){
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
    private void encodeMapping(MarketingPreUserDetailDTO user,String type, Integer isCheck) {
        String content = "";
        switch (type){
            case "cell":
                content = StringUtils.isBlank(user.getCell())?"":user.getCell();
                break;
            case "id":
                content = StringUtils.isBlank(user.getId())?"":user.getId();
                break;
            case "name":
                content = StringUtils.isBlank(user.getName())?"":user.getName();
                break;
        }
        if (decodeClient.isMd5(content)) {
            //cell md5
            content = decodeClient.query(content, type, "md5", "");
            if (StringUtils.isBlank(content)&&"cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (content.length() == 64) {
            //cell sha256
            content = decodeClient.query(content, type, "sha", "");
            if (StringUtils.isBlank(content)&&"cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        }
        //明文规则校验
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotBlank(content)&&"cell".equals(type)) {
            if (!userValidator.validatePhone(content)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setCell(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content)&&"id".equals(type)) {
            if (!userValidator.validateId(content)) {
                user.setId(content);
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setId(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content)&&"name".equals(type)) {
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
}
