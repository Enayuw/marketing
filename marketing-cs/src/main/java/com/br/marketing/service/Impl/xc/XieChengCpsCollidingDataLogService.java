package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.XieChengCpsCollidingDataLog;

import java.util.List;

public interface XieChengCpsCollidingDataLogService {

    /**
     * 构造撞库正常log
     *
     * @param id id
     * @param packageId packageId
     * @param packageRuleId packageRuleId
     * @param dataSourceType 数据源类型 T True数据,F False数据
     * @param returnData 返回数据
     * @param httpcode httpcode
     * @param businessCode 客户返回Code码
     * @return {@link XieChengCpsCollidingDataLog }
     * @date 2024/03/23
     */
    XieChengCpsCollidingDataLog buildSuccessXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
        JSONObject returnData, String httpcode, Integer businessCode);

    /**
     * 构建失败谢程碰撞数据日志
     *
     * @param id id
     * @param packageId packageId
     * @param packageRuleId packageRuleId
     * @param dataSourceType 数据源类型 T True数据,F False数据
     * @param cellSha256CodeList 手机号
     * @param resJson res json
     * @return {@link XieChengCpsCollidingDataLog }

     * @date 2024/03/23
     */
    XieChengCpsCollidingDataLog buildFailXieChengCpsCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
        String cellSha256CodeList, JSONObject resJson);

    /**
     * 生产者-推送保存log消息
     * @param collidingLogs 碰撞日志
     * @date 2024/03/23
     */
    void pushLogMessage(List<XieChengCpsCollidingDataLog> collidingLogs);

    /**
     * 生产者-推送外呼消息
     * @param collidingLogs 碰撞日志
     * @date 2024/03/23
     */
    void pushRobotMessage(List<XieChengCpsCollidingDataLog> collidingLogs);

    /**
     * 消费者-批量保存撞库log
     *
     * @param collidingLogs 碰撞日志
     * @return {@link Result }<{@link Boolean }>
     * @date 2024/03/23
     */
    Result<Boolean> saveXieChengCpsCollidingDataLog(List<XieChengCpsCollidingDataLog> collidingLogs);
}
