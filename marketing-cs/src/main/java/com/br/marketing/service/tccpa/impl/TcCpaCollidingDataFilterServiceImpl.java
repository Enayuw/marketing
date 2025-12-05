package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.TcyrCpaCollidingTask;
import com.br.marketing.entity.TcyrCpaCollidingTaskExample;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaCollidingDealStatusEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TcCpaCollidingDataFilterServiceImpl implements TcCpaCollidingDataFilterService {

    private final static String TITLE = "【同程易融CPA-撞库数据过滤Job】";

    @Resource
    TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Override
    public void process() {
        //1.查询统计完成的任务
        TcyrCpaCollidingTaskExample taskExample = new TcyrCpaCollidingTaskExample();
        taskExample.createCriteria()
                .andCollidingDateEqualTo(new Date())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT)
                .andStatusLessThanOrEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
        List<TcyrCpaCollidingTask> tasks = tcyrCpaCollidingTaskMapper.selectByExample(taskExample);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        //2.

    }
}
