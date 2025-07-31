package com.br.marketing.chain.zhongan.report;

import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.mapper.ZhongAnCollidingDataLogMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsSend2DaysHandler implements ZhongAnReportHandler {

    @Resource
    private ZhongAnCollidingDataLogMapper zhongAnCollidingDataLogMapper;

    /**
     * 执行一次检查
     *
     * @return true 表示通过；false 表示不通过，需要短路
     * @throws Exception 如果检查过程中有异常，可抛出，最终视为失败
     */
    @Override
    public boolean check(String cellMd5, String userType, String bizDate) throws Exception {
        int count = zhongAnCollidingDataLogMapper.countSmsSendSuccess(cellMd5, userType, bizDate);
        return count < 1;
    }

    @Override
    public String ruleName() {
        return "SmsSend2DaysHandler";
    }

}
