package com.br.marketing.dto.datamap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 链路节点配置DTO
 * 包含链路、链路节点、节点字典的完整信息
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkNodeConfigDTO {
    
    /**
     * 链路ID
     */
    private Long linkId;
    
    /**
     * 链路标识
     */
    private String linkCode;
    
    /**
     * 链路名称
     */
    private String linkName;
    
    /**
     * 业务场景
     */
    private String bizScene;
    
    /**
     * 链路节点ID
     */
    private Long linkNodeId;
    
    /**
     * 节点顺序
     */
    private Integer nodeOrder;
    
    /**
     * 节点别名
     */
    private String nodeAlias;
    
    /**
     * 节点字典ID
     */
    private Long nodeDictId;
    
    /**
     * API编码
     */
    private String apiCode;
    
    /**
     * 节点代码
     */
    private String nodeCode;
    
    /**
     * 节点类型
     */
    private String nodeType;
    
    /**
     * 节点名称
     */
    private String nodeName;
}


