package com.br.marketing.service.mark.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataUpdateEsMarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @ClassName DataUpdateEsMarkServiceImpl
 * @Description pp停车文件数据更新es数据信息实现
 * @Author kongbx
 * @Date 2025/2/19 15:16
 */
@Service
@Slf4j
public class DataUpdateEsMarkServiceImpl implements DataUpdateEsMarkService {
    @Autowired
    RedisChgService redisChgService;
    @Resource
    FlagDataMapper flagDataMapper;

    @Override
    public void process() {

        String key = RedisKeyConstant.DATA_UPDATE_ES_MARK.concat(":").concat("api_code值");
        String lockValue = UUID.randomUUID().toString();
        try {
            boolean lock = redisChgService.lock(key, lockValue, 5000L);
            if (lock) {
                // 查询打标表数据 apiCode+UserType+本日+es状态为初始状态
                // 更新es状态为同步中
                // 释放锁
                // 找到最新文件 batchNumber+fieldId
                // 更新ES
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "pp停车文件数据更新es数据抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
            redisChgService.unlock(key, lockValue);
        }
    }

}
