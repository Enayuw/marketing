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
import java.util.List;
import java.util.stream.Collectors;

/**
 * https://c.100credit.cn/pages/viewpage.action?pageId=130941554
 * 保存触达记录删除逻辑
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-27 11:12
 */
@Service
@Slf4j
public class SaveReachDeleteRecordHandle extends IMonkeyDataHandle<SaveReachDeleteRecordReqBO
        , SaveReachDeleteRecordReqBO, Page2Condition<QifuSaveReachDeleteRecordApiPushLog>> {
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
        JSONObject jsonObject;
        return marketingCommonConfig.getCustomerJobConfig() == null
                ? super.isThread() : (jsonObject = marketingCommonConfig.getCustomerJobConfig()
                .get(this.getClass().getSimpleName())) == null
                ? super.isThread() : jsonObject.containsKey("isThread")
                ? jsonObject.getBoolean("isThread") : super.isThread();
    }

    @Override
    public Boolean isPause() {
        JSONObject jsonObject;
        return marketingCommonConfig.getCustomerJobConfig() == null
                ? super.isPause() : (jsonObject = marketingCommonConfig.getCustomerJobConfig()
                .get(this.getClass().getSimpleName())) == null
                ? super.isPause() : jsonObject.containsKey("isPause")
                ? jsonObject.getBoolean("isPause") : super.isPause();
    }

    @Override
    public Integer getThread() {
        JSONObject jsonObject;
        return marketingCommonConfig.getCustomerJobConfig() == null
                ? super.getThread() : (jsonObject = marketingCommonConfig.getCustomerJobConfig()
                .get(this.getClass().getSimpleName())) == null
                ? super.getThread() : jsonObject.containsKey("threadNum")
                ? jsonObject.getInteger("threadNum") : super.getThread();
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
        List<SaveReachDeleteRecordReqBO> reqBOList = cusBatchList.stream().filter(b -> {
            QifuSaveReachDeleteRecordApiPushLogExample example = new QifuSaveReachDeleteRecordApiPushLogExample();
            example.createCriteria()
                    .andBatchNoEqualTo(b)
                    .andApiCodeEqualTo(param.getApiCode())
                    .andPushDateEqualTo(param.getPushDate())
                    .andSyncAppletDateEqualTo(param.getSyncAppletDate());
            return qifuSaveReachDeleteRecordApiPushLogMapper.countByExample(example) < 1;
        }).map(b -> {
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
                List<SaveReachDeleteRecordReqBO> reqBOList = batchNoList.stream().map(b -> {
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
            methodRetryHandlerService.callSaveReachDeleteRecord(bo, null);
            log.warn("奇富触达记录,日期[{}],批次[{}]已物理删除,后续无法恢复！", bo.getAppletDate(), bo.getReq().getBatchNo());
        }
        Result<Object> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }
}
