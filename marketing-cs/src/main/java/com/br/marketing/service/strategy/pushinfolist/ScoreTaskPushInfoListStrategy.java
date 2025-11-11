package com.br.marketing.service.strategy.pushinfolist;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import com.br.marketing.mapper.MarketingTaskUserTypeMapper;
import com.br.marketing.vo.PushInfoListVO;
import com.br.marketing.vo.RulePushLogOfStatusVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跑分任务推送信息列表查询策略
 * 完全按照原 getScorePushInfoList 方法逻辑实现
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class ScoreTaskPushInfoListStrategy extends AbstractPushInfoListStrategy {

    @Resource
    private CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    private CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Resource
    private MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Override
    public PageResultReturn execute(PushInfoFilterDTO dto) {
        // 分页查询
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        
        // 跑分任务的 pushTarget = 0, 2, 3
        List<Integer> pushTargets = new ArrayList<>(Arrays.asList(0, 2, 3));
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(dto, pushTargets);
        
        // 提取ID列表
        List<Long> ids = extractIds(list);
        List<String> failStatusIds = extractFailStatusIds(list);
        
        if (ids.size() > 0) {
            // 查询批次信息
            CustomerInfoPushBatchExample example = new CustomerInfoPushBatchExample();
            example.createCriteria().andMIdIn(ids).andIsDelEqualTo(1);
            List<CustomerInfoPushBatch> batches = customerInfoPushBatchMapper.selectByExample(example);
            
            // 按 mId 分组批次号
            HashMap<Long, String> batchNumberOfMid = batches.stream()
                    .collect(Collectors.groupingBy(
                            CustomerInfoPushBatch::getmId,
                            HashMap::new,
                            Collectors.mapping(CustomerInfoPushBatch::getmBatchNumber, Collectors.joining(","))
                    ));
            
            // 查询真实状态
            List<RulePushLogOfStatusVO> rulePushLogOfStatusVOS = customerInfoPushLogMapper.selectRealStatusByMid(ids);
            Map<Long, List<RulePushLogOfStatusVO>> realStatusOfMid = rulePushLogOfStatusVOS.stream()
                    .collect(Collectors.groupingBy(RulePushLogOfStatusVO::getMId));
            
            // 查询失败任务的决策结果
            Map<String, Map<String, Object>> resultMap = queryFailTaskResults(dto, failStatusIds);
            
            // 组装每个任务的详细信息
            list.forEach(t -> {
                // 设置批次号
                String batchNumber = batchNumberOfMid.get(t.getId());
                t.setBatchNumbers(batchNumber);
                
                // 查询 userType
                if (!StringUtils.isEmpty(batchNumber)) {
                    List<String> userTypeList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumbertikv_(batchNumber);
                    if (null != userTypeList) {
                        String userType = userTypeList.stream().collect(Collectors.joining(","));
                        t.setUserType(userType);
                    }
                }
                
                // 设置返回消息
                List<Map> msgList = new ArrayList<>();
                Map<String, Object> map = resultMap.get(t.getId().toString());
                msgList.add(map);
                t.setReturnMessages(msgList);
            });
        }
        
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
    }
}

