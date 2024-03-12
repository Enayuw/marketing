package com.br.marketing.monkeydata.handle.didi;

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.monkeydata.entity.didi.DiDiAllowCondition;
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
public class DiDiAllowHandle  extends IMonkeyDataHandle<DidiData, DiDiProcessData, DiDiAllowCondition> {

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

    @Override
    public Boolean isThread() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getBoolean("isThread")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getBoolean("isThread");
        }
        return super.isThread();
    }


    @Override
    public Boolean isPause() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getBoolean("isPause")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getBoolean("isPause");
        }
        return super.isPause();
    }

    @Override
    public Integer getThread() {
        if(marketingCommonConfig.getCustomerJobConfig()!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow")!=null
                && marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getInteger("threadNum")!=null){
            return marketingCommonConfig.getCustomerJobConfig().get("DiDiAllow").getInteger("threadNum");
        }
        return super.getThread();
    }


    @Override
    public Result<IterationResult<DidiData, DiDiAllowCondition>> getInputData(DiDiAllowCondition condition) {
        DidiDataExample example = new DidiDataExample();
        example.setOrderByClause(" id asc limit ".concat(condition.getPageSize().toString()));
        DidiDataExample.Criteria criteria = example.createCriteria();
        if(condition.getDataId() !=null){
            criteria.andIdGreaterThan(condition.getDataId());
        }
        criteria.andLocalIdEqualTo(condition.getLocalId())
                .andStatusEqualTo(Constants.DATA_VALID)
                .andPushStatusIn(Arrays.asList(1,3));

        List<DidiData> didiData = didiDataMapper.selectByExample(example);
        if(didiData.size()>0){
            IterationResult<DidiData, DiDiAllowCondition> didiRes = new IterationResult<>();
            didiRes.setInputDataList(didiData);
            DiDiAllowCondition diDiAllowCondition = new DiDiAllowCondition();
            diDiAllowCondition.setLocalId(condition.getLocalId());
            diDiAllowCondition.setDataId(didiData.get(didiData.size()-1).getId());
            diDiAllowCondition.setPageSize(condition.getPageSize());
            didiRes.setInDatacondition(diDiAllowCondition);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(didiRes);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result<List<DiDiProcessData>> processData(List<DidiData> inList) throws Exception {
        DidiData didiData = inList.get(0);
        String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> cells = inList.stream().map(t -> t.getCell()).collect(Collectors.toList());
        List<DidiData> firstDidiByFiletikv_ = didiDataMapper.getFirstDidiByFiletikv_(didiData.getLocalId(),cells);
        Map<String,DidiData> firstDiDi = firstDidiByFiletikv_.stream().collect(Collectors.toMap(DidiData::getCell
                , Function.identity()
                ,(t1,t2)-> {if(t1.getId()>t2.getId()) return t2; else return t1;}));
        Result<List<MarketingDataValidConfig>> dataValidConfigByType = iMarketingDataValidService.getDataValidConfigByType(didiData.getApiCode(), 1);
        List<String> uploadDates = new ArrayList<>();
        Map<String,DidiData> marketingDiDi = new HashMap<>();
        if(ResultCode.SUCCESS.getValue().equals(dataValidConfigByType.getCode())){
            List<MarketingDataValidConfig> validConfigs = dataValidConfigByType.getData();
            for (MarketingDataValidConfig validConfig : validConfigs) {
                if(!"1".equals(validConfig.getUserType())){
                    continue;
                }
                if(validConfig.getValidStartDate().compareTo(nowDay)<=0
                        && validConfig.getValidEndDate().compareTo(nowDay)>=0){
                    uploadDates.add(validConfig.getAppletDate());
                }
            }
        }
        if(uploadDates.size()>0){
            DidiDataExample didiDataExample = new DidiDataExample();
            didiDataExample.createCriteria()
                    .andApiCodeEqualTo(didiData.getApiCode())
                    .andCellIn(cells)
                    .andStatusEqualTo(1)
                    .andPushDateIn(uploadDates);
            List<DidiData> didiData1 = didiDataMapper.selectByExample(didiDataExample);
            marketingDiDi = didiData1.stream().collect(Collectors.toMap(DidiData::getCell
                    , Function.identity()
                    ,(t1,t2)-> {if(t1.getId()>t2.getId()) return t2; else return t1;}));
        }
        List<DiDiProcessData> diDiProcessDatas = new ArrayList<>();
        for (DidiData data : inList) {
            //文件内重复数据判断
            DidiData firstData = firstDiDi.get(data.getCell());
            if(firstData != null && !firstData.getId().equals(data.getId())){
                diDiProcessDatas.add(new DiDiProcessData(1,null,data));
                continue;
            }
            //有效期数据判断
            DidiData marketingData = marketingDiDi.get(data.getCell());
            if(marketingData!=null){
                diDiProcessDatas.add(new DiDiProcessData(2,marketingData.getPushDate(),data));
                continue;
            }
            //调用准入接口
            Result<Boolean> booleanResult = methodRetryHandlerService.didiAllow(new DiDiAllowReqDTO(data.getCell(), data.getId()),null);
            if(ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(booleanResult.getCode())){
                diDiProcessDatas.add(new DiDiProcessData(5,null,data));
                continue;
            }
            if(ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())){
                diDiProcessDatas.add(new DiDiProcessData(booleanResult.getData()?3:4,null,data));
                continue;
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(diDiProcessDatas);
    }

    @Override
    public Result resultAction(List<DiDiProcessData> outputDataList) {

        DiDiProcessData didi = outputDataList.get(0);
        String apiCode = didi.getApiCode();
        List<Long> moreIds = new ArrayList<>();
        StringBuilder validSql = new StringBuilder();
        validSql.append("update b_didi_data set status = 4,is_marketing = 1,");
        StringBuilder pushDateSql = new StringBuilder();
        pushDateSql.append("push_date = (case id ");
        StringBuilder pushDateWhereSql = new StringBuilder();
        pushDateWhereSql.append(" where id in ( ");
        Boolean validMark = Boolean.FALSE;
        Boolean errorMark = Boolean.FALSE;
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (DiDiProcessData diDiProcessData : outputDataList) {
            switch (diDiProcessData.getDataStatus()){
                case 1:
                    moreIds.add(diDiProcessData.getId());
                    break;
                case 2:
                    validMark= Boolean.TRUE;
                    pushDateSql.append(String.format(" when %d then '%s'",diDiProcessData.getId(),diDiProcessData.getPushDate()));
                    pushDateWhereSql.append(String.format(" %d,",diDiProcessData.getId()));
                    break;
                case 3:
                    MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
                    marketingPreUserDetailDTO.setCustNum(diDiProcessData.getCell());
                    marketingPreUserDetailDTO.setCell(diDiProcessData.getCell());
                    JSONObject rf = new JSONObject();
                    rf.put("userType","1");
                    if (StringUtils.isNotBlank(diDiProcessData.getExtend())) {
                        try {
                            JSONObject jsonObject = JSON.parseObject(diDiProcessData.getExtend());
                            rf.putAll(jsonObject);
                        }catch (Exception ex){
                            rf.put("tmpKey",diDiProcessData.getExtend());
                        }
                    }
                    marketingPreUserDetailDTO.setReserveField1(rf.toJSONString());
                    syncUsers.add(marketingPreUserDetailDTO);
                    break;
                case 5:
                    errorMark = Boolean.TRUE;
                    break;

            }
        }
        if(moreIds.size()>0){
            DidiData update = new DidiData();
            update.setStatus(3);
            DidiDataExample didiDataExample = new DidiDataExample();
            didiDataExample.createCriteria().andIdIn(moreIds);
            didiDataMapper.updateByExampleSelective(update,didiDataExample);
        }

        if(validMark){
            String fieldSql = pushDateSql.append(" end)").toString();
            String whereSql = pushDateWhereSql.toString().substring(0, pushDateWhereSql.toString().length() - 1).concat(")");
            String updateSql = validSql.append(fieldSql).append(whereSql).toString();
            didiDataMapper.updateMarketingBatch(updateSql.toString());
        }
        if(syncUsers.size()>0){
            MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
            marketingPreUserDTO.setTaskId(apiCode.concat("_").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            marketingPreUserDTO.setRequestId(marketingPreUserDTO.getTaskId().concat("_").concat(UUID.randomUUID().toString()));
            marketingPreUserDTO.setDataItems(syncUsers);
            UploadDataDTO uploadDataDTO = new UploadDataDTO();
            uploadDataDTO.setApiCode(apiCode);
            uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
            pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        }
        return new Result().setCode(errorMark?ResultCode.FAIL.getValue():ResultCode.SUCCESS.getValue());
    }
}
