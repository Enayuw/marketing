package com.br.marketing.api.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.api.service.QiFuDataService;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QifuActuation;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.mapper.QifuActuationMapper;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.br.marketing.common.constants.MarketingErrorInfo.SUCCESS;


/**
 * This is a Javadoc comment
 * @param <T> the parameter of the class
 */
@Service
@Slf4j
public class QiFuDataServiceImpl implements QiFuDataService {


    @Autowired
    private QifuStrategyReportDataMapper qifuStrategyReportDataMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private QifuActuationMapper qifuActuationMapper;

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
            QifuActuation reportData = JSON.parseObject(jsonData, new TypeReference<QifuActuation>() {
            }.getType());

            reportData.setApiCode(apiCode);
            reportData.setCreateDate(LocalDate.now().toString());
            reportData.setCreateTime(new Date());
            reportData.setUpdateTime(new Date());
            reportData.setIsDel(1);
            qifuActuationMapper.insertSelective(reportData);
            return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(),
                    "jsonData:" + jsonData, "该apiCode:" + apiCode + "奇富促动支定制上传数据接入异常！！！"), e);
            return new ApiNoDataResult().setCode(MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode()).
                    setMessage(MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg());
        }
    }
}
