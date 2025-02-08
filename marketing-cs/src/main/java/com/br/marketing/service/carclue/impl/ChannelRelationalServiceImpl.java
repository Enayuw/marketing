package com.br.marketing.service.carclue.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.carclue.CarClueClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueInitMappingMapper;
import com.br.marketing.mapper.CarClueProvincesInformationMapper;
import com.br.marketing.mapper.CarClueRelationalMappingMapper;
import com.br.marketing.mapper.CarClueSeriesInformationMapper;
import com.br.marketing.service.carclue.ChannelRelationalService;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.clueenums.ProvinceTypeEnum;
import com.br.marketing.service.carclue.web.impl.CarClueReportServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

/**
 * @ClassName ChannelRelationalServiceImpl
 * @Description 外采渠道映射关系维护
 * @Author kongbx
 * @Date 2025/1/19 17:15
 */
@Service
@Slf4j
public class ChannelRelationalServiceImpl implements ChannelRelationalService {

    @Resource
    CarClueClient carClueClient;
    @Resource
    CarClueProvincesInformationMapper carClueProvincesInformationMapper;
    @Resource
    CarClueSeriesInformationMapper carClueSeriesInformationMapper;
    @Resource
    CarClueInitMappingMapper carClueInitMappingMapper;
    @Resource
    CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    CarClueReportServiceImpl carClueReportServiceImpl;

    private static final String YCKATASK = "7-1";
    private static final String YCMEMBERTASK = "6+";
    public static final String ALL_SERVIES = "全系";
    private static final String TITL = "【车线索外采数据相关-】";

    @Override
    public void getProvinceAndCity() {

        CarClueProvincesInformationExample carClueProvincesInformationExample = new CarClueProvincesInformationExample();
        carClueProvincesInformationExample.createCriteria()
                .andAppletDateEqualTo(LocalDate.now().toString())
                .andIsDelEqualTo(Constants.DATA_VALID);
        int i = carClueProvincesInformationMapper.countByExample(carClueProvincesInformationExample);
        if(i > 0){
            return;
        }
        //省市信息
        buildZjCity();
        buildYcCity(YCKATASK, ChannelRule.MatchChannelRuleEnum.YC_KA.getLabel());
        buildYcCity(YCMEMBERTASK,ChannelRule.MatchChannelRuleEnum.YC_MEMBER.getLabel());
        //车辆信息
        buildZjCar();
        buildYcCar(YCKATASK,ChannelRule.MatchChannelRuleEnum.YC_KA.getLabel());
        buildYcCar(YCMEMBERTASK,ChannelRule.MatchChannelRuleEnum.YC_MEMBER.getLabel());
    }

