package com.br.marketing.service;

import com.br.marketing.dto.score.ProductCatalogValidationResult;
import com.br.marketing.entity.MarketingTask;

/**
 * 跑分前校验任务所选产品/版本是否仍在产管当前许可列表内。
 */
public interface ProductCatalogValidationService {

    /**
     * {@link com.br.marketing.common.enums.TaskTypeEnum#DIRECTDATA}、{@link com.br.marketing.common.enums.TaskTypeEnum#STRATYGYDATA} 直接视为通过。
     * 仅 {@link com.br.marketing.common.enums.TaskTypeEnum#PRODUCTDATA} 解析 {@code product_info} 与产管列表比对。
     */
    ProductCatalogValidationResult validate(MarketingTask task);
}
