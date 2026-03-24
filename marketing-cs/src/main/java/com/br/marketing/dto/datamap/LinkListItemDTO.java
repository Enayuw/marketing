package com.br.marketing.dto.datamap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    private String apiCode;
    private String linkCode;
    private String linkName;
    private String bizScene;
    private String description;
    private Integer status;
    private String sourceType;
    private String templateId;
    private Integer nodeCount;
    private Date createdTime;
    private Date updatedTime;
}

