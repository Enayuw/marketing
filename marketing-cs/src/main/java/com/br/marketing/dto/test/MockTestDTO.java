package com.br.marketing.dto.test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    
    /**
     * 备注
     */
    private String remark;
}
