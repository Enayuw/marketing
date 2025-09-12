package com.br.marketing.service.Impl.sanliuling;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingSanLiuLingCollection;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.MarketingSanLiuLingCollectionMapper;
import com.br.marketing.service.PushInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName SanLiuLingCollectionServiceImpl
 * @Description 360催收业务数据清洗服务实现
 * 
 * 性能优化说明：
 * 1. 分页查询applicationId：避免千万级数据一次性加载到内存
 * 2. 线程池并发处理：每个applicationId分组独立处理，提高效率
 * 3. 数据顺序保证：通过ORDER BY application_id确保分页查询的数据顺序一致性
 * 
 * 状态管理说明：
 * 1. cleanStatus状态流转：0(待清洗) -> 1(清洗中) -> 2(清洗完成)
 * 2. 状态更新时机：
 *    - 捞取数据前：0 -> 1 (防止重复处理)
 *    - 推送完成后：1 -> 2 (标记完成)
 *    - 异常发生时：1 -> 0 (回滚待重新处理)
 * 3. 并发安全：通过数据库行锁和状态检查确保同一applicationId不会被重复处理
 * 
 * @Author kongbx
 * @Date 2025/9/12 16:53
 */
@Service
@Slf4j
public class SanLiuLingCollectionServiceImpl implements SanLiuLingCollectionService {

    @Resource
    MarketingSanLiuLingCollectionMapper marketingSanLiuLingCollectionMapper;
    @Autowired
    PushInfoService pushInfoService;
    @Autowired
    SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;
    
    private final static String TITLE = "【360-催收业务】";
    
    /**
     * 分页查询applicationId的页面大小，避免内存溢出
     */
    private final static int APPLICATION_ID_PAGE_SIZE = 1000;

    @Override
    public void cleanData(String apiCode) {
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        String receiveDate = LocalDate.now().toString();
        Integer cleanStatus = DataCleanStatusEnum.READY.getCode();

        // 1. 先查询总数量，避免一次性加载所有applicationId到内存
        Long totalCount = marketingSanLiuLingCollectionMapper.countDistinctApplicationIds(
                apiCode, receiveDate, cleanStatus);
        
        if (totalCount == null || totalCount == 0) {
            log.warn(TITLE + "查询待清洗数据为空，apiCode：{}", apiCode);
            return;
        }
        
        log.warn(TITLE + "共查询到{}个不重复的applicationId，apiCode：{}", totalCount, apiCode);

        // 2. 分页查询并处理applicationId，支持千万级数据量
        long offset = 0;
        int processedCount = 0;
        
        while (offset < totalCount) {
            // 分页查询applicationId列表，通过ORDER BY确保数据顺序一致性
            List<String> applicationIds = marketingSanLiuLingCollectionMapper.selectDistinctApplicationIdsWithPaging(
                    apiCode, receiveDate, cleanStatus, offset, APPLICATION_ID_PAGE_SIZE);
            
            if (CollectionUtils.isEmpty(applicationIds)) {
                log.warn(TITLE + "分页查询applicationId为空，offset：{}, pageSize：{}", offset, APPLICATION_ID_PAGE_SIZE);
                break;
            }
            
            log.warn(TITLE + "分页查询到{}个applicationId，offset：{}, pageSize：{}", 
                    applicationIds.size(), offset, APPLICATION_ID_PAGE_SIZE);

            // 3. 按applicationId分组处理数据
            for (String applicationId : applicationIds) {
                pushPool.submit(() -> processApplicationIdData(apiCode, receiveDate, applicationId));
                processedCount++;
            }
            
            // 更新偏移量
            offset += APPLICATION_ID_PAGE_SIZE;
        }
        
        log.warn(TITLE + "总共提交{}个applicationId处理任务", processedCount);
    }

    /**
     * 处理单个applicationId的数据
     */
    private void processApplicationIdData(String apiCode, String receiveDate, String applicationId) {
        try {
            // 1. 先将状态更新为清洗中(1)，防止重复处理
            int updateCount = marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationId(
                    apiCode, receiveDate, applicationId, 
                    DataCleanStatusEnum.READY.getCode(), 
                    DataCleanStatusEnum.RUNNING.getCode());
            
            if (updateCount == 0) {
                log.warn(TITLE + "applicationId: {} 状态更新失败，可能已被其他线程处理", applicationId);
                return;
            }
            
            // 2. 查询该applicationId下的所有数据（状态已更新为清洗中）
            List<MarketingSanLiuLingCollection> collectionList = marketingSanLiuLingCollectionMapper.selectByApplicationId(
                    apiCode, receiveDate, DataCleanStatusEnum.RUNNING.getCode(), applicationId);
            
            if (CollectionUtils.isEmpty(collectionList)) {
                log.warn(TITLE + "applicationId: {} 下无数据，跳过处理", applicationId);
                // 如果没有数据，将状态回滚为待清洗
                marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationId(
                        apiCode, receiveDate, applicationId,
                        DataCleanStatusEnum.RUNNING.getCode(),
                        DataCleanStatusEnum.READY.getCode());
                return;
            }

            log.warn(TITLE + "处理applicationId: {}, 数据量: {}", applicationId, collectionList.size());

            // 构建上传数据
            List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
            String taskId = null;
            String batchNo = null;

            for (MarketingSanLiuLingCollection collection : collectionList) {
                if (taskId == null) {
                    taskId = collection.getTaskId();
                    batchNo = collection.getBatchNo();
                }

                MarketingPreUserDetailDTO detailDTO = buildMarketingPreUserDetailDTO(collection);
                if (detailDTO != null) {
                    syncUsers.add(detailDTO);
                }
            }

            if (CollectionUtils.isEmpty(syncUsers)) {
                log.warn(TITLE + "applicationId: {} 下无有效数据，跳过上传", applicationId);
                // 无有效数据时，将状态回滚为待清洗
                marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationId(
                        apiCode, receiveDate, applicationId,
                        DataCleanStatusEnum.RUNNING.getCode(),
                        DataCleanStatusEnum.READY.getCode());
                return;
            }

            // 构建上传对象
            MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
            marketingPreUserDTO.setTaskId(taskId);
            marketingPreUserDTO.setRequestId(batchNo);
            marketingPreUserDTO.setDataItems(syncUsers);

            UploadDataDTO uploadDataDTO = new UploadDataDTO();
            uploadDataDTO.setApiCode(apiCode);
            uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));

            // 3. 执行推送
            log.warn(TITLE + "开始推送applicationId: {} 的数据，数据量: {}", applicationId, syncUsers.size());
            pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            
            // 4. 推送成功后，将状态更新为清洗完成(2)
            int completedCount = marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationId(
                    apiCode, receiveDate, applicationId,
                    DataCleanStatusEnum.RUNNING.getCode(),
                    DataCleanStatusEnum.COMPLETE.getCode());
            
            if (completedCount > 0) {
                log.warn(TITLE + "完成推送applicationId: {} 的数据，已更新{}条记录状态为完成", applicationId, completedCount);
            } else {
                log.error(TITLE + "推送完成但状态更新失败，applicationId: {}", applicationId);
            }

        } catch (Exception e) {
            log.error(TITLE + "处理applicationId: {} 数据时发生异常", applicationId, e);
            
            // 发生异常时，尝试将状态回滚为待清洗，便于重新处理
            try {
                marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationId(
                        apiCode, receiveDate, applicationId,
                        DataCleanStatusEnum.RUNNING.getCode(),
                        DataCleanStatusEnum.READY.getCode());
                log.warn(TITLE + "异常回滚：applicationId: {} 状态已回滚为待清洗", applicationId);
            } catch (Exception rollbackException) {
                log.error(TITLE + "异常回滚失败，applicationId: {}", applicationId, rollbackException);
            }
        }
    }

    /**
     * 构建MarketingPreUserDetailDTO对象
     */
    private MarketingPreUserDetailDTO buildMarketingPreUserDetailDTO(MarketingSanLiuLingCollection collection) {
        try {
            MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
            
            // 设置基本字段
            detailDTO.setCell(collection.getPhone());
            detailDTO.setName(collection.getCustomerName());
            detailDTO.setCustNum(collection.getApplicationId());
            
            // 构建业务保留字段
            Map<String, Object> reserveField1 = new HashMap<>();
            reserveField1.put("caseCode", collection.getCaseCode());
            reserveField1.put("productType", collection.getProductType());
            reserveField1.put("prologueRemark", collection.getPrologueRemark());
            reserveField1.put("phoneLabel", collection.getPhoneLabel());
            
            if (!StringUtils.isEmpty(collection.getSpeechParamSet())) {
                reserveField1.put("speechParamSet", collection.getSpeechParamSet());
            }
            
            detailDTO.setReserveField1(JSON.toJSONString(reserveField1));
            
            // 生成数据指纹
            try {
                detailDTO.setFingerprint(snowflakeRedisGeneratorHandle.nextId());
            } catch (Exception e) {
                log.error(TITLE + "生成数据指纹失败，applicationId: {}", collection.getApplicationId(), e);
            }
            
            return detailDTO;
        } catch (Exception e) {
            log.error(TITLE + "构建MarketingPreUserDetailDTO失败，applicationId: {}", collection.getApplicationId(), e);
            return null;
        }
    }

}
