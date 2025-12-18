package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckSwap;
import com.br.marketing.entity.AutoCheckSwapExample;
import com.br.marketing.mapper.AutoCheckConfigMapper;
import com.br.marketing.mapper.AutoCheckSwapMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.vo.MarketingCustomerVO;
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

    @Resource
    private MarketingCustomerService marketingCustomerService;

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

        // 查询apiCode信息
        List<MarketingCustomerVO> apiCodeInfoList = marketingCustomerService.getApiCodeList(apiCodeList);
        Map<String, MarketingCustomerVO> apiCodeInfoMap = apiCodeInfoList.stream()
                .collect(Collectors.toMap(MarketingCustomerVO::getApiCode, e -> e));

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
        List<SenceVO> senceVOList = autoCheckSwapMapper.selectBySenceCodes(allSenceCodes);
        Map<String, SenceVO> senceMap = senceVOList.stream()
                .collect(Collectors.toMap(SenceVO::getSenceCode, sence -> sence));
        Map<String, AutoCheckConfig> configMapByApiCode = configList.stream()
                .collect(Collectors.toMap(AutoCheckConfig::getApiCode, e -> e));

        // 组装AutoConfigVO
        for (String apiCode : configMapByApiCode.keySet()) {
            AutoConfigVO vo = new AutoConfigVO();
            AutoCheckConfig apiConfig = configMapByApiCode.get(apiCode);
            vo.setId(apiConfig.getId());
            vo.setApiCode(apiCode);
            vo.setName(apiCodeInfoMap.get(apiCode).getName());

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
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<SenceVO> searchSenceList(String searchContent) {
        return autoCheckSwapMapper.searchSenceList(searchContent);
    }

    @Override
    public Boolean saveAutoCheckConfig(SaveAutoCheckConfigDto dto) {
        int result;
        if (Objects.isNull(dto.getId())) {
            // 新增
            AutoCheckConfig config = new AutoCheckConfig();
            config.setApiCode(dto.getApiCode());
            config.setSenceCode(dto.getSenceCodes());
            Date now = new Date();
            config.setCreateTime(now);
            config.setUpdateTime(now);
            // 插入数据库
            result = autoCheckConfigMapper.insertSelective(config);
        } else {
            // 编辑
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByPrimaryKey(dto.getId());
            if (Objects.isNull(existingConfig)) {
                log.warn("要编辑的配置不存在，id: {}", dto.getId());
                return false;
            }
            // 更新字段
            existingConfig.setSenceCode(dto.getSenceCodes());
            existingConfig.setUpdateTime(new Date());
            // 更新数据库
            result = autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig);
        }
        return result > 0;
    }
}
