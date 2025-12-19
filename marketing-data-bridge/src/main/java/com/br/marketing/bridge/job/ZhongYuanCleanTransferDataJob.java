package com.br.marketing.bridge.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.ZhongYuanTransfer;
import com.br.marketing.entity.ZhongYuanTransferExample;
import com.br.marketing.enums.ZhongYuanCleanStatusEnum;
import com.br.marketing.mapper.ZhongYuanTransferMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ZhongYuanCleanTransferDataJob
 * @Description 中原消金转化数据清洗Job
 * @Author kongbx
 * @Date 2025/11/24 15:36
 */
@Component
@Slf4j
public class ZhongYuanCleanTransferDataJob extends AbstractSimpleElasticJob {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ZhongYuanTransferMapper zhongYuanTransferMapper;
    @Resource
    private PushInfoService pushInfoService;
    @Resource
    private TrackingService trackingService;
    private static final String TITLE = "【中原消金转化数据清洗】";
    private static final Integer PUSH_NUM = 1000; // 单次推送最大数量


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            // 1. 从配置获取中原消金的apiCode
            String apiCode = jobExecutionMultipleShardingContext.getJobParameter();
            if(StringUtils.isEmpty(apiCode)){
                apiCode = getZhongYuanApiCode();
            }

            if (apiCode == null) {
                log.warn("{}Job执行失败：未配置apiCode", TITLE);
                return;
            }

            log.warn("{}开始执行，apiCode: {}", TITLE, apiCode);

            // 2. 查询待清洗数据
            List<ZhongYuanTransfer> transferList = queryPendingCleanData(apiCode);

            if (CollectionUtils.isEmpty(transferList)) {
                log.warn("{}无待清洗数据", TITLE);
                return;
            }

            log.warn("{}查询到待清洗数据数量: {}", TITLE, transferList.size());

            // 3. 处理数据并推送
            processAndPushData(apiCode, transferList);

