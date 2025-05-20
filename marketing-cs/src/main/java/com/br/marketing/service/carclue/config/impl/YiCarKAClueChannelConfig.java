package com.br.marketing.service.carclue.config.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueSupplementMapper;
import com.br.marketing.service.carclue.clueenums.CarInformationTypeEnum;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.config.AbstractClueChannelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName YiCarKAClueChannelConfig
 * @Author kongbx
 * @Date 2025/5/9 10:18
 */
@Service
@Slf4j
public class YiCarKAClueChannelConfig extends AbstractClueChannelConfig {
    @Resource
    CarClueSupplementMapper carClueSupplementMapper;

    /**
     * 校验省市车辆信息是否匹配
     *
     * @param stringBuilder
     * @param carClueRelationalMapping
     * @param carClueInitMapping
     * @param provinceNameMap
     * @param cityNameMap
     * @param brandNameMap
     */
    @Override
    public void verifyCarClueInit(StringBuilder stringBuilder, CarClueRelationalMapping carClueRelationalMapping,
                                  CarClueInitMapping carClueInitMapping, Map<String, List<CarClueProvincesInformation>> provinceNameMap,
                                  Map<String, List<CarClueProvincesInformation>> cityNameMap, Map<String, List<CarClueSeriesInformation>> brandNameMap,
                                  Map<String, List<CarClueSeriesInformation>> subBrandNameMap) {

        // 处理省份和城市
        String satisfyProvinceName = carClueInitMapping.getSatisfyProvinceName();
        if (satisfyProvinceName != null) {
            String processedNames = processRegionNames(satisfyProvinceName, provinceNameMap, "未匹配到该省：", stringBuilder);
            carClueRelationalMapping.setSatisfyProvinceName(processedNames);
        }

        String excludeProvinceName = carClueInitMapping.getExcludeProvinceName();
        if (excludeProvinceName != null) {
            String processedNames = processRegionNames(excludeProvinceName, provinceNameMap, "未匹配到该省(排除)：", stringBuilder);
            carClueRelationalMapping.setExcludeProvinceName(processedNames);
        }

        String satisfyCityName = carClueInitMapping.getSatisfyCityName();
        if (satisfyCityName != null) {
            String processedNames = processRegionNames(satisfyCityName, cityNameMap, "未匹配到该城市：", stringBuilder);
            carClueRelationalMapping.setSatisfyCityName(processedNames);
        }

        String excludeCityName = carClueInitMapping.getExcludeCityName();
        if (excludeCityName != null) {
            String processedNames = processRegionNames(excludeCityName, cityNameMap, "未匹配到该城市(排除)：", stringBuilder);
            carClueRelationalMapping.setExcludeCityName(processedNames);
        }

        // 从品牌映射中获取品牌信息列表
        List<CarClueSeriesInformation> brandInfoList = brandNameMap.get(carClueInitMapping.getBrandName());

        // 品牌映射中直接找到匹配项
        if (!CollectionUtils.isEmpty(brandInfoList)) {
            carClueRelationalMapping.setBrandId(brandInfoList.get(0).getBrandId());
            return;
        }
        // 未直接匹配时，查询补充数据表
        List<CarClueSupplement> supplements = queryClueSupplement(carClueRelationalMapping.getApiCode(),
                carClueInitMapping.getBrandName(), CarInformationTypeEnum.BRAND.getValue());

        // 补充表中无匹配记录
        if (CollectionUtils.isEmpty(supplements)) {
            stringBuilder.append("未匹配到该品牌：").append(carClueInitMapping.getBrandName()).append(" | ");
            carClueRelationalMapping.setMatchingType(1);
            return;
        }

        // 从补充表中获取新品牌名并再次查询
        String alternativeBrandName = supplements.get(0).getNewName();
        List<CarClueSeriesInformation> alternativeBrandInfo = brandNameMap.get(alternativeBrandName);

        // 补充品牌名也无匹配
        if (CollectionUtils.isEmpty(alternativeBrandInfo)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "未匹配到该品牌(补充)! apiCode：" + carClueInitMapping.getApiCode() + "，品牌：" + alternativeBrandName));
            stringBuilder.append("未匹配到该品牌(补充)：").append(alternativeBrandName).append(" | ");
            carClueRelationalMapping.setMatchingType(1);
            return;
        }
        // 补充品牌名匹配成功
        carClueRelationalMapping.setBrandName(alternativeBrandInfo.get(0).getBrandName());
        carClueRelationalMapping.setBrandId(alternativeBrandInfo.get(0).getBrandId());
    }

    /**
     * 处理省份或城市名称（包含或排除）
     *
     * @param rawNames    原始名称字符串（如："北京,上海,广东"）
     * @param nameMap     省份或城市的映射表（Map<String, List<CarClueProvincesInformation>>）
     * @param errorPrefix 未匹配时的错误前缀（如："未匹配到该省"）
     * @return 处理后的名称字符串（如："北京,上海"）
     */
    private String processRegionNames(String rawNames, Map<String, List<CarClueProvincesInformation>> nameMap,
                                      String errorPrefix, StringBuilder stringBuilder) {
        if (rawNames == null || rawNames.isEmpty()) {
            return "";
        }

        String[] names = rawNames.split(",");
        StringBuilder filteredNames = new StringBuilder();
        boolean isFirst = true;

        for (String name : names) {
            String searchKey = name.trim().replaceAll("市$", "");
            String matchedKey = findMatchedKey(searchKey, nameMap);

            if (nameMap.containsKey(matchedKey) && !CollectionUtils.isEmpty(nameMap.get(matchedKey))) {
                if (!isFirst) {
                    filteredNames.append(",");
                } else {
                    isFirst = false;
                }
                filteredNames.append(matchedKey);
            } else {
                stringBuilder.append(errorPrefix).append(searchKey).append(" | ");
            }
        }

        return filteredNames.toString();
    }

    /**
     * 在Map中查找匹配的Key（支持模糊匹配）
     *
     * @param searchKey 待匹配的关键字（如："广东"）
     * @param nameMap   省份或城市的映射表
     * @return 匹配到的Key（如："广东省"），若未匹配则返回原Key
     */
    private String findMatchedKey(String searchKey, Map<String, List<CarClueProvincesInformation>> nameMap) {
        for (String key : nameMap.keySet()) {
            if (key.contains(searchKey)) {
                // 返回匹配到的标准Key（如："广东省"）
                return key;
            }
        }
        // 未匹配时返回原Key
        return searchKey;
    }

    private List<CarClueSupplement> queryClueSupplement(String apiCode, String oldName, Integer type) {
        // 未直接匹配时，查询补充数据表
        CarClueSupplementExample supplementExample = new CarClueSupplementExample();
        supplementExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andOldNameEqualTo(oldName)
                .andTypeEqualTo(type)
                .andIsDelEqualTo(1);

        return carClueSupplementMapper.selectByExample(supplementExample);
    }

    @Override
    public String label() {
        return ChannelRule.ConfigChannelRuleEnum.YC_KA_CONFIG.getLabel();
    }
}