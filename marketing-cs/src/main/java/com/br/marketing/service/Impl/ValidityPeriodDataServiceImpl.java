package com.br.marketing.service.Impl;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomizeDataValidConfig;
import com.br.marketing.entity.MarketingCustomizeDataValidConfigExample;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ValidityPeriodResendRecordService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Resource
    private MarketingCustomizeDataValidConfigMapper marketingCustomizeDataValidConfigMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Resource
    private TrackingService trackingService;


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
        try {
            List<String> validityPeriodApiCodeList = marketingCommonConfig.getValidityPeriodApiCodeList();
            // 校验apiCode
            if (!validityPeriodApiCodeList.contains(apiCode)) {
                log.error("有效期变更接口异常：{}，{}，jsonData:{}", API_CODE_AUTH_ERROR.getErrorMsg(), apiCode, jsonData);
                return new ApiNoDataResult().setCode(API_CODE_AUTH_ERROR.getErrorCode())
                        .setMessage(API_CODE_AUTH_ERROR.getErrorMsg());
            }
            // 校验 jsonData
            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(jsonData);
            } catch (Exception e) {
                log.error("有效期变更接口异常：{}，jsonData:{},{}", JSON_DATA_ERROR.getErrorMsg(), jsonData, e);
                return new ApiNoDataResult().setCode(JSON_DATA_ERROR.getErrorCode())
                        .setMessage(JSON_DATA_ERROR.getErrorMsg());
            }

            // 校验 taskId
            String taskId = jsonObject.getString("taskId");
            if (StringUtils.isBlank(taskId)) {
                log.error("有效期变更接口异常：{}，jsonData:{}", TASK_ID_ERROR.getErrorMsg(), jsonData);
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
                if (effectiveDate.length() != 8 || expireDate.length() != 8) {
                    log.error("有效期变更接口异常：日期格式不符合要求，jsonData:{} ,{}", jsonData);
                    return new ApiNoDataResult().setCode(TIME_FORMAT_ERROR.getErrorCode())
                            .setMessage(TIME_FORMAT_ERROR.getErrorMsg());
                }
                effectiveDateTransfer = formatDate(effectiveDate);
                expireDateTransfer = formatDate(expireDate);
            } catch (Exception e) {
                log.error("有效期变更接口异常：日期格式不符合要求，jsonData:{} ,{}", jsonData, e);
                return new ApiNoDataResult().setCode(TIME_FORMAT_ERROR.getErrorCode())
                        .setMessage(TIME_FORMAT_ERROR.getErrorMsg());
            }
            // 根据批次号查询appletDate
            String appletDate = marketingSyncInfoMapper.getAppletDateByCusBatch(taskId, apiCode);
            if (StringUtils.isBlank(appletDate)) {
                log.error("有效期变更接口异常：{}，jsonData:{}", TASK_ID_ERROR.getErrorMsg(), jsonData);
                return new ApiNoDataResult().setCode(TASK_ID_ERROR.getErrorCode())
                        .setMessage(TASK_ID_ERROR.getErrorMsg());
            }
            // 查询当前taskId下的修改前有效期
            MarketingCustomizeDataValidConfigExample me = new MarketingCustomizeDataValidConfigExample();
            me.createCriteria().andApiCodeEqualTo(apiCode)
                    .andTaskIdEqualTo(taskId)
                    .andIsDelEqualTo(1);
            List<MarketingCustomizeDataValidConfig> marketingCustomizeDataValidConfigList =
                    marketingCustomizeDataValidConfigMapper.selectByExample(me);
            if (marketingCustomizeDataValidConfigList.size() == 1) {
                MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfigOld = marketingCustomizeDataValidConfigList.get(0);

                MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfigNew = new MarketingCustomizeDataValidConfig();
                BeanUtils.copyProperties(marketingCustomizeDataValidConfigOld, marketingCustomizeDataValidConfigNew);
                marketingCustomizeDataValidConfigNew.setValidStartDate(effectiveDateTransfer);
                marketingCustomizeDataValidConfigNew.setValidEndDate(expireDateTransfer);

                //创建测试用户信息
                MarketingUserInfo marketingUserInfo = new MarketingUserInfo();
                marketingUserInfo.setId(9999);
                marketingUserInfo.setUserName("360_customer");
                MarketingUserDetail marketingUserDetail = new MarketingUserDetail(marketingUserInfo, null, null, null);

                entityOptService.writeOptLog(marketingCustomizeDataValidConfigOld.getId(), marketingCustomizeDataValidConfigNew, marketingCustomizeDataValidConfigOld, marketingUserDetail);

            }

            // 更新task_id 对应有效期
            MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfig = new MarketingCustomizeDataValidConfig();
            marketingCustomizeDataValidConfig.setValidStartDate(effectiveDateTransfer);
            marketingCustomizeDataValidConfig.setValidEndDate(expireDateTransfer);
            MarketingCustomizeDataValidConfigExample marketingCustomizeDataValidConfigExample = new MarketingCustomizeDataValidConfigExample();
            marketingCustomizeDataValidConfigExample.createCriteria().andTaskIdEqualTo(taskId);
            int updateCount = marketingCustomizeDataValidConfigMapper.updateByExampleSelective(
                    marketingCustomizeDataValidConfig,
                    marketingCustomizeDataValidConfigExample);
            if (updateCount == 0) {
                log.error("有效期变更接口异常：有效期变更 定制表 taskId 未匹配到:联系运营确认，手动处理，jsonData:{}", jsonData);
                return new ApiNoDataResult().setCode(SUCCESS.getErrorCode()).setMessage(SUCCESS.getErrorMsg());
            }

            List<MarketingCustomizeDataValidConfig> marketingCustomizeDataValidConfigs =
                    marketingCustomizeDataValidConfigMapper.selectByExample(marketingCustomizeDataValidConfigExample);
            for (MarketingCustomizeDataValidConfig m : marketingCustomizeDataValidConfigs) {
                recordService.saveRecord(apiCode, m.getUserType(), m.getId());
            }

            try {
                JSONObject condition = new JSONObject();
                condition.put("task_id", taskId);
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "360有效期变更接口"
                        , "b_marketing_customize_data_valid_config"
                        , JSON.toJSONString(condition)
                        , 1L
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }
        } catch (Exception e) {
            log.error("有效期并更接口异常,{}", e);
            return new ApiNoDataResult().setCode(UNKNOWN_ERROR.getErrorCode()).setMessage(UNKNOWN_ERROR.getErrorMsg());
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
