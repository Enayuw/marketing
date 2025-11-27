package com.br.marketing.innerapi.controller.test;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.test.MockTestDTO;
import com.br.marketing.service.test.MockTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Mock测试接口", description = "测试MockableAspect支持的各种返回值类型")
public class MockTestController {

    @Resource
    private MockTestService mockTestService;

    /**
     * 批量测试所有类型
     */
    @PostMapping("/batch-test")
    @Operation(summary = "批量测试所有类型", description = "批量测试MockableAspect支持的所有返回值类型")
    public ApiResult<Map<String, Object>> batchTest() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 测试基础类型
            mockTestService.testVoidReturn(results,"批量测试");
            
            Object objResult = mockTestService.testObjectReturn("批量测试");
            results.put("Object", objResult);
            
            // 测试包装类型
            ApiResult<MockTestDTO> apiResult = mockTestService.testApiResultReturn(999L);
            results.put("ApiResult", apiResult);
            
            Result<List<MockTestDTO>> resultType = mockTestService.testResultReturn(5);
            results.put("Result", resultType);

            // 测试复杂对象类型
            results.put("DTO", mockTestService.testDtoReturn(888L));
            results.put("List", mockTestService.testListReturn(3));

        } catch (Exception e) {
            log.error("批量测试异常", e);
            results.put("error", e.getMessage());
        }
        
        return new ApiResult<Map<String, Object>>().success().setData(results);
    }
}