            try {
                JSONObject condition = new JSONObject();
                condition.put("clean_status", 0);
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金转化数据清洗"
                        , "b_marketing_zhongyuan_transfer"
                        , JSON.toJSONString(condition)
                        , Long.valueOf(transferList.size())
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }
            log.warn("{}执行完成", TITLE);

        } catch (Exception e) {
            log.error("{}Job执行异常", TITLE, e);
        }
    }

    /**
     * 查询待清洗数据
     */
    private List<ZhongYuanTransfer> queryPendingCleanData(String apiCode) {
        try {
            ZhongYuanTransferExample example = new ZhongYuanTransferExample();
            ZhongYuanTransferExample.Criteria criteria = example.createCriteria();
            criteria.andCleanStatusEqualTo(ZhongYuanCleanStatusEnum.PENDING.getValue());
            if (StringUtils.isNotBlank(apiCode)) {
                criteria.andApiCodeEqualTo(apiCode);
            }
            example.setOrderByClause("id ASC");
            return zhongYuanTransferMapper.selectByExample(example);
        } catch (Exception e) {
            log.error("{}查询待清洗数据异常", TITLE, e);
            return new ArrayList<>();
        }
    }

    /**
     * 处理数据并推送
     */
    private void processAndPushData(String apiCode, List<ZhongYuanTransfer> transferList) {
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        int successCount = 0;
        int failCount = 0;

        try {
            for (ZhongYuanTransfer transfer : transferList) {
                try {
                    // 更新状态为清洗中
                    updateCleanStatus(transfer.getId(), ZhongYuanCleanStatusEnum.PROCESSING.getValue());

                    // 解析taskuidList
                    List<String> taskuidList = parseTaskuidList(transfer.getTaskuidList());

                    if (CollectionUtils.isEmpty(taskuidList)) {
                        log.warn("{}数据ID: {}, taskuidList为空，跳过处理", TITLE, transfer.getId());
                        updateCleanStatus(transfer.getId(), ZhongYuanCleanStatusEnum.COMPLETED.getValue());
                        continue;
                    }

                    // 分批处理，每批最多1000条
                    List<List<String>> partitions = partitionList(taskuidList, PUSH_NUM);

                    for (List<String> partition : partitions) {
                        // 拼装转化数据
                        List<TransferDataItemDTO> dataItems = buildTransferDataItems(partition, apiCode, transfer.getOperation());

                        if (CollectionUtils.isEmpty(dataItems)) {
                            continue;
                        }

                        // 构建推送DTO
                        PushTransferDataDetailDTO dto = buildPushTransferDataDTO(apiCode, dataItems);

                        // 异步推送
                        pushPool.submit(() -> {
                            pushInfoService.pushTransferByRetry(dto, null);
                        });
                    }

                    // 更新状态为清洗完成
                    updateCleanStatus(transfer.getId(), ZhongYuanCleanStatusEnum.COMPLETED.getValue());
                    successCount++;

                } catch (Exception e) {
                    log.error("{}处理数据ID: {}异常", TITLE, transfer.getId(), e);
                    updateCleanStatus(transfer.getId(), ZhongYuanCleanStatusEnum.PENDING.getValue()); // 失败后恢复为待清洗状态
                    failCount++;
                }
            }

            log.warn("{}处理完成，成功: {}, 失败: {}", TITLE, successCount, failCount);

        } catch (Exception e) {
            log.error("{}处理数据异常", TITLE, e);
        } finally {
            // 等待所有推送任务完成，放在finally中确保线程池被正确关闭
            pushPool.shutdown();
            try {
                if (!pushPool.awaitTermination(60L, TimeUnit.SECONDS)) {
                    log.warn("{}线程池未在60秒内完成，强制关闭", TITLE);
                    pushPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("{}-线程池中断异常-", TITLE, e);
                pushPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 解析taskuidList
     * 支持JSON数组格式和逗号分隔字符串格式
     */
    private List<String> parseTaskuidList(String taskuidListStr) {
        List<String> result = new ArrayList<>();

        if (StringUtils.isBlank(taskuidListStr)) {
            return result;
        }

        try {
            // 尝试解析为JSON数组
            if (taskuidListStr.trim().startsWith("[")) {
                JSONArray jsonArray = JSONArray.parseArray(taskuidListStr);
                for (int i = 0; i < jsonArray.size(); i++) {
                    String taskuid = jsonArray.getString(i);
                    if (StringUtils.isNotBlank(taskuid)) {
                        result.add(taskuid.trim());
                    }
                }
            } else {
                // 按逗号分隔
                String[] taskuids = taskuidListStr.split(",");
                for (String taskuid : taskuids) {
                    if (StringUtils.isNotBlank(taskuid)) {
                        result.add(taskuid.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}解析taskuidList异常: {}", TITLE, taskuidListStr, e);
        }

        return result;
    }

    /**
     * 拼装转化数据项
     */
    private List<TransferDataItemDTO> buildTransferDataItems(List<String> taskuidList, String apiCode, String operation) {
        List<TransferDataItemDTO> dataItems = new ArrayList<>();

        for (String taskuid : taskuidList) {
            if (StringUtils.isBlank(taskuid)) {
                continue;
            }

            TransferDataItemDTO item = new TransferDataItemDTO();
            // custNum对应taskuid
            item.setCustNum(taskuid.trim());
            // userType固定为"1"（机构运营场景）
            item.setUserType("1");
            // 构建reserveField1 JSON
            JSONObject reserveField1 = new JSONObject();
            // 如果operation=cancel，默认isBlack=1
            if ("cancel".equalsIgnoreCase(operation)) {
                reserveField1.put("isBlack", "1");
            }
            item.setReserveField1(reserveField1.toJSONString());
            item.setApiCode(apiCode);

            dataItems.add(item);
        }

        return dataItems;
    }

    /**
     * 构建推送转化数据DTO
     */
    private PushTransferDataDetailDTO buildPushTransferDataDTO(String apiCode, List<TransferDataItemDTO> dataItems) {
        TransferDataDTO<TransferDataItemDTO> transferDataDTO = new TransferDataDTO<>();

        // requestId格式: apicode + 时间戳(毫秒级) + 五位以上随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomNum = RandomStringUtils.randomNumeric(5);
        String requestId = apiCode + timestamp + randomNum;
        transferDataDTO.setRequestId(requestId);

        transferDataDTO.setDataItems(dataItems);

        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));

        return dto;
    }

    /**
     * 更新清洗状态
     */
    private void updateCleanStatus(Long id, Integer cleanStatus) {
        try {
            ZhongYuanTransfer record = new ZhongYuanTransfer();
            record.setId(id);
            record.setCleanStatus(cleanStatus);
            record.setUpdateTime(new Date());
            zhongYuanTransferMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            log.error("{}更新清洗状态异常，ID: {}, status: {}", TITLE, id, cleanStatus, e);
        }
    }

    /**
     * 列表分批
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return partitions;
        }

        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            partitions.add(list.subList(i, end));
        }

        return partitions;
    }

    /**
     * 从配置获取中原消金的apiCode
     */
    private String getZhongYuanApiCode() {
        try {
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            return zhongYuanIdentity != null ? zhongYuanIdentity.get("apiCode") : null;
        } catch (Exception e) {
            log.error("{}获取apiCode异常", TITLE, e);
            return null;
        }
    }

}
