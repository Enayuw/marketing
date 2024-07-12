package com.br.marketing.service.Impl.wuba;

import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.client.wuba.input.WuBaSubmitDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.orika.OrikaBeanMapperUtil;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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


    public void action(Page2Condition<WubaSubmitConversionData> condition) {
        scanData(condition);
    }

    public void scanData(Page2Condition<WubaSubmitConversionData> condition) {
        WubaSubmitConversionData param = condition.getParam();
        String apiCode = param.getApiCode();
        Integer status = param.getStatus();
        Integer pushStatus = param.getPushStatus();
        Integer pageSize = condition.getPageSize();

        Long indexId = null;
        while (true) {
            // 循环获取条件数据，每次pageSize条
            final List<WubaSubmitConversionData> pageList = wubaSubmitConversionDataMapper.findByConditionAndPage(
                    apiCode, status, pushStatus, "", indexId, pageSize);

            if (CollectionUtils.isEmpty(pageList)) {
                log.warn(TITLE+"未获取到数据");
                break;
            }

            indexId = pageList.get(pageList.size() - 1).getId();

            processData(pageList, condition);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Result<?> processData(List<WubaSubmitConversionData> pageList, Page2Condition<WubaSubmitConversionData> condition) {
        // callClient
        Result<String> result = callClient(pageList);
        if(!result.isSuccess() || result.getData()==null){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }

        String batchNo = result.getData();
        if(StringUtils.isEmpty(batchNo)){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }

        // 上报批次表增加记录，query_status置为0-未查询
        WubaCollidingBatchNo batchRecord = new WubaCollidingBatchNo();
        batchRecord.setBatchNo(batchNo);
        batchRecord.setBatchType(2);
        batchRecord.setPushTime(new Date());
        batchRecord.setQueryStatus(0);
        int insert = wubaCollidingBatchNoMapper.insert(batchRecord);
        if(insert < 1){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        // 上报日志表增加记录，submit_result置为0-上报中
        List<WubaSubmitConversionDataLog> dataLogList = pageList.stream().map((WubaSubmitConversionData data) -> {
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
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        //营销名单上报表push_status置为1-推送中
        WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
        dataUpdate.setPushStatus(1);
        WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
        List<Long> ids = pageList.stream().map((WubaSubmitConversionData data) -> data.getId()).collect(Collectors.toList());
        dataExample.createCriteria().andIdIn(ids);
        int updateStatus = wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);
        return new Result();
    }

    public Result<String> callClient(List<WubaSubmitConversionData> outputDataList) {
        Result result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }

        int magnitudes = outputDataList.size();
        long startTime = System.currentTimeMillis();
        // TODO mapping
        List<WuBaSubmitDTO> wuBaSubmitDTOS = OrikaBeanMapperUtil.mapAsList(outputDataList, WuBaSubmitDTO.class);
        Result callResult = wuBaServiceClient.submitConversionList(wuBaSubmitDTOS);
        if(!callResult.isSuccess() || callResult.getData()==null){
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        String batchNo = (String) callResult.getData();
        if(StringUtils.isEmpty(batchNo)){
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"callClient, 量级{}, 耗时{}", magnitudes, (endTime-startTime));
        return result.setCode(ResultCode.SUCCESS.getValue()).setDate(batchNo);
    }
}
