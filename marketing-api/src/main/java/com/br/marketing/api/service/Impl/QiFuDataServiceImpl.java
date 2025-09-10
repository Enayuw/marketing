package com.br.marketing.api.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.api.service.QiFuDataService;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QiFuEffectReportData;
import com.br.marketing.entity.QifuActuation;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.mapper.QiFuEffectReportDataMapper;
import com.br.marketing.mapper.QifuActuationMapper;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.br.marketing.common.constants.MarketingErrorInfo.SUCCESS;


/**
 * This is a Javadoc comment
 */
@Service
@Slf4j
public class QiFuDataServiceImpl implements QiFuDataService {


    @Autowired
    private QifuStrategyReportDataMapper qifuStrategyReportDataMapper;
    @Resource
    private QifuActuationMapper qifuActuationMapper;
    @Resource
    private QiFuEffectReportDataMapper qifuEffectReportDataMapper;

    @Override
    public ApiNoDataResult strategyReportData(String apiCode, String jsonData) {

        QifuStrategyReportData reportData = JSON.parseObject(jsonData, new TypeReference<QifuStrategyReportData>() {
        }.getType());
        //必填字段校验
        List paramCheckList = Lists.newArrayList(reportData.getStrategyMonth(), reportData.getApplySubmitRate(), reportData.getApplySubmitUserCount(),
                reportData.getCanvasName(), reportData.getCreditSuccessRate(), reportData.getCreditSuccessUserCount(), reportData.getGroupName(),
                reportData.getPassRate(), reportData.getSupplier(), reportData.getUpdateDate(), reportData.getUserCount());
        boolean paramNull = paramCheckList.stream().anyMatch(param -> StringUtils.isEmpty(param));
        if (paramNull) {
            return new ApiNoDataResult().setCode(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorCode()).
                    setMessage(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorMsg());
        }

        List deltaParamCheckList = Lists.newArrayList(reportData.getDeltaApplySubmitRate(), reportData.getDeltaApplySubmitCount(),
                reportData.getDeltaCreditSuccessCount(), reportData.getDeltaCreditSuccessRate());
        if ("实验组".equals(reportData.getGroupName())) {
            boolean deltaParamNull = deltaParamCheckList.stream().anyMatch(param -> StringUtils.isEmpty(param));
            if (deltaParamNull) {
                return new ApiNoDataResult().setCode(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorCode()).
                        setMessage(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorMsg());
            }
        }
        reportData.setApiCode(apiCode);
        reportData.setCreateTime(new Date());
        reportData.setUpdateTime(new Date());
        qifuStrategyReportDataMapper.insertSelective(reportData);
        return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
    }

    @Override
    public ApiNoDataResult analysisStatistics(String apiCode, String jsonData) {
        try {
            List<QifuActuation> reportDataList = JSON.parseObject(jsonData, new TypeReference<List<QifuActuation>>() {
            }.getType());

            reportDataList.parallelStream().forEach(item -> {
                item.setApiCode(apiCode);
                item.setCreateDate(LocalDate.now().toString());
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
            });

            if (!CollectionUtils.isEmpty(reportDataList)) {
                qifuActuationMapper.batchInsert(reportDataList);
                log.warn("奇富促动支定制上传数据接入 数量:{}", reportDataList.size());
            }

            return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(),
                    "jsonData:" + jsonData, "该apiCode:" + apiCode + "奇富促动支定制上传数据接入异常！！！"), e);
            return new ApiNoDataResult().setCode(MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode())
                    .setMessage(MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg());
        }
    }

    @Override
    public ApiNoDataResult effectReport(String apiCode, String jsonData) {
        List<QiFuEffectReportData> qiFuEffectReportDataList = JSON.parseArray(jsonData, QiFuEffectReportData.class);

        if (CollectionUtils.isEmpty(qiFuEffectReportDataList)) {
            return new ApiNoDataResult().setCode(MarketingErrorInfo.QUANTITY_ERROR.getErrorCode()).
                    setMessage(MarketingErrorInfo.QUANTITY_ERROR.getErrorMsg());
        }

        for (QiFuEffectReportData qiFuEffectReportData : qiFuEffectReportDataList) {
            boolean hasEmptyField = StringUtils.isEmpty(qiFuEffectReportData.getBelongMonth())
                    || StringUtils.isEmpty(qiFuEffectReportData.getStrategyMonth())
                    || StringUtils.isEmpty(qiFuEffectReportData.getUpdDate())
                    || StringUtils.isEmpty(qiFuEffectReportData.getCanvasName())
                    || StringUtils.isEmpty(qiFuEffectReportData.getAgentOperator())
                    || StringUtils.isEmpty(qiFuEffectReportData.getGroupName());
            if (hasEmptyField) {
                return new ApiNoDataResult().setCode(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorCode()).
                        setMessage(MarketingErrorInfo.PARAM_ISNULL_ERROR.getErrorMsg());
            }

            qiFuEffectReportData.setApiCode(apiCode);
            qiFuEffectReportData.setCreateTime(new Date());
            qiFuEffectReportData.setUpdateTime(new Date());
            qiFuEffectReportData.setIsDel(1); // 1-有效

            try {
                qifuEffectReportDataMapper.insertSelective(qiFuEffectReportData);
            } catch (DuplicateKeyException keyException) {
                log.warn("奇富效果报告数据存在重复数据！");
            } catch (Exception e) {
                log.error("奇富效果报告数据插入异常。apiCode:{}, jsonData:{}", apiCode, jsonData, e);
                return new ApiNoDataResult().setCode(MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode())
                        .setMessage(MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg());
            }
        }
        return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
    }
}
