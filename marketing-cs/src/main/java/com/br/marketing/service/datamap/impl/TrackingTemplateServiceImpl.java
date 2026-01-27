package com.br.marketing.service.datamap.impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.template.*;
import com.br.marketing.entity.BizTrackingTemplate;
import com.br.marketing.entity.BizTrackingTemplateEdge;
import com.br.marketing.entity.BizTrackingTemplateNode;
import com.br.marketing.mapper.BizTrackingTemplateEdgeMapper;
import com.br.marketing.mapper.BizTrackingTemplateMapper;
import com.br.marketing.mapper.BizTrackingTemplateNodeMapper;
import com.br.marketing.service.datamap.TrackingTemplateService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Long> createTemplate(CreateTemplateRequest request) {
        // 1. 创建模板主表
        BizTrackingTemplate template = new BizTrackingTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setStatus(request.getStatus() != null ? request.getStatus() : (byte) 1);
        template.setGraphJson(request.getGraphJson());
        template.setCreateTime(new Date());
        template.setUpdateTime(new Date());
        templateMapper.insertSelective(template);
        Long templateId = template.getId();

        // 2. 创建模板节点
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            // 保存节点时，建立前端ID到数据库ID的映射（用于边的保存）
            Map<Long, Long> nodeIdMapping = new HashMap<>();

            for (TemplateNodeVO nodeVO : request.getNodes()) {
                BizTrackingTemplateNode node = new BizTrackingTemplateNode();
                node.setTemplateId(templateId);
                node.setNodeDictId(nodeVO.getNodeDictId());
                node.setNodeName(nodeVO.getNodeName());
                node.setCreateTime(new Date());
                node.setUpdateTime(new Date());
                templateNodeMapper.insertSelective(node);

                // 记录映射关系：前端传的id -> 数据库生成的id
                if (nodeVO.getId() != null) {
                    nodeIdMapping.put(nodeVO.getId(), node.getId());
                }
            }

            // 3. 创建模板边（需要将前端节点ID映射为数据库节点ID）
            if (!CollectionUtils.isEmpty(request.getEdges())) {
                List<BizTrackingTemplateEdge> edgeEntities = new ArrayList<>();
                for (TemplateEdgeVO edgeVO : request.getEdges()) {
                    BizTrackingTemplateEdge edge = new BizTrackingTemplateEdge();
                    edge.setTemplateId(String.valueOf(templateId));
                    // 映射节点ID
                    Long fromNodeId = nodeIdMapping.getOrDefault(edgeVO.getFromNodeId(), edgeVO.getFromNodeId());
                    Long toNodeId = nodeIdMapping.getOrDefault(edgeVO.getToNodeId(), edgeVO.getToNodeId());
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
        }

        return new ApiResult<Long>().success(templateId, "创建模板成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateTemplate(CreateTemplateRequest request) {
        Long templateId = request.getId();
        if (templateId == null) {
            return new ApiResult<Boolean>().fail("模板ID不能为空");
        }

        // 1. 检查模板是否存在
        BizTrackingTemplate existingTemplate = templateMapper.selectByPrimaryKey(templateId);
        if (existingTemplate == null) {
            return new ApiResult<Boolean>().fail("模板不存在");
        }

        // 2. 更新模板主表
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

        // 3. 删除原有节点和边
        templateNodeMapper.deleteByTemplateId(templateId);
        templateEdgeMapper.deleteByTemplateId(String.valueOf(templateId));

        // 4. 重新创建节点
        Map<Long, Long> nodeIdMapping = new HashMap<>();
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            for (TemplateNodeVO nodeVO : request.getNodes()) {
                BizTrackingTemplateNode node = new BizTrackingTemplateNode();
                node.setTemplateId(templateId);
                node.setNodeDictId(nodeVO.getNodeDictId());
                node.setNodeName(nodeVO.getNodeName());
                node.setCreateTime(new Date());
                node.setUpdateTime(new Date());
                templateNodeMapper.insertSelective(node);

                if (nodeVO.getId() != null) {
                    nodeIdMapping.put(nodeVO.getId(), node.getId());
                }
            }
        }

        // 5. 重新创建边
        if (!CollectionUtils.isEmpty(request.getEdges())) {
            List<BizTrackingTemplateEdge> edgeEntities = new ArrayList<>();
            for (TemplateEdgeVO edgeVO : request.getEdges()) {
                BizTrackingTemplateEdge edge = new BizTrackingTemplateEdge();
                edge.setTemplateId(String.valueOf(templateId));
                Long fromNodeId = nodeIdMapping.getOrDefault(edgeVO.getFromNodeId(), edgeVO.getFromNodeId());
                Long toNodeId = nodeIdMapping.getOrDefault(edgeVO.getToNodeId(), edgeVO.getToNodeId());
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

        return new ApiResult<Boolean>().success(true, "更新模板成功");
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

        return PageResultReturn.setPageResult(list, page, pageSize);
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

        // 2. 查询模板节点（包含字典信息）
        List<TemplateNodeDetailVO> nodes = templateNodeMapper.selectNodeDetailsByTemplateId(id);

        // 3. 查询模板边
        List<TemplateEdgeVO> edges = templateEdgeMapper.selectEdgeVOsByTemplateId(id);

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
