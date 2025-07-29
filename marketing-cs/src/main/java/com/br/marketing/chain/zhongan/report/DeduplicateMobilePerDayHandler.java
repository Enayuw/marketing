package com.br.marketing.chain.zhongan.report;

import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.mapper.ZhongAnCollidingDataLogMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeduplicateMobilePerDayHandler implements ZhongAnReportHandler {

    @Resource
    private ZhongAnCollidingDataLogMapper zhongAnCollidingDataLogMapper;

    /**
     * 执行一次检查
     *
     * @param cellMd5 cellMd5
     * @param bizDate bizDate
     * @return boolean
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2025/07/22
     */
    @Override
    public boolean check(String cellMd5, String userType, String bizDate) throws Exception {
        int count = zhongAnCollidingDataLogMapper.countMobilePerDay(cellMd5, userType, bizDate);
        return count == 0;
    }

    @Override
    public String ruleName() {
        return "DeduplicateMobilePerDayHandler";
    }
}
