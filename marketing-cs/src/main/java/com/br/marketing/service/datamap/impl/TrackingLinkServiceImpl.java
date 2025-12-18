package com.br.marketing.service.datamap.impl;

import java.util.*;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.*;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.BizTrackingLinkMapper;
import com.br.marketing.mapper.BizTrackingLinkNodeMapper;
import com.br.marketing.mapper.BizTrackingNodeDictMapper;
import com.br.marketing.mapper.MkNodeStatisticsMapper;
import com.br.marketing.service.datamap.TrackingLinkService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 链路实现
 *
 * @author Austin
 * @since 2025/10/16
 */
@Slf4j
@Service
public class TrackingLinkServiceImpl implements TrackingLinkService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private BizTrackingNodeDictMapper nodeDictMapper;

    @Resource
    private BizTrackingLinkMapper linkMapper;

    @Resource
    private BizTrackingLinkNodeMapper linkNodeMapper;

    @Resource
    private MkNodeStatisticsMapper statisticsMapper;


    @Override
    public ApiResult<List<NodeDictVO>> selectNodesByApiCode(String apiCode) {
        if (StringUtils.isEmpty(apiCode)) {
            return new ApiResult<List<NodeDictVO>>().fail("apiCode is null");
        }
        List<BizTrackingNodeDict> nodes = nodeDictMapper.selectByApiCode(apiCode);

        List<NodeDictVO> result = nodes.stream()
                .map(node -> NodeDictVO.builder()
                        .id(node.getId())
                        .apiCode(node.getApiCode())
                        .nodeCode(node.getNodeCode())
                        .nodeType(node.getNodeType())
                        .nodeName(node.getNodeName())
                        .nodeDesc(node.getNodeDesc())
                        .isActive(node.getIsActive())
                        .createTime(node.getCreateTime())
                        .updateTime(node.getUpdateTime())
                        .build())
                .collect(Collectors.toList());

        return new ApiResult<List<NodeDictVO>>().success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<CreateLinkResponse> createLink(CreateLinkRequest request) {
        // 1. Generate link code
        String linkCode = generateLinkCode();

        // 2. Create link
        BizTrackingLink bizTrackingLink = new BizTrackingLink();
        bizTrackingLink.setLinkCode(linkCode);
        bizTrackingLink.setApiCode(request.getApiCode());
        bizTrackingLink.setLinkName(request.getLinkName());
        bizTrackingLink.setBizScene(request.getBizScene());
        bizTrackingLink.setDescription(request.getDescription());
        bizTrackingLink.setGraphJson(request.getGraphJson());
        bizTrackingLink.setStatus((byte) 1);
        bizTrackingLink.setCreatedTime(new Date());
        bizTrackingLink.setUpdatedTime(new Date());
        linkMapper.insertSelective(bizTrackingLink);
        Long linkId = bizTrackingLink.getId();

        // 3. Build response
        CreateLinkResponse response = CreateLinkResponse.builder()
                .linkId(linkId)
                .linkCode(linkCode)
                .nodeCount(request.getNodes() != null ? request.getNodes().size() : 0)
                .build();

        return new ApiResult<CreateLinkResponse>().success(response, "success!");
    }

    @Override
    public ApiResult<LinkDetailResponse> getLinkDetail(QueryLinkRequest request) {
        // 1. 查询链路基本信息
        Long linkId = request.getLinkId();
        BizTrackingLink link = linkMapper.selectByPrimaryKey(linkId);
        if (link == null) {
            return new ApiResult<LinkDetailResponse>().fail("没有查询到链路信息, linkId：" + linkId);
        }

        // 2. 处理日期参数，若未传则默认为当天
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        String statDate = StringUtils.isEmpty(request.getStartDate()) ? currentDate : request.getStartDate();
        String endDate = StringUtils.isEmpty(request.getEndDate()) ? currentDate : request.getEndDate();

        // 3. 构建链路详情响应
        LinkDetailResponse response = buildLinkDetailResponse(link, statDate, endDate);
        return new ApiResult<LinkDetailResponse>().success(response);
    }

    @Override
    public ApiResult<List<LinkDetailResponse>> getLinkDetailListByApiCode(QueryLinkByApiCodeRequest request) {
        // 1. 参数校验
        if (StringUtils.isEmpty(request.getApiCode())) {
            return new ApiResult<List<LinkDetailResponse>>().fail("apiCode不能为空");
        }

        // 2. 根据apiCode查询所有链路
        BizTrackingLinkExample example = new BizTrackingLinkExample();
        example.createCriteria().andApiCodeEqualTo(request.getApiCode());
        List<BizTrackingLink> links = linkMapper.selectByExample(example);
        
        if (CollectionUtils.isEmpty(links)) {
            return new ApiResult<List<LinkDetailResponse>>().success(new ArrayList<>());
        }

        // 3. 处理日期参数，若未传则默认为当天
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        String statDate = StringUtils.isEmpty(request.getStartDate()) ? currentDate : request.getStartDate();
        String endDate = StringUtils.isEmpty(request.getEndDate()) ? currentDate : request.getEndDate();

        // 4. 遍历每个链路，构建详情信息
        List<LinkDetailResponse> resultList = new ArrayList<>();
        for (BizTrackingLink link : links) {
            LinkDetailResponse linkDetail = buildLinkDetailResponse(link, statDate, endDate);
            if (linkDetail != null) {
                resultList.add(linkDetail);
            }
        }

        return new ApiResult<List<LinkDetailResponse>>().success(resultList);
    }

    /**
     * 构建链路详情响应（提取公共逻辑）
     * 
     * @param link 链路实体
     * @param statDate 开始日期
     * @param endDate 结束日期
     * @return 链路详情响应
     */
    private LinkDetailResponse buildLinkDetailResponse(BizTrackingLink link, String statDate, String endDate) {
        Long linkId = link.getId();

        // 1. 查询链路节点信息
        List<LinkNodeDetailDTO> nodeDetailDTOList = linkNodeMapper.selectLinkNodeDetailsWithStatistics(linkId);

        // 2. 查询节点统计信息（来自 Doris）并聚合计算链路总统计
        LinkStatisticsDTO linkStatistics = null;
        if (!CollectionUtils.isEmpty(nodeDetailDTOList)) {
            List<Long> linkNodeIds = nodeDetailDTOList.stream()
                    .map(LinkNodeDetailDTO::getId)
                    .collect(Collectors.toList());

            List<MkNodeStatistics> statistics = statisticsMapper.selectByLinkNodeIdsDM_(linkNodeIds, statDate, endDate);

            // 3. 一次遍历完成：将统计信息合并到节点详情 + 聚合计算链路总统计
            if (!CollectionUtils.isEmpty(statistics)) {
                // 初始化链路总统计的累加变量
                long totalCountSum = 0L;
                long totalMagnitudeSum = 0L;
                LocalDateTime minFirstUpdateTime = null;
                LocalDateTime maxLastUpdateTime = null;
                int updateCountSum = 0;
                
                // 一次遍历同时完成节点信息合并和链路统计聚合
                for (MkNodeStatistics stat : statistics) {
                    // 3.1 将统计信息合并到对应的节点详细信息中
                    nodeDetailDTOList.stream()
                            .filter(node -> node.getId().equals(stat.getLinkNodeId()))
                            .findFirst()
                            .ifPresent(node -> {
                                node.setTotalCount(stat.getTotalCount());
                                node.setTotalMagnitude(stat.getTotalMagnitude());
                                node.setFirstUpdateTime(convertToLocalDateTime(stat.getFirstUpdateTime()));
                                node.setLastUpdateTime(convertToLocalDateTime(stat.getLastUpdateTime()));
                                node.setUpdateCount(stat.getUpdateCount());
                            });

                    // 3.2 累加计算链路总统计
                    totalCountSum += (stat.getTotalCount() != null ? stat.getTotalCount() : 0L);
                    totalMagnitudeSum += (stat.getTotalMagnitude() != null ? stat.getTotalMagnitude() : 0L);
                    updateCountSum += (stat.getUpdateCount() != null ? stat.getUpdateCount() : 0);
                    
                    // 计算最早的首次更新时间
                    LocalDateTime firstTime = convertToLocalDateTime(stat.getFirstUpdateTime());
                    if (firstTime != null && (minFirstUpdateTime == null || firstTime.isBefore(minFirstUpdateTime))) {
                        minFirstUpdateTime = firstTime;
                    }
                    
                    // 计算最晚的最后更新时间
                    LocalDateTime lastTime = convertToLocalDateTime(stat.getLastUpdateTime());
                    if (lastTime != null && (maxLastUpdateTime == null || lastTime.isAfter(maxLastUpdateTime))) {
                        maxLastUpdateTime = lastTime;
                    }
                }
                
                // 构建链路总统计
                linkStatistics = LinkStatisticsDTO.builder()
                        .totalCount(totalCountSum)
                        .totalMagnitude(totalMagnitudeSum)
                        .firstUpdateTime(minFirstUpdateTime)
                        .lastUpdateTime(maxLastUpdateTime)
                        .updateCount(updateCountSum)
                        .build();
            }
        }

        // 4. 构建链接信息
        LinkInfoVO linkInfo = LinkInfoVO.builder()
                .id(link.getId())
                .apiCode(link.getApiCode())
                .linkCode(link.getLinkCode())
                .linkName(link.getLinkName())
                .bizScene(link.getBizScene())
                .description(link.getDescription())
                .graphJson(link.getGraphJson())
                .status(link.getStatus())
                .createdTime(link.getCreatedTime())
                .updatedTime(link.getUpdatedTime())
                .build();

        // 5. 将统计信息填充到链接信息中（如果可用）
        if (linkStatistics != null) {
            linkInfo.setTotalCount(linkStatistics.getTotalCount());
            linkInfo.setTotalMagnitude(linkStatistics.getTotalMagnitude());
            linkInfo.setFirstUpdateTime(linkStatistics.getFirstUpdateTime());
            linkInfo.setLastUpdateTime(linkStatistics.getLastUpdateTime());
            linkInfo.setUpdateCount(linkStatistics.getUpdateCount());
        }

        // 6. 构建节点列表
        List<LinkNodeDetailVO> nodes = nodeDetailDTOList.stream()
                .map(node -> LinkNodeDetailVO.builder()
                        .id(node.getId())
                        .linkId(node.getLinkId())
                        .nodeId(node.getNodeId())
                        .nodeDictId(node.getNodeDictId())
                        .nodeAlias(node.getNodeAlias())
                        .status(node.getStatus())
                        .nodeCode(node.getNodeCode())
                        .apiCode(node.getApiCode())
                        .nodeType(node.getNodeType())
                        .nodeName(node.getNodeName())
                        .totalCount(node.getTotalCount())
                        .totalMagnitude(node.getTotalMagnitude())
                        .firstUpdateTime(node.getFirstUpdateTime())
                        .lastUpdateTime(node.getLastUpdateTime())
                        .updateCount(node.getUpdateCount())
                        .build())
                .collect(Collectors.toList());

        return LinkDetailResponse.builder()
                .linkInfo(linkInfo)
                .nodes(nodes)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateLink(CreateLinkRequest request) {
        Long linkId = request.getLinkId();
        // 1. 更新链路基本信息
        BizTrackingLink bizTrackingLink = new BizTrackingLink();
        bizTrackingLink.setId(linkId);
        bizTrackingLink.setApiCode(request.getApiCode());
        bizTrackingLink.setLinkName(request.getLinkName());
        bizTrackingLink.setBizScene(request.getBizScene());
        bizTrackingLink.setDescription(request.getDescription());
        bizTrackingLink.setGraphJson(request.getGraphJson());
        bizTrackingLink.setUpdatedTime(new Date());

        linkMapper.updateByPrimaryKeySelective(bizTrackingLink);

        // 2. 查询现有节点
        BizTrackingLinkNodeExample existingNodeExample = new BizTrackingLinkNodeExample();
        existingNodeExample.createCriteria().andLinkIdEqualTo(linkId);
        List<BizTrackingLinkNode> existingNodes = linkNodeMapper.selectByExample(existingNodeExample);

        // 3. 将现有节点转为 Map，以 nodeId 为 key
        Map<Long, BizTrackingLinkNode> existingNodeMap = existingNodes.stream()
                .collect(Collectors.toMap(BizTrackingLinkNode::getNodeId, node -> node, (k1, k2) -> k1));

        // 4. 收集请求中的 nodeId 集合
        Set<Long> requestNodeIds = request.getNodes().stream()
                .map(LinkNodeVO::getNodeId)
                .collect(Collectors.toSet());

        // 5. 遍历请求节点，存在则更新，不存在则新增
        for (LinkNodeVO nodeDTO : request.getNodes()) {
            BizTrackingLinkNode existingNode = existingNodeMap.get(nodeDTO.getNodeId());
            if (existingNode != null) {
                // 节点已存在，更新
                existingNode.setNodeDictId(nodeDTO.getNodeDictId());
                existingNode.setNodeAlias(nodeDTO.getNodeAlias());
                existingNode.setStatus((byte) 1);
                existingNode.setUpdatedTime(new Date());
                linkNodeMapper.updateByPrimaryKeySelective(existingNode);
            } else {
                // 节点不存在，新增
                BizTrackingLinkNode newNode = new BizTrackingLinkNode();
                newNode.setLinkId(linkId);
                newNode.setNodeId(nodeDTO.getNodeId());
                newNode.setNodeDictId(nodeDTO.getNodeDictId());
                newNode.setNodeAlias(nodeDTO.getNodeAlias());
                newNode.setStatus((byte) 1);
                newNode.setCreatedTime(new Date());
                newNode.setUpdatedTime(new Date());
                linkNodeMapper.insertSelective(newNode);
            }
        }

        // 6. 逻辑删除请求中不存在的节点
        for (BizTrackingLinkNode existingNode : existingNodes) {
            if (!requestNodeIds.contains(existingNode.getNodeId())) {
                existingNode.setStatus((byte) 0);
                existingNode.setUpdatedTime(new Date());
                linkNodeMapper.updateByPrimaryKeySelective(existingNode);
            }
        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public PageResultReturn selectLinkList(LinkListRequest request) {

        Integer page = request.getPageNum();
        Integer pageSize = request.getPageSize();
        PageHelper.startPage(page, pageSize);

        // 查询列表
        List<LinkListItemDTO> list = linkMapper.selectLinkList(request);

        // 转换为 VO
        List<LinkListItemVO> voList = list.stream()
                .map(item -> LinkListItemVO.builder()
                        .id(item.getId())
                        .apiCode(item.getApiCode())
                        .linkCode(item.getLinkCode())
                        .linkName(item.getLinkName())
                        .bizScene(item.getBizScene())
                        .description(item.getDescription())
                        .status(item.getStatus())
                        .nodeCount(item.getNodeCount())
                        .createdTime(item.getCreatedTime())
                        .updatedTime(item.getUpdatedTime())
                        .build())
                .collect(Collectors.toList());

        // 返回结果
        return PageResultReturn.setPageResult(voList, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateLinkStatus(UpdateLinkStatusRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ApiResult<Boolean>().fail("链路ID列表不能为空");
        }

        int rows = linkMapper.updateLinkStatus(request.getIds(), request.getStatus());
        return new ApiResult<Boolean>().success(rows > 0, "更新状态成功");
    }

    @Override
    public ApiResult<Boolean> deleteLink(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ApiResult<Boolean>().fail("链路ID列表不能为空");
        }

        BizTrackingLinkExample bizTrackingLinkExample = new BizTrackingLinkExample();
        bizTrackingLinkExample.createCriteria().andIdIn(ids);
        linkMapper.deleteByExample(bizTrackingLinkExample);
        return new ApiResult<Boolean>().success("删除成功");
    }

    /**
     * 生成链路代码
     *
     * @return 链路代码
     */
    private String generateLinkCode() {
        // 使用时间戳 + UUID 后6位生成唯一标识
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "link_" + timestamp + "_" + uuid;
    }

    private LocalDateTime convertToLocalDateTime(Object timeObj) {
        if(timeObj != null){
            return ((Timestamp) timeObj).toLocalDateTime();
        }
        return null;
    }
}

