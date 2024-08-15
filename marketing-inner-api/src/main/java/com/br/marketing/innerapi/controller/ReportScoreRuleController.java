package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.entity.ReportTaskVO;
import com.br.marketing.service.ReportScoreRuleService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.FromItem;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 前端页面 跑分模型分布 规则选择并保存任务记录 功能对应接口
 * 技术方案地址： https://c.100credit.cn/pages/viewpage.action?pageId=174496665
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-14
 */
@Slf4j
@RestController
@RequestMapping("/reportScoreRule")
public class ReportScoreRuleController {

    @Resource
    ReportScoreRuleService reportScoreRuleService;

    @GetMapping("/getTaskScoreProducts")
    public ApiResult<Map> getTaskScoreProducts(@RequestParam(required = true) String ids){
        return new ApiResult<Map>().success(reportScoreRuleService.getProducts(ids));
    }

    @PostMapping("/addReportTaskScore")
    public ApiResult<Boolean> addReportTaskScore(@RequestBody ReportTaskVO reportTaskVO){
        try {
            return reportScoreRuleService.addReportTask(reportTaskVO);
        }catch (Exception e){
            log.warn("添加跑分报表任务异常,入参:{}--", reportTaskVO, e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

}
