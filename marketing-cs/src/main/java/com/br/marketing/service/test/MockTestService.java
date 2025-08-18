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

/**
 * @ClassName MockTestService
 * @Description Mock测试服务类 - 测试各种返回值类型
 * @Author bingxu.kong
 * @Date 2025/01/27
 */
@Slf4j
@Service
public class MockTestService {

    // ==================== 基础类型测试 ====================

    /**
     * 测试void返回类型
     */
    @Mockable(mockName = MockConstants.TEST_VOID_RETURN)
    public void testVoidReturn(String message) {
        log.warn("执行void方法，参数：{}", message);
        // 实际业务逻辑
        System.out.println("实际执行的void方法：" + message);
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

    // ==================== 基本数据类型测试 ====================

    /**
     * 测试boolean返回类型
     */
    @Mockable(mockName = MockConstants.TEST_BOOLEAN_RETURN)
    public boolean testBooleanReturn(String condition) {
        log.warn("执行boolean方法，条件：{}", condition);
        // 实际业务逻辑
        return "success".equals(condition);
    }

    /**
     * 测试Boolean包装类型
     */
    @Mockable(mockName = MockConstants.TEST_BOOLEAN_WRAPPER_RETURN)
    public Boolean testBooleanWrapperReturn(String condition) {
        log.warn("执行Boolean方法，条件：{}", condition);
        // 实际业务逻辑
        return "active".equals(condition);
    }

    /**
     * 测试int返回类型
     */
    @Mockable(mockName = MockConstants.TEST_INT_RETURN)
    public int testIntReturn(String category) {
        log.warn("执行int方法，分类：{}", category);
        // 实际业务逻辑
        return category.hashCode();
    }

    /**
     * 测试Integer包装类型
     */
    @Mockable(mockName = MockConstants.TEST_INTEGER_RETURN)
    public Integer testIntegerReturn(String type) {
        log.warn("执行Integer方法，类型：{}", type);
        // 实际业务逻辑
        return type.length() * 10;
    }

    /**
     * 测试long返回类型
     */
    @Mockable(mockName = MockConstants.TEST_LONG_RETURN)
    public long testLongReturn(String timestamp) {
        log.warn("执行long方法，时间戳：{}", timestamp);
        // 实际业务逻辑
        return System.currentTimeMillis();
    }

    /**
     * 测试Long包装类型
     */
    @Mockable(mockName = MockConstants.TEST_LONG_WRAPPER_RETURN)
    public Long testLongWrapperReturn(String id) {
        log.warn("执行Long方法，ID：{}", id);
        // 实际业务逻辑
        return Long.valueOf(id.hashCode());
    }

    /**
     * 测试double返回类型
     */
    @Mockable(mockName = MockConstants.TEST_DOUBLE_RETURN)
    public double testDoubleReturn(String amount) {
        log.warn("执行double方法，金额：{}", amount);
        // 实际业务逻辑
        return 999.99;
    }

    /**
     * 测试Double包装类型
     */
    @Mockable(mockName = MockConstants.TEST_DOUBLE_WRAPPER_RETURN)
    public Double testDoubleWrapperReturn(String price) {
        log.warn("执行Double方法，价格：{}", price);
        // 实际业务逻辑
        return Double.valueOf(price.length() * 100.5);
    }

    /**
     * 测试float返回类型
     */
    @Mockable(mockName = MockConstants.TEST_FLOAT_RETURN)
    public float testFloatReturn(String rate) {
        log.warn("执行float方法，比率：{}", rate);
        // 实际业务逻辑
        return 0.85f;
    }

    /**
     * 测试Float包装类型
     */
    @Mockable(mockName = MockConstants.TEST_FLOAT_WRAPPER_RETURN)
    public Float testFloatWrapperReturn(String percentage) {
        log.warn("执行Float方法，百分比：{}", percentage);
        // 实际业务逻辑
        return Float.valueOf(percentage.length() * 12.5f);
    }

    // ==================== 字符串类型测试 ====================

    /**
     * 测试String返回类型
     */
    @Mockable(mockName = MockConstants.TEST_STRING_RETURN)
    public String testStringReturn(String input) {
        log.warn("执行String方法，输入：{}", input);
        // 实际业务逻辑
        return "实际处理结果：" + input.toUpperCase();
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

    // ==================== 其他基本类型测试 ====================

    /**
     * 测试byte返回类型
     */
    @Mockable(mockName = MockConstants.TEST_BYTE_RETURN)
    public byte testByteReturn(String code) {
        log.warn("执行byte方法，代码：{}", code);
        // 实际业务逻辑
        return (byte) code.length();
    }

    /**
     * 测试Byte包装类型
     */
    @Mockable(mockName = MockConstants.TEST_BYTE_WRAPPER_RETURN)
    public Byte testByteWrapperReturn(String status) {
        log.warn("执行Byte方法，状态：{}", status);
        // 实际业务逻辑
        return Byte.valueOf((byte) status.hashCode());
    }

    /**
     * 测试short返回类型
     */
    @Mockable(mockName = MockConstants.TEST_SHORT_RETURN)
    public short testShortReturn(String level) {
        log.warn("执行short方法，级别：{}", level);
        // 实际业务逻辑
        return (short) (level.length() * 100);
    }

    /**
     * 测试Short包装类型
     */
    @Mockable(mockName = MockConstants.TEST_SHORT_WRAPPER_RETURN)
    public Short testShortWrapperReturn(String priority) {
        log.warn("执行Short方法，优先级：{}", priority);
        // 实际业务逻辑
        return Short.valueOf((short) (priority.hashCode() % 1000));
    }

    /**
     * 测试char返回类型
     */
    @Mockable(mockName = MockConstants.TEST_CHAR_RETURN)
    public char testCharReturn(String grade) {
        log.warn("执行char方法，等级：{}", grade);
        // 实际业务逻辑
        return grade.isEmpty() ? 'N' : grade.charAt(0);
    }

    /**
     * 测试Character包装类型
     */
    @Mockable(mockName = MockConstants.TEST_CHARACTER_RETURN)
    public Character testCharacterReturn(String type) {
        log.warn("执行Character方法，类型：{}", type);
        // 实际业务逻辑
        return type.isEmpty() ? 'X' : Character.toUpperCase(type.charAt(0));
    }
}
