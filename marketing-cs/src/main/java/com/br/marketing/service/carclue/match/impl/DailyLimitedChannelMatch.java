package com.br.marketing.service.carclue.match.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ClueRelationshipMapper;
import com.br.marketing.service.carclue.clueenums.*;
import com.br.marketing.service.carclue.common.MatchPatternCommon;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName DailyLimitedChannelMatch
 * @Description 日限量命中规则
 * @Author kongbx
 * @Date 2025/3/10 16:22
 */
@Service
@Slf4j
public class DailyLimitedChannelMatch extends AbstractClueChannelMatch {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ClueRelationshipMapper clueRelationshipMapper;

    public static final String ALL_SERVIES = "全系";

    /**
     *
     * @param config 渠道商配置
     * @param carClueInfo 车线索
     * @param provincesInfoConfig 省市配置
     * @param seriesInfoConfig 品牌车系配置
     * @param relationalMappingConfig 映射表配置
     * @return 车线索匹配结果
     */
    @Override
    public Result<CarClueInfo> action(CarChannelConfig config, CarClueInfo carClueInfo, List<CarClueProvincesInformation> provincesInfoConfig,
                                      List<CarClueSeriesInformation> seriesInfoConfig, List<CarClueRelationalMapping> relationalMappingConfig) {
        String configApiCode = provincesInfoConfig.get(0).getApiCode();
        String brand = carClueInfo.getBrand();
        String series = carClueInfo.getSeries();
        String city = carClueInfo.getCity();
        List<String> brandConfig = seriesInfoConfig.stream().map(CarClueSeriesInformation::getBrandName).collect(Collectors.toList());
        List<String> cityConfig = provincesInfoConfig.stream().map(CarClueProvincesInformation::getCityName).collect(Collectors.toList());
        //数据缺失
        if (StringUtils.isEmpty(city) || StringUtils.isEmpty(series)) {
            carClueErrorReasonSet(carClueInfo, config.getName().concat("[").concat(configApiCode).concat("]").concat("城市或车系为空"),
                    CarClueDataStatusEnum.LACK_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        //校验特殊字符
        String carClueVerifyStr = marketingCommonConfig.getCarClueVerifyStr();
        if (containsAnyChar(brand, carClueVerifyStr) || containsAnyChar(series, carClueVerifyStr)
                || containsAnyChar(city, carClueVerifyStr)) {
            carClueErrorReasonSet(carClueInfo, config.getName().concat("[").concat(configApiCode).concat("]").concat("该线索存在多条（特殊字符分隔）"),
                    CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        //精确匹配
        Boolean completeMatch = culeCompleteMatch(brand, series, brandConfig, seriesInfoConfig);
        if (completeMatch) {
            carClueInfo.setClueMatchBrand(brand);
            carClueInfo.setClueMatchSeries(series);
            carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue());
            //非手动补全
            if (carClueInfo.getClueCompleteStatus() == 0 || carClueInfo.getClueCompleteStatus() == 1) {
                carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.NORMAL_COMPLETE.getValue());
            }
        } else {
            //精确匹配失败
            if (!culeFuzzyMatch(carClueInfo, brandConfig, seriesInfoConfig)) {
                //模糊匹配失败
                carClueErrorReasonSet(carClueInfo, config.getName().concat("[").concat(configApiCode).concat("]").concat("品牌车系匹配失败"),
                        CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
            }
        }
        //城市匹配
        String cityMatch = MatchPatternCommon.fuzzyMatchByShort(city, cityConfig, marketingCommonConfig.getCarClueFilterStr());
        if (StringUtils.isEmpty(cityMatch)) {
            carClueErrorReasonSet(carClueInfo, config.getName().concat("[").concat(configApiCode).concat("]").concat("城市未在城市配置表中"),
                    CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        String provinceName = provincesInfoConfig.stream().filter(provincesInfo -> provincesInfo.getCityName().equals(cityMatch)).
                collect(Collectors.toList()).get(0).getProvinceName();
        //城市品牌关联校验
        CarClueRelationalMapping clueRelationalMapping = new CarClueRelationalMapping();
        List<CarClueRelationalMapping> relationBrand = relationalMappingConfig.stream().filter(relationalMapping -> relationalMapping.getBrandName()
                .equals(carClueInfo.getClueMatchBrand())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(relationBrand)) {
            carClueErrorReasonSet(carClueInfo, config.getName().concat("[").concat(configApiCode).concat("]").concat("品牌=").concat(carClueInfo.
                    getClueMatchBrand()).concat("未在外采配置中"), CarClueDataStatusEnum.NORMAL_MAPPER_LACK_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }

        // 获取映射表中车系配置
        Optional<CarClueRelationalMapping> allSeriesOpt = relationBrand.stream()
                .filter(mapping -> ALL_SERVIES.equals(mapping.getSeriesName()))
                .findFirst();

        if (allSeriesOpt.isPresent()) {
            clueRelationalMapping = allSeriesOpt.get();
        } else {
            // 查找具体车系配置
            List<CarClueRelationalMapping> seriesMappings = relationBrand.stream()
                    .filter(mapping -> carClueInfo.getClueMatchSeries().equals(mapping.getSeriesName()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(seriesMappings)) {
                return buildErrorResult(carClueInfo, config, configApiCode,
                        "车系=" + carClueInfo.getClueMatchSeries() + "未在外采配置中",
                        CarClueDataStatusEnum.NORMAL_MAPPER_LACK_CLUE);
            }

            // 查找城市匹配的配置
            Optional<CarClueRelationalMapping> cityMatchOpt = seriesMappings.stream()
                    .filter(mapping -> isCityValid(mapping, cityMatch, provincesInfoConfig))
                    .findFirst();

            if (!cityMatchOpt.isPresent()) {
                return buildErrorResult(carClueInfo, config, configApiCode,
                        "城市=" + cityMatch + "未在外采配置中",
                        CarClueDataStatusEnum.NORMAL_MAPPER_LACK_CLUE);
            }
            clueRelationalMapping = cityMatchOpt.get();
        }

        // 检查限量配置
        if (isLimitExceeded(clueRelationalMapping)) {
            return buildErrorResult(carClueInfo, config, configApiCode, "今日已限量",
                    CarClueDataStatusEnum.LIMITED_LACK_CLUE);
        }

        //全国不用判断
        carClueInfo.setClueMatchProvince(provinceName);
        carClueInfo.setClueMatchCity(cityMatch);
        carClueInfo.setClueDataStatus(CarClueDataStatusEnum.NORMAL_CLUE.getValue());
        carClueInfo.setCluePushStatus(CarCluePushStatusEnum.READY.getValue());
        carClueInfo.setCluePushChannel(configApiCode);
        carClueInfo.setClueMatchBrandId(clueRelationalMapping.getBrandId().toString());
        carClueInfo.setClueMatchSeriesId(seriesInfoConfig.stream().filter(carClueSeriesInfo -> carClueSeriesInfo.getSeriesName()
                .equals(carClueInfo.getClueMatchSeries())).collect(Collectors.toList()).get(0).getSeriesId().toString());
        carClueInfo.setDemandId(clueRelationalMapping.getDemandId());

        //增加线索-外采对应关系
        ClueRelationship clueRelationship = new ClueRelationship();
        clueRelationship.setClueInfoId(carClueInfo.getId());
        clueRelationship.setApiCode(carClueInfo.getApiCode());
        clueRelationship.setMappingId(clueRelationalMapping.getId());
        clueRelationship.setStatus(0);
        clueRelationship.setCreateTime(new Date());
        clueRelationship.setUpdateTime(new Date());
        clueRelationshipMapper.insertSelective(clueRelationship);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(carClueInfo);
    }

    // 城市验证方法
    private boolean isCityValid(CarClueRelationalMapping mapping, String city,
                                List<CarClueProvincesInformation> config) {

        Set<String> cities = new HashSet<>();
        if (ProvinceTypeEnum.FIXED.getValue().equals(mapping.getProvinceType())) {
            getCityByProvince(cities, mapping.getSatisfyProvinceName(),
                    mapping.getSatisfyCityName(), config);
            return cities.contains(city);
        }
        else if (ProvinceTypeEnum.EXCLUDE.getValue().equals(mapping.getProvinceType())) {
            getCityByProvince(cities, mapping.getExcludeProvinceName(),
                    mapping.getExcludeCityName(), config);
            return cities.contains(city);
        }
        return true;
    }

    // 构建错误结果方法
    private Result<CarClueInfo> buildErrorResult(CarClueInfo carClueInfo, CarChannelConfig config,
                                    String configApiCode, String reason, CarClueDataStatusEnum status) {

        String fullMsg = config.getName() + "[" + configApiCode + "]" + reason;
        carClueErrorReasonSet(carClueInfo, fullMsg, status.getValue());
        return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
    }

    // 限量检查方法
    private boolean isLimitExceeded(CarClueRelationalMapping mapping) {
        return mapping.getDailyLimited() == 0 ||
                mapping.getMatchDailyLimited() >= mapping.getDailyLimited();
    }


    private void carClueErrorReasonSet(CarClueInfo carClueInfo, String errorMsg, Integer status) {
        carClueInfo.setClueErrorReason(errorMsg);
        carClueInfo.setClueDataStatus(status);

    }

    private void getCityByProvince(Set<String> cityList, String provinceName, String cityName, List<CarClueProvincesInformation> provincesInfoConfig){
        if (StringUtils.isNotEmpty(provinceName)) {
            List<String> provinceList = Arrays.stream(provinceName.split(",")).collect(Collectors.toList());
            provinceList.forEach(province -> {
                Set<String> cityByProvince = provincesInfoConfig.stream().filter(provincesInformation -> provincesInformation.getProvinceName()
                        .equals(province)).map(CarClueProvincesInformation::getCityName).collect(Collectors.toSet());
                cityList.addAll(cityByProvince);
            });
        }
        if (StringUtils.isNotEmpty(cityName)) {
            cityList.addAll(Arrays.stream(cityName.split(",")).collect(Collectors.toList()));
        }
    }

    private Boolean culeFuzzyMatch(CarClueInfo carClueInfo, List<String> brandConfig, List<CarClueSeriesInformation> seriesInfoConfig) {

        Boolean seriesResult = Boolean.FALSE;
        String matchBrand = "";
        String series = carClueInfo.getSeries();
        List<String> seriesList = seriesInfoConfig.stream().map(CarClueSeriesInformation::getSeriesName).collect(Collectors.toList());
        String brandMatch = MatchPatternCommon.fuzzyMatchByShort(carClueInfo.getBrand(), brandConfig, marketingCommonConfig.getCarClueFilterStr());
        //品牌车系匹配
        if (StringUtils.isNotEmpty(brandMatch)) {
            List<String> seriesConfig = getSeriesBybrand(brandMatch, seriesInfoConfig);
            String seriesMatch = MatchPatternCommon.fuzzyMatchByShort(series, seriesConfig, marketingCommonConfig.getCarClueFilterStr());
            if (StringUtils.isNotEmpty(seriesMatch)) {
                carClueInfo.setClueMatchBrand(brandMatch);
                carClueInfo.setClueMatchSeries(seriesMatch);
                //非手动补全
                if (carClueInfo.getClueCompleteStatus() == 0 || carClueInfo.getClueCompleteStatus() == 1) {
                    carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.SYSTEM_COMPLETE.getValue());
                }
                carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.FUZZY_MATCH.getValue());
                return Boolean.TRUE;
            }
        }

        //品牌车系未匹配成功，模糊匹配车系
        String seriesMatch = MatchPatternCommon.fuzzyMatchByShort(series, seriesList, marketingCommonConfig.getCarClueFilterStr());
        if (StringUtils.isNotEmpty(seriesMatch)) {
            List<String> brandList = getBrandBySeries(seriesMatch, seriesInfoConfig);
            if (brandList.size() == 1) {
                matchBrand = brandList.get(0);
            }
            if (brandList.size() > 1) {
                if (brandList.contains(carClueInfo.getBrand())) {
                    matchBrand = carClueInfo.getBrand();
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车系匹配出多个品牌series=" + seriesMatch));
                    return seriesResult;
                }
            }
            carClueInfo.setClueMatchBrand(matchBrand);
            carClueInfo.setClueMatchSeries(seriesMatch);
            //非手动补全
            if (carClueInfo.getClueCompleteStatus() == 0 || carClueInfo.getClueCompleteStatus() == 1) {
                carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.SYSTEM_COMPLETE.getValue());
            }
            carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.FUZZY_MATCH.getValue());
            seriesResult = Boolean.TRUE;
        }
        return seriesResult;

    }

    private Boolean culeCompleteMatch(String brand, String series, List<String> brandConfig, List<CarClueSeriesInformation>
            seriesInfoConfig) {

        Boolean matchResult = Boolean.FALSE;
        if (StringUtils.isEmpty(brand)) {
            return matchResult;
        }
        //精确匹配品牌
        if (MatchPatternCommon.completeMatch(brand, brandConfig)) {
            List<String> seriesConfig = getSeriesBybrand(brand, seriesInfoConfig);
            if (MatchPatternCommon.completeMatch(series, seriesConfig)) {
                matchResult = Boolean.TRUE;
            }
        }
        return matchResult;
    }

    private List<String> getSeriesBybrand(String brand, List<CarClueSeriesInformation> seriesInfoConfig) {
        return seriesInfoConfig.stream().filter(clueSeriesInfo -> clueSeriesInfo.getBrandName().equals(brand))
                .map(CarClueSeriesInformation::getSeriesName).collect(Collectors.toList());


    }

    private List<String> getBrandBySeries(String series, List<CarClueSeriesInformation> seriesInfoConfig) {
        return seriesInfoConfig.stream().filter(clueSeriesInfo -> clueSeriesInfo.getSeriesName().equals(series))
                .map(CarClueSeriesInformation::getBrandName).collect(Collectors.toList());


    }

    public static boolean containsAnyChar(String str, String chars) {
        return chars.chars().anyMatch(ch -> str.indexOf(ch) != -1);
    }

    @Override
    public String label() {
        return ChannelRule.MatchChannelRuleEnum.DAILY_LIMITED.getLabel();
    }
}
