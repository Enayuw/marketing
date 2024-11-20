package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengBlackList;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.mapper.XieChengBlackListMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EncAndDecUtil;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class XieChengBlackEncAndDecJobServiceImpl implements XieChengBlackEncAndDecJobService {
    private final static int PAGE_SIZE = 2000;
    private final static int PARTITION_SIZE = 500;
    @Resource
    XieChengBlackListMapper blackListMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;

    @Override
    public void process() {
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengBlackEncAndDesThread(),
                        marketingCommonConfig.getXieChengBlackEncAndDesThread());
        Long minId = null;
        while (true) {
            List<XieChengBlackList> backList = blackListMapper.selectByPage(minId, PAGE_SIZE);
            if (backList.isEmpty()) {
                break;
            }
            minId = backList.get(backList.size() - 1).getId();
            //加密转换(log解密,sha256加密)
            backList.forEach(t -> {
                CompletableFuture.runAsync(() -> {
                    String cell = "";
                    XieChengBlackList entity = new XieChengBlackList();
                    try {
                        cell = EncAndDecUtil.logTodigest(t.getPhoneNumEncoded().trim(), ThreeKeyEncryptEnum.sha256);
                        entity.setId(t.getId());
                        entity.setCellSha256(cell);
                        entity.setStatus(1);
                    } catch (Exception e) {
                        entity.setStatus(2);
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                                "携程撞库黑名单log解密-shar256加密异常,异常id=" + t.getId()), e);
                    }
                    blackListMapper.updateByPrimaryKeySelective(entity);
                }, threadPool);
            });
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库黑名单logCell解密,sha256加密：线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程撞库黑名单logCell解密,sha256加密：日志保存线程池结束异常！"), ex);
            Thread.currentThread().interrupt();
        }

    }

}
