package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMapping;
import com.br.marketing.mapper.XieChengCollidingDataHitRequestNoMappingMapper;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 撞库日志相关service
 * @author senyang.zheng
 * @date 2024/03/23
 */
@Service
@Slf4j
public class XieChengCollidingDataLogServiceImpl implements XieChengCollidingDataLogService {

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    @Resource
    private XieChengCollidingDataHitRequestNoMappingMapper xieChengCollidingDataHitRequestNoMappingMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private DatabaseOperationService dbService;

    public static final ThreadPoolExecutor XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL = BrExecutors.getThreadPool(50, 50);

    /**
     * 构造撞库正常log
     * @param id             id
     * @param packageId      packageId
     * @param packageRuleId  packageRuleId
     * @param dataSourceType 数据源类型 T True数据,F False数据
     * @param returnData     返回数据
     * @param httpcode       httpcode
     * @param businessCode   客户返回Code码
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public XieChengCollidingDataLog buildSuccessXieChengCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
                                                                         JSONObject returnData, String httpcode, Integer businessCode) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");
        String releaseDate = returnData.getString("releaseDate");
        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(id);
        xieChengCollidingDataLog.setPackageId(packageId);
        if (packageRuleId != null) {
            xieChengCollidingDataLog.setPackageRuleId(packageRuleId);
        }
        xieChengCollidingDataLog.setDataSourceType(dataSourceType);
        xieChengCollidingDataLog.setCellSha256CodeList(sha256Code);
        xieChengCollidingDataLog.setReleaseTime(releaseTime);
        xieChengCollidingDataLog.setReleaseDate(releaseDate);
        xieChengCollidingDataLog.setOrgChannel(orgChannel);
        xieChengCollidingDataLog.setHttpCode(Integer.valueOf(httpcode));
        xieChengCollidingDataLog.setBusinessCode(businessCode);
        xieChengCollidingDataLog.setMktLevel(mktLevel);
        xieChengCollidingDataLog.setInfo(info);
        xieChengCollidingDataLog.setResult(result);
        try {
            JSONArray jsonArray = returnData.getJSONArray("marketCouponList");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                xieChengCollidingDataLog.setMarketCouponList(jsonArray.toJSONString());
                JSONObject firstCoupon = jsonArray.getJSONObject(0);
                String couponCode = firstCoupon.getString("couponCode");
                String couponDesc = firstCoupon.getString("couponDesc");
                xieChengCollidingDataLog.setCouponCode(couponCode);
                xieChengCollidingDataLog.setCouponDesc(couponDesc);
            }
        } catch (Exception e) {
            xieChengCollidingDataLog.setMarketCouponList(String.valueOf(returnData.get("marketCouponList")));
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程撞库日志析出marketCouponList异常！"), e);
        }
        xieChengCollidingDataLog.setReturnContent(returnData.toString(SerializerFeature.WriteMapNullValue));
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());
        return xieChengCollidingDataLog;
    }

    /**
     * 构建异常携程撞库日志
     * @param id                 id
     * @param packageId          packageId
     * @param packageRuleId      packageRuleId
     * @param dataSourceType     数据源类型 T True数据,F False数据
     * @param cellSha256CodeList 手机号
     * @param resJson            res json
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public XieChengCollidingDataLog buildFailXieChengCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
                                                                      String cellSha256CodeList, JSONObject resJson) {
        String httpcode = resJson.getString("httpcode");
        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(id);
        xieChengCollidingDataLog.setPackageId(packageId);
        xieChengCollidingDataLog.setDataSourceType(dataSourceType);
        xieChengCollidingDataLog.setCellSha256CodeList(cellSha256CodeList);
        xieChengCollidingDataLog.setHttpCode(StringUtils.isEmpty(httpcode) ? null : Integer.valueOf(httpcode));
        try {
            if (StringUtils.isNotEmpty(resJson.getString("content"))) {
                JSONObject contentJson = JSONObject.parseObject(resJson.getString("content"));
                Integer businessCode = contentJson.getInteger("code");
                xieChengCollidingDataLog.setBusinessCode(businessCode);
            }
        } catch (Exception e) {
            log.warn("解析businessCode异常:", e);
        }
        xieChengCollidingDataLog.setReturnContent(resJson.toString(SerializerFeature.WriteMapNullValue));
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());
        return xieChengCollidingDataLog;
    }

    /**
     * 推送保存log消息
     * @param collidingLogs 碰撞日志
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public void pushLogMessage(List<XieChengCollidingDataLog> collidingLogs) {
        try {
            rocketMqSwitch.syncSend(MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_CPA_COLLIDING_LOG_QUEUE
                    , MarketingXieChengConstants.TAG_MARKETING_XIECHENG_CPA_COLLIDING_LOG_QUEUE, JSONObject.toJSONString(collidingLogs));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "推送携程撞库日志消息异常！"), e);
        }
    }

    @Override
    public Result<Boolean> saveXieChengCollidingDataLog(List<XieChengCollidingDataLog> collidingLogs) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
            XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL,
            marketingCommonConfig.getXiechengSaveCollidingLogThread()
        );
        for (XieChengCollidingDataLog collidingLog : collidingLogs) {
            XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL.submit(() -> {
                        saveLogAndMapping(collidingLog);
                    }
            );
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    /**
     * 保存撞库日志和流水号手机号映射
     * @param collidingLog
     */
    private void saveLogAndMapping(XieChengCollidingDataLog collidingLog) {
        // 写入日志表
        try {
            xieChengCollidingDataLogMapper.insertSelective(collidingLog);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程撞库保存日志异常！"), e);
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    xieChengCollidingDataLogMapper.insertSelective(collidingLog);
                }
                @Override
                public Object getParams() {
                    return collidingLog;
                }
                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XieChengCollidingDataLogMapperBase";
                }
                @Override
                public String getMapperMethod() {
                    return "insertSelective";
                }
            },"携程撞库日志写入", config);
        }

        // 写入映射表
        try {
            XieChengCollidingDataHitRequestNoMapping requestNoMapping = new XieChengCollidingDataHitRequestNoMapping();
            String returnContent = collidingLog.getReturnContent();
            JSONObject jsonReturnContent = JSONObject.parseObject(returnContent);

            String hitRequestNo = jsonReturnContent.getString("hitRequestNo");
            requestNoMapping.setLogId(collidingLog.getId());
            requestNoMapping.setHitRequestNo(hitRequestNo);
            requestNoMapping.setCellSha256CodeList(collidingLog.getCellSha256CodeList());
            requestNoMapping.setCreateDate(Integer.valueOf(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN)));
            xieChengCollidingDataHitRequestNoMappingMapper.insertSelective(requestNoMapping);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程撞库保存映射表异常！"), e);
        }
    }

}
