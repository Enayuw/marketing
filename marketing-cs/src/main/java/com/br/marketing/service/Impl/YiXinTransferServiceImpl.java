package com.br.marketing.service.Impl;


import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.vo.PhoneSaleInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class YiXinTransferServiceImpl  implements IYiXinTransferService {


    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    MarketingTransferInfoMapper transferInfoMapper;

    @Resource
    TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public Result actionYiXinToDx(String apiCode,String date) {

        if(StringUtils.isBlank(date)){
            date= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if(StringUtils.isBlank(apiCode)){
            apiCode = "3710012";
        }

        Date dayOfDate = null;
        try {
            dayOfDate = DateUtils.parseDate(date, "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //region check 1.查询推送记录；2.查询推送记录的状态；3.查询数据处理情况
        Result<TransferActionFront> frontDataRes = getFrontData(apiCode, date, 2);
        if(!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(frontDataRes.getMessage());
        }
        TransferActionFront frontData = frontDataRes.getData();
        if(frontData!=null&&new Integer(2).equals(frontData.getStatus())){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
        }
        Result<Date> dateResult = checkPush(apiCode, date);
        if(!ResultCode.SUCCESS.getValue().equals(dateResult.getCode())){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(dateResult.getMessage());
        }
        //todo 差黑名单逻辑
        //endregion

        Boolean mark = Boolean.TRUE;
        Integer page = 0;
        ConcurrentHashMap custNum = new ConcurrentHashMap();
        //
        String _7startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -7));
        String _60startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -60));
        String _endDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -1));

        while (mark){
            Result<List<MarketingTransferSyncUser>> delayData = getDelayData(apiCode, date, page);
            if(!ResultCode.SUCCESS.equals(delayData.getCode())){
                mark=Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = delayData.getData();
            List<Long> ids = new ArrayList<>();
            HashSet<String> custNums = new HashSet();
            List<MarketingTransferSyncUser> dataFilter1 = new ArrayList<>();
            for (MarketingTransferSyncUser datum : data) {
                if(!(StringUtils.isNotBlank(datum.getReserveField1())
                        && datum.getReserveField1().contains("\"transformType\":\"1\""))){
                    if(custNums.add(datum.getCustNum())){
                        dataFilter1.add(datum);
                    }
                }
            }

            PhoneSaleRecordInfoDTO _7recordInfoDTO = new PhoneSaleRecordInfoDTO();
            _7recordInfoDTO.setCustNums(custNums);
            _7recordInfoDTO.setApiCode(apiCode);
            _7recordInfoDTO.setStartDate(_7startDay);
            _7recordInfoDTO.setEndDate(_endDay);
            _7recordInfoDTO.setTransferType("1");
            List<PhoneSaleInfoVO> _7records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(_7recordInfoDTO);
            Set<String> _7filerCustNumSet = _7records.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());

            PhoneSaleRecordInfoDTO _60recordInfoDTO = new PhoneSaleRecordInfoDTO();
            _60recordInfoDTO.setCustNums(custNums);
            _60recordInfoDTO.setApiCode(apiCode);
            _60recordInfoDTO.setStartDate(_60startDay);
            _60recordInfoDTO.setEndDate(_endDay);
            _60recordInfoDTO.setTransferType("0");
            List<PhoneSaleInfoVO> _60records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(_60recordInfoDTO);
            Map<String, List<PhoneSaleInfoVO>> _60filterCustNumsMap = _60records.stream().collect(Collectors.groupingBy(PhoneSaleInfoVO::getCustNum));



        }




        return null;
    }

    /**
     * 获取数据
     * @param apiCode
     * @param date
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getDelayData(String apiCode,String date,Integer pageIndex){
        String tcId = tableCreateService.getTcId(apiCode);
        Integer limitStart = pageIndex*5000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, date, limitStart);
        if(transferOrderInsertTime.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

    /**
     * 校验数据解析是否完成
     * @param apiCode
     * @param date
     * @return
     */
    private Result<Date> checkPush(String apiCode,String date){

        Date startDate = null;
        try {
            startDate = DateUtils.parseDate(date.concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = DateUtils.addDays(startDate, 1);
        MarketingTransferInfoExample infoExample = new MarketingTransferInfoExample();
        infoExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andLastEqualTo("1")
                .andCreateTimeGreaterThanOrEqualTo(startDate)
                .andCreateTimeLessThan(endDate);
        List<MarketingTransferInfo> marketingTransferInfos = transferInfoMapper.selectByExample(infoExample);
        if(marketingTransferInfos.size()<=0){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("还未传输last标识数据");
        }

        MarketingTransferInfoExample statusExample = new MarketingTransferInfoExample();
        statusExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(1)
                .andCreateTimeGreaterThanOrEqualTo(startDate)
                .andCreateTimeLessThan(endDate);
        int statusIngs = transferInfoMapper.countByExample(statusExample);
        if(statusIngs>=0){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("数据还未解析完");
        }

        MarketingTransferInfo transferInfo = marketingTransferInfos.get(0);
        Date limitTime = transferInfo.getCreateTime();
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(limitTime);
    }

    /**
     * 获取推送记录
     * @param apiCode
     * @param date
     * @param actionType
     * @return
     */
    private Result<TransferActionFront> getFrontData(String apiCode,String date,Integer actionType){
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);

        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if(transferActionFronts.size()>1){
            log.error(String.format("该推送日志当前有条 请检查apiCode:%s,data:%s,type:%s",apiCode,date,actionType));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        if(transferActionFronts.size()>0){
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferActionFronts.get(0));
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(null);
    }


}
