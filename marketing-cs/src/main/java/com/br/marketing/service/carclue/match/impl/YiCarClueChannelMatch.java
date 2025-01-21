package com.br.marketing.service.carclue.match.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueSeriesInformation;
import com.br.marketing.service.carclue.clueenums.CarClueCompleteStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueMatchTypeEnum;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import com.br.marketing.service.carclue.common.MatchPatternCommon;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class YiCarClueChannelMatch extends AbstractClueChannelMatch {


    public static final String ALL_SERVIES = "全系";


    @Override
    public Result action(CarClueInfo carClueInfo, List<CarClueProvincesInformation> provincesInfoConfig, List<CarClueSeriesInformation>
            seriesInfoConfig, List<CarClueRelationalMapping> relationalMappingConfig) {

        String brand = carClueInfo.getBrand();
        String series = carClueInfo.getSeries();
        String city = carClueInfo.getCity();
        List<String> brandConfig = seriesInfoConfig.stream().map(CarClueSeriesInformation::getBrandName).collect(Collectors.toList());
        List<String> cityConfig = provincesInfoConfig.stream().map(CarClueProvincesInformation::getCityName).collect(Collectors.toList());
        //数据缺失
        if (StringUtils.isEmpty(city) || StringUtils.isEmpty(series)) {
            carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]城市或车系为空"));
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.LACK_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        //精确匹配
        Boolean completeMatch = culeCompleteMatch(brand, series, brandConfig, seriesInfoConfig);
        if (completeMatch) {
            carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue());
        } else {
            //有效线索并且模糊匹配，结束
            if (carClueInfo.getClueDataStatus().equals(CarClueDataStatusEnum.NORMAL_CLUE.getValue()) &&
                    carClueInfo.getMatchBrandSeriesType().equals(CarClueMatchTypeEnum.FUZZY_MATCH.getValue())) {
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            if (!culeFuzzyMatch(carClueInfo, brandConfig, seriesInfoConfig)) {
                carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]品牌车系匹配失败"));
                carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        //城市匹配
        String cityMatch = MatchPatternCommon.fuzzyMatchByShort(city, cityConfig);
        if (StringUtils.isEmpty(cityMatch)) {
            carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]城市未在配置表中，匹配失败"));
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        String provinceName = provincesInfoConfig.stream().filter(provincesInfo -> provincesInfo.getCityName().equals(cityMatch)).
                collect(Collectors.toList()).get(0).getProvinceName();
        //城市品牌关联校验
        CarClueRelationalMapping clueRelationalMapping = new CarClueRelationalMapping();
        List<CarClueRelationalMapping> relationBrand = relationalMappingConfig.stream().filter(relationalMapping -> relationalMapping.getBrandName()
                .equals(carClueInfo.getClueMatchBrand())).collect(Collectors.toList());
        if (StringUtils.isEmpty(relationBrand)) {
            carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]品牌未在映射表中，匹配失败"));
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        List<CarClueRelationalMapping> allServies = relationBrand.stream().filter(relationalMapping -> relationalMapping.getSeriesName()
                .equals(ALL_SERVIES)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allServies)) {
            clueRelationalMapping = relationBrand.stream().filter(relationalMapping -> relationalMapping.getSeriesName()
                    .equals(carClueInfo.getClueMatchSeries())).collect(Collectors.toList()).get(0);
        } else {
            clueRelationalMapping = relationBrand.get(0);
        }
        if (Objects.isNull(clueRelationalMapping)) {
            carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]车系未在映射表中，匹配失败"));
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        List<String> relationCityList = new ArrayList<>();
        List<String> relationNotCityList = new ArrayList<>();
        if (clueRelationalMapping.getProvinceType().equals("1")) {
            if (StringUtils.isNotEmpty(clueRelationalMapping.getSatisfyProvinceName())) {
                relationCityList.addAll(Arrays.stream(clueRelationalMapping.getSatisfyProvinceName().split(",")).collect(Collectors.toList()));
            }
            if (StringUtils.isNotEmpty(clueRelationalMapping.getSatisfyCityName())) {
                relationCityList.addAll(Arrays.stream(clueRelationalMapping.getSatisfyCityName().split(",")).collect(Collectors.toList()));
            }
            if (!relationCityList.contains(cityMatch)) {
                carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]城市不在映射表中城市中，匹配失败"));
                carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        if (clueRelationalMapping.getProvinceType().equals("2")) {
            if (StringUtils.isNotEmpty(clueRelationalMapping.getExcludeProvinceName())) {
                relationNotCityList.addAll(Arrays.stream(clueRelationalMapping.getExcludeProvinceName().split(",")).collect(Collectors.toList()));
            }
            if (StringUtils.isNotEmpty(clueRelationalMapping.getExcludeCityName())) {
                relationNotCityList.addAll(Arrays.stream(clueRelationalMapping.getExcludeCityName().split(",")).collect(Collectors.toList()));
            }
            if (relationNotCityList.contains(cityMatch)) {
                carClueInfo.setClueErrorReason(carClueInfo.getClueErrorReason().concat("[YiCarClueChannelMatch]城市在映射表中排除城市中，匹配失败"));
                carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        //全国不用判断
        carClueInfo.setClueMatchProvince(provinceName);
        carClueInfo.setClueMatchCity(cityMatch);
        if (StringUtils.isEmpty(carClueInfo.getMatchBrandSeriesType())) {
            carClueInfo.setMatchBrandSeriesType(CarClueMatchTypeEnum.FUZZY_MATCH.getValue());
        }
        carClueInfo.setClueDataStatus(CarClueDataStatusEnum.NORMAL_CLUE.getValue());
        carClueInfo.setCluePushStatus(CarCluePushStatusEnum.READY.getValue());
        return new Result().setCode(ResultCode.SUCCESS.getValue());

    }

    private Boolean culeFuzzyMatch(CarClueInfo carClueInfo, List<String> brandConfig, List<CarClueSeriesInformation> seriesInfoConfig) {

        Boolean seriesResult = Boolean.FALSE;
        String brand = carClueInfo.getBrand();
        String series = carClueInfo.getSeries();
        if (StringUtils.isEmpty(brand)) {
            //根据车系找品牌
            List<String> seriesConfig = seriesInfoConfig.stream().map(CarClueSeriesInformation::getSeriesName).collect(Collectors.toList());
            String seriesMatch = MatchPatternCommon.fuzzyMatchByShort(series, seriesConfig);
            if (StringUtils.isNotEmpty(seriesMatch)) {
                String brandName = seriesInfoConfig.stream().filter(seriesInformation -> seriesInformation.getSeriesName().equals(seriesMatch))
                        .collect(Collectors.toList()).get(0).getBrandName();
                carClueInfo.setClueMatchBrand(brandName);
                carClueInfo.setClueMatchSeries(seriesMatch);
                carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.SYSTEM_COMPLETE.getValue());
                seriesResult = Boolean.TRUE;
            }
        } else {
            String brandMatch = MatchPatternCommon.fuzzyMatchByShort(brand, brandConfig);
            if (StringUtils.isNotEmpty(brandMatch)) {
                List<String> seriesConfig = getSeriesBybrand(brandMatch, seriesInfoConfig);
                String seriesMatch = MatchPatternCommon.fuzzyMatchByShort(series, seriesConfig);
                if (StringUtils.isNotEmpty(seriesMatch)) {
                    carClueInfo.setClueMatchBrand(brandMatch);
                    carClueInfo.setClueMatchSeries(seriesMatch);
                    carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.NORMAL_COMPLETE.getValue());
                    seriesResult = Boolean.TRUE;
                }
            }
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

    @Override
    public String label() {
        return "Yi_Car_Channel_Match";

    }
}
