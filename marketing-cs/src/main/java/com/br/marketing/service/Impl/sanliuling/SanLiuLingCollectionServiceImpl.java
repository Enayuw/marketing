package com.br.marketing.service.Impl.sanliuling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.sanliuling.request.ContactListDTO;
import com.br.marketing.entity.MarketingCustomerConfig;
import com.br.marketing.entity.MarketingCustomerConfigExample;
import com.br.marketing.entity.MarketingSanLiuLingCollection;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.MarketingCustomerConfigMapper;
import com.br.marketing.mapper.MarketingSanLiuLingCollectionMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.util.aes.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName SanLiuLingCollectionServiceImpl
 * @Description 360催收业务数据清洗服务实现
 * 
 * 性能优化说明：
 * 1. 分页查询applicationId：避免千万级数据一次性加载到内存
 * 2. 线程池并发处理：每个applicationId分组独立处理，提高效率
 * 3. 数据顺序保证：通过ORDER BY application_id确保分页查询的数据顺序一致性
 * 4. 批量状态更新：先查询数据获取ID，然后通过ID批量更新状态，减少数据库交互次数
 * 
 * 状态管理说明：
 * 1. cleanStatus状态流转：0(待清洗) -> 1(清洗中) -> 2(清洗完成)
 * 2. 状态更新优化：
 *    - 查询阶段：一次查询获取所有数据和ID
 *    - 状态更新：通过ID列表批量更新，避免多次applicationId条件查询
 *    - 异常回滚：基于已获取的ID列表进行回滚
 * 3. 并发安全：通过数据库行锁和状态检查确保同一applicationId不会被重复处理
 * 4. 数据量级：每个applicationId数据量不超过10条，适合批量处理
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
    @Resource
    MarketingCustomerConfigMapper marketingCustomerConfigMapper;
    
    private final static String TITLE = "【360-催收业务】";

    @Override
    public void cleanData(String apiCode) {
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        String receiveDate = LocalDate.now().toString();
        Integer cleanStatus = DataCleanStatusEnum.READY.getCode();

        // 1. 先查询总数量，用于日志显示
        Long totalCount = marketingSanLiuLingCollectionMapper.countDistinctApplicationIds(
                apiCode, receiveDate, cleanStatus);
        
        if (totalCount == null || totalCount == 0) {
            log.warn(TITLE + "查询待清洗数据为空，apiCode：{}", apiCode);
            return;
        }
        
        log.warn(TITLE + "共查询到{}个不重复的applicationId，开始分页处理，apiCode：{}", totalCount, apiCode);

        MarketingCustomerConfigExample configExample = new MarketingCustomerConfigExample();
        configExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingCustomerConfig> configs = marketingCustomerConfigMapper.selectByExample(configExample);
        MarketingCustomerConfig marketingCustomerConfig = configs.get(0);
        
        // 2. 动态分页查询并处理applicationId，以查询结果为空作为结束条件
        int batchNumber = 1;
        while (true) {
            // 分页查询applicationId列表，始终从offset=0开始查询待清洗数据
            List<String> applicationIds = marketingSanLiuLingCollectionMapper.selectDistinctApplicationIdsWithPaging(
                    apiCode, receiveDate, cleanStatus);
            
            if (CollectionUtils.isEmpty(applicationIds)) {
                log.warn(TITLE + "第{}批分页查询applicationId为空，所有数据处理完成", batchNumber);
                break;
            }
            
            log.warn(TITLE + "第{}批分页查询到{}个applicationId", batchNumber, applicationIds.size());

            // 在主线程中批量将这批applicationIds的状态更新为"清洗中"
            int updateCount = marketingSanLiuLingCollectionMapper.updateCleanStatusByApplicationIds(
                    applicationIds, apiCode, receiveDate, DataCleanStatusEnum.RUNNING.getCode());

            if (updateCount > 0) {
                log.warn(TITLE + "第{}批成功将{}个applicationId状态更新为清洗中", batchNumber, updateCount);
                // 异步处理数据，处理完成后在线程内更新为"清洗完成"
                pushPool.submit(() -> processApplicationIdData(apiCode, receiveDate, applicationIds, marketingCustomerConfig));
            } else {
                log.warn(TITLE + "第{}批批量更新状态失败，跳过这批applicationIds: {}", batchNumber, applicationIds);
            }
            
            batchNumber++;
        }
    }

    /**
     * 处理单个applicationId的数据
     */
    private void processApplicationIdData(String apiCode, String receiveDate, List<String> applicationIds, MarketingCustomerConfig marketingCustomerConfig) {

        for (String applicationId : applicationIds){
            // AES解密参数
            AesGeneralDTO aesGeneralDTO = new AesGeneralDTO();
            aesGeneralDTO.setCipherMode(marketingCustomerConfig.getCipherMode());
            aesGeneralDTO.setPaddingScheme(marketingCustomerConfig.getPaddingScheme());
            aesGeneralDTO.setCharset(marketingCustomerConfig.getCharset());
            aesGeneralDTO.setIv(marketingCustomerConfig.getIv());
            aesGeneralDTO.setDynamicKeys(marketingCustomerConfig.getDynamicKeys());

            List<Long> dataIds = new ArrayList<>();
            try {
                // 1. 先查询该applicationId下的所有待清洗数据
                List<MarketingSanLiuLingCollection> collectionList = marketingSanLiuLingCollectionMapper.selectByApplicationId(
                        apiCode, receiveDate, DataCleanStatusEnum.RUNNING.getCode(), applicationId);

                if (CollectionUtils.isEmpty(collectionList)) {
                    log.warn(TITLE + "applicationId: {} 下无待清洗数据，跳过处理", applicationId);
                    continue;
                }

                // 2. 提取所有数据的ID，用于后续状态更新
                dataIds = collectionList.stream()
                        .map(MarketingSanLiuLingCollection::getId)
                        .collect(Collectors.toList());

                // 3. 数据已在主线程中更新为"清洗中"状态，直接处理即可

                // 拆分为br前缀的列表
                List<MarketingSanLiuLingCollection> brList = collectionList.stream()
                        .filter(collection -> collection.getPhoneLabel() != null &&
                                collection.getPhoneLabel().trim().equals("br1"))
                        .collect(Collectors.toList());

                if(brList.isEmpty()){
                    log.warn(TITLE + "applicationId: {} 不存在br1的数据", applicationId);
                    continue;
                }

                // 拆分为lxr前缀的列表
                List<MarketingSanLiuLingCollection> lxrList = collectionList.stream()
                        .filter(collection -> collection.getPhoneLabel() != null &&
                                collection.getPhoneLabel().startsWith("lxr"))
                        .collect(Collectors.toList());

                // 构建上传数据 - 按applicationId合并数据
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String currentDate = sdf.format(new Date());
                String taskId = apiCode+"_"+currentDate;

                // 获取taskId（优先使用数据库中的taskId）
                for (MarketingSanLiuLingCollection collection : brList) {
                    if (!StringUtils.isEmpty(collection.getTaskId())) {
                        taskId = collection.getTaskId();
                        break;
                    }
                }

                // 按applicationId合并数据：一个applicationId生成一条记录
                MarketingPreUserDetailDTO mergedDetailDTO = buildMergedMarketingPreUserDetailDTO(brList,lxrList,aesGeneralDTO);

                List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
                if (mergedDetailDTO != null) {
                    syncUsers.add(mergedDetailDTO);
                }

                // 构建上传对象
                MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
                marketingPreUserDTO.setTaskId(taskId);

                StringBuilder sb = new StringBuilder();
                sb.append(apiCode).append("_")
                        .append(taskId).append("_")
                        .append(System.currentTimeMillis()).append("_")
                        .append(UUID.randomUUID());
                marketingPreUserDTO.setRequestId(sb.toString());
                marketingPreUserDTO.setDataItems(syncUsers);

                UploadDataDTO uploadDataDTO = new UploadDataDTO();
                uploadDataDTO.setApiCode(apiCode);
                uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));

                // 3. 执行推送
                pushInfoService.pushUploadByRetry(uploadDataDTO, null);

                // 4. 推送成功后，将状态更新为清洗完成(2)
                marketingSanLiuLingCollectionMapper.updateCleanStatusByIds(dataIds,
                        DataCleanStatusEnum.COMPLETE.getCode());

            } catch (Exception e) {
                log.error(TITLE + "处理applicationId: {} 数据时发生异常", applicationId, e);

                // 发生异常时，尝试将状态回滚为待清洗，便于重新处理
                if (!CollectionUtils.isEmpty(dataIds)) {
                    try {
                        marketingSanLiuLingCollectionMapper.updateCleanStatusByIds(dataIds,
                                DataCleanStatusEnum.READY.getCode());
                        log.warn(TITLE + "异常回滚：applicationId: {} 状态已回滚为待清洗", applicationId);
                    } catch (Exception rollbackException) {
                        log.error(TITLE + "异常回滚失败，applicationId: {}", applicationId, rollbackException);
                    }
                }
            }
        }
    }

    /**
     * 合并同一applicationId的多条记录为一条上传记录
     */
    private MarketingPreUserDetailDTO buildMergedMarketingPreUserDetailDTO(List<MarketingSanLiuLingCollection> brList,
                                                                           List<MarketingSanLiuLingCollection> lxrList,
                                                                           AesGeneralDTO aesGeneralDTO ) {
        try {
            // 联系人列表
            List<ContactListDTO> contactList = getContactListDTOS(lxrList,aesGeneralDTO);

            MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
            
            // 主要信息（从br1记录中获取）
            String phone = "";
            String customerName = "";
            // 解析语音参数获取详细信息
            String sex = "";
            String money = "";
            String overdue_date = "";
            String overdue_days = "";

            // 遍历所有记录，分别处理本人和联系人信息
            for (MarketingSanLiuLingCollection collection : brList) {

                phone = collection.getPhone();
                customerName = collection.getCustomerName();

                // 设置基本字段
                detailDTO.setCell(phone);
                detailDTO.setName(customerName);
                detailDTO.setCustNum(collection.getApplicationId());
                detailDTO.setOperateType("4");

                // 解析speechParamSet获取更多信息
                if (!StringUtils.isEmpty(collection.getSpeechParamSet())) {
                    JSONObject speechParams = JSONObject.parseObject(collection.getSpeechParamSet());
                    sex = speechParams.getString("sex");
                    money = speechParams.getString("money");
                    overdue_date = speechParams.getString("overdue_date");
                    overdue_days = speechParams.getString("overdue_days");
                }

                // 构建业务保留字段reserveField1
                Map<String, Object> reserveField1 = new HashMap<>();
                // 基础信息
                reserveField1.put("userType", "催收");
                reserveField1.put("gender", sex);
                // 案件信息
                reserveField1.put("caseCode", collection.getCaseCode());
                reserveField1.put("productType", collection.getProductType());
                reserveField1.put("overdue_date", overdue_date);
                reserveField1.put("overdue_days", overdue_days);
                reserveField1.put("money", money);
                reserveField1.put("prologueRemark", collection.getPrologueRemark());

                // 批次信息
                reserveField1.put("batchNumber", collection.getBatchNo());
                String strategyCode = collection.getBatchNo();
                if (!StringUtils.isEmpty(strategyCode) && strategyCode.length() > 12) {
                    strategyCode = strategyCode.substring(Math.max(0, strategyCode.length() - 12));
                }

                reserveField1.put("strategyCode", strategyCode);
                reserveField1.put("applicationId", collection.getApplicationId());

                // 联系人信息
                if (!contactList.isEmpty()) {
                    reserveField1.put("contactList", contactList);
                }

                // 用户姓名和手机号（原始值，用于模板）
                reserveField1.put("customerName", collection.getCustomerName());
                reserveField1.put("template_no", collection.getPhone());

                detailDTO.setReserveField1(JSON.toJSONString(reserveField1));
            }

            return detailDTO;
        } catch (Exception e) {
            log.error(TITLE + "构建合并MarketingPreUserDetailDTO失败，applicationId: {}",
                    brList.get(0).getApplicationId(), e);
            return null;
        }
    }

    private static List<ContactListDTO> getContactListDTOS(List<MarketingSanLiuLingCollection> lxrList,
                                                           AesGeneralDTO aesGeneralDTO) {
        List<ContactListDTO> contactList = new ArrayList<>();

        if(lxrList.isEmpty()){
            return contactList;
        }

        // 按phoneLabel排序：lxr1、lxr2、lxr3...
        lxrList.sort(Comparator.comparing(
                item -> Optional.ofNullable(item.getPhoneLabel())
                        .map(label -> label.replaceAll("\\D", ""))  // 提取数字
                        .filter(num -> !num.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(Integer.MAX_VALUE)  // 无数字的排到最后
        ));

        for (MarketingSanLiuLingCollection marketingSanLiuLingCollection : lxrList){
            ContactListDTO contact = new ContactListDTO();
            contact.setOriginalCell(marketingSanLiuLingCollection.getPhone());

            JSONObject jsonObject = JSONObject.parseObject(marketingSanLiuLingCollection.getSpeechParamSet());

            aesGeneralDTO.setText(jsonObject.getString("name"));
            String name = AesUtil.decrypt(aesGeneralDTO);

            aesGeneralDTO.setText(marketingSanLiuLingCollection.getPhone());
            String phone = AesUtil.decrypt(aesGeneralDTO);

            contact.setContactCustNum(marketingSanLiuLingCollection.getApplicationId());
            contact.setContactName(name);
            contact.setContactCell(phone);
            contact.setContactRelationship(marketingSanLiuLingCollection.getPhoneLabel());
            contactList.add(contact);
        }
        return contactList;
    }


}
