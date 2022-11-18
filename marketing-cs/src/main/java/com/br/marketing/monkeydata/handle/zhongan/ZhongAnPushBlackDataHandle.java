package com.br.marketing.monkeydata.handle.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZkReqDTO;
import com.br.marketing.client.zhongan.output.ZkReponseVO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.monkeydata.handle.commonhandle.InputCommonHandle;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.CustomerBlackListHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class ZhongAnPushBlackDataHandle extends IMonkeyDataHandle<MarketingSyncUser, String, MarketingSyncCondition> {
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

    @Override
    public Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> getInputData(MarketingSyncCondition inputData) {
        //暂停开关
        if (Boolean.FALSE.equals(marketingCommonConfig.getZhongAnPushBlackDataSwitch())) {
            log.warn("众安推送黑名单任务暂停");
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return inputCommonHandle.getMarketingSyncUserByPage(inputData);
    }

    @Override
    public Result customizedAction(MarketingSyncCondition inputData) {
        Result res = new Result();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(2, 2, 2);
        for (; ; ) {
            Result<IterationResult<MarketingSyncUser, MarketingSyncCondition>> inputRes = getInputData(inputData);
            if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                break;
            }
            List<String> inputDataList = inputRes.getData().getInputDataList().stream().map(MarketingSyncUser::getCell).collect(Collectors.toList());
            inputDataList.add(inputData.getApiCode());
            pool.submit(() -> {
                Result result = resultAction(inputDataList);
                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    res.setCode(ResultCode.FAIL.getValue());
                    log.warn(res.getMessage());
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


    @Override
    public Result<List<String>> processData(List<MarketingSyncUser> inList) {
        return null;
    }


    @Override
    public Result resultAction(List<String> dataList) {
        //获取到apiCode
        String apiCode = dataList.get(dataList.size() - 1);
        dataList.remove(dataList.size() - 1);
        List<String> retryDataList = new ArrayList<>();
        List<BlackDetailDTO> blackDetailDTOList = new ArrayList<>();
        dataList.forEach(cell -> {
            String decodeCell = BrCipherMaker.getInstance().decode(cell);
            ZkReqDTO xd = new ZkReqDTO();
            xd.setCustMobileMd5(Md5OfZanUtils.getMD5(decodeCell));
            xd.setChannelCode(ZhongAnClient.XdChannelCode);
            Result<ZkReponseVO> result = zhongAnClient.zkXd(xd);
            //需要重试加入重试表
            if (result.getCode().equals(ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
                retryDataList.add(cell);
            }
            if (result.getData() != null && Boolean.FALSE.equals(result.getData().getAccess()) && "SUCCESS".equals(result.getData().getStatus())) {
                BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
                blackDetailDTO.setExpireDate(LocalDate.now() + " 23:59:59");
                blackDetailDTO.setPhone(decodeCell);
                blackDetailDTOList.add(blackDetailDTO);
            }
        });
        if (!CollectionUtils.isEmpty(retryDataList)) {
            retryDataList.add(apiCode);
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
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        customerBlackListHandler.call(blackDetailDTOList, context);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
