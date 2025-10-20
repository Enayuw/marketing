package com.br.marketing.service.test;

import com.br.marketing.aspect.Mockable;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.dto.test.MockTestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MockTestService
 * @Description Mock测试服务类 - 测试各种返回值类型
 * @Author bingxu.kong
 * @Date 2025/01/27
 */
@Slf4j
@Service
public class MockTestService {

    /**
     * 测试void返回类型
     */
    @Mockable(mockName = MockConstants.TEST_VOID_RETURN)
    public void testVoidReturn(Map<String, Object> results, String message) {
        log.warn("执行void方法，参数：{}", message);
        results.put("void", "实际返回Void");
    }

    /**
     * 测试Object返回类型
     */
    @Mockable(mockName = MockConstants.TEST_OBJECT_RETURN)
    public Object testObjectReturn(String param) {
        log.warn("执行Object方法，参数：{}", param);
        // 实际业务逻辑
        return "实际返回的Object对象：" + param;
    }

    // ==================== 框架封装类型测试 ====================

    /**
     * 测试ApiResult返回类型
     */
    @Mockable(mockName = MockConstants.TEST_API_RESULT_RETURN)
    public ApiResult<MockTestDTO> testApiResultReturn(Long userId) {
        log.warn("执行ApiResult方法，用户ID：{}", userId);
        // 实际业务逻辑
        MockTestDTO dto = new MockTestDTO();
        dto.setUserId(userId);
        dto.setUserName("实际用户");
        dto.setAge(25);
        dto.setEmail("real@example.com");
        dto.setActive(true);
        dto.setBalance(1000.0);
        dto.setCreateTime(LocalDateTime.now());
        dto.setRemark("实际业务数据");

        return new ApiResult<MockTestDTO>().success().setData(dto);
    }

    /**
     * 测试Result返回类型
     */
    @Mockable(mockName = MockConstants.TEST_RESULT_RETURN)
    public Result<List<MockTestDTO>> testResultReturn(int pageSize) {
        log.warn("执行Result方法，页面大小：{}", pageSize);
        // 实际业务逻辑
        MockTestDTO dto1 = new MockTestDTO(1L, "用户1", 20, "user1@test.com", true, 500.0, LocalDateTime.now(), "备注1");
        MockTestDTO dto2 = new MockTestDTO(2L, "用户2", 30, "user2@test.com", false, 800.0, LocalDateTime.now(), "备注2");

        Result<List<MockTestDTO>> result = new Result<>();
        result.success();
        result.setDate(Arrays.asList(dto1, dto2));
        return result;
    }

    // ==================== 复杂对象类型测试 ====================

    /**
     * 测试自定义DTO返回类型
     */
    @Mockable(mockName = MockConstants.TEST_DTO_RETURN)
    public MockTestDTO testDtoReturn(Long userId) {
        log.warn("执行DTO方法，用户ID：{}", userId);
        // 实际业务逻辑
        MockTestDTO dto = new MockTestDTO();
        dto.setUserId(userId);
        dto.setUserName("实际DTO用户");
        dto.setAge(28);
        dto.setEmail("dto@example.com");
        dto.setActive(true);
        dto.setBalance(2000.0);
        dto.setCreateTime(LocalDateTime.now());
        dto.setRemark("实际DTO数据");
        return dto;
    }

    /**
     * 测试List返回类型
     */
    @Mockable(mockName = MockConstants.TEST_LIST_RETURN)
    public List<MockTestDTO> testListReturn(int count) {
        log.warn("执行List方法，数量：{}", count);
        // 实际业务逻辑
        MockTestDTO dto1 = new MockTestDTO(100L, "列表用户1", 25, "list1@test.com", true, 1500.0, LocalDateTime.now(), "列表数据1");
        MockTestDTO dto2 = new MockTestDTO(200L, "列表用户2", 35, "list2@test.com", false, 2500.0, LocalDateTime.now(), "列表数据2");
        return Arrays.asList(dto1, dto2);
    }

}
