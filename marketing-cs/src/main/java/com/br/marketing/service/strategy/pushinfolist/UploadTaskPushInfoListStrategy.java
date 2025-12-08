package com.br.marketing.service.strategy.pushinfolist;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.vo.PushInfoListVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 上传任务推送信息列表查询策略
 * 完全按照原 getUplodPushInfoList 方法逻辑实现
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class UploadTaskPushInfoListStrategy extends AbstractPushInfoListStrategy {

    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Override
    public PageResultReturn execute(PushInfoFilterDTO dto) {
        // 分页查询
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        
        // 上传任务的 pushTarget = 4
        List<Integer> pushTargets = new ArrayList<>(Arrays.asList(4));
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(dto, pushTargets);
        
        // 提取ID列表
        List<Long> ids = extractIds(list);
        List<String> failStatusIds = extractFailStatusIds(list);
        
        if (!ids.isEmpty()) {
            // 查询失败任务的决策结果
            Map<String, Map<String, Object>> resultMap = queryFailTaskResults(dto, failStatusIds);
            
            // 组装每个任务的详细信息
            list.forEach(t -> {
                // 查询上传报告获取 userType
                String uploadReportIds = t.getUploadReportIds();
                List<Long> listIds = Arrays.stream(uploadReportIds.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                
                MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
                syncReportExample.createCriteria().andIdIn(listIds);
                List<MarketingSyncReport> marketingSyncReports = syncReportMapper.selectByExample(syncReportExample);
                
                String result = marketingSyncReports.stream()
                        .map(MarketingSyncReport::getUserType)
                        .filter(userType -> userType != null && !userType.trim().isEmpty())
                        .collect(Collectors.joining(","));
                t.setUserType(result);
                
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

