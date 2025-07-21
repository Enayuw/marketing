package com.br.marketing.chain.zhongan.report;

import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.mapper.ZhongAnCollidingDataLogMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectOrSmsSendMonthHandler implements ZhongAnReportHandler {

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
    public boolean check(String cellMd5, String bizDate) throws Exception {
        int connectCount = zhongAnCollidingDataLogMapper.countConnectByMonth(cellMd5, bizDate);
        int smsSendCount = zhongAnCollidingDataLogMapper.countSmsSendByMonth(cellMd5, bizDate);
        return connectCount + smsSendCount < 8;
    }
}
