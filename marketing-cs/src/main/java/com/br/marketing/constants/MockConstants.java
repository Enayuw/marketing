package com.br.marketing.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName MockConstants
 * @Description Mock测试常量类 - 统一管理Mock名称常量
 * @Author bingxu.kong
 * @Date 2025/01/27
 */
public final class MockConstants {

    private MockConstants() {}
    /**
     * 基础类型Mock名称
     */
    public static final String TEST_VOID_RETURN = "test_void_return";
    public static final String TEST_OBJECT_RETURN = "test_object_return";
    /**
     * 框架封装类型Mock名称
     */
    public static final String TEST_API_RESULT_RETURN = "test_api_result_return";
    public static final String TEST_RESULT_RETURN = "test_result_return";
    /**
     * 复杂对象类型Mock名称
     */
    public static final String TEST_DTO_RETURN = "test_dto_return";
    public static final String TEST_LIST_RETURN = "test_list_return";
    public static final String TEST_INTEGER_RETURN = "test_integer_return";
    // ==================== 工具方法 ====================
    
    /**
     * 获取所有Mock名称的列表
     * @return 所有Mock名称列表
     */
    public static List<String> getAllMockNames() {
        return Arrays.asList(
            // 基础类型
            TEST_VOID_RETURN,
            TEST_OBJECT_RETURN,
            // 框架封装类型
            TEST_API_RESULT_RETURN,
            TEST_RESULT_RETURN,
            // 复杂对象类型
            TEST_DTO_RETURN,
            TEST_LIST_RETURN,
            TEST_INTEGER_RETURN
        );
    }
    
    /**
     * 检查指定的Mock名称是否存在
     * @param mockName Mock名称
     * @return 是否存在
     */
    public static boolean contains(String mockName) {
        return getAllMockNames().contains(mockName);
    }
}
