package com.br.marketing.innerapi.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.HaloHistoryCleanService;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;


/**
 * 哈啰历史数据洗数
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.innerapi.controller
 * @Description: 哈啰历史数据洗数
 * @CreateTime: 2022-06-30 16 :25
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@RestController
@Configuration
@RequestMapping("/rule/cleanHistory")
@Tag(name = "哈啰历史数据洗数", description = "哈啰历史数据洗数")
public class HaloHistoryCleanController {


    @Autowired
    RedisChgService redisChgService;
    @Autowired
    private HaloHistoryCleanService haloHistoryCleanService;


    @GetMapping("getHaloButton")
    @Operation(summary = "判断哈啰按钮是否显示", description = "判断哈啰按钮是否显示")
    public ApiResult<Boolean> getHaloButton(String cid) {

        return new ApiResult<Boolean>().success(redisChgService.exists("cid-halo-button" + cid));
    }

    @PostMapping("haluoCleanHistory")
    @Operation(summary = "哈啰历史数据清洗", description = "哈啰历史数据清洗")
    public ApiResult<Boolean> cleanHistory(@RequestBody String jsonData ) {
        return  haloHistoryCleanService.cleanHistory(jsonData);
    }


}
