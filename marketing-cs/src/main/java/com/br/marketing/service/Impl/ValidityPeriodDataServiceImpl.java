package com.br.marketing.service.Impl;

import IceInternal.Ex;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ValidityPeriodResendRecordService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.br.marketing.common.constants.MarketingErrorInfo.*;
import static com.br.marketing.common.constants.MarketingErrorInfo.SUCCESS;

/**
 * 描述：： 根据有效期框定数据范围实现
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ValidityPeriodDataServiceImpl
 * @author: it-yml
 * @create: 2023-08-25 21:24
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ValidityPeriodDataServiceImpl implements ValidityPeriodDataService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;


    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ValidityPeriodResendRecordService recordService;


    @Override
    public Boolean judgmentMarketingTransferDataInvalidWithValidityPeriod(String apiCode, String custNum) {
        String tcId = tableCreateService.getTcId(apiCode);
        // isBlack = 1
        Integer countIsBlackByCustNum = marketingTransferSyncUserMapper.getCountIsBlackByCustNum(tcId, custNum);
        if (countIsBlackByCustNum > 0) {
            return Boolean.TRUE;
        }

        Pair<String, String> validityRange = getMarketingTransferDataWithValidityRange(apiCode);
        if (validityRange == null) {
            return Boolean.FALSE;
        }
        Integer countIfApplyByCustNum = marketingTransferSyncUserMapper.getCountIfApplyByCustNum(tcId, custNum, validityRange.getKey(), validityRange.getValue());
        if (countIfApplyByCustNum > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Pair<String, String> getMarketingTransferDataWithValidityRange(String apiCode) {
        MarketingDataValidConfig marketingTransferDataWithValidityPeriod =
                marketingDataValidConfigMapper.getMarketingTransferDataWithValidityPeriod(apiCode);
        if (marketingTransferDataWithValidityPeriod == null) {
            return null;
        }

        String validStartDate = marketingTransferDataWithValidityPeriod.getValidStartDate();
        String validEndDate = marketingTransferDataWithValidityPeriod.getValidEndDate();
        String dateStartStr = getDateStr(validStartDate, -1);
        String dateEndStr = getDateStr(validEndDate, 1);
        return new Pair<>(dateStartStr, dateEndStr);
    }


    /* 获取指定日后 后 dayAddNum 天的 日期
     * @param day  日期，格式为String："2013-9-3";
     * @param dayAddNum 增加天数 格式为int;
     * @return
     */
    public static String getDateStr(String day, int dayAddNum) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); //定义日期格式化的格式
        String stringDate = null;
        //需要加减的字符串型日期
        try {
            if (("9999-12-31").equals(day)) {
                return day;
            }
            Date classDate = format.parse(day);
            //把字符串转化成指定格式的日期
            Calendar calendar = Calendar.getInstance(); //使用Calendar日历类对日期进行加减
            calendar.setTime(classDate);
            calendar.add(Calendar.DAY_OF_MONTH, dayAddNum);
            classDate = calendar.getTime();//获取加减以后的Date类型日期
            stringDate = format.format(classDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return stringDate;
    }

    @Override
    public ApiNoDataResult marketingValidityPeriod(String apiCode, String jsonData) {
        List<String> validityPeriodApiCodeList = marketingCommonConfig.getValidityPeriodApiCodeList();
        // 校验apiCode
        if (!validityPeriodApiCodeList.contains(apiCode)) {
            log.error("有效期变更接口异常：{}", API_CODE_AUTH_ERROR.getErrorMsg());
            return new ApiNoDataResult().setCode(API_CODE_AUTH_ERROR.getErrorCode())
                    .setMessage(API_CODE_AUTH_ERROR.getErrorMsg());
        }
        // 校验 jsonData
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(jsonData);
        } catch (Exception e) {
            log.error("有效期变更接口异常：{}", JSON_DATA_ERROR.getErrorMsg());
            return new ApiNoDataResult().setCode(JSON_DATA_ERROR.getErrorCode())
                    .setMessage(JSON_DATA_ERROR.getErrorMsg());
        }

        // 校验 taskId
        String taskId = jsonObject.getString("taskId");
        if (StringUtils.isBlank(taskId)) {
            log.error("有效期变更接口异常：{}", TASK_ID_ERROR.getErrorMsg());
            return new ApiNoDataResult().setCode(TASK_ID_ERROR.getErrorCode())
                    .setMessage(TASK_ID_ERROR.getErrorMsg());
        }
        // 校验 判断开关
        if (Boolean.TRUE.equals(marketingCommonConfig.getChangeValidityPeriodIndex())) {
            return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
        }
        String effectiveDate = jsonObject.getString("effectiveDate");
        String expireDate = jsonObject.getString("expireDate");
        String effectiveDateTransfer = "";
        String expireDateTransfer = "";
        try {
            effectiveDateTransfer = formatDate(effectiveDate);
            expireDateTransfer = formatDate(expireDate);
        } catch (Exception e) {
            log.error("有效期变更接口异常：日期格式不符合要求，{},{} ",effectiveDate,expireDate);
            return new ApiNoDataResult().setCode(TIME_FORMAT_ERROR.getErrorCode())
                    .setMessage(TIME_FORMAT_ERROR.getErrorMsg());
        }
        // 根据批次号查询appletDate
        String appletDate = marketingSyncInfoMapper.getAppletDateByCusBatch(taskId);
        if (!StringUtils.isBlank(appletDate)) {
            // 根据appletDate 查询有效期配置表
            List<MarketingDataValidConfig> validityDataByAppletDate = marketingDataValidConfigMapper.getValidityDataByAppletDate(apiCode, appletDate);
            for (int i = 0; i < validityDataByAppletDate.size(); i++) {
                MarketingDataValidConfig marketingDataValidConfig = validityDataByAppletDate.get(i);
                String validStartDate = marketingDataValidConfig.getValidStartDate();
                String validEndDate = marketingDataValidConfig.getValidEndDate();
                // 判断开始时间和结束时间是否有变化 有变化则更改 没有变化返回成功报警通知
                if (effectiveDateTransfer.equals(validStartDate) && expireDateTransfer.equals(validEndDate)) {
                    log.error("有效期变更接口传入有效期参数与历史有效期时间相同，未重新推送数据 ：{},{},{}", taskId, effectiveDateTransfer, expireDateTransfer);
                } else {
                    // 更新有效期配置表
                    MarketingDataValidConfig newData = new MarketingDataValidConfig();
                    newData.setId(marketingDataValidConfig.getId());
                    newData.setValidStartDate(validStartDate);
                    newData.setValidEndDate(validEndDate);
                    int n = marketingDataValidConfigMapper.updateByPrimaryKeySelective(newData);
                    if (n > 0) {
                        // 新增记录表
                        recordService.saveRecord(apiCode, marketingDataValidConfig.getUserType(), newData.getId());
                    }
                }
            }
        } else {
            log.error("有效期变更接口异常：{}", TASK_ID_ERROR.getErrorMsg());
            return new ApiNoDataResult().setCode(TASK_ID_ERROR.getErrorCode())
                    .setMessage(TASK_ID_ERROR.getErrorMsg());
        }

        return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
    }

    private String formatDate(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat simpleDateFormatResult = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = simpleDateFormat.parse(date);
        String format = simpleDateFormatResult.format(parse);
        return format;
    }
}
