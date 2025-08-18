package com.br.marketing.innerapi.controller.test;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.test.MockTestDTO;
import com.br.marketing.service.test.MockTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MockTestController
 * @Description Mock测试控制器 - 提供各种返回值类型的测试接口
 * @Author bingxu.kong
 * @Date 2025/01/27
 */
@Slf4j
@RestController
@RequestMapping("/api/mock/test")
@Api(tags = "Mock测试接口", description = "测试MockableAspect支持的各种返回值类型")
public class MockTestController {

    @Resource
    private MockTestService mockTestService;

    /**
     * 批量测试所有类型
     */
    @PostMapping("/batch-test")
    @ApiOperation(value = "批量测试所有类型", notes = "批量测试MockableAspect支持的所有返回值类型")
    public ApiResult<Map<String, Object>> batchTest() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 测试基础类型
            mockTestService.testVoidReturn("批量测试");
            results.put("void", "执行成功");
            
            Object objResult = mockTestService.testObjectReturn("批量测试");
            results.put("Object", objResult);
            
            // 测试包装类型
            ApiResult<MockTestDTO> apiResult = mockTestService.testApiResultReturn(999L);
            results.put("ApiResult", apiResult);
            
            Result<List<MockTestDTO>> resultType = mockTestService.testResultReturn(5);
            results.put("Result", resultType);
            
            // 测试基本数据类型
            results.put("boolean", mockTestService.testBooleanReturn("success"));
            results.put("Boolean", mockTestService.testBooleanWrapperReturn("active"));
            results.put("int", mockTestService.testIntReturn("test"));
            results.put("Integer", mockTestService.testIntegerReturn("test"));
            results.put("long", mockTestService.testLongReturn("test"));
            results.put("Long", mockTestService.testLongWrapperReturn("12345"));
            results.put("double", mockTestService.testDoubleReturn("test"));
            results.put("Double", mockTestService.testDoubleWrapperReturn("test"));
            results.put("float", mockTestService.testFloatReturn("test"));
            results.put("Float", mockTestService.testFloatWrapperReturn("test"));
            
            // 测试字符串类型
            results.put("String", mockTestService.testStringReturn("批量测试"));
            
            // 测试复杂对象类型
            results.put("DTO", mockTestService.testDtoReturn(888L));
            results.put("List", mockTestService.testListReturn(3));
            
            // 测试其他基本类型
            results.put("byte", mockTestService.testByteReturn("test"));
            results.put("Byte", mockTestService.testByteWrapperReturn("test"));
            results.put("short", mockTestService.testShortReturn("test"));
            results.put("Short", mockTestService.testShortWrapperReturn("test"));
            results.put("char", mockTestService.testCharReturn("A"));
            results.put("Character", mockTestService.testCharacterReturn("B"));
            
        } catch (Exception e) {
            log.error("批量测试异常", e);
            results.put("error", e.getMessage());
        }
        
        return new ApiResult<Map<String, Object>>().success().setData(results);
    }
}
