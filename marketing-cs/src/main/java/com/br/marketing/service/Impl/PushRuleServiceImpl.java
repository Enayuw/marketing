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

    final static SimpleDateFormat yyyyMMddHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
                if (log.isWarnEnabled()) {
                    log.warn("人员信息：cusnum:{};batchnumber:{}", marketingHistory.getCusNum(),
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
                pushMarketingUserDetailVariablesDTO.setUpdate("");
                if (first.isPresent()) {
                    pushMarketingUserDetailVariablesDTO.setScore(String.valueOf(first.get().getScore()));
                }
                TaskExtendInfoVO taskExtendInfoVO = hsTaskExtend.get(Long.valueOf(marketingHistory.getFileId()));
                if(taskExtendInfoVO !=null){
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
            if (ex.getMessage().contains("not match")) {
                throw new ParamValidErrorException("请核实下是否jsonData过长，jsonData解析异常", ex);
            } else {
                throw new ParamValidErrorException("jsonData解析异常", ex);
            }
        }
        if (log.isInfoEnabled()) {
            log.info("反序列化耗时:{}", (System.currentTimeMillis() - l1));
        }
        if (!StringUtils.isNotBlank(dto.getApiCode())) {
            throw new ParamValidErrorException("apiCode必传");
        }
        if (dto.getJsonData() == null) {
            throw new ParamValidErrorException("jsonData必传");
        }
        if (!StringUtils.isNotBlank(dto.getJsonData().getTaskId())) {
            throw new ParamValidErrorException("taskid必传");
        }
        if (!StringUtils.isNotBlank(dto.getJsonData().getRequestId())) {
            throw new ParamValidErrorException("requestId必传");
        }
        int size = dto.getJsonData().getDataItems().size();
        if (size > 2000) {
            throw new ParamValidErrorException("传输的数据不要超过2000条");
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
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次内有重复的requestId");
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
        ArrayList<Callable<Result>> list = new ArrayList<>();
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
            Integer finalIsCheck = isCheck;
            list.add(() -> {
                if (!StringUtils.isNotBlank(marketingPreUserDetailDTO.getCustNum())) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("无客户编号");
                }
                if (!StringUtils.isNotBlank(marketingPreUserDetailDTO.getGroupType())
                        || !StringUtils.isNotBlank(marketingPreUserDetailDTO.getCell())) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(marketingPreUserDetailDTO.getCustNum()
                            .concat(":无grouptype或cell"));
                }
                //解密、规则校验
                String cell = marketingPreUserDetailDTO.getCell();
                encodeMapping(marketingPreUserDetailDTO, finalIsCheck);
                String date = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                String appletDate = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd");
                String dataStr = String.format("( '%s','%s','%s','%s','%s' ,'%s' ,'%s' ,'%s' ,'%s' ,'%s','%s','%s','%s',%s)"
                        , marketingSyncInfo.getApiCode(), marketingSyncInfo.getCusBatch()
                        , marketingSyncInfo.getRequestBatch(), marketingPreUserDetailDTO.getCustNum()
                        , marketingPreUserDetailDTO.getCell(), marketingPreUserDetailDTO.getGroupType()
                        , marketingPreUserDetailDTO.getRegisterDate()
                        , marketingPreUserDetailDTO.getReserveField1()
                        , marketingPreUserDetailDTO.getReserveField2()
                        , date, date, appletDate,
                        marketingPreUserDetailDTO.getFailType() == null ? "" : marketingPreUserDetailDTO.getFailType(),
                        marketingPreUserDetailDTO.getStatus());
                try {
                    marketingUserMapper.insertBatchMarketingPreUserByDatas(marketingSyncInfo.getApiCode(), dataStr);
                } catch (Exception ex) {
                    if (ex.getMessage().contains("IDX_taskId_custNum")) {
                        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(marketingPreUserDetailDTO.getCustNum()
                                .concat("重复客户编号"));
                    } else if (ex.getMessage().contains("uk_taskId_cell")) {
                        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(cell.concat("重复电话"));
                    } else {
                        log.error(ex.getMessage(),ex);
                        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(cell.concat("入库异常"));
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        StringBuilder errorBuild = new StringBuilder();
        Integer errorSize = 0;
        List<Future<Result>> futures = null;
        try {
            futures = currentDbPoolExecutor.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        if (futures != null && !futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Result result = futures.get(i).get();
                    if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        errorSize++;
                        errorBuild.append(result.getMessage().concat(","));
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
            errorInfo.setErrorInfo(errorBuild.toString());
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(new Date());
            errorInfo.setErrorInfo(errorBuild.toString());
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
        JSONObject jsonObject = JSON.parseObject(jsonData);
        String requestId = jsonObject.getString("requestId");
        if(StringUtils.isBlank(requestId)){
            return new Result().setCode(ResultCode.FAIL.getValue())
                    .setMessage("requestId不能为空");
        }
        marketingSyncInfoMapper.createMarketingTransferTable("b_marketing_transfer_".concat(apiCode));
        Integer hasData = marketingSyncInfoMapper.selectTransfersByRequestId(apiCode, requestId);
        if(hasData>0){
            return new Result().setCode(ResultCode.FAIL.getValue())
                    .setMessage("requestId数据已存在");
        }

        List<TransferUserVO> transfers = JSON.parseObject(jsonObject.getString("dataItems"), new TypeReference<List<TransferUserVO>>() {
        }.getType());
        if(transfers.size()>100){
            return new Result().setCode(ResultCode.FAIL.getValue())
                    .setMessage("最多传输100条数据");
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder sqlByTaskAndCustNum = new StringBuilder();
        String nowDate = yyyyMMddHMS.format(new Date());
        for (int i = 0; i < transfers.size(); i++) {

            TransferUserVO transferUserVO = transfers.get(i);
            //region 校验参数
            if(StringUtils.isBlank(transferUserVO.getTaskId())){
                return new Result().setCode(ResultCode.FAIL.getValue())
                        .setMessage("该批次数据含有taskId为空数据");
            }

            if(StringUtils.isBlank(transferUserVO.getCustNum())){
                return new Result().setCode(ResultCode.FAIL.getValue())
                        .setMessage("该批次数据含有custNum为空数据");
            }
            //ednregion

            //region 拼接sql

            if(i==0){
                sql.append("insert into b_marketing_transfer_").append(apiCode)
                    .append(" (request_id,task_id,cust_num,transform_time,create_time,group_type) values");
                sqlByTaskAndCustNum.append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                        ,transferUserVO.getTaskId()
                        ,transferUserVO.getCustNum()));
            }
            if(i>0){
                sql.append(",");
                sqlByTaskAndCustNum.append(" or ")
                        .append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                                ,transferUserVO.getTaskId()
                                ,transferUserVO.getCustNum()));
            }
            Date parse = null;
            try {
                if(StringUtils.isNotBlank(transferUserVO.getTransformTime())) {
                    parse = yyyyMMddHMS.parse(transferUserVO.getTransformTime());
                }
            } catch (Exception e) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("转化时间格式错误");
            }
            sql.append("('").append(requestId).append("'")
                    .append(",'").append(transferUserVO.getTaskId()).append("'")
                    .append(",'").append(transferUserVO.getCustNum()).append("'")
                    .append(",").append(parse==null?"null":("'".concat(yyyyMMddHMS.format(parse)).concat("'")))
                    .append(",'").append(nowDate).append("'")
                    .append(",'").append(transferUserVO.getGroupType()).append("')");
            //endregion
        }
        if(StringUtils.isNotBlank(sql.toString())){
            marketingSyncInfoMapper.insertBatchTransfer(sql.toString());
        }
        HashMap<String,MarketingSyncUser> hmPreUser = new HashMap();
        if(StringUtils.isNotBlank(sqlByTaskAndCustNum.toString())){
            List<MarketingSyncUser> preUserByTaskAndCust = marketingSyncInfoMapper.getPreUserByTaskAndCust(apiCode, sqlByTaskAndCustNum.toString());
            for (MarketingSyncUser marketingSyncUser : preUserByTaskAndCust) {
                hmPreUser.put(marketingSyncUser.getCusBatch()
                        .concat("_")
                        .concat(marketingSyncUser.getCustNum()),marketingSyncUser);
            }
        }
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
            data.setBusinessType("");
            MarketingSyncUser marketingSyncUser = hmPreUser.get(transfer.getTaskId()
                    .concat("_")
                    .concat(transfer.getCustNum()));
            if(marketingSyncUser!=null){
                data.setPhone(StringUtils.isBlank(marketingSyncUser.getFailType())
                        ?BrCipherMaker.getInstance().decode(marketingSyncUser.getCell())
                        :marketingSyncUser.getCell());
            }
            conversionDataList.add(data);
        }
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setConversionData(conversionDataList);
        jsonDataDTO.setMethod("conversionData");
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(apiCode);
        robotOutboundDTO.setJsonData(jsonDataDTO);
        //todo 调用客服接口
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        System.out.println(transferRobotOutboundVO.toString());
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 解密、规则校验
     *
     * @param user
     * @param isCheck
     * @return
     */
    private void encodeMapping(MarketingPreUserDetailDTO user, Integer isCheck) {
        user.setStatus(MonitorTypeEnum.STATUS_1.getTypeCode());
        String cell = user.getCell();
        if (decodeClient.isMd5(cell)) {
            //cell md5
            cell = decodeClient.query(cell, "cell", "md5", "");
            if (StringUtils.isBlank(cell)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (cell.length() == 64) {
            //cell sha256
            cell = decodeClient.query(cell, "cell", "sha", "");
            if (StringUtils.isBlank(cell)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        }
        //明文规则校验
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotBlank(cell)) {
            if (!userValidator.validatePhone(cell)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setCell(BrCipherMaker.getInstance().encode(cell));
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
            return marketingPreUserSyncDetailVOResult.setCode(ResultCode.FAIL.getValue()).setMessage("该批次信息不存在");
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
                vo.setErrorInfo(errorInfo.getErrorInfo());
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
}