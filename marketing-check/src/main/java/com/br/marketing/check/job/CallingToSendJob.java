package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.halo.HaluoApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.vo.HaloCallingDataVo;
import com.br.marketing.vo.HaloCallingLocalFileDataVo;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class CallingToSendJob extends AbstractSimpleElasticJob {

    @Value("${api.halo.method}")
    private String method;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Resource
    CustomerCallingMapper customerCallingMapper;

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @Resource
    CustomerCallingDataStatusMapper customerCallingDataStatusMapper;

    @Resource
    CustomerCallingPushLogMapper customerCallingPushLogMapper;

    @Resource
    HaluoApiServiceClient haluoApiServiceClient;

    @Resource
    LocalFileMapper localFileMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        this.process(getCustomerCallings());
    }

    private void process(List<CustomerCalling> customerCallings) {
        log.warn("1用户信息：{}", customerCallings);
        for (CustomerCalling customerCalling : customerCallings) {
            String tableColumns = getTableColumns(customerCalling);

            if (tableColumns != null) {
                doThreadSubmit(customerCalling, tableColumns);
            }

        }
    }

    private void doThreadSubmit(CustomerCalling customerCalling, String tableColumns) {
        ThreadPoolExecutor pushExecutor;
        if (customerCalling.getPushThreadNum() != null) {
            pushExecutor = BrExecutors.getThreadPool(customerCalling.getPushThreadNum(), customerCalling.getPushThreadNum());
        } else {
            pushExecutor = BrExecutors.getThreadPool(5, 5);
        }
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("columns", tableColumns);
        cusMap.put("apiCode", customerCalling.getApiCode());
        cusMap.put("sendStatus", 0);
        // 查询需要发送的数据
        int haloCallingCount = customerCallingDialogMapper.getHaloCallingCount(cusMap);
        if (haloCallingCount <= 0) {
            return;
        }
        // 更新文件记录推送开始时间
        HaloCallingLocalFileDataVo haloCallingLocalFileDataVo = customerCallingDialogMapper.getNewOne();
        Long localId = haloCallingLocalFileDataVo.getLocalId();
        LocalFile localFile = new LocalFile();
        localFile.setId(localId);
        localFile.setPushStartTime(new Date());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        CountDownLatch countDownLatch = new CountDownLatch(haloCallingCount);
        boolean index = true;
        String taskId = null;
        while (index) {
            cusMap.put("pageSize",2000);
            List<HaloCallingDataVo> haloCallingDataVoList = customerCallingDialogMapper.getInfoByColumns(cusMap);
            index = haloCallingDataVoList.size() != 0;
            if (index) {
                if (taskId == null) {
                    taskId = haloCallingDataVoList.get(0).getTaskId();
                }
                updateSendStatus(haloCallingDataVoList);
                List<List<HaloCallingDataVo>> partitions = Lists.partition(haloCallingDataVoList, 20);
                partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(
                        new CallingDataThread(
                                customerCallingDialogLists,
                                countDownLatch,
                                customerCallingDialogMapper,
                                customerCalling,
                                haluoApiServiceClient,
                                customerCallingPushLogMapper,
                                customerCallingDataStatusMapper,
                                method)));
            }
        }
        // 等待线程执行完毕
        try {
            countDownLatch.await();
            log.warn("线程执行完毕");
        } catch (InterruptedException e) {
            log.error("countDownLatch 线程执行异常", e);
        }

        callbackEnd(haloCallingCount, customerCalling.getApiCode(), taskId,localId);
    }

    public void callbackEnd(int haloCallingCount, String apiCode, String taskId,Long localId) {
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("sendStatus", 2);
        cusMap.put("apiCode", apiCode);
        cusMap.put("taskId", taskId);
        int haloCallingDealCount = customerCallingDialogMapper.getHaloCallingCount(cusMap);
        if (haloCallingDealCount == haloCallingCount) {
            JSONObject param = new JSONObject();
            param.put("openSerialNo", apiCode + "_callbackEnd_" + UUID.randomUUID());
            param.put("batchNo", taskId);

            ReqHaluoApiDTO reqHaluoApiDTO = new ReqHaluoApiDTO();
            reqHaluoApiDTO.setData(param.toJSONString());
            reqHaluoApiDTO.setMethod("hello.finance.loan.marketing.callback.end");
            Result<String> stringResult = haluoApiServiceClient.postHaluoOpenApi(reqHaluoApiDTO);
            log.warn("哈罗数据 批次:{}, 总量: {}, 处理成功: {}，处理结果: {}", taskId, haloCallingCount, haloCallingDealCount, JSON.toJSON(stringResult));
        }
        //
        LocalFile localFile = new LocalFile();
        localFile.setId(localId);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(haloCallingCount);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        try {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(apiCode).concat("\r\n"))
                    .append("taskId：".concat(taskId).concat("\r\n"))
                    .append(String.format("数据总量: %d,回调成功数量：%d", haloCallingCount, haloCallingDealCount));
            alarmClient.sendAlarm(content.toString(), "哈罗用户接收数据结束通知接口任务",
                    AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    private void updateSendStatus(List<HaloCallingDataVo> haloCallingDataVoList) {
        List<Long> ids = haloCallingDataVoList
                .stream()
                .map(HaloCallingDataVo::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setSendStatus(1);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

    private String getTableColumns(CustomerCalling customerCalling) {
        String column = customerCalling.getApiColumnsDetail();
        if (column != null && !column.isEmpty()) {
            String[] columns = column.split(",");
            List<String> columnsList = new ArrayList<>();
            Arrays.stream(columns).sequential().forEach(c -> {
                if("callStartTime".equals(c)){
                    c="UNIX_TIMESTAMP(call_start_time) as callStartTime";
                    columnsList.add(c);
                } else if("customerNo".equals(c)){
                    c="case_num as customerNo";
                    columnsList.add(c);
                }else {
                    c = "groupType".equals(c) ? "userType" : c;
                    columnsList.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, c));
                }
            });
            return Joiner.on(",").join(columnsList);
        }
        return null;
    }


    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }


}
