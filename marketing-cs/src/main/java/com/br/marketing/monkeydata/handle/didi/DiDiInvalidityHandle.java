package com.br.marketing.monkeydata.handle.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.didi.DiDiClient;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiFailUserVO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.DidiData;
import com.br.marketing.entity.DidiDataExample;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.didi.DiDiAllowCondition;
import com.br.marketing.monkeydata.entity.didi.DiDiFailedCondition;
import com.br.marketing.monkeydata.entity.didi.DiDiFailedProcessData;
import com.br.marketing.monkeydata.entity.didi.DiDiProcessData;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.vo.DiDiAllowReqDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DiDiInvalidityHandle extends IMonkeyDataHandle<DidiData, DiDiFailedProcessData, DiDiFailedCondition> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    IMarketingDataValidService iMarketingDataValidService;

    @Resource
    DidiDataMapper didiDataMapper;

    @Autowired
    MethodRetryHandlerService methodRetryHandlerService;

    @Autowired
    PushInfoService pushInfoService;

    @Autowired
    DiDiClient diDiClient;

    @Override
    public Boolean isThread() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getBoolean("isThread")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getBoolean("isThread");
        }
        return super.isThread();
    }


    @Override
    public Boolean isPause() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getBoolean("isPause")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getBoolean("isPause");
        }
        return super.isPause();
    }

    @Override
    public Integer getThread() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getInteger("threadNum")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiInvalidity").getInteger("threadNum");
        }
        return super.getThread();
    }


    @Override
    public Result<IterationResult<DidiData, DiDiFailedCondition>> getInputData(DiDiFailedCondition condition) {
        DidiDataExample example = new DidiDataExample();
        example.setOrderByClause(" id asc limit ".concat(condition.getPageSize().toString()));
        DidiDataExample.Criteria criteria = example.createCriteria();
        if(condition.getDataId() !=null){
            criteria.andIdGreaterThan(condition.getDataId());
        }
        criteria.andLocalIdEqualTo(condition.getLocalId())
                .andStatusEqualTo(Constants.DATA_VALID)
                .andPushStatusEqualTo(2)
                .andIsMarketingEqualTo(1)
                .andPushDateEqualTo(condition.getDay());

        List<DidiData> didiData = didiDataMapper.selectByExample(example);
        if(didiData.size()>0){
            IterationResult<DidiData, DiDiFailedCondition> didiRes = new IterationResult<>();
            didiRes.setInputDataList(didiData);
            DiDiFailedCondition diDiFailedCondition = new DiDiFailedCondition();
            diDiFailedCondition.setLocalId(condition.getLocalId());
            diDiFailedCondition.setDataId(didiData.get(didiData.size()-1).getId());
            diDiFailedCondition.setPageSize(condition.getPageSize());
            diDiFailedCondition.setDay(condition.getDay());
            didiRes.setInDatacondition(diDiFailedCondition);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(didiRes);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result<List<DiDiFailedProcessData>> processData(List<DidiData> inList) throws Exception {
        DidiData didiData = inList.get(0);
        Long localId = didiData.getLocalId();
        String apiCode = didiData.getApiCode();

        List<DiDiProcessData> diDiProcessDatas = new ArrayList<>();
        List<String> cells = inList.stream().map(t -> t.getCell()).collect(Collectors.toList());
        //todo 获取当天触达的数据
        Map<String,String> pushData = new HashMap<>();
        for (DidiData data : inList) {
            String pd = pushData.get(data.getCell());
            if(pd==null){
                //调用营销失败接口
                DiDiReqVO diDiReqVO = new DiDiReqVO();
                diDiReqVO.setCustMobileMd5(data.getCell());
                Result<DiDiFailUserVO> diDiFailUserVOResult = diDiClient.failUser(diDiReqVO);
                //todo
                // 1、需要根据营销失败接口的状态来更新数据
                // 2、更新
            }

        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(diDiProcessDatas);
    }

    @Override
    public Result resultAction(List<DiDiFailedProcessData> outputDataList) {
        Boolean errorMark =Boolean.FALSE;

        return new Result().setCode(errorMark?ResultCode.FAIL.getValue():ResultCode.SUCCESS.getValue());
    }
}
