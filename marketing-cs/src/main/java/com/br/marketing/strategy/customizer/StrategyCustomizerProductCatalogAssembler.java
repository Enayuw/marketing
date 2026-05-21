package com.br.marketing.strategy.customizer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.config.properties.ProductManagementApiProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 从产管接口拉取产品列表与类型，解析后按 {@code productName} 聚合版本信息，供策略定制等场景复用。
 */
@Component
public class StrategyCustomizerProductCatalogAssembler {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ProductManagementApiProperties productManagementApiProperties;

    /**
     * 使用配置中的 {@code api.product-management.url} 与 {@code api.product-management.productTypeUrl}。
     */
    public List<Map<String, Object>> buildGroupedProductCatalog() {
        String productUrl = productManagementApiProperties.getUrl();
        String productTypeUrl = productManagementApiProperties.getProductTypeUrl();
        if (!StringUtils.hasText(productUrl) || !StringUtils.hasText(productTypeUrl)) {
            throw new IllegalStateException("api.product-management.url / productTypeUrl 未配置或为空");
        }
        return buildGroupedProductCatalog(productUrl, productTypeUrl);
    }

    /**
     * 使用指定 URL 拉取并聚合（便于联调或与配置解耦的调用方）。
     */
    public List<Map<String, Object>> buildGroupedProductCatalog(String productListUrl, String productTypeListUrl) {
        String productBody = restTemplate.getForObject(productListUrl, String.class);
        String productTypeBody = restTemplate.getForObject(productTypeListUrl, String.class);

        JSONObject productTypeJson = (JSONObject) JSONObject.parse(productTypeBody);
        JSONArray productTypeData = productTypeJson == null ? null : productTypeJson.getJSONArray("data");

        JSONObject productJson = (JSONObject) JSONObject.parse(productBody);
        JSONArray datArray = productJson == null ? null : productJson.getJSONArray("data");

        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<String> productSet = new TreeSet<>();

        if (datArray != null) {
            for (int i = 0; i < datArray.size(); i++) {
                Map<String, Object> productMap = new HashMap<>();
                JSONObject row = datArray.getJSONObject(i);
                String productionChineseName = row.getString("productionChineseName");
                String productName = row.getString("productionName");
                String productionTypeCode = row.getString("productionTypeCode");
                String spreadStatus = row.getString("spreadStatus");
                String version = row.getString("version");
                productMap.put("productChineseName", productionChineseName);
                productMap.put("productName", productName);
                productMap.put("spreadStatus", Integer.valueOf(spreadStatus));
                productMap.put("versions", version);
                productMap.put("version", "");
                if (productTypeData != null) {
                    for (int j = 0; j < productTypeData.size(); j++) {
                        JSONObject typeRow = productTypeData.getJSONObject(j);
                        String typeCode = typeRow.getString("typeCode");
                        String secondType = typeRow.getString("secondType");
                        if (productionTypeCode != null && productionTypeCode.equals(typeCode)) {
                            productMap.put("secondTypeName", secondType);
                            productMap.put("secondTypeCode", typeCode);
                        }
                    }
                }
                dataList.add(productMap);
                if (productName != null) {
                    productSet.add(productName);
                }
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String productSetName : productSet) {
            List<Map<String, Object>> versionList = new ArrayList<>();
            Map<String, Object> productMap = new HashMap<>();
            for (Map<String, Object> stringObjectMap : dataList) {
                Object pn = stringObjectMap.get("productName");
                if (pn != null && pn.equals(productSetName)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("spreadStatus", stringObjectMap.get("spreadStatus"));
                    String ver = stringObjectMap.get("versions").toString();
                    map.put("version", ver);
                    versionList.add(map);
                    productMap = stringObjectMap;
                }
            }
            productMap.put("versions", versionList);
            resultList.add(productMap);
        }
        return resultList;
    }

    /**
     * 与 createView 聚合结果一致：{@code productName}（产管 productionName）→ 当前允许的 version 集合（聚合后 versions[].version）。
     * 跑分任务 {@code product_info} 中 {@code code} 与 {@code productName} 对齐。
     */
    public Map<String, Set<String>> buildAllowedProductCodeVersionMap() {
        return toAllowedProductCodeVersionMap(buildGroupedProductCatalog());
    }

    /**
     * 由已聚合的目录列表构造允许集合（便于单测或缓存结果复用）。
     */
    @SuppressWarnings("unchecked")
    public Map<String, Set<String>> toAllowedProductCodeVersionMap(List<Map<String, Object>> groupedCatalog) {
        Map<String, Set<String>> allowed = new HashMap<>();
        if (groupedCatalog == null) {
            return allowed;
        }
        for (Map<String, Object> row : groupedCatalog) {
            Object nameObj = row.get("productName");
            if (nameObj == null) {
                continue;
            }
            String code = nameObj.toString().trim();
            Set<String> vers = allowed.computeIfAbsent(code, k -> new HashSet<>());
            Object vObj = row.get("versions");
            if (vObj instanceof List) {
                for (Object o : (List<?>) vObj) {
                    if (o instanceof Map) {
                        Object v = ((Map<?, ?>) o).get("version");
                        if (v != null) {
                            vers.add(v.toString().trim());
                        }
                    }
                }
            }
        }
        return allowed;
    }
}
