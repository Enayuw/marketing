package com.br.marketing.service.Impl.tongcheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.tongcheng.TongChengClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.TongChengUndoData;
import com.br.marketing.entity.TongChengUndoDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TongChengUndoDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 同程不运营名单推送客户实现类
 * @Author hong.chen
 * @CreateTime 2023/12/07
 */
@Service
@Slf4j
public class TongChengUndoListPushToCustomerServiceImpl implements TongChengUndoListPushToCustomerService {
    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    TongChengUndoDataMapper tongChengUndoDataMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    TongChengClient tongChengClient;

    @Override
    public void process(LocalFile localFile) {
        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            if (marketingCommonConfig.getTongChengUndoThreadNum() != null) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getTongChengUndoThreadNum());
            }

            // local_id = #{localId}  and status =1 正常 and push_status =1 未推送  2000
            List<TongChengUndoData> tongChengUndoDataList = tongChengUndoDataMapper.tongChengUndoDataPage(localFile.getId(), minId);
            if (tongChengUndoDataList.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }

            minId = tongChengUndoDataList.get(tongChengUndoDataList.size() - 1).getId();
            pool.submit(() -> buildDataAndPush(tongChengUndoDataList));
        }
        pool.shutdown();

        try {
            while (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        // 更新文件表状态并发送告警
        updateFileStatusAndSendAlarm(localFile);
    }

    private void updateFileStatusAndSendAlarm(LocalFile localFile) {
        //更新文件表推送数据量
        TongChengUndoDataExample TongChengUndoDataExample = new TongChengUndoDataExample();
        TongChengUndoDataExample.createCriteria().andLocalIdEqualTo(localFile.getId()).andPushStatusEqualTo(2).andStatusEqualTo(1);
        Long num = tongChengUndoDataMapper.countByExample(TongChengUndoDataExample);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(num.intValue());
        //更新状态推送成功
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        //统计告警
        if (!localFile.getPushNumber().equals(localFile.getActualNumber())) {
            sendAlarm(localFile.getActualNumber() - localFile.getPushNumber(), "同程不运营名单推送客户接口失败数量统计");
        }
    }

    private void buildDataAndPush(List<TongChengUndoData> tongChengUndoDataList) {
        try {
            Map<String, List<TongChengUndoData>> listMap = tongChengUndoDataList.stream().collect(Collectors.groupingBy(t -> t.getTaskId()));
            List<String> taskIds = listMap.keySet().stream().collect(Collectors.toList());
            log.warn("同程不运营名单推送客户，单批次taskId：{},size：{}", Joiner.on(",").join(taskIds), taskIds.size());

            for (Map.Entry<String, List<TongChengUndoData>> entry : listMap.entrySet()) {
                String taskId = entry.getKey();
                List<TongChengUndoData> dataList = entry.getValue();

                // 组装数据调接口
                JSONArray jsonArray = new JSONArray();
                dataList.forEach(tongChengUndoData -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("custNum", tongChengUndoData.getCustNum());
                    jsonObject.put("reason", tongChengUndoData.getReason());

                    jsonArray.add(jsonObject);
                });

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("taskId", taskId);
                jsonObject.put("dataList", jsonArray);
                log.warn("同程不运营名单推送客户接口，单次推送条数：{}，taskId：{}", jsonArray.size(), taskId);
                Result result = tongChengClient.pushToTongChengCustomer(jsonObject, null);

                // 更新数据表状态
                List<Long> ids = dataList.stream().map(t -> t.getId()).collect(Collectors.toList());
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    //更新成功
                    updateStatus(ids, 2);
                } else {
                    //更新失败
                    updateStatus(ids, 3);
                }
            }
        } catch (Exception ex) {
            log.error("同程不运营名单推送客户接口子线程异常", ex);
        }
    }

    private void updateStatus(List<Long> ids, int status) {
        if (ids.size() > 0) {
            TongChengUndoDataExample updateExample = new TongChengUndoDataExample();
            updateExample.createCriteria().andIdIn(ids);
            TongChengUndoData record = new TongChengUndoData();
            record.setPushStatus(status);
            tongChengUndoDataMapper.updateByExampleSelective(record, updateExample);
        }
    }

    private void sendAlarm(Integer failNum, String title) {
        if (failNum > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum, title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
