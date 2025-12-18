package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckSwap;
import com.br.marketing.entity.AutoCheckSwapExample;
import com.br.marketing.mapper.AutoCheckConfigMapper;
import com.br.marketing.mapper.AutoCheckSwapMapper;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.vo.autocheck.AutoConfigVO;
import com.br.marketing.vo.autocheck.SenceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: fuzhen.zhang
 * @description: 自动化巡检service
 * @date: 2025/12/18 15:35
 */
@Service
@Slf4j
public class AutoCheckServiceImpl implements AutoCheckService {

    @Resource
    private AutoCheckSwapMapper autoCheckSwapMapper;

    @Resource
    private AutoCheckConfigMapper autoCheckConfigMapper;

    @Override
    public List<AutoConfigVO> getConfigList(String apiCodes, String senceCodes) {
        List<AutoConfigVO> result = new ArrayList<>();

        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(apiCodes)) {
            apiCodeList = Arrays.stream(apiCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 处理senceCodes参数，用逗号分隔
        List<String> senceCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(senceCodes)) {
            senceCodeList = Arrays.stream(senceCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 根据apiCodes和senceCodes查询配置信息
        List<AutoCheckConfig> configList = autoCheckConfigMapper.
                selectByApiCodesAndSenceCodes(apiCodeList, senceCodeList);

        if (CollUtil.isEmpty(configList)) {
            return result;
        }

        // 收集所有配置中涉及的场景编码，用于查询场景信息
        List<String> allSenceCodes = new ArrayList<>();
        for (AutoCheckConfig config : configList) {
            if (StringUtils.isNotBlank(config.getSenceCode())) {
                // 配置中的sence_code字段可能包含逗号分隔的多个场景
                List<String> configSenceCodes = Arrays.stream(config.getSenceCode().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                allSenceCodes.addAll(configSenceCodes);
            }
        }

        // 去重
        allSenceCodes = allSenceCodes.stream().distinct().collect(Collectors.toList());

        // 查询场景信息
        Map<String, SenceVO> senceMap = new HashMap<>();
        if (!CollUtil.isEmpty(allSenceCodes)) {
            List<SenceVO> senceVOList = autoCheckSwapMapper.selectBySenceCodes(allSenceCodes);
            senceMap = senceVOList.stream()
                    .collect(Collectors.toMap(SenceVO::getSenceCode, sence -> sence));
        }

        // 按api_code分组配置，用于组装AutoConfigVO
        Map<String, AutoCheckConfig> configMapByApiCode = configList.stream()
                .collect(Collectors.toMap(AutoCheckConfig::getApiCode, e -> e));

        // 组装AutoConfigVO
        for (String apiCode : configMapByApiCode.keySet()) {
            AutoConfigVO vo = new AutoConfigVO();
            AutoCheckConfig apiConfig = configMapByApiCode.get(apiCode);
            vo.setId(apiConfig.getId());
            vo.setApiCode(apiCode);
            // 注意：这里设置name为apiCode，如果需要从其他地方获取，请调整
            vo.setName(apiCode);
            // 收集该ApiCode下的所有场景信息
            List<SenceVO> senceList = new ArrayList<>();

            String simpleConfigSenceCodes = apiConfig.getSenceCode();
            if (StringUtils.isNotBlank(simpleConfigSenceCodes)) {
                String[] split = simpleConfigSenceCodes.split(",");
                for (String senceCode : split) {
                    SenceVO senceVO = senceMap.get(senceCode);
                    if (senceVO != null) {
                        senceList.add(senceVO);
                    }
                }
            }
            vo.setSence(senceList);
            // 设置第一个配置的ID作为VO的ID
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<SenceVO> searchSenceList(String searchContent) {
        return autoCheckSwapMapper.searchSenceList(searchContent);
    }
}
