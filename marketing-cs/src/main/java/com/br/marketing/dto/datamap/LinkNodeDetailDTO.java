package com.br.marketing.dto.datamap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 链路节点详情DTO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkNodeDetailDTO {
    
    private Long id;
    private Long linkId;
    private Long nodeId;
    private Long nodeDictId;
    private String nodeAlias;
    private Integer status;
    private String edgeType;
    
    // 节点字典信息
    private String nodeCode;
    private String apiCode;
    private String nodeType;
    private String nodeName;
    
    // 统计信息
    private Long totalCount;
    private Long totalMagnitude;
    private LocalDateTime firstUpdateTime;
    private LocalDateTime lastUpdateTime;
    private Integer updateCount;
}



