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
                CarClueProvincesInformation info = new CarClueProvincesInformation();
                info.setApiCode(carClueReportServiceImpl.getValueByKey
                        (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel()).get(0));
                info.setProvinceId(provinceId);
                info.setProvinceName(provinceName);
                info.setCityId(cityId);
                info.setCityName(cityName);
                list.add(info);
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
        List<CarClueProvincesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            CarClueProvincesInformation info = new CarClueProvincesInformation();
            info.setApiCode(carClueReportServiceImpl.getValueByKey(provincesType).get(0));
            info.setProvinceId(firstData.getInteger("provinceId"));
            info.setProvinceName(firstData.getString("provinceName"));
            info.setCityId(firstData.getInteger("cityId"));
            info.setCityName(firstData.getString("cityName"));
            list.add(info);
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

        List<CarClueSeriesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            CarClueSeriesInformation info = new CarClueSeriesInformation();
            info.setApiCode(carClueReportServiceImpl.getValueByKey
                    (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel()).get(0));
            info.setBrandId(firstData.getInteger("brand_id"));
            info.setBrandName(firstData.getString("brand_name"));
            info.setSubBrandId(firstData.getInteger("son_brand_id"));
            info.setSubBrandName(firstData.getString("son_brand_name"));
            info.setSeriesId(firstData.getInteger("series_id"));
            info.setSeriesName(firstData.getString("series_name"));
            list.add(info);
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

        List<CarClueSeriesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            CarClueSeriesInformation info = new CarClueSeriesInformation();
            info.setApiCode(carClueReportServiceImpl.getValueByKey(provincesType).get(0));
            info.setBrandId(firstData.getInteger("brandId"));
            info.setBrandName(firstData.getString("brandName"));
            info.setSeriesId(firstData.getInteger("seriesId"));
            info.setSeriesName(firstData.getString("seriesName"));
            list.add(info);
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
            //获取品牌车系集合
            CarClueSeriesInformationExample carClueSeriesInformationExample = new CarClueSeriesInformationExample();
            carClueSeriesInformationExample.createCriteria()
                    .andAppletDateEqualTo(LocalDate.now().toString())
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueSeriesInformation> list = carClueSeriesInformationMapper.selectByExample(carClueSeriesInformationExample);
            Map<String, List<CarClueSeriesInformation>> groupedBySeriesType = list.stream()
                    .collect(Collectors.groupingBy(CarClueSeriesInformation::getApiCode));

            //获取外采初始信息
            CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
            carClueInitMappingExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueInitMapping> carClueInitMappingList = carClueInitMappingMapper.selectByExample(carClueInitMappingExample);

            Map<String, List<CarClueInitMapping>> carClueInitMappingMap = carClueInitMappingList.stream()
                    .collect(Collectors.groupingBy(CarClueInitMapping::getApiCode));

            carClueInitMappingMap.forEach((apiCode, v) -> {

                List<CarClueRelationalMapping> list1 = new ArrayList<>();
                List<CarClueSeriesInformation> carClueSeriesInformations = groupedBySeriesType.get(apiCode);

                Map<String, List<CarClueSeriesInformation>> brandNameMap = carClueSeriesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueSeriesInformation::getBrandName));

                Map<String, List<CarClueSeriesInformation>> seriesNameMap = carClueSeriesInformations.stream()
                        .collect(Collectors.groupingBy(CarClueSeriesInformation::getSeriesName));

                //匹配初始信息
                for (CarClueInitMapping carClueInitMapping : v) {

                    CarClueRelationalMapping carClueRelationalMapping = new CarClueRelationalMapping();
                    carClueRelationalMapping.setApiCode(carClueInitMapping.getApiCode());
                    carClueRelationalMapping.setMatchingType(0);
                    carClueRelationalMapping.setBrandName(carClueInitMapping.getBrandName());
                    carClueRelationalMapping.setSatisfyProvinceName(carClueInitMapping.getSatisfyProvinceName());
                    carClueRelationalMapping.setSatisfyCityName(carClueInitMapping.getSatisfyCityName());
                    carClueRelationalMapping.setExcludeProvinceName(carClueInitMapping.getExcludeProvinceName());
                    carClueRelationalMapping.setExcludeCityName(carClueInitMapping.getExcludeCityName());
                    //判断省市类型
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

                    StringBuilder stringBuilder = new StringBuilder();
                    List<CarClueSeriesInformation> brandNameList = brandNameMap.get(carClueInitMapping.getBrandName());
                    if(CollectionUtils.isEmpty(brandNameList)){
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                "未匹配到该品牌! apiCode：" + carClueInitMapping.getApiCode() + "，品牌：" + carClueInitMapping.getBrandName()));

                        stringBuilder.append("未匹配到该品牌：").append(carClueInitMapping.getBrandName()).append(" | ");
                        carClueRelationalMapping.setMatchingType(1);
                    }else {
                        carClueRelationalMapping.setBrandId(brandNameList.get(0).getBrandId());
                    }

                    //是否为全系
                    String seriesName = carClueInitMapping.getSeriesName();
                    if(seriesName == null || "全系".equals(seriesName)){
                        carClueRelationalMapping.setSeriesName(seriesName);
                        list1.add(carClueRelationalMapping);
                        continue;
                    }
                    String[] split = seriesName.split(",");
                    for (String s : split){
                        List<CarClueSeriesInformation> seriesNameList = seriesNameMap.get(s);
                        if(CollectionUtils.isEmpty(seriesNameList)){
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                    "未匹配到该车系! apiCode：" + carClueInitMapping.getApiCode() + "，车系：" + s));

                            stringBuilder.append("未匹配到该车系：").append(s).append(" | ");
                            carClueRelationalMapping.setMatchingType(1);
                        }else {
                            carClueRelationalMapping.setSeriesId(seriesNameList.get(0).getSeriesId());
                        }
                        carClueRelationalMapping.setSeriesName(s);
                        carClueRelationalMapping.setMatchingCause(stringBuilder.toString());
                        list1.add(carClueRelationalMapping);
                        if (list1.size() >= 500) {
                            carClueRelationalMappingMapper.batchInsert(list1);
                            list1.clear();
                        }
                    }
                    // 插入剩余的数据
                    if (!list1.isEmpty()) {
                        carClueRelationalMappingMapper.batchInsert(list1);
                        list1.clear();
                    }
                }
            });
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "维护外采渠道商信息异常"), e);
        }
    }


}
