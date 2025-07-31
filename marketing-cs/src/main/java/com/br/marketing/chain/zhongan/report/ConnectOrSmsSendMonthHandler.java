package com.br.marketing.chain.zhongan.report;

import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.mapper.ZhongAnCollidingDataLogMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
    public boolean check(String cellMd5, String userType, String bizDate) throws Exception {
        List<Long> connectIds =
                Optional.ofNullable(zhongAnCollidingDataLogMapper.getConnectIdsByMonth(cellMd5, userType, bizDate)).orElse(Collections.emptyList());
        List<Long> smsSendIds =
                Optional.ofNullable(zhongAnCollidingDataLogMapper.getSmsIdsByMonth(cellMd5, userType, bizDate)).orElse(Collections.emptyList());
        long distinctCount = Stream.concat(connectIds.stream(), smsSendIds.stream()).distinct().count();
        return distinctCount < 8;
    }

    @Override
    public String ruleName() {
        return "ConnectOrSmsSendMonthHandler";
    }
}
