package com.br.marketing.monkeydata.handle.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.didi.DiDiClient;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiJMassResponseTO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhen.li1
 * @date 2023/04/26
 * @desc: 滴滴联合建模数据落转化表处理器
 */
@Service
@Slf4j
public class DiDiModelingDataHandle extends IMonkeyDataHandle<MarketingSyncUser, MarketingSyncUser, MarketingSyncCondition> {

    @Autowired
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private RetryMainLogMapper retryMainLogMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Autowired
    private DiDiClient diDiClient;

    @Autowired
    private PushRuleService pushRuleService;

    @Override
    public Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> getInputData(MarketingSyncCondition inputData) {
        //暂停开关
        if (Boolean.FALSE.equals(marketingCommonConfig.getDidiModelingDataSwitch())) {
            log.warn("滴滴联合建模任务暂停");
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        List<String> executeDateList = inputData.getExecuteDateList();
        for (Iterator<String> iterator = executeDateList.iterator(); iterator.hasNext(); ) {
            String executeDate = iterator.next();
            Long minId = inputData.getMinId();
            List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getCellByAppletDateAndUserType(inputData.getApiCode(), executeDate, minId, "1");
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
    public Result<List<MarketingSyncUser>> processData(List<MarketingSyncUser> inList) throws Exception {
        return null;
    }


    @Override
    public Result customizedAction(MarketingSyncCondition inputData) {
        Result res = new Result();
        String date = LocalDate.now().minusDays(1).toString();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(50, 50, 50);
        List<MarketingDataValidConfig> configList = findConfigByBetweenDate(inputData.getApiCode(), date);
        List<String> appletDateList = configList.stream().map(marketingDataValidConfig -> marketingDataValidConfig.getAppletDate()).collect(Collectors.toList());
        inputData.setExecuteDateList(appletDateList);
        Set<String> CellSets = new HashSet<>();
        for (; ; ) {
            if (StringUtils.isNotEmpty(marketingCommonConfig.getDidiModelingThreadNum())) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                    pool,
                    Integer.parseInt(marketingCommonConfig.getDidiModelingThreadNum())
                );
                log.warn("滴滴联合建模接口线程调整，corePoolSize={},maxPoolSize={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
            }
            Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> inputRes = getInputData(inputData);
            if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                break;
            }
            List<MarketingSyncUser> inputDataList = inputRes.getData().getInputDataList();
            //数据去重
            inputDataList.removeIf(marketingSyncUser -> !CellSets.add(marketingSyncUser.getCell()));
            inputDataList.add(null);
            pool.execute(() -> {
                try {
                    Result result = resultAction(inputDataList);
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        res.setCode(ResultCode.FAIL.getValue());
                        log.warn(res.getMessage());
                    }
                } catch (Exception ex) {
                    log.error("滴滴联合建模调用异常", ex);
                }
            });
        }
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return res;
    }


    /**
     * apicode有效期配置
     * userType = 1
     */
    private List<MarketingDataValidConfig> findConfigByBetweenDate(String apiCode, String date) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo("1").andValidStartDateLessThanOrEqualTo(date)
                .andValidEndDateGreaterThanOrEqualTo(date).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }

    @Override
    public Result resultAction(List<MarketingSyncUser> dataList) {

        //获取到apiCode
        //重试方法 这里反序列化过来的不是 MarketingSyncUser类型
        if (!(dataList.get(0) instanceof MarketingSyncUser)) {
            List<MarketingSyncUser> list = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i) != null) {
                    list.add(JSON.parseObject(JSON.toJSONString(dataList.get(i)), MarketingSyncUser.class));
                }
            }
            dataList = list;
        }
        MarketingSyncUser retryMark = dataList.get(dataList.size() - 1);
        String apiCode = dataList.get(0).getApiCode();
        if (retryMark == null) {
            dataList.remove(dataList.size() - 1);
        }
        List<MarketingSyncUser> retryDataList = new ArrayList<>();
        List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
        dataList.forEach(t -> {
            String decodeCell = BrCipherMaker.getInstance().decode(t.getCell());
            DiDiReqVO diDiReqVO = new DiDiReqVO();
            diDiReqVO.setCustMobileMd5(Md5OfZanUtils.getMD5(decodeCell));
            diDiReqVO.setMediaName(marketingCommonConfig.getDidiModelingMediaNameMap().get("oldMediaName"));
            Result<DiDiJMassResponseTO> result = diDiClient.pushJMASS(diDiReqVO);
            //需要重试加入重试表
            if (result.getCode().equals(ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
                retryDataList.add(t);
            }
            JSONObject json = JSON.parseObject(t.getReserveField1());
            if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
                String data = result.getData().getData();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("data", data);
                jsonObject.put("extend", json.getString("extend"));
                transferDataItemDTO.setCustNum(t.getCustNum());
                transferDataItemDTO.setUserType("1");
                transferDataItemDTO.setReserveField1(jsonObject.toString());
                transferDataItemDTO.setApplyDt(t.getAppletDate());
                transferDataItemDTOS.add(transferDataItemDTO);
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
            retryMainLog.setRetryService(DiDiModelingDataHandle.class.getName());
            retryMainLog.setServiceType(2);
            retryMainLog.setRetryNum(0);
            retryMainLog.setRetryStatus(1);
            retryMainLog.setCreateTime(new Date());
            retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
            retryMainLog.setRetryMethod("resultAction");
            retryMainLog.setRetryMaxNum(3);
            retryMainLogMapper.insertSelective(retryMainLog);
        }
        //落转化表
        if (!CollectionUtils.isEmpty(transferDataItemDTOS)) {
            TransferDataDTO transferDataDTO = new TransferDataDTO();
            transferDataDTO.setDataItems(transferDataItemDTOS);
            transferDataDTO.setRequestId(apiCode.concat("_").concat(LocalDate.now().toString()).concat("_").concat(UUID.randomUUID().toString()));
            RuntimeDataContext.initData();
            pushRuleService.insertTransferData(dataList.get(0).getApiCode(), JSON.toJSONString(transferDataDTO));
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
