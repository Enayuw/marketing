package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author 贺东硕
 * @Date 2024/07/13 10:46
 * @Description:奇富360转化数据全量提取
 * 2024/07/09 https://c.100credit.cn/pages/viewpage.action?pageId=166648062
 */
@Slf4j
@Service
public class TransferToFileByQiFuFullServiceImpl extends AbstractTransferToFileByQiFuService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    String getExtractTime(String apiCode) {
        return marketingCommonConfig.getQiFuFullExtDataConfig().get(apiCode).getString("extTime");
    }

    @Override
    String getSuffix(String apiCode) {
        return marketingCommonConfig.getQiFuFullExtDataConfig().get(apiCode).getString("suffix");
    }

    @Override
    void writeQifuTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
        transferSyncUser.settCid(tcId);
        transferSyncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        for (; ; ) {
            List<Map<String, Object>> result = marketingTransferSyncUserMapper.selectFullTransferWithValid(transferSyncUser);
            if (CollectionUtils.isEmpty(result)) {
                break;
            }
            Long minId = Long.parseLong(String.valueOf(result.get(result.size() - 1).get("id"))) + 1;
            transferSyncUser.setId(minId);
            List<Map<String, Object>> extData = result.stream()
                    .filter(transfer -> transfer.get("taskId") != null)
                    .collect(Collectors.groupingBy(transfer -> transfer.get("id").toString()))
                    .values()
                    .stream()
                    .map((List<Map<String, Object>> transfers) ->
                            transfers.stream().max(Comparator.comparing(transfer ->
                                    transfer.getOrDefault("validEndDate", "").toString())).get()
                    ).collect(Collectors.toList());
            threadPool.submit(() -> {
                writeDataForOneQuery(fw, totalSize, extData);
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("奇富360转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("奇富360转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.error("奇富360转化数据提取-本地文件生成失败！" , e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
    }

    private void writeDataForOneQuery(Writer fw, AtomicInteger totalSize, List<Map<String, Object>> result) {
        for (Map<String, Object> data : result) {
            String custNum = ObjectUtils.isEmpty(data.get("custNum")) ?
                    "" : String.valueOf(data.get("custNum"));
            String applyDt = ObjectUtils.isEmpty(data.get("applyDt")) ?
                    "" : String.valueOf(data.get("applyDt")).replace(":000","");
            String applyResult = ObjectUtils.isEmpty(data.get("applyResult")) ?
                    "" : String.valueOf(data.get("applyResult"));
            String loginTime = ObjectUtils.isEmpty(data.get("loginTime")) ?
                    "" : String.valueOf(data.get("loginTime")).replace(":000","");
            String requestTime = ObjectUtils.isEmpty(data.get("requestTime")) ?
                    "" : String.valueOf(data.get("requestTime")).replace(":000","");
            String userType = ObjectUtils.isEmpty(data.get("userType")) ?
                    "" : String.valueOf(data.get("userType"));
            String taskId = ObjectUtils.isEmpty(data.get("taskId")) ?
                    "" : String.valueOf(data.get("taskId"));
            String expireDate = ObjectUtils.isEmpty(data.get("expireDate")) ?
                    "" : String.valueOf(data.get("expireDate"));
            String effectiveDate = ObjectUtils.isEmpty(data.get("effectiveDate")) ?
                    "" : String.valueOf(data.get("effectiveDate"));
            StringBuilder sb = new StringBuilder();
            sb.append(custNum.concat(","))
                    .append(applyDt.concat(","))
                    .append(applyResult.concat(","))
                    .append(loginTime.concat(","))
                    .append(requestTime.concat(","))
                    .append(userType.concat(","))
                    .append(taskId.concat(","))
                    .append(expireDate.concat(","))
                    .append(effectiveDate)
                    .append("\r\n");
            try {
                fw.append(sb.toString());
                totalSize.incrementAndGet();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
