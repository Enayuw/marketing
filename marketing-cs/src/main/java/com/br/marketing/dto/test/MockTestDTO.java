package com.br.marketing.dto.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName MockTestDTO
 * @Description Mock测试用的DTO类
 * @Author bingxu.kong
 * @Date 2025/01/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockTestDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 是否激活
     */
    private Boolean active;
    
    /**
     * 余额
     */
    private Double balance;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 备注
     */
    private String remark;
}
