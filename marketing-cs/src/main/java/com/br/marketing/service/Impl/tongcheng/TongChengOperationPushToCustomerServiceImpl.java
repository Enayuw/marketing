package com.br.marketing.service.Impl.tongcheng;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.tongcheng.TongChengAgentMktClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TongChengAgentMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 同程集团迁移可营销名单推送客户实现类
 * @author guangxiu.li
 * @dateTime 2024/01/25 16:13
 */
@Service
@Slf4j
public class TongChengOperationPushToCustomerServiceImpl implements TongChengOperationPushToCustomerService {
    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    TongChengAgentMapper tongChengAgentMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    TongChengAgentMktClient tongChengAgentMktClient;

    @Override
    public void process(LocalFile localFile) {
        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        String apiCode = localFile.getApiCode();
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            if (marketingCommonConfig.getTongChengGroupOperationThreadNum() != null) {
                pool.setCorePoolSize(marketingCommonConfig.getTongChengGroupOperationThreadNum());
                pool.setMaximumPoolSize(marketingCommonConfig.getTongChengGroupOperationThreadNum());
            }

            List<TongChengAgent> tongchengAgentList = tongChengAgentMapper.tongChengGroupOperationDataPage(localFile.getId(), minId);
            if (tongchengAgentList.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }

            minId = tongchengAgentList.get(tongchengAgentList.size() - 1).getId();
            List<TongChengAgent> list= Collections.synchronizedList(tongchengAgentList);
            pool.submit(() -> buildDataAndPush(list, apiCode));
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
        TongChengAgentExample tongchengAgentExample = new TongChengAgentExample();
        tongchengAgentExample.createCriteria().andLocalIdEqualTo(localFile.getId()).andPushStatusEqualTo(2).andStatusEqualTo(1);
        int num = tongChengAgentMapper.countByExample(tongchengAgentExample);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(num);
        //更新状态推送成功
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        //统计告警
        if (!localFile.getPushNumber().equals(localFile.getActualNumber())) {
            sendAlarm(localFile.getActualNumber() - localFile.getPushNumber(), "同程集团运营名单推送客户接口失败数量统计");
        }
    }

    private void buildDataAndPush(List<TongChengAgent> tongchengAgents, String apiCode) {
        try {
            List<Map<String,String>> dataLists = null;
            List<Long> ids = null;
            for (TongChengAgent data : tongchengAgents) {
                String mobileMd5 = data.getMobileMd5();
                // 查询是否已推送
                TongChengAgent newData = tongChengAgentMapper.selectByMobileMd5(mobileMd5);
                int pushStatus = newData.getPushStatus();
                if (pushStatus == 3){
                    continue;
                }
                newData.setPushStatus(1);
                tongChengAgentMapper.updateByPrimaryKeySelective(newData);
                ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
                map.put("mobileMd5", mobileMd5);
                dataLists.add(map);
            }
            Result result = tongChengAgentMktClient.pushToTongChengAgentMkt(dataLists, apiCode,null);
            // 更新数据表状态
            ids = tongchengAgents.stream().map(t -> t.getId()).collect(Collectors.toList());
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                //更新成功
                updateStatus(ids, 2);
            } else {
                //更新失败
                updateStatus(ids, 3);
            }
        } catch (Exception ex) {
            log.error("同程集团运营名单推送客户接口子线程异常", ex);
        }
    }

    private void updateStatus(List<Long> ids, int status) {
        if (ids.size() > 0) {
            TongChengAgentExample updateExample = new TongChengAgentExample();
            updateExample.createCriteria().andIdIn(ids);
            TongChengAgent record = new TongChengAgent();
            record.setPushStatus(status);
            if (status == 3){
                record.setDataMessage("重复推送");
            }
            tongChengAgentMapper.updateByExampleSelective(record, updateExample);
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
