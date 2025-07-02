package com.br.marketing.service.Impl.umeng;

import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.mapper.UMengTimingTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UMengTimingTaskServiceImpl implements IUMengTimingTaskService {

    @Resource
    private UMengTimingTaskMapper umengTimingTaskMapper;

    @Override
    public void insertSelective(UMengTimingTask uMengTimingTask) {
         umengTimingTaskMapper.insertSelective(uMengTimingTask);
    }

    @Override
    public UMengTimingTask getTodayLastTask(Long localId, String apiCode) {
        return umengTimingTaskMapper.getTodayLastTask(localId,apiCode);
    }

    @Override
    public UMengTimingTask getDataByTaskId(String taskId) {
        return umengTimingTaskMapper.getDataByTaskId(taskId);
    }
}