    private void buildZjCity() {
        Result<JSONArray> zjCityResult = carClueClient.getZjCity();

        if (!ResultCode.SUCCESS.getValue().equals(zjCityResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "之家，省市调用异常, result：" +  zjCityResult.getMessage()));
            return;
        }
        JSONArray jsonArray = zjCityResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "之家，省市调用异常，返回数据为空");
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey
                (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel());
        List<CarClueProvincesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            Integer provinceId = firstData.getInteger("id");
            String provinceName = firstData.getString("name");
            JSONArray nodesArray = firstData.getJSONArray("nodes");
            for (int j = 0; j < nodesArray.size(); j++) {
                JSONObject node = nodesArray.getJSONObject(j);
                Integer cityId = node.getInteger("id");
                String cityName = node.getString("name");
                for (String apiCode : apiCodes) {
                    CarClueProvincesInformation info = new CarClueProvincesInformation();
                    info.setApiCode(apiCode);
                    info.setProvinceId(provinceId);
                    info.setProvinceName(provinceName);
                    info.setCityId(cityId);
                    info.setCityName(cityName);
                    list.add(info);
                }
                if (list.size() >= 500) {
                    carClueProvincesInformationMapper.batchInsert(list);
                    list.clear();
                }
            }
            // 插入剩余的数据
            if (!list.isEmpty()) {
                carClueProvincesInformationMapper.batchInsert(list);
            }
        }
    }

    private void buildYcCity(String task, String provincesType) {
        Result<JSONArray> ycCityResult = carClueClient.getYcCity(task);

        if (!ResultCode.SUCCESS.getValue().equals(ycCityResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "易车，省市调用异常, provincesType:" + provincesType +", result:"+ ycCityResult.getMessage()));
            return;
        }
        JSONArray jsonArray = ycCityResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "易车，省市调用异常，返回数据为空  provincesType = {},",provincesType);
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey(provincesType);
        List<CarClueProvincesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            for (String apiCode : apiCodes) {
                CarClueProvincesInformation info = new CarClueProvincesInformation();
                info.setApiCode(apiCode);
                info.setProvinceId(firstData.getInteger("provinceId"));
                info.setProvinceName(firstData.getString("provinceName"));
                info.setCityId(firstData.getInteger("cityId"));
                info.setCityName(firstData.getString("cityName"));
                list.add(info);
            }
            if (list.size() >= 500) {
                carClueProvincesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueProvincesInformationMapper.batchInsert(list);
        }
    }

    private void buildZjCar() {
        Result<JSONArray> zjCarResult = carClueClient.getZjCar();

        if (!ResultCode.SUCCESS.getValue().equals(zjCarResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "之家，车辆信息获取异常, result：" +  zjCarResult.getMessage()));
            return;
        }
        JSONArray jsonArray = zjCarResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "之家，车辆信息获取异常，返回数据为空");
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey
                (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel());
        List<CarClueSeriesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            for (String apiCode : apiCodes) {
                CarClueSeriesInformation info = new CarClueSeriesInformation();
                info.setApiCode(apiCode);
                info.setBrandId(firstData.getInteger("brand_id"));
                info.setBrandName(firstData.getString("brand_name"));
                info.setSubBrandId(firstData.getInteger("son_brand_id"));
                info.setSubBrandName(firstData.getString("son_brand_name"));
                info.setSeriesId(firstData.getInteger("series_id"));
                info.setSeriesName(firstData.getString("series_name"));
                list.add(info);
            }
            if (list.size() >= 500) {
                carClueSeriesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueSeriesInformationMapper.batchInsert(list);
        }
    }

    private void buildYcCar(String task,String provincesType) {
        Result<JSONArray> ycCarResult = carClueClient.getYcCar(task);

        if (!ResultCode.SUCCESS.getValue().equals(ycCarResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "易车，车辆信息获取异常, provincesType:" + provincesType +", result:"+ ycCarResult.getMessage()));
            return;
        }
        JSONArray jsonArray = ycCarResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "易车，车辆信息获取异常，返回数据为空, provincesType = {}",provincesType);
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey(provincesType);
        List<CarClueSeriesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            for (String apiCode : apiCodes) {
                CarClueSeriesInformation info = new CarClueSeriesInformation();
                info.setApiCode(apiCode);
                info.setBrandId(firstData.getInteger("brandId"));
                info.setBrandName(firstData.getString("brandName"));
                info.setSeriesId(firstData.getInteger("seriesId"));
                info.setSeriesName(firstData.getString("seriesName"));
                list.add(info);
            }
            if (list.size() >= 500) {
                carClueSeriesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueSeriesInformationMapper.batchInsert(list);
        }
    }

    @Override
    public void relationalMapping() {
        try {
            //获取最新日期
            String proviceCleanDate = carClueProvincesInformationMapper.getMaxCleanDate();
            String seriesCleanDate = carClueSeriesInformationMapper.getMaxCleanDate();

            //获取省市集合
            CarClueProvincesInformationExample carClueProvincesInformationExample = new CarClueProvincesInformationExample();
            carClueProvincesInformationExample.createCriteria()
                    .andAppletDateEqualTo(proviceCleanDate)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueProvincesInformation> list = carClueProvincesInformationMapper.selectByExample(carClueProvincesInformationExample);
            Map<String, List<CarClueProvincesInformation>> groupByProvinces = list.stream()
                    .collect(Collectors.groupingBy(CarClueProvincesInformation::getApiCode));

            //获取品牌车系集合
            CarClueSeriesInformationExample carClueSeriesInformationExample = new CarClueSeriesInformationExample();
            carClueSeriesInformationExample.createCriteria()
                    .andAppletDateEqualTo(seriesCleanDate)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueSeriesInformation> list1 = carClueSeriesInformationMapper.selectByExample(carClueSeriesInformationExample);
            Map<String, List<CarClueSeriesInformation>> groupBySeries = list1.stream()
                    .collect(Collectors.groupingBy(CarClueSeriesInformation::getApiCode));

            //获取外采初始信息
            CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
            carClueInitMappingExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueInitMapping> carClueInitMappingList = carClueInitMappingMapper.selectByExample(carClueInitMappingExample);
            Map<String, List<CarClueInitMapping>> carClueInitMappingMap = carClueInitMappingList.stream()
                    .collect(Collectors.groupingBy(CarClueInitMapping::getApiCode));

            carClueInitMappingMap.forEach((apiCode, v) -> {

                List<CarClueProvincesInformation> carClueProvincesInformations = groupByProvinces.get(apiCode);
                Map<String, List<CarClueProvincesInformation>> provinceNameMap = carClueProvincesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueProvincesInformation::getProvinceName));

                Map<String, List<CarClueProvincesInformation>> cityNameMap = carClueProvincesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueProvincesInformation::getCityName));

                List<CarClueSeriesInformation> carClueSeriesInformations = groupBySeries.get(apiCode);
                Map<String, List<CarClueSeriesInformation>> brandNameMap = carClueSeriesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueSeriesInformation::getBrandName));

                Map<String, List<CarClueSeriesInformation>> seriesNameMap = carClueSeriesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueSeriesInformation::getSeriesName));

                //匹配初始信息
                List<CarClueRelationalMapping> carClueRelationalMappings = new ArrayList<>();
                for (CarClueInitMapping carClueInitMapping : v) {

                    CarClueRelationalMapping carClueRelationalMapping = new CarClueRelationalMapping();
                    carClueRelationalMapping.setMatchingType(0);
                    carClueRelationalMapping.setApiCode(carClueInitMapping.getApiCode());
                    carClueRelationalMapping.setBrandName(carClueInitMapping.getBrandName());

                    //校验初始外采信息是否能匹配
                    StringBuilder stringBuilder = new StringBuilder();
                    verifyCarClueInit(stringBuilder,carClueRelationalMapping,
                            carClueInitMapping,provinceNameMap,cityNameMap,brandNameMap);

                    //匹配省市类型
                    matchProvincesType(carClueInitMapping,carClueRelationalMapping);

                    //处理车系信息
                    String seriesName = carClueInitMapping.getSeriesName();
                    if(seriesName == null){
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                "未找到车系名称! apiCode：" + carClueInitMapping.getApiCode() + "，品牌：" + carClueInitMapping.getBrandName()));
                    }
                    //是否为全系
                    if(ALL_SERVIES.equals(seriesName)){
                        carClueRelationalMapping.setSeriesName(seriesName);
                        carClueRelationalMapping.setMatchingCause(stringBuilder.toString());
                        carClueRelationalMappings.add(carClueRelationalMapping);
                        continue;
                    }

                    String[] split = seriesName.split(",");
                    for (String s : split){
                        StringBuilder builder = new StringBuilder();
                        CarClueRelationalMapping mapping = new CarClueRelationalMapping();
                        BeanUtils.copyProperties(carClueRelationalMapping, mapping);
                        List<CarClueSeriesInformation> seriesNameList = seriesNameMap.get(s);
                        if(CollectionUtils.isEmpty(seriesNameList)){
                            builder.append("未匹配到该车系：").append(s).append(" | ");
                            mapping.setMatchingType(1);
                        }else {
                            mapping.setSeriesId(seriesNameList.get(0).getSeriesId());
                        }
                        mapping.setSeriesName(s);
                        mapping.setMatchingCause(stringBuilder.toString() + builder);
                        carClueRelationalMappings.add(mapping);
                        //报警
                        if(!StringUtils.isEmpty(stringBuilder)){
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                    stringBuilder.toString()));
                        }
                    }
                    if (carClueRelationalMappings.size() >= 500) {
                        carClueRelationalMappingMapper.batchInsert(carClueRelationalMappings);
                        carClueRelationalMappings.clear();
                    }
                }
                // 插入剩余的数据
                if (!carClueRelationalMappings.isEmpty()) {
                    carClueRelationalMappingMapper.batchInsert(carClueRelationalMappings);
                }

            });
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "维护外采渠道商信息异常"), e);
        }
    }

    /**
     * 匹配省市类型
     * @param carClueInitMapping
     * @param carClueRelationalMapping
     */
    private void matchProvincesType(CarClueInitMapping carClueInitMapping, CarClueRelationalMapping carClueRelationalMapping) {
        if (carClueInitMapping.getNation() != null &&
                carClueInitMapping.getExcludeProvinceName() == null &&
                carClueInitMapping.getExcludeCityName() == null) {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.NATIONWIDE.getValue());
        } else if (carClueInitMapping.getSatisfyProvinceName() != null ||
                carClueInitMapping.getSatisfyCityName() != null) {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.FIXED.getValue());
        } else {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.EXCLUDE.getValue());
        }
    }

    /**
     * 校验省市车辆信息是否匹配
     * @param stringBuilder
     * @param carClueRelationalMapping
     * @param carClueInitMapping
     * @param provinceNameMap
     * @param cityNameMap
     * @param brandNameMap
     */
    private void verifyCarClueInit(StringBuilder stringBuilder, CarClueRelationalMapping carClueRelationalMapping,
                                   CarClueInitMapping carClueInitMapping, Map<String, List<CarClueProvincesInformation>> provinceNameMap,
                                   Map<String, List<CarClueProvincesInformation>> cityNameMap, Map<String, List<CarClueSeriesInformation>> brandNameMap) {

        String satisfyProvinceName = carClueInitMapping.getSatisfyProvinceName();
        if (satisfyProvinceName != null) {
            String[] split = satisfyProvinceName.split(",");
            StringBuilder filteredProvinceNames = new StringBuilder();
            boolean first = true;
            for (String s : split) {
                String searchKey = s.trim().replaceAll("市$", "");

                for (String key : provinceNameMap.keySet()) {
                    if (key.contains(searchKey)) {
                        searchKey = key;
                        break;
                    }
                }
                List<CarClueProvincesInformation> provinceNameList = provinceNameMap.get(searchKey);
                if (!CollectionUtils.isEmpty(provinceNameList)) {
                    if (!first) {
                        filteredProvinceNames.append(",");
                    } else {
                        first = false;
                    }
                    filteredProvinceNames.append(searchKey);
                } else {
                    stringBuilder.append("未匹配到该省：").append(searchKey).append(" | ");
                }
            }
            carClueRelationalMapping.setSatisfyProvinceName(filteredProvinceNames.toString());
        }

        String excludeProvinceName = carClueInitMapping.getExcludeProvinceName();
        if (excludeProvinceName != null) {
            String[] split = excludeProvinceName.split(",");
            StringBuilder filteredProvinceNames = new StringBuilder();
            boolean first = true;
            for (String s : split) {
                String searchKey = s.trim().replaceAll("市$", "");

                for (String key : provinceNameMap.keySet()) {
                    if (key.contains(searchKey)) {
                        searchKey = key;
                        break;
                    }
                }
                List<CarClueProvincesInformation> provinceNameList = provinceNameMap.get(searchKey);
                if (!CollectionUtils.isEmpty(provinceNameList)) {
                    if (!first) {
                        filteredProvinceNames.append(",");
                    } else {
                        first = false;
                    }
                    filteredProvinceNames.append(searchKey);
                } else {
                    stringBuilder.append("未匹配到该省(排除)：").append(searchKey).append(" | ");
                }
            }
            carClueRelationalMapping.setExcludeProvinceName(filteredProvinceNames.toString());
        }

        //处理城市信息
        String satisfyCityName = carClueInitMapping.getSatisfyCityName();
        if (satisfyCityName != null) {
            String[] split = satisfyCityName.split(",");
            StringBuilder filteredProvinceNames = new StringBuilder();
            boolean first = true;
            for (String s : split) {
                String searchKey = s.trim().replaceAll("市$", "");

                for (String key : cityNameMap.keySet()) {
                    if (key.contains(searchKey)) {
                        searchKey = key;
                        break;
                    }
                }
                List<CarClueProvincesInformation> cityNameList = cityNameMap.get(searchKey);
                if (!CollectionUtils.isEmpty(cityNameList)) {
                    if (!first) {
                        filteredProvinceNames.append(",");
                    } else {
                        first = false;
                    }
                    filteredProvinceNames.append(searchKey);
                } else {
                    stringBuilder.append("未匹配到该城市：").append(searchKey).append(" | ");
                }
            }
            carClueRelationalMapping.setSatisfyCityName(filteredProvinceNames.toString());
        }

        String excludeCityName = carClueInitMapping.getExcludeCityName();
        if (excludeCityName != null) {
            String[] split = excludeCityName.split(",");
            StringBuilder filteredProvinceNames = new StringBuilder();
            boolean first = true;
            for (String s : split) {
                String searchKey = s.trim().replaceAll("市$", "");

                for (String key : cityNameMap.keySet()) {
                    if (key.contains(searchKey)) {
                        searchKey = key;
                        break;
                    }
                }
                List<CarClueProvincesInformation> cityNameList = cityNameMap.get(searchKey);
                if (!CollectionUtils.isEmpty(cityNameList)) {
                    if (!first) {
                        filteredProvinceNames.append(",");
                    } else {
                        first = false;
                    }
                    filteredProvinceNames.append(searchKey);
                } else {
                    stringBuilder.append("未匹配到该城市(排除)：").append(searchKey).append(" | ");
                }
            }
            carClueRelationalMapping.setExcludeCityName(filteredProvinceNames.toString());
        }

        List<CarClueSeriesInformation> brandNameList = brandNameMap.get(carClueInitMapping.getBrandName());
        if(CollectionUtils.isEmpty(brandNameList)){
            stringBuilder.append("未匹配到该品牌：").append(carClueInitMapping.getBrandName()).append(" | ");
            carClueRelationalMapping.setMatchingType(1);
        }else {
            carClueRelationalMapping.setBrandId(brandNameList.get(0).getBrandId());
        }

    }

}
