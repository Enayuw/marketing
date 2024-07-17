package com.br.marketing.service.Impl.wuba;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.wuba.WubaQueryConversionDto;
import com.br.marketing.entity.WubaCollidingBatchNo;
import com.br.marketing.entity.WubaCollidingBatchNoExample;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description 58新客修改营销名单上报批次
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaChangeQueryBatchService {

    private static final String TITLE = "【58新客修改营销名单上报批次】";

    @Resource
    private WubaCollidingBatchNoMapper batchNoMapper;


    public Result action(Page2Condition<WubaQueryConversionDto> condition) {
        return scanData(condition);
    }

    public Result scanData(Page2Condition<WubaQueryConversionDto> condition) {
        Result result = new Result<>().failure();

        // 扫描批次, BatchType 2-上报
        WubaQueryConversionDto param = condition.getParam();
        Integer batchType = param.getBatchType();
        Date pushTimeStart = param.getPushTimeStart();
        Date pushTimeEnd = param.getPushTimeEnd();

        WubaCollidingBatchNo batchUpdate = new WubaCollidingBatchNo();
        batchUpdate.setQueryStatus(0);
        WubaCollidingBatchNoExample batchExample = new WubaCollidingBatchNoExample();
        batchExample.createCriteria().andBatchTypeEqualTo(batchType)
                .andPushTimeGreaterThanOrEqualTo(pushTimeStart)
                .andPushTimeLessThan(pushTimeEnd)
                .andIsDeletedEqualTo(0);
        int i = batchNoMapper.updateByExampleSelective(batchUpdate, batchExample);
        log.warn(TITLE+"修改上报批次状态为未查询成功, 条数{}", i);

        return result.success();
    }
}
