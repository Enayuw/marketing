package com.br.marketing.dto.datamap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 链路列表项DTO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkListItemDTO {
    
    private Long id;
    private String linkCode;
    private String linkName;
    private String bizScene;
    private String description;
    private Integer status;
    private Integer nodeCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

