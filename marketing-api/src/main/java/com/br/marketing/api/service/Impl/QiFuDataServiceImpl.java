package com.br.marketing.api.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.api.service.QiFuDataService;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.br.marketing.common.constants.MarketingErrorInfo.SUCCESS;


/**
 * @author zhen.Li1
 * @date 2024/3/22 17:57
 * @desc: 360 策略效果数据报表
 */
@Service
@Slf4j
public class QiFuDataServiceImpl implements QiFuDataService {


    @Autowired
    private QifuStrategyReportDataMapper qifuStrategyReportDataMapper;

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
        if (reportData.getGroupName().equals("实验组")) {
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
}
