package com.br.marketing.monkeydata.handle.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZkReqDTO;
import com.br.marketing.client.zhongan.output.ZkReponseVO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.monkeydata.handle.commonhandle.InputCommonHandle;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.CustomerBlackListHandler;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhen.li1
 * @date 2022/11/16
 * @desc:众安推送黑名单至客服处理器
 */
@Service
@Slf4j
public class ZhongAnPushBlackDataHandle extends IMonkeyDataHandle<MarketingSyncUser, MarketingSyncUser, MarketingSyncCondition> {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    ZhongAnClient zhongAnClient;
    @Autowired
    private InputCommonHandle inputCommonHandle;

    @Autowired
    private CustomerBlackListHandler customerBlackListHandler;

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    ZhonganMarketingBanMapper zhonganMarketingBanMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Autowired
    private  MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private IPeriodOfValidityService iPeriodOfValidityService;


    @Override
    public Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> getInputData(MarketingSyncCondition inputData) {
        //暂停开关
        if (Boolean.FALSE.equals(marketingCommonConfig.getZhongAnPushBlackDataSwitch())) {
            log.warn("众安推送黑名单任务暂停");
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        List<String> executeDateList = inputData.getExecuteDateList();
        for (Iterator<String> iterator = executeDateList.iterator(); iterator.hasNext(); ) {
            String executeDate = iterator.next();
            Long minId = inputData.getMinId();
            List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByAppletDateAndUserType(inputData.getApiCode(), executeDate, minId,inputData.getUserType());
            if (marketingSyncUserList.size() <= 0) {
                //该日期执行完成，开始执行下一个日期
                iterator.remove();
                inputData.setMinId(null);
                continue;
            }
            minId = marketingSyncUserList.get(marketingSyncUserList.size() - 1).getId() + 1;
            IterationResult<MarketingSyncUser, MarketingSyncCondition> content = new IterationResult<>();
            inputData.setMinId(minId);
            content.setInDatacondition(inputData);
            content.setInputDataList(marketingSyncUserList);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result customizedAction(MarketingSyncCondition inputData) {
        Result res = new Result();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(200, 200, 200);
        String date = LocalDate.now().toString();
        List<String> userTypes = marketingCommonConfig.getZhongAnZkUserType();
        //根据userType循环处理
        userTypes.forEach(usertype -> {
          /*  String appletDateStart, appletDateEnd;
            // apicode+userType有效期配置
            List<MarketingDataValidConfig> configList = findConfigByUserType(inputData.getApiCode(), usertype);
            if (CollectionUtils.isEmpty(configList) || (configList.size() > 1)) {
                return;
            }
            MarketingDataValidConfig dataValidConfig = configList.get(0);
            if (StringUtils.isNotEmpty(dataValidConfig.getValidDays())) {
                PeriodOfValidityBO builder = iPeriodOfValidityService.getPeriodOfValidityRange(dataValidConfig.getValidDays().replace("+","-")
                        , new Date()).addDateString().builder();
                appletDateStart = builder.getBeginDateStr();
                appletDateEnd = builder.getEnDateStr();
            } else {
                appletDateStart = dataValidConfig.getValidStartDate();
                appletDateEnd = dataValidConfig.getValidEndDate();
            }*/
            List<MarketingDataValidConfig> configList = findConfigByBetweenDate(inputData.getApiCode(), date,usertype);
            if (CollectionUtils.isEmpty(configList)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_SERVICEERROR.getCode(),
                        "众安撞库未配置有效期，请检查"));
                return;
            }
            List<String> appletDateList = configList.stream().map(marketingDataValidConfig -> marketingDataValidConfig.getAppletDate()).collect(Collectors.toList());
            inputData.setExecuteDateList(appletDateList);
            inputData.setUserType(usertype);
            for (; ; ) {
                if (StringUtils.isNotEmpty(marketingCommonConfig.getZhongAnPushBlackThreadNum().get(usertype))) {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                        pool,
                        Integer.parseInt(marketingCommonConfig.getZhongAnPushBlackThreadNum().get(usertype))
                    );
                    log.warn("众安推送黑名单线程调整，userType={},corePoolSize={},maxPoolSize={}", usertype, pool.getCorePoolSize(), pool.getMaximumPoolSize());
                }
                Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> inputRes = getInputData(inputData);
                if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                    break;
                }
                List<MarketingSyncUser> inputDataList = inputRes.getData().getInputDataList();
                inputDataList.add(null);
//            List<String> inputDataList = inputRes.getData().getInputDataList().stream().map(MarketingSyncUser::getCell).collect(Collectors.toList());
//            inputDataList.add(inputData.getApiCode());
                pool.submit(() -> {
                    Result result = resultAction(inputDataList);
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        res.setCode(ResultCode.FAIL.getValue());
                        log.warn(res.getMessage());
                    }
                });
            }
        });
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_SERVICEERROR.getCode(),
                    ex.getMessage()), ex);
        }
        return res;
    }

    private List<MarketingDataValidConfig> findConfigByUserType(String apiCode,String userType) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1).andUserTypeEqualTo(userType);
        return marketingDataValidConfigMapper.selectByExample(example);
    }


    @Override
    public Result<List<MarketingSyncUser>> processData(List<MarketingSyncUser> inList) {
        return null;
    }


    @Override
    public Result resultAction(List<MarketingSyncUser> dataList) {
        //获取到apiCode
        //重试参数apicode-1
        //重试方法 这里反序列化过来的不是 MarketingSyncUser类型
        if (!(dataList.get(0) instanceof MarketingSyncUser)) {
            List<MarketingSyncUser> list = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                if(dataList.get(i) !=null){
                    list.add(JSON.parseObject(JSON.toJSONString(dataList.get(i)),MarketingSyncUser.class));
                }
            }
            dataList = list;
        }
        MarketingSyncUser retryMark = dataList.get(dataList.size() - 1);
        String apiCode = dataList.get(0).getApiCode();
        if (retryMark == null) {
            dataList.remove(dataList.size() - 1);
        }
        Map<String,String> channelCodes = marketingCommonConfig.getZhongAnZkUserTypeChannelCode();
        List<MarketingSyncUser> retryDataList = new ArrayList<>();
        List<BlackDetailDTO> blackDetailDTOList = new ArrayList<>();
        String key = RedisKeyConstant.ZHONGAN_ZK_CELL_TODAY.concat(apiCode).concat(":new:").concat(LocalDate.now().toString());
        dataList.forEach(t -> {
            String redisKey = key.concat(":").concat(t.getUserType()).concat(":").concat(t.getCell());
            //添加当日userType+cell到redis中，过期时间第二日凌晨
            try {
                Boolean setnx = redisChgService.setnx(redisKey, UUID.randomUUID().toString(), DateHelper.getRemainSecondsOneDay(new Date()));
                //重复数据，跳过继续循环
                if (!setnx) {
                    return;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_SERVICEERROR.getCode(),
                        "众安撞库cell存入redis失败，key=" + redisKey), e);
            }
            String decodeCell = BrCipherMaker.getInstance().decode(t.getCell());
            ZkReqDTO xd = new ZkReqDTO();
            xd.setCustMobileMd5(Md5OfZanUtils.getMD5(decodeCell));
            xd.setChannelCode(channelCodes.get(t.getUserType()));
            Result<ZkReponseVO> result = zhongAnClient.zkXd(xd);
            //需要重试加入重试表
            if (result.getCode().equals(ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
                //需要重试的删除key
                try {
                    if (redisChgService.exists(redisKey)) {
                        redisChgService.del(redisKey);
                    }
                }catch(Exception e){
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_SERVICEERROR.getCode(),
                            "众安撞库cell存入redis失败，key=" + redisKey), e);
                }
                retryDataList.add(t);
            }
            if (result.getData() != null
                    && Boolean.FALSE.equals(result.getData().getAccess())
                    && "SUCCESS".equals(result.getData().getStatus())) {
                Date date = new Date();
                BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
                blackDetailDTO.setExpireDate(LocalDate.now() + " 23:59:59");
                blackDetailDTO.setPhone(decodeCell);
                blackDetailDTOList.add(blackDetailDTO);
                ZhonganMarketingBan zhonganMarketingBan = new ZhonganMarketingBan();
                zhonganMarketingBan.setApiCode(t.getApiCode());
                zhonganMarketingBan.setCustNum(t.getCustNum());
                zhonganMarketingBan.setCell(t.getCell());
                zhonganMarketingBan.setUserType(t.getUserType());
                zhonganMarketingBan.setTaskId(t.getCusBatch());
                zhonganMarketingBan.setRequestId(t.getRequestBatch());
                zhonganMarketingBan.setAppletDate(t.getAppletDate());
                zhonganMarketingBan.setZkDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                zhonganMarketingBan.setInitId(t.getId().toString());
                zhonganMarketingBan.setCreateTime(date);
                zhonganMarketingBan.setUpdateTime(date);
                zhonganMarketingBanMapper.insertSelective(zhonganMarketingBan);
                blackDetailDTO.setDataId(zhonganMarketingBan.getId().toString());
            }
        });
        if (!CollectionUtils.isEmpty(retryDataList)) {
            //重试调用，不在重复插入重试表
            if (retryMark != null) {
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            RetryMainLog retryMainLog = new RetryMainLog();
            retryMainLog.setRetryType(1);
            retryMainLog.setRetryParam(JSON.toJSONString(retryDataList));
            retryMainLog.setRetryParamType(List.class.getName());
            retryMainLog.setRetryService(ZhongAnPushBlackDataHandle.class.getName());
            retryMainLog.setServiceType(2);
            retryMainLog.setRetryNum(0);
            retryMainLog.setRetryStatus(1);
            retryMainLog.setCreateTime(new Date());
            retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
            retryMainLog.setRetryMethod("resultAction");
            retryMainLog.setRetryMaxNum(3);
            retryMainLogMapper.insertSelective(retryMainLog);
        }
        customerBlackListHandler.xieChengCall(blackDetailDTOList, apiCode);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }


    /**
     * apicode有效期配置
     */
    private List<MarketingDataValidConfig> findConfigByBetweenDate(String apiCode, String date,String userType) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType).andValidStartDateLessThanOrEqualTo(date)
                .andValidEndDateGreaterThanOrEqualTo(date).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }

}
