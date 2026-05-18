package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.score.ProductCatalogValidationResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.service.ProductCatalogValidationService;
import com.br.marketing.strategy.customizer.StrategyCustomizerProductCatalogAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ProductCatalogValidationServiceImpl implements ProductCatalogValidationService {

    @Resource
    private StrategyCustomizerProductCatalogAssembler strategyCustomizerProductCatalogAssembler;

    @Override
    public ProductCatalogValidationResult validate(MarketingTask task) {
        Integer taskType = task.getTaskType();
        if (taskType == null
                || TaskTypeEnum.DIRECTDATA.getValue().equals(taskType)
                || TaskTypeEnum.STRATYGYDATA.getValue().equals(taskType)) {
            return ProductCatalogValidationResult.ok();
        }
        if (!TaskTypeEnum.PRODUCTDATA.getValue().equals(taskType)) {
            return ProductCatalogValidationResult.ok();
        }

        if (StringUtils.isBlank(task.getProductInfo())) {
            return ProductCatalogValidationResult.fail(Collections.singletonList(failRow("", "", "")));
        }

        JSONArray arr;
        try {
            arr = JSONArray.parseArray(task.getProductInfo());
        } catch (Exception e) {
            log.warn("product_info 非合法 JSON，batchNumber={}", task.getBatchNumber(), e);
            return ProductCatalogValidationResult.fail(Collections.singletonList(failRow("", "", "")));
        }
        if (arr == null || arr.isEmpty()) {
            return ProductCatalogValidationResult.fail(Collections.singletonList(failRow("", "", "")));
        }

        Map<String, Set<String>> allowed;
        try {
            allowed = strategyCustomizerProductCatalogAssembler.buildAllowedProductCodeVersionMap();
        } catch (Exception e) {
            log.error("拉取产管产品目录失败，batchNumber={}", task.getBatchNumber(), e);
            return ProductCatalogValidationResult.fail(Collections.singletonList(failRow("", "", "")));
        }

        List<Map<String, String>> failed = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o == null) {
                continue;
            }
            // product_info：code 对应产管聚合项 productName；name 为中文名；version 对应 versions[].version
            String name = trimToEmpty(o.getString("name"));
            String code = firstNonBlank(o.getString("code"), o.getString("productName"));
            String version = trimToEmpty(o.getString("version"));
            if (StringUtils.isBlank(code)) {
                failed.add(failRow("", name, version));
                continue;
            }
            Set<String> okVersions = allowed.get(code);
            if (okVersions == null || okVersions.isEmpty()) {
                failed.add(failRow(code, name, version));
                continue;
            }
            if (!okVersions.contains(version)) {
                failed.add(failRow(code, name, version));
            }
        }

        if (failed.isEmpty()) {
            return ProductCatalogValidationResult.ok();
        }
        return ProductCatalogValidationResult.fail(failed);
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /** code 优先，兼容历史字段 productName */
    private static String firstNonBlank(String primary, String fallback) {
        String a = trimToEmpty(primary);
        if (!a.isEmpty()) {
            return a;
        }
        return trimToEmpty(fallback);
    }

    private static Map<String, String> failRow(String code, String name, String version) {
        Map<String, String> m = new HashMap<>(8);
        m.put("code", code);
        m.put("name", name);
        m.put("version", version);
        return m;
    }
}
