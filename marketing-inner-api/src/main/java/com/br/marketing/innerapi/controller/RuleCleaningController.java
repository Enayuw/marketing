package com.br.marketing.innerapi.controller;

import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 规则数据清洗
 * @author guangxiu.li
 * @date 2025/5/6
 */
@RestController
@RequestMapping(value = "/ruleCleaning")
@Api(value = "规则数据清洗", tags = "规则数据清洗", produces = "application/json", consumes = "application/json", protocols = "http")
@Slf4j
public class RuleCleaningController {

    @Resource
    private RuleCleaningService ruleCleaningService;

    // 规则列表查询接口


    // 规则列表新增、编辑接口


    // 字段样例查询接口


    // 字段清洗配置接口


    // 字段运算清洗结果预览接口


    // 字符串处理清洗结果预览接口


    // 优先级清洗结果预览接口




}
