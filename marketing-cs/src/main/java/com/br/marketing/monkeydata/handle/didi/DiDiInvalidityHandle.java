package com.br.marketing.monkeydata.handle.didi;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.DidiData;
import com.br.marketing.entity.DidiDataExample;
import com.br.marketing.mapper.DidiCallRecordMapper;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.didi.DiDiFailedCondition;
import com.br.marketing.monkeydata.entity.didi.DiDiFailedProcessData;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.service.Impl.DiDiServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    DiDiServiceImpl diDiService;

    @Resource
    DidiCallRecordMapper didiCallRecordMapper;

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
        String pushDate = didiData.getPushDate();
        Integer intDate = Integer.valueOf(pushDate.replace("-", ""));
        List<DiDiFailedProcessData> diDiProcessDatas = new ArrayList<>();
        Set<String> cellSets = inList.stream().map(t -> t.getCell()).collect(Collectors.toSet());
        Set<String> pushDatas = didiCallRecordMapper.getCustNumByStatusIs1AndCellSet(apiCode, intDate, cellSets);
        Integer bad = 0;
        for (DidiData data : inList) {
            boolean isUpload = pushDatas.contains(data.getCell());
            if(!isUpload){
                try {
                    diDiService.invalidData(data);
                }catch (Exception ex){
                    log.error(ex.getMessage());
                    bad++;
                }
            }
        }
        diDiProcessDatas.add(new DiDiFailedProcessData(bad));
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(diDiProcessDatas);
    }

    @Override
    public Result resultAction(List<DiDiFailedProcessData> outputDataList) {
        Boolean errorMark =Boolean.FALSE;
        DiDiFailedProcessData diDiFailedProcessData = outputDataList.get(0);
        if(diDiFailedProcessData != null){
            if(diDiFailedProcessData.getBad()>0){
                errorMark =Boolean.TRUE;
            }
        }
        return new Result().setCode(errorMark?ResultCode.FAIL.getValue():ResultCode.SUCCESS.getValue());
    }
}
