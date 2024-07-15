package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.client.wuba.input.WuBaSubmitDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.WubaCollidingBatchNo;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.entity.WubaSubmitConversionDataExample;
import com.br.marketing.entity.WubaSubmitConversionDataLog;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataLogMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaSubmitConversionService {

    private final static String TITLE = "【58新客提交营销名单】";

    @Resource
    private WubaSubmitConversionDataMapper wubaSubmitConversionDataMapper;

    @Resource
    private WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;

    @Resource
    private WubaSubmitConversionDataLogMapper wubaSubmitConversionDataLogMapper;

    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private WuBaDingDingService wuBaDingDingService;

    @Resource
    private WuBaSubmitConversionSoleProcessor soleProcessor;


    public void action(Page2Condition<WubaSubmitConversionData> condition) {
        scanData(condition);
    }

    public void scanData(Page2Condition<WubaSubmitConversionData> condition) {
        WubaSubmitConversionData param = condition.getParam();
        String apiCode = param.getApiCode();
        Integer status = param.getStatus();
        Integer pushStatus = param.getPushStatus();
        Integer createDate = param.getCreateDate();
        Integer pageSize = condition.getPageSize();

        Long indexId = null;
        while (true) {
            try{
                // 循环获取条件数据，每次pageSize条
                List<WubaSubmitConversionData> pageList = wubaSubmitConversionDataMapper.findByConditionAndPage(
                        apiCode, status, pushStatus, createDate, "", indexId, pageSize);
                if (CollectionUtils.isEmpty(pageList)) {
                    log.warn(TITLE+"未获取到数据");
                    break;
                }
                indexId = pageList.get(pageList.size() - 1).getId();

                // 去重
                List<Long> noPushIds = soleProcessor.checkExists(pageList, param);
                if(!CollectionUtils.isEmpty(noPushIds)){
                    // 营销名单上报表status置为3-重复数据
                    WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
                    dataUpdate.setStatus(3);
                    WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
                    dataExample.createCriteria().andIdIn(noPushIds);
                    wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);
                }

                // process submit data
                processData(pageList, condition);
                // 按indexId 每页间隔5s
                Thread.sleep(5000);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
    }

    public Result processData(List<WubaSubmitConversionData> pageList, Page2Condition<WubaSubmitConversionData> condition)
            throws Exception {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(pageList)) {
            log.warn(TITLE+"processData无数据");
            return result.success();
        }

        String apiCode = condition.getParam().getApiCode();
        // callClient
        Result<String> callResult = callClient(pageList);
        if (callResult == null || !callResult.isSuccess() || callResult.getData() == null) {
            // Alert
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    TITLE + "调用接口失败, apiCode: " + apiCode));
            wuBaDingDingService.sendAlert(TITLE, "调用接口失败, apiCode: " + apiCode);
            return result;
        }

        // call success
        String batchNo = callResult.getData();
        if (StringUtils.isEmpty(batchNo)) {
            return result;
        }

        // 上报批次表增加记录，query_status置为0-未查询
        WubaCollidingBatchNo batchRecord = new WubaCollidingBatchNo();
        batchRecord.setApiCode(apiCode);
        batchRecord.setBatchNo(batchNo);
        batchRecord.setBatchType(2);
        batchRecord.setPushTime(new Date());
        batchRecord.setQueryStatus(0);
        int insert = wubaCollidingBatchNoMapper.insertSelective(batchRecord);
        if (insert < 1) {
            throw new Exception(TITLE + "上报批次表增加记录异常");
        }

        processSuccess(pageList, batchNo);
        return result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result processSuccess(List<WubaSubmitConversionData> pushList, String batchNo)
        throws Exception {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(pushList)) {
            log.warn(TITLE+"processData无数据");
            return result.success();
        }

        // 上报日志表增加记录，submit_result置为0-上报中
        List<WubaSubmitConversionDataLog> dataLogList = pushList.stream().map((WubaSubmitConversionData data) -> {
            WubaSubmitConversionDataLog dataLogRecord = new WubaSubmitConversionDataLog();
            dataLogRecord.setApiCode(data.getApiCode());
            dataLogRecord.setDataId(data.getId());
            dataLogRecord.setCell(data.getCell());
            dataLogRecord.setBatchNo(batchNo);
            dataLogRecord.setSubmitResult(0);
            return dataLogRecord;
        }).collect(Collectors.toList());

        int batchAdd = wubaSubmitConversionDataLogMapper.batchAdd(dataLogList);
        if(batchAdd != dataLogList.size()){
            throw new Exception(TITLE+"上报日志表增加记录异常");
        }

        // 营销名单上报表push_status置为1-推送中
        WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
        dataUpdate.setPushStatus(1);
        //
        List<Long> ids = pushList.stream().map((WubaSubmitConversionData data) -> data.getId()).collect(Collectors.toList());
        WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
        dataExample.createCriteria().andIdIn(ids);
        wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);

        // addDistributeLog
        soleProcessor.addDistributeLog(pushList);
        return result.success();
    }

    public Result<String> callClient(List<WubaSubmitConversionData> outputDataList) {
        Result result = new Result<>().failure();
        if (CollectionUtils.isEmpty(outputDataList)) {
            return result;
        }

        int magnitudes = outputDataList.size();
        long startTime = System.currentTimeMillis();
        List<WuBaSubmitDTO> wuBaSubmitDTOS = outputDataList.stream().map((WubaSubmitConversionData data) -> {
            String marketingTime ="";
            try {
                String createDateStr = String.valueOf(data.getCreateDate());
                Date createDate = DateUtils.parse(createDateStr, "yyyyMMdd");
                marketingTime = DateUtils.format(createDate, "yyyy-MM-dd 00:00:00");
            } catch (ParseException e) {
                marketingTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            }
            WuBaSubmitDTO wuBaSubmitDTO = new WuBaSubmitDTO();
            wuBaSubmitDTO.setMobile(data.getCell());
            wuBaSubmitDTO.setMarketingTime(marketingTime);
            return wuBaSubmitDTO;
        }).collect(Collectors.toList());

        // call submitConversionList
        Result callResult = wuBaServiceClient.submitConversionList(wuBaSubmitDTOS);
        if(callResult==null || !callResult.isSuccess() || callResult.getData()==null){
            return result;
        }
        String batchNo = (String) callResult.getData();
        if(StringUtils.isEmpty(batchNo)){
            return result;
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"callClient, 量级{}, 耗时{}", magnitudes, (endTime-startTime));
        return result.success().setDate(batchNo);
    }
}
