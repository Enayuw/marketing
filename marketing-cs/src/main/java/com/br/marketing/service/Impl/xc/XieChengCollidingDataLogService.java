package com.br.marketing.service.Impl.xc;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.XieChengCollidingDataLog;

public interface XieChengCollidingDataLogService {

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
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    XieChengCollidingDataLog buildSuccessXieChengCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
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
     * @return {@link XieChengCollidingDataLog }
     * @author senyang.zheng
     * @date 2024/03/23
     */
    XieChengCollidingDataLog buildFailXieChengCollidingDataLog(Long id, Long packageId, Long packageRuleId, String dataSourceType,
        String cellSha256CodeList, JSONObject resJson);

    /**
     * 推送保存log消息
     *
     * @param collidingLogs 碰撞日志
     * @author senyang.zheng
     * @date 2024/03/23
     */
    void pushLogMessage(List<XieChengCollidingDataLog> collidingLogs);

    /**
     * 批量保存撞库log
     *
     * @param collidingLogs 碰撞日志
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/03/23
     */
    Result<Boolean> saveXieChengCollidingDataLog(List<XieChengCollidingDataLog> collidingLogs);
}
