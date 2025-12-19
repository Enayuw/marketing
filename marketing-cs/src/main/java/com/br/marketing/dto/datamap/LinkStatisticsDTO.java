package com.br.marketing.dto.datamap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 链路统计DTO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatisticsDTO {
    
    private Long totalCount;
    private Long totalMagnitude;
    private LocalDateTime firstUpdateTime;
    private LocalDateTime lastUpdateTime;
    private Integer updateCount;
}



