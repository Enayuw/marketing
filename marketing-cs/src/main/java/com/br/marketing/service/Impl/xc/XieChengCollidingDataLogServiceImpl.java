package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 撞库日志相关service
 *
 * @author senyang.zheng
 * @date 2024/03/23
 */
@Service
@Slf4j
public class XieChengCollidingDataLogServiceImpl implements XieChengCollidingDataLogService {

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RabbitMqProducter rabbitMqProducter;

    public static final ThreadPoolExecutor XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL = BrExecutors.getThreadPool(50, 50);

    /**
     * 构造撞库正常log
     *
     * @param id id
     * @param packageId packageId
     * @param dataSourceType 数据源类型 T True数据,F False数据
     * @param returnData 返回数据
     * @param httpcode httpcode
     * @param businessCode 客户返回Code码
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public XieChengCollidingDataLog buildSuccessXieChengCollidingDataLog(Long id, Long packageId, String dataSourceType, JSONObject returnData,
        String httpcode, Integer businessCode) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");
        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(id);
        xieChengCollidingDataLog.setPackageId(packageId);
        xieChengCollidingDataLog.setDataSourceType(dataSourceType);
        xieChengCollidingDataLog.setCellSha256CodeList(sha256Code);
        xieChengCollidingDataLog.setReleaseTime(releaseTime);
        xieChengCollidingDataLog.setOrgChannel(orgChannel);
        xieChengCollidingDataLog.setHttpCode(Integer.valueOf(httpcode));
        xieChengCollidingDataLog.setBusinessCode(businessCode);
        xieChengCollidingDataLog.setMktLevel(mktLevel);
        xieChengCollidingDataLog.setInfo(info);
        xieChengCollidingDataLog.setResult(result);
        xieChengCollidingDataLog.setReturnContent(returnData.toString(SerializerFeature.WriteMapNullValue));
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());
        return xieChengCollidingDataLog;
    }

    /**
     * 构建失败谢程碰撞数据日志
     *
     * @param id id
     * @param packageId packageId
     * @param dataSourceType 数据源类型 T True数据,F False数据
     * @param cellSha256CodeList 手机号
     * @param resJson res json
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public XieChengCollidingDataLog buildFailXieChengCollidingDataLog(Long id, Long packageId, String dataSourceType, String cellSha256CodeList,
        JSONObject resJson) {
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
     *
     * @param collidingLogs 碰撞日志
     * @author senyang.zheng
     * @date 2024/03/23
     */
    @Override
    public void pushLogMessage(List<XieChengCollidingDataLog> collidingLogs) {
        try {
            rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, JSONObject.toJSONString(collidingLogs));
        } catch (Exception e) {
            log.error("推送携程撞库日志消息异常", e);
        }
    }

    @Override
    public Result<Boolean> saveXieChengCollidingDataLog(List<XieChengCollidingDataLog> collidingLogs) {
        XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL.setMaximumPoolSize(marketingCommonConfig.getXiechengSaveCollidingLogThread());
        XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL.setCorePoolSize(marketingCommonConfig.getXiechengSaveCollidingLogThread());
        XIECHENG_SAVE_COLLIDING_LOG_THREAD_POOL.submit(() -> xieChengCollidingDataLogMapper.batchSave(collidingLogs));
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
