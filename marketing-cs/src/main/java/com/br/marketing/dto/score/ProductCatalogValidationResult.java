package com.br.marketing.dto.score;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 产管产品目录与任务 product_info 比对结果。
 */
public class ProductCatalogValidationResult {

    private final boolean passed;
    /**
     * 未通过项：含 code、name（product_info 中文名）、version、reason（中文说明）
     */
    private final List<Map<String, String>> failedItems;

    private ProductCatalogValidationResult(boolean passed, List<Map<String, String>> failedItems) {
        this.passed = passed;
        this.failedItems = failedItems;
    }

    public static ProductCatalogValidationResult ok() {
        return new ProductCatalogValidationResult(true, Collections.emptyList());
    }

    public static ProductCatalogValidationResult fail(List<Map<String, String>> failedItems) {
        return new ProductCatalogValidationResult(false, failedItems == null ? Collections.emptyList() : failedItems);
    }

    public boolean isPassed() {
        return passed;
    }

    public List<Map<String, String>> getFailedItems() {
        return failedItems;
    }
}
