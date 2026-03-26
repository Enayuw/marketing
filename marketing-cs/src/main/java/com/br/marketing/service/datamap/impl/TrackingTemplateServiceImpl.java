package com.br.marketing.service.datamap.impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.template.*;
import com.br.marketing.entity.BizTrackingTemplate;
import com.br.marketing.entity.BizTrackingTemplateEdge;
import com.br.marketing.entity.BizTrackingTemplateNode;
import com.br.marketing.mapper.BizTrackingNodeDictMapper;
import com.br.marketing.mapper.BizTrackingTemplateEdgeMapper;
import com.br.marketing.mapper.BizTrackingTemplateMapper;
import com.br.marketing.mapper.BizTrackingTemplateNodeMapper;
import com.br.marketing.service.datamap.TrackingTemplateService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 链路模板管理服务实现类
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Slf4j
@Service
public class TrackingTemplateServiceImpl implements TrackingTemplateService {

    @Resource
    private BizTrackingTemplateMapper templateMapper;

    @Resource
    private BizTrackingTemplateNodeMapper templateNodeMapper;

    @Resource
    private BizTrackingTemplateEdgeMapper templateEdgeMapper;

    @Resource
    private BizTrackingNodeDictMapper nodeDictMapper;

    @Override
    public ApiResult<List<TemplateNodeDictVO>> getDistinctNodeDictList(String nodeType, String nodeName, String nodeCode) {
        List<TemplateNodeDictVO> list = nodeDictMapper.selectDistinctNodes(nodeType, nodeName, nodeCode);
        return new ApiResult<List<TemplateNodeDictVO>>().success(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Long> saveTemplate(CreateTemplateRequest request) {
        Long templateId = request.getId();
        boolean isCreate = (templateId == null);

        if (isCreate) {
            // 创建模板主表
            BizTrackingTemplate template = new BizTrackingTemplate();
            template.setTemplateName(request.getTemplateName());
            template.setDescription(request.getDescription());
            template.setStatus(request.getStatus() != null ? request.getStatus() : (byte) 1);
            template.setGraphJson(request.getGraphJson());
            template.setCreateTime(new Date());
            template.setUpdateTime(new Date());
            templateMapper.insertSelective(template);
            templateId = template.getId();
        } else {
            // 检查模板是否存在
            BizTrackingTemplate existingTemplate = templateMapper.selectByPrimaryKey(templateId);
            if (existingTemplate == null) {
                return new ApiResult<Long>().fail("模板不存在");
            }

            // 更新模板主表
            BizTrackingTemplate template = new BizTrackingTemplate();
            template.setId(templateId);
            template.setTemplateName(request.getTemplateName());
            template.setDescription(request.getDescription());
            if (request.getStatus() != null) {
                template.setStatus(request.getStatus());
            }
            template.setGraphJson(request.getGraphJson());
            template.setUpdateTime(new Date());
            templateMapper.updateByPrimaryKeySelective(template);

            // 删除原有节点和边
            templateNodeMapper.deleteByTemplateId(templateId);
            templateEdgeMapper.deleteByTemplateId(String.valueOf(templateId));
        }

        // 创建节点并建立tempId到数据库ID的映射
        Map<String, Long> tempIdToDbIdMapping = new HashMap<>();
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            for (TemplateNodeVO nodeVO : request.getNodes()) {
                BizTrackingTemplateNode node = new BizTrackingTemplateNode();
                node.setTemplateId(templateId);
                node.setNodeCode(nodeVO.getNodeCode());
                node.setNodeType(nodeVO.getNodeType());
                node.setNodeName(nodeVO.getNodeName());
                node.setCreateTime(new Date());
                node.setUpdateTime(new Date());
                templateNodeMapper.insertSelective(node);

                // 记录映射关系：前端临时ID -> 数据库生成的ID
                if (StringUtils.hasText(nodeVO.getTempId())) {
                    tempIdToDbIdMapping.put(nodeVO.getTempId(), node.getId());
                }
            }
        }

        // 创建边（使用映射将临时ID转换为数据库ID）
        if (!CollectionUtils.isEmpty(request.getEdges())) {
            List<BizTrackingTemplateEdge> edgeEntities = new ArrayList<>();
            for (TemplateEdgeVO edgeVO : request.getEdges()) {
                // 通过临时ID获取数据库ID
                Long fromNodeId = tempIdToDbIdMapping.get(edgeVO.getSourceNodeTempId());
                Long toNodeId = tempIdToDbIdMapping.get(edgeVO.getTargetNodeTempId());

                if (fromNodeId == null || toNodeId == null) {
                    log.warn("边的节点临时ID无法映射: sourceNodeTempId={}, targetNodeTempId={}",
                            edgeVO.getSourceNodeTempId(), edgeVO.getTargetNodeTempId());
                    continue;
                }

                BizTrackingTemplateEdge edge = new BizTrackingTemplateEdge();
                edge.setTemplateId(String.valueOf(templateId));
                edge.setFromNodeId(fromNodeId);
                edge.setToNodeId(toNodeId);
                edge.setEdgeType(edgeVO.getEdgeType() != null ? edgeVO.getEdgeType() : "SOLID");
                edge.setDescription(edgeVO.getDescription());
                edge.setCreateTime(new Date());
                edge.setUpdateTime(new Date());
                edgeEntities.add(edge);
            }
            if (!edgeEntities.isEmpty()) {
                templateEdgeMapper.batchInsert(edgeEntities);
            }
        }

        String message = isCreate ? "创建模板成功" : "更新模板成功";
        return new ApiResult<Long>().success(templateId, message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> deleteTemplate(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ApiResult<Boolean>().fail("模板ID列表不能为空");
        }

        // 1. 删除模板节点和边
        for (Long templateId : ids) {
            templateNodeMapper.deleteByTemplateId(templateId);
            templateEdgeMapper.deleteByTemplateId(String.valueOf(templateId));
        }

        // 2. 删除模板主表
        templateMapper.deleteByIds(ids);

        return new ApiResult<Boolean>().success(true, "删除模板成功");
    }

    @Override
    public PageResultReturn selectTemplateList(TemplateListRequest request) {
        Integer page = request.getPageNum() != null ? request.getPageNum() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        PageHelper.startPage(page, pageSize);

        List<TemplateListItemVO> list = templateMapper.selectTemplateList(request);
        long total = new PageInfo<>(list).getTotal();
        return PageResultReturn.setPageResult(list, page, pageSize, total);
    }

    @Override
    public ApiResult<TemplateDetailResponse> getTemplateDetail(Long id) {
        if (id == null) {
            return new ApiResult<TemplateDetailResponse>().fail("模板ID不能为空");
        }

        // 1. 查询模板主表
        BizTrackingTemplate template = templateMapper.selectByPrimaryKey(id);
        if (template == null) {
            return new ApiResult<TemplateDetailResponse>().fail("模板不存在");
        }

        // 2. 查询模板节点
        List<TemplateNodeDetailVO> nodes = templateNodeMapper.selectNodeDetailsByTemplateId(id);

        // 3. 查询模板边
        List<TemplateEdgeDetailVO> edges = templateEdgeMapper.selectEdgeDetailsByTemplateId(id);

        // 4. 构建响应
        TemplateDetailResponse response = TemplateDetailResponse.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .status(template.getStatus())
                .graphJson(template.getGraphJson())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
                .nodes(nodes)
                .edges(edges)
                .build();

        return new ApiResult<TemplateDetailResponse>().success(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateTemplateStatus(UpdateTemplateStatusRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ApiResult<Boolean>().fail("模板ID列表不能为空");
        }

        int rows = templateMapper.updateTemplateStatus(request.getIds(), request.getStatus());
        return new ApiResult<Boolean>().success(rows > 0, "更新状态成功");
    }
}
