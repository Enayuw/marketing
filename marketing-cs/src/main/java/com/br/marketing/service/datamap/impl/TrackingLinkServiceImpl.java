package com.br.marketing.service.datamap.impl;

import java.util.Date;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.*;
import com.br.marketing.entity.BizTrackingLink;
import com.br.marketing.entity.BizTrackingLinkNode;
import com.br.marketing.entity.BizTrackingNodeDict;
import com.br.marketing.entity.MkNodeStatistics;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
        bizTrackingLink.setLinkName(request.getLinkName());
        bizTrackingLink.setBizScene(request.getBizScene());
        bizTrackingLink.setDescription(request.getDescription());
        bizTrackingLink.setStatus((byte) 1);
        bizTrackingLink.setCreatedTime(new Date());
        bizTrackingLink.setUpdatedTime(new Date());
        linkMapper.insertSelective(bizTrackingLink);
        Long linkId = bizTrackingLink.getId();

        // 3. Create link nodes
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            for (LinkNodeVO nodeDTO : request.getNodes()) {
                BizTrackingLinkNode bizTrackingLinkNode = new BizTrackingLinkNode();
                bizTrackingLinkNode.setLinkId(linkId);
                bizTrackingLinkNode.setNodeDictId(nodeDTO.getNodeDictId());
                bizTrackingLinkNode.setNodeOrder(nodeDTO.getNodeOrder());
                bizTrackingLinkNode.setNodeAlias(nodeDTO.getNodeAlias());
                bizTrackingLinkNode.setNodeSourceId(nodeDTO.getNodeSourceId());
                bizTrackingLinkNode.setNodeTargetId(nodeDTO.getNodeTargetId());
                bizTrackingLinkNode.setStatus((byte)1);
                bizTrackingLinkNode.setCreatedTime(new Date());
                bizTrackingLinkNode.setUpdatedTime(new Date());
                linkNodeMapper.insertSelective(bizTrackingLinkNode);
            }
        }

        // 4. Build response
        CreateLinkResponse response = CreateLinkResponse.builder()
                .linkId(linkId)
                .linkCode(linkCode)
                .nodeCount(request.getNodes() != null ? request.getNodes().size() : 0)
                .build();

        return new ApiResult<CreateLinkResponse>().success(response, "success!");
    }

    @Override
    public ApiResult<LinkDetailResponse> getLinkDetail(Long linkId) {
        // 1. Query link basic information
        BizTrackingLink link = linkMapper.selectByPrimaryKey(linkId);
        if (link == null) {
            return new ApiResult<LinkDetailResponse>().fail("没有查询到链路信息, linkId：" + linkId);
        }

        // 2. Query node details (without statistics)
        String statDate = LocalDate.now().format(DATE_FORMATTER);
        List<LinkNodeDetailDTO> nodeDetailDTOList = linkNodeMapper.selectLinkNodeDetailsWithStatistics(linkId, statDate);

        // 3. Query node statistics (from Doris)
        if (!CollectionUtils.isEmpty(nodeDetailDTOList)) {
            List<Long> linkNodeIds = nodeDetailDTOList.stream()
                    .map(LinkNodeDetailDTO::getId)
                    .collect(Collectors.toList());

            List<MkNodeStatistics> statistics = statisticsMapper.selectByLinkNodeIdsbI_(linkNodeIds, statDate);

            // 4. Merge statistics into node details
            if (!CollectionUtils.isEmpty(statistics)) {
                statistics.forEach(stat -> {
                    nodeDetailDTOList.stream()
                            .filter(node -> node.getId().equals(stat.getLinkNodeId()))
                            .findFirst()
                            .ifPresent(node -> {
                                node.setTotalCount(stat.getTotalCount());
                                node.setTotalMagnitude(stat.getTotalMagnitude());
                                node.setFirstUpdateTime((LocalDateTime) stat.getFirstUpdateTime());
                                node.setLastUpdateTime((LocalDateTime) stat.getLastUpdateTime());
                                node.setUpdateCount(stat.getUpdateCount());
                            });
                });
            }
        }

        // 5. Query link aggregated statistics
        LinkStatisticsDTO linkStatistics = statisticsMapper.selectLinkStatisticsbI_(linkId, statDate);

        // Build link information
        LinkInfoVO linkInfo = LinkInfoVO.builder()
                .id(link.getId())
                .linkCode(link.getLinkCode())
                .linkName(link.getLinkName())
                .bizScene(link.getBizScene())
                .description(link.getDescription())
                .status(link.getStatus())
                .createdTime(link.getCreatedTime())
                .updatedTime(link.getUpdatedTime())
                .build();

        // Fill statistics into link information if available
        if (linkStatistics != null) {
            linkInfo.setTotalCount(linkStatistics.getTotalCount());
            linkInfo.setTotalMagnitude(linkStatistics.getTotalMagnitude());
            linkInfo.setFirstUpdateTime(linkStatistics.getFirstUpdateTime());
            linkInfo.setLastUpdateTime(linkStatistics.getLastUpdateTime());
            linkInfo.setUpdateCount(linkStatistics.getUpdateCount());
        }

        // Build node list
        List<LinkNodeDetailVO> nodes = nodeDetailDTOList.stream()
                .map(node -> LinkNodeDetailVO.builder()
                        .id(node.getId())
                        .linkId(node.getLinkId())
                        .nodeDictId(node.getNodeDictId())
                        .nodeOrder(node.getNodeOrder())
                        .nodeAlias(node.getNodeAlias())
                        .nodeSourceId(node.getNodeSourceId())
                        .nodeTargetId(node.getNodeTargetId())
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

        LinkDetailResponse response = LinkDetailResponse.builder()
                .linkInfo(linkInfo)
                .nodes(nodes)
                .build();

        return new ApiResult<LinkDetailResponse>().success(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateLink(CreateLinkRequest request) {
        Long linkId = request.getLinkId();
        // 1. 更新链路基本信息
        BizTrackingLink bizTrackingLink = new BizTrackingLink();
        bizTrackingLink.setId(linkId);
        bizTrackingLink.setLinkName(request.getLinkName());
        bizTrackingLink.setBizScene(request.getBizScene());
        bizTrackingLink.setDescription(request.getDescription());
        bizTrackingLink.setUpdatedTime(new Date());

        linkMapper.updateByPrimaryKeySelective(bizTrackingLink);

        // 2. 删除原有节点
        linkNodeMapper.deleteByLinkId(linkId);

        // 3. 重新插入节点
        for (LinkNodeVO nodeDTO : request.getNodes()) {
            BizTrackingLinkNode bizTrackingLinkNode = new BizTrackingLinkNode();
            bizTrackingLinkNode.setLinkId(linkId);
            bizTrackingLinkNode.setNodeDictId(nodeDTO.getNodeDictId());
            bizTrackingLinkNode.setNodeOrder(nodeDTO.getNodeOrder());
            bizTrackingLinkNode.setNodeAlias(nodeDTO.getNodeAlias());
            bizTrackingLinkNode.setNodeSourceId(nodeDTO.getNodeSourceId());
            bizTrackingLinkNode.setNodeTargetId(nodeDTO.getNodeTargetId());
            bizTrackingLinkNode.setStatus((byte)1);
            bizTrackingLinkNode.setCreatedTime(new Date());
            bizTrackingLinkNode.setUpdatedTime(new Date());
            linkNodeMapper.insertSelective(bizTrackingLinkNode);
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
}

