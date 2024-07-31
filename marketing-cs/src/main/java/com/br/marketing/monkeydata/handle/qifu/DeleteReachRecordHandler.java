package com.br.marketing.monkeydata.handle.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SaveReachDeleteRecordReqBO;
import com.br.marketing.client.qifu.SaveReachDeleteRecordReq;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLog;
import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLogExample;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.QifuSaveReachDeleteRecordApiPushLogMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * D20240622促动支用户删除-3710139（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=166635171
 *
 * @author lixiang
 * @dateTime 2024-06-27 19:22
 */
@Service
@Slf4j
public class DeleteReachRecordHandler extends IMonkeyDataHandle<SaveReachDeleteRecordReqBO
        , SaveReachDeleteRecordReqBO, Page2Condition<QifuSaveReachDeleteRecordApiPushLog>> {

    private final static String TITLE = "【奇富删除触达记录促动支】";

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private QifuSaveReachDeleteRecordApiPushLogMapper qifuSaveReachDeleteRecordApiPushLogMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public Boolean isThread() {
        HashMap<String, JSONObject> customerJobConfig = marketingCommonConfig.getCustomerJobConfig();
        if(customerJobConfig == null){
            return super.isThread();
        }
        JSONObject jsonObject = customerJobConfig.get(this.getClass().getSimpleName());
        if(jsonObject == null){
            return super.isThread();
        }
        if(!jsonObject.containsKey("isThread")){
            return super.isThread();
        }
        Boolean isThread = jsonObject.getBoolean("isThread");
        return isThread;
    }

    @Override
    public Boolean isPause() {
        HashMap<String, JSONObject> customerJobConfig = marketingCommonConfig.getCustomerJobConfig();
        if(customerJobConfig == null){
            return super.isPause();
        }
        JSONObject jsonObject = customerJobConfig.get(this.getClass().getSimpleName());
        if(jsonObject == null){
            return super.isPause();
        }
        if(!jsonObject.containsKey("isPause")){
            return super.isPause();
        }
        Boolean isPause = jsonObject.getBoolean("isPause");
        return isPause;
    }

    @Override
    public Integer getThread() {
        HashMap<String, JSONObject> customerJobConfig = marketingCommonConfig.getCustomerJobConfig();
        if(customerJobConfig == null){
            return super.getThread();
        }
        JSONObject jsonObject = customerJobConfig.get(this.getClass().getSimpleName());
        if(jsonObject == null){
            return super.getThread();
        }
        if(!jsonObject.containsKey("threadNum")){
            return super.getThread();
        }
        Integer threadNum = jsonObject.getInteger("threadNum");
        return threadNum;
    }

    @Override
    public Result<IterationResult<SaveReachDeleteRecordReqBO, Page2Condition<QifuSaveReachDeleteRecordApiPushLog>>> getInputData(
            Page2Condition<QifuSaveReachDeleteRecordApiPushLog> condition) {
        Result<IterationResult<SaveReachDeleteRecordReqBO, Page2Condition<QifuSaveReachDeleteRecordApiPushLog>>> result
                = new Result<>();
        QifuSaveReachDeleteRecordApiPushLog param = condition.getParam();
        if (param.getStatus() != 1) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        List<String> cusBatchList = marketingSyncUserMapper.findCusBatchByAppletDatePage(param.getApiCode()
                , param.getBatchNo()
                , param.getSyncAppletDate()
                , condition.getPageSize());
        if (CollectionUtils.isEmpty(cusBatchList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        String cusBatch = cusBatchList.get(cusBatchList.size() - 1);
        param.setBatchNo(cusBatch);
        IterationResult<SaveReachDeleteRecordReqBO, Page2Condition<QifuSaveReachDeleteRecordApiPushLog>> content
                = new IterationResult<>();
        List<SaveReachDeleteRecordReqBO> reqBOList = cusBatchList.stream().filter((String b) -> {
            QifuSaveReachDeleteRecordApiPushLogExample example = new QifuSaveReachDeleteRecordApiPushLogExample();
            example.createCriteria()
                    .andBatchNoEqualTo(b)
                    .andApiCodeEqualTo(param.getApiCode())
                    .andPushDateEqualTo(param.getPushDate())
                    .andSyncAppletDateEqualTo(param.getSyncAppletDate());
            return qifuSaveReachDeleteRecordApiPushLogMapper.countByExample(example) < 1;
        }).map((String b) -> {
            SaveReachDeleteRecordReqBO bo = new SaveReachDeleteRecordReqBO();
            bo.setApiCode(param.getApiCode());
            bo.setAppletDate(param.getSyncAppletDate());
            SaveReachDeleteRecordReq req = new SaveReachDeleteRecordReq("bairong", b);
            bo.setReq(req);
            return bo;
        }).collect(Collectors.toList());
        content.setInputDataList(reqBOList);
        content.setInDatacondition(condition);
        result.setDate(content);
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    @Override
    public Result<?> customizedAction(Page2Condition<QifuSaveReachDeleteRecordApiPushLog> condition) {
        QifuSaveReachDeleteRecordApiPushLog param = condition.getParam();
        try {
            for (; ; ) {
                // 补数逻辑，日志状态为0（status=0）默认为补偿数据，直接推送
                List<QifuSaveReachDeleteRecordApiPushLog> batchNoList =
                        qifuSaveReachDeleteRecordApiPushLogMapper.findBatchNoByStatusPage(param.getApiCode()
                                , param.getBatchNo()
                                , 0
                                , condition.getPageSize());
                if (CollectionUtils.isEmpty(batchNoList)) {
                    break;
                }
                int size = batchNoList.size();
                String batchNo = batchNoList.get(size - 1).getBatchNo();
                param.setBatchNo(batchNo);
                List<Long> list = new ArrayList<>();
                List<SaveReachDeleteRecordReqBO> reqBOList = batchNoList.stream().map((QifuSaveReachDeleteRecordApiPushLog b) -> {
                    list.add(b.getId());
                    SaveReachDeleteRecordReqBO bo = new SaveReachDeleteRecordReqBO();
                    bo.setApiCode(b.getApiCode());
                    bo.setAppletDate(b.getSyncAppletDate());
                    SaveReachDeleteRecordReq req = new SaveReachDeleteRecordReq("bairong", b.getBatchNo());
                    bo.setReq(req);
                    return bo;
                }).collect(Collectors.toList());
                this.resultAction(reqBOList);
                qifuSaveReachDeleteRecordApiPushLogMapper.updateStatusByIds(-1, list);
                if (size < condition.getPageSize()) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        param.setBatchNo(null);
        return null;
    }

    @Override
    public Result<List<SaveReachDeleteRecordReqBO>> processData(List<SaveReachDeleteRecordReqBO> inList) {
        Result<List<SaveReachDeleteRecordReqBO>> result = new Result<>();
        result.setDate(inList);
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    @Override
    public Result<?> resultAction(List<SaveReachDeleteRecordReqBO> outputDataList) {
        for (SaveReachDeleteRecordReqBO bo : outputDataList) {
            methodRetryHandlerService.callDeleteReachRecordCuDongZhi(bo, null);
            log.warn(TITLE + "日期[{}],批次[{}]已物理删除,后续无法恢复！", bo.getAppletDate(), bo.getReq().getBatchNo());
        }
        Result<Object> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }
}
