package com.br.marketing.util;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定制化接口字段检查
 *
 * @author Guo Zeqiang
 * @dateTime 2023-12-22 14:12
 */
@Slf4j
public class ApiFieldCheckUtils {
    private static final Map<String, Set<String>> LOCAL_CACHE_FIELD_MAP = new ConcurrentHashMap<>(16);

    /**
     * 2023-12-22 15:39
     *
     * @param fieldSet          需要检查的字段集合
     * @param redisChgService   缓存客户端
     * @param customerNameShort 客户简称
     * @param apiNameShort      接口简称
     * @param apiCode           客户编号
     * @param requestId         请求id
     * @return 提示消息，非{@code null}时表示字段有新增，为{@code null}时表示没字段新增
     */
    public static String checkField(Set<String> fieldSet
            , final RedisChgService redisChgService
            , String apiCode
            , String customerNameShort
            , String apiNameShort
            , String requestId) {
        Assert.notNull(apiNameShort, "接口名称不可为空！");
        Assert.notNull(apiCode, "客户编号不可为空！");
        StringBuilder fieldStr = new StringBuilder();
        String localKey = apiNameShort.concat(":").concat(apiCode);
        String redisKey = RedisKeyConstant.CUSTOMER_FIELD_KEY.concat(localKey);
        String separator = "、";
        Set<String> localCacheFieldSet;
        if (LOCAL_CACHE_FIELD_MAP.containsKey(localKey)) {
            localCacheFieldSet = LOCAL_CACHE_FIELD_MAP.get(localKey);
        } else {
            localCacheFieldSet = Collections.newSetFromMap(new ConcurrentHashMap<>(32));
            LOCAL_CACHE_FIELD_MAP.put(localKey, localCacheFieldSet);
        }
        for (String field : fieldSet) {
            if (localCacheFieldSet.add(field)) {
                Long aLong = redisChgService.saddMember(redisKey, field);
                if (aLong == 1) {
                    fieldStr.append(fieldStr.length() > 0 ? separator : "\n").append(field);
                }
            }
        }
        if (fieldStr.length() > 0) {
            String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_NEW_FIELD_CHECK.getCode()
                    , "“" + apiNameShort + "”接口接收到客户编码为" + apiCode
                            + (StringUtils.isEmpty(customerNameShort) ? "" : ("(" + customerNameShort + ")"))
                            + "的请求"
                            + (StringUtils.isEmpty(requestId) ? "" : ("(" + requestId + ")"))
                            + "中有新增字段：".concat(fieldStr.toString())
                            .concat("\n请及时与客户沟通确认^_^")
                    , apiCode
                            + "(" + customerNameShort + ")"
                            + "定制化接口字段检查"
                            + AlarmSendCodeEnum.EXCEPTION_NEW_FIELD_CHECK.getMessage());
            log.warn(msg);
            Long rSum = redisChgService.scard(redisKey);
            if (rSum == null || rSum < localCacheFieldSet.size()) {
                redisChgService.sadd(redisKey, new ArrayList<>(localCacheFieldSet));
            }
            return msg;
        }
        return null;
    }
}
