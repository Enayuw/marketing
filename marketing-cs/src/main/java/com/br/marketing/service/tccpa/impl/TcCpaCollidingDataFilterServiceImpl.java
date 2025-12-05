package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.TcyrCpaCollidingTask;
import com.br.marketing.entity.TcyrCpaCollidingTaskExample;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaCollidingDealStatusEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process() {
        //1.查询统计完成和待统计的撞库任务
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
        //2.遍历撞库任务
        for (TcyrCpaCollidingTask task : tasks) {
            process(task);
        }
    }

    /**
     * 处理撞库任务
     * @param task
     */
    private void process(TcyrCpaCollidingTask task) {
        //1.对于新创建和重新统计的任务，在过滤前进行统计
        if(task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()){
            //todo 更新量级的方法
        }
        //2.更新撞库任务状态为3-筛选中
        task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
        tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        //3.过滤撞库数据
        filter(task);
    }


    private void filter(TcyrCpaCollidingTask task) {

        task.getSupplyRuleInfo();
    }
}
