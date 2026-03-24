package com.br.marketing.mapper;


import com.br.marketing.dto.datamap.template.TemplateNodeDictVO;
import com.br.marketing.entity.BizTrackingNodeDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingNodeDictMapper extends BizTrackingNodeDictMapperBase {

    /**
     * 检查节点是否存在
     *
     * @param nodeCode 节点编码
     * @param apiCode API编码
     * @param nodeType 节点类型
     * @return 存在返回1，不存在返回0
     */
    Integer checkNodeExists(@Param("nodeCode") String nodeCode,
                            @Param("apiCode") String apiCode,
                            @Param("nodeType") String nodeType);

    /**
     * 更新节点的最后出现时间
     *
     * @param nodeCode 节点编码
     * @param apiCode API编码
     * @param nodeType 节点类型
     * @return 更新的行数
     */
    Integer updateLastSeenTime(@Param("nodeCode") String nodeCode,
                               @Param("apiCode") String apiCode,
                               @Param("nodeType") String nodeType);

    /**
     * 根据API代码查询节点列表
     *
     * @param apiCode API代码
     * @return 节点列表
     */
    List<BizTrackingNodeDict> selectByApiCode(@Param("apiCode") String apiCode);

    /**
     * 查询所有去重的节点列表（用于模板配置时选择）
     * 按 node_code + node_type + node_name 去重
     *
     * @param nodeType 节点类型（可选，用于过滤）
     * @param nodeName 节点名称（可选，模糊查询）
     * @param nodeCode 节点代码（可选，模糊查询）
     * @return 去重后的节点列表
     */
    List<TemplateNodeDictVO> selectDistinctNodes(@Param("nodeType") String nodeType,
                                                  @Param("nodeName") String nodeName,
                                                  @Param("nodeCode") String nodeCode);

}