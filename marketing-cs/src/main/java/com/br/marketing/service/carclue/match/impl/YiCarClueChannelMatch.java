package com.br.marketing.service.carclue.match.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueSeriesInformation;
import com.br.marketing.service.carclue.clueenums.*;
import com.br.marketing.service.carclue.common.MatchPatternCommon;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class YiCarClueChannelMatch extends AbstractClueChannelMatch {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public static final String ALL_SERVIES = "全系";


    @Override
    public Result<CarClueInfo> action(CarClueInfo carClueInfo, List<CarClueProvincesInformation> provincesInfoConfig, List<CarClueSeriesInformation>
            seriesInfoConfig, List<CarClueRelationalMapping> relationalMappingConfig) {
        String configApiCode = provincesInfoConfig.get(0).getApiCode();
        String brand = carClueInfo.getBrand();
        String series = carClueInfo.getSeries();
        String city = carClueInfo.getCity();
        List<String> brandConfig = seriesInfoConfig.stream().map(CarClueSeriesInformation::getBrandName).collect(Collectors.toList());
        List<String> cityConfig = provincesInfoConfig.stream().map(CarClueProvincesInformation::getCityName).collect(Collectors.toList());
        //数据缺失
        if (StringUtils.isEmpty(city) || StringUtils.isEmpty(series)) {
            carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("城市或车系为空"), CarClueDataStatusEnum.LACK_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        //精确匹配
        Boolean completeMatch = culeCompleteMatch(brand, series, brandConfig, seriesInfoConfig);
        if (completeMatch) {
            carClueInfo.setClueMatchBrand(brand);
            carClueInfo.setClueMatchSeries(series);
            carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue());
            //为空进行赋值补全状态
            if (Objects.isNull(carClueInfo.getClueCompleteStatus())) {
                carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.NORMAL_COMPLETE.getValue());
            }
        } else {
            if (!culeFuzzyMatch(carClueInfo, brandConfig, seriesInfoConfig)) {
                carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("品牌车系匹配失败"),
                        CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
            }
        }
        //城市匹配
        String cityMatch = MatchPatternCommon.fuzzyMatchByShort(city, cityConfig, marketingCommonConfig.getCarClueFilterStr());
        if (StringUtils.isEmpty(cityMatch)) {
            carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("城市未在配置表中，匹配失败"), CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        String provinceName = provincesInfoConfig.stream().filter(provincesInfo -> provincesInfo.getCityName().equals(cityMatch)).
                collect(Collectors.toList()).get(0).getProvinceName();
        //城市品牌关联校验
        CarClueRelationalMapping clueRelationalMapping = new CarClueRelationalMapping();
        List<CarClueRelationalMapping> relationBrand = relationalMappingConfig.stream().filter(relationalMapping -> relationalMapping.getBrandName()
                .equals(carClueInfo.getClueMatchBrand())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(relationBrand)) {
            carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("品牌未在映射表中，匹配失败"), CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
        }
        //获取映射表中车系
        List<CarClueRelationalMapping> allServies = relationBrand.stream().filter(relationalMapping -> relationalMapping.getSeriesName()
                .equals(ALL_SERVIES)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(allServies)) {
            //全系只有一条
            clueRelationalMapping = relationBrand.get(0);
        } else {
            List<CarClueRelationalMapping> seriesConfig = relationBrand.stream().filter(relationalMapping -> relationalMapping.getSeriesName()
                    .equals(carClueInfo.getClueMatchSeries())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(seriesConfig)) {
                carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("车系未在映射表中，匹配失败"), CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
            }
            clueRelationalMapping = seriesConfig.get(0);
        }
        Set<String> relationCityList = new HashSet<>();
        Set<String> relationNotCityList = new HashSet<>();
        if (clueRelationalMapping.getProvinceType().equals(ProvinceTypeEnum.FIXED.getValue())) {
            getCityByProvince(relationCityList, clueRelationalMapping.getSatisfyProvinceName(), clueRelationalMapping.getSatisfyCityName(), provincesInfoConfig);
            //不在映射城市中
            if (!relationCityList.contains(cityMatch)) {
                carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("城市不在映射表中城市中，匹配失败"), CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
            }
        }
        if (clueRelationalMapping.getProvinceType().equals(ProvinceTypeEnum.EXCLUDE.getValue())) {
            getCityByProvince(relationNotCityList, clueRelationalMapping.getExcludeProvinceName(), clueRelationalMapping.getExcludeCityName(), provincesInfoConfig);
            //在排除的城市中
            if (relationNotCityList.contains(cityMatch)) {
                carClueErrorReasonSet(carClueInfo, "渠道[".concat(configApiCode).concat("]").concat("城市在映射表中排除城市中，匹配失败"), CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(carClueInfo);
            }
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
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(carClueInfo);
    }


    private void carClueErrorReasonSet(CarClueInfo carClueInfo, String errorMsg, Integer status) {
        carClueInfo.setClueErrorReason(errorMsg);
        carClueInfo.setClueDataStatus(status);

    }

    private void getCityByProvince(Set<String> cityList, String provinceName, String cityName, List<CarClueProvincesInformation> provincesInfoConfig) {
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
        String brand = "";
        String series = carClueInfo.getSeries();
        List<String> seriesList = seriesInfoConfig.stream().map(CarClueSeriesInformation::getSeriesName).collect(Collectors.toList());
        //匹配车系
        String seriesMatch = MatchPatternCommon.fuzzyMatchByShort(series, seriesList, marketingCommonConfig.getCarClueFilterStr());
        if (StringUtils.isNotEmpty(seriesMatch)) {
            List<String> brandList = getBrandBySeries(seriesMatch, seriesInfoConfig);
            if (brandList.size() == 1) {
                brand = brandList.get(0);
            }
            if (brandList.size() > 1) {
                if (brandList.contains(carClueInfo.getBrand())) {
                    brand = carClueInfo.getBrand();
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车系匹配出多个品牌series=" + seriesMatch));
                    return seriesResult;
                }
            }
            carClueInfo.setClueMatchBrand(brand);
            carClueInfo.setClueMatchSeries(seriesMatch);
            if (Objects.isNull(carClueInfo.getClueCompleteStatus())) {
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

    @Override
    public String label() {
        return ChannelRule.MatchChannelRuleEnum.YC_KA.getLabel();

    }
}
