package com.br.marketing.innerapi.controller.tc;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.innerapi.dto.tc.TcyrApiCodeFillRequest;
import com.br.marketing.service.tc.TcyrSyncRecordApiCodeFillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 同程易融内部接口（不走 Session/菜单权限拦截）。
 * 灵霄 roster-gods 回调本服务时，配置的 {@code otherConfig.marketing.tcyr-api-code-fill-url} 须为带 {@code http(s)://} 的绝对 URL；
 * 灵霄侧发起 HTTP 客户端请求时可复用 {@link com.br.marketing.common.utils.http.HttpBaseUrlHelper#ensureHttpScheme(String)}（见 marketing-utils）。
 */
@RestController
@RequestMapping("/tcyr/inner/syncRecord")
@Tag(name = "同程易融-syncRecord内部接口", description = "apiCode 补齐等")
public class TcyrSyncRecordInnerController {

    private static final Integer INNER_CODE = 1;

    @Resource
    private TcyrSyncRecordApiCodeFillService tcyrSyncRecordApiCodeFillService;

    @Operation(summary = "按 batchNo 补齐 apiCode", description = "更新 b_marketing_tcyr_sync_record 最新一条；仅 api_code 为空时可写；已相同则幂等成功。")
    @PostMapping("/tcapiCodeFill")
    public ResponseEntity<ApiResult<Void>> tcapiCodeFill(@RequestBody TcyrApiCodeFillRequest body) {
        if (body == null || !StringUtils.hasText(body.getBatchNo()) || !StringUtils.hasText(body.getApiCode())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResult<Void>().fail("batchNo、apiCode不能为空"));
        }
        Result<Void> result = tcyrSyncRecordApiCodeFillService.fillApiCode(body.getBatchNo(), body.getApiCode());
        ApiResult<Void> api = new ApiResult<Void>().fromResult(result, INNER_CODE);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(api);
        }
        return ResponseEntity.ok(api);
    }

    @Operation(summary = "按 batchNo 补齐 apiCode（GET）", description = "与 POST 逻辑一致，便于运维 curl")
    @GetMapping("/tcapiCodeFill")
    public ResponseEntity<ApiResult<Void>> tcapiCodeFillGet(
            @RequestParam String batchNo,
            @RequestParam String apiCode) {
        if (!StringUtils.hasText(batchNo) || !StringUtils.hasText(apiCode)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResult<Void>().fail("batchNo、apiCode不能为空"));
        }
        Result<Void> result = tcyrSyncRecordApiCodeFillService.fillApiCode(batchNo, apiCode);
        ApiResult<Void> api = new ApiResult<Void>().fromResult(result, INNER_CODE);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(api);
        }
        return ResponseEntity.ok(api);
    }
}
