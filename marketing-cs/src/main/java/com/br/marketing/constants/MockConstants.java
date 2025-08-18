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
     * 基本数据类型Mock名称
     */
    public static final String TEST_BOOLEAN_RETURN = "test_boolean_return";
    public static final String TEST_BOOLEAN_WRAPPER_RETURN = "test_boolean_wrapper_return";
    public static final String TEST_INT_RETURN = "test_int_return";
    public static final String TEST_INTEGER_RETURN = "test_integer_return";
    public static final String TEST_LONG_RETURN = "test_long_return";
    public static final String TEST_LONG_WRAPPER_RETURN = "test_long_wrapper_return";
    public static final String TEST_DOUBLE_RETURN = "test_double_return";
    public static final String TEST_DOUBLE_WRAPPER_RETURN = "test_double_wrapper_return";
    public static final String TEST_FLOAT_RETURN = "test_float_return";
    public static final String TEST_FLOAT_WRAPPER_RETURN = "test_float_wrapper_return";
    
    /**
     * 字符串类型Mock名称
     */
    public static final String TEST_STRING_RETURN = "test_string_return";
    
    /**
     * 复杂对象类型Mock名称
     */
    public static final String TEST_DTO_RETURN = "test_dto_return";
    public static final String TEST_LIST_RETURN = "test_list_return";
    
    /**
     * 其他基本类型Mock名称
     */
    public static final String TEST_BYTE_RETURN = "test_byte_return";
    public static final String TEST_BYTE_WRAPPER_RETURN = "test_byte_wrapper_return";
    public static final String TEST_SHORT_RETURN = "test_short_return";
    public static final String TEST_SHORT_WRAPPER_RETURN = "test_short_wrapper_return";
    public static final String TEST_CHAR_RETURN = "test_char_return";
    public static final String TEST_CHARACTER_RETURN = "test_character_return";
    
    /**
     * 业务相关Mock名称
     */
    public static final String TEST_POLLING = "test_polling";
    public static final String TEST_RANDOM = "test_random";
    
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
            
            // 基本数据类型
            TEST_BOOLEAN_RETURN,
            TEST_BOOLEAN_WRAPPER_RETURN,
            TEST_INT_RETURN,
            TEST_INTEGER_RETURN,
            TEST_LONG_RETURN,
            TEST_LONG_WRAPPER_RETURN,
            TEST_DOUBLE_RETURN,
            TEST_DOUBLE_WRAPPER_RETURN,
            TEST_FLOAT_RETURN,
            TEST_FLOAT_WRAPPER_RETURN,
            
            // 字符串类型
            TEST_STRING_RETURN,
            
            // 复杂对象类型
            TEST_DTO_RETURN,
            TEST_LIST_RETURN,
            
            // 其他基本类型
            TEST_BYTE_RETURN,
            TEST_BYTE_WRAPPER_RETURN,
            TEST_SHORT_RETURN,
            TEST_SHORT_WRAPPER_RETURN,
            TEST_CHAR_RETURN,
            TEST_CHARACTER_RETURN,
            
            // 业务相关
            TEST_POLLING,
            TEST_RANDOM
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
