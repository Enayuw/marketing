package com.br.marketing.service.Impl.carclue.impl;

import java.time.LocalDate;
import java.util.Date;
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
import com.br.marketing.service.Impl.CarClueReportServiceImpl;
import com.br.marketing.service.Impl.carclue.ChannelRelationalService;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.clueenums.ProvincesTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public void getProvinceAndCity() {
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
        JSONArray jsonArray = new JSONArray();
        if (ResultCode.SUCCESS.getValue().equals(zjCityResult.getCode())) {
            jsonArray = zjCityResult.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                    , "车线索-之家，省市调用异常,result= " + zjCityResult.getMessage()));
        }
        Integer provinceId;
        String provinceName;
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject firstData = jsonArray.getJSONObject(i);
                provinceId = firstData.getInteger("id");
                provinceName = firstData.getString("name");
                JSONArray nodesArray = firstData.getJSONArray("nodes");
                for (int j = 0; j < nodesArray.size(); j++) {
                    JSONObject node = nodesArray.getJSONObject(j);
                    Integer cityId = node.getInteger("id");
                    String cityName = node.getString("name");
                    CarClueProvincesInformation carClueProvincesInformation = new CarClueProvincesInformation();
                    carClueProvincesInformation.setApiCode(carClueReportServiceImpl.getValueByKey
                            (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel()));
                    carClueProvincesInformation.setProvinceId(provinceId);
                    carClueProvincesInformation.setProvinceName(provinceName);
                    carClueProvincesInformation.setCityId(cityId);
                    carClueProvincesInformation.setCityName(cityName);
                    carClueProvincesInformation.setAppletDate(LocalDate.now().toString());
                    carClueProvincesInformation.setCreateTime(new Date());
                    carClueProvincesInformation.setUpdateTime(new Date());
                    carClueProvincesInformationMapper.insertSelective(carClueProvincesInformation);
                }
            }
        }
    }

    private void buildYcCity(String task,String provincesType) {
        Result<JSONArray> ycCityResult = carClueClient.getYcCity(task);
        JSONArray jsonArray = new JSONArray();
        if (ResultCode.SUCCESS.getValue().equals(ycCityResult.getCode())) {
            jsonArray = ycCityResult.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                    , "车线索-易车，省市调用异常,result= " + ycCityResult.getMessage()));
        }
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject firstData = jsonArray.getJSONObject(i);
                CarClueProvincesInformation carClueProvincesInformation = new CarClueProvincesInformation();
                carClueProvincesInformation.setApiCode(carClueReportServiceImpl.getValueByKey(provincesType));
                carClueProvincesInformation.setProvinceId(firstData.getInteger("provinceId"));
                carClueProvincesInformation.setProvinceName(firstData.getString("provinceName"));
                carClueProvincesInformation.setCityId(firstData.getInteger("cityId"));
                carClueProvincesInformation.setCityName(firstData.getString("cityName"));
                carClueProvincesInformation.setAppletDate(LocalDate.now().toString());
                carClueProvincesInformation.setCreateTime(new Date());
                carClueProvincesInformation.setUpdateTime(new Date());
                carClueProvincesInformationMapper.insertSelective(carClueProvincesInformation);

            }
        }
    }

    private void buildZjCar() {
        Result<JSONArray> zjCarResult = carClueClient.getZjCar();

        JSONArray jsonArray = new JSONArray();
        if (ResultCode.SUCCESS.getValue().equals(zjCarResult.getCode())) {
            jsonArray = zjCarResult.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                    , "车线索-之家，车辆信息获取异常,result= " + zjCarResult.getMessage()));
        }
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject firstData = jsonArray.getJSONObject(i);
                CarClueSeriesInformation carClueSeriesInformation = new CarClueSeriesInformation();
                carClueSeriesInformation.setApiCode(carClueReportServiceImpl.getValueByKey
                        (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel()));
                carClueSeriesInformation.setBrandId(firstData.getInteger("brand_id"));
                carClueSeriesInformation.setBrandName(firstData.getString("brand_name"));
                carClueSeriesInformation.setSubBrandId(firstData.getInteger("son_brand_id"));
                carClueSeriesInformation.setSubBrandName(firstData.getString("son_brand_name"));
                carClueSeriesInformation.setSeriesId(firstData.getInteger("series_id"));
                carClueSeriesInformation.setSeriesName(firstData.getString("series_name"));
                carClueSeriesInformation.setAppletDate(LocalDate.now().toString());
                carClueSeriesInformation.setCreateTime(new Date());
                carClueSeriesInformation.setUpdateTime(new Date());
                carClueSeriesInformationMapper.insertSelective(carClueSeriesInformation);
            }
        }
    }

    private void buildYcCar(String task,String provincesType) {
        Result<JSONArray> ycCarResult = carClueClient.getYcCar(task);

        JSONArray jsonArray = new JSONArray();
        if (ResultCode.SUCCESS.getValue().equals(ycCarResult.getCode())) {
            jsonArray = ycCarResult.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                    , "车线索-易车，车辆信息获取异常,result= " + ycCarResult.getMessage()));
        }
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject firstData = jsonArray.getJSONObject(i);
                CarClueSeriesInformation carClueSeriesInformation = new CarClueSeriesInformation();
                carClueSeriesInformation.setApiCode(carClueReportServiceImpl.getValueByKey(provincesType));
                carClueSeriesInformation.setBrandId(firstData.getInteger("brandId"));
                carClueSeriesInformation.setBrandName(firstData.getString("brandName"));
                carClueSeriesInformation.setSeriesId(firstData.getInteger("seriesId"));
                carClueSeriesInformation.setSeriesName(firstData.getString("seriesName"));
                carClueSeriesInformation.setAppletDate(LocalDate.now().toString());
                carClueSeriesInformation.setCreateTime(new Date());
                carClueSeriesInformation.setUpdateTime(new Date());
                carClueSeriesInformationMapper.insertSelective(carClueSeriesInformation);
            }
        }
    }

    @Override
    public void relationalMapping() {
        //获取品牌车系集合
        CarClueSeriesInformationExample carClueSeriesInformationExample = new CarClueSeriesInformationExample();
        carClueSeriesInformationExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
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
            List<CarClueSeriesInformation> carClueSeriesInformations = groupedBySeriesType.get(apiCode);

            //匹配初始信息
            for (CarClueInitMapping carClueInitMapping : v) {
                Integer brandId = null;
                for (CarClueSeriesInformation carClueSeriesInformation : carClueSeriesInformations) {
                    //匹配品牌id
                    if(carClueSeriesInformation.getBrandName().equals(carClueInitMapping.getBrandName())){
                        brandId = carClueSeriesInformation.getBrandId();
                    }

                    //匹配车系id
                    String seriesName = carClueInitMapping.getSeriesName();
                    String[] split = seriesName.split(",");
                    for (String s : split){
                        if(carClueSeriesInformation.getSeriesName().equals(s)){
                            CarClueRelationalMapping carClueRelationalMapping = new CarClueRelationalMapping();
                            carClueRelationalMapping.setApiCode(carClueSeriesInformation.getApiCode());
                            carClueRelationalMapping.setBrandId(brandId);
                            carClueRelationalMapping.setBrandName(carClueInitMapping.getBrandName());
                            carClueRelationalMapping.setSeriesId(carClueSeriesInformation.getSeriesId());
                            carClueRelationalMapping.setSeriesName(carClueInitMapping.getSeriesName());
                            carClueRelationalMapping.setSatisfyProvinceName(carClueInitMapping.getSatisfyProvinceName());
                            carClueRelationalMapping.setSatisfyCityName(carClueInitMapping.getSatisfyCityName());
                            carClueRelationalMapping.setExcludeProvinceName(carClueInitMapping.getExcludeProvinceName());
                            carClueRelationalMapping.setExcludeCityName(carClueInitMapping.getExcludeCityName());
                            //todo
                            carClueRelationalMapping.setProvinceType(0);
                            carClueRelationalMapping.setAppletDate(LocalDate.now().toString());
                            carClueRelationalMapping.setCreateTime(new Date());
                            carClueRelationalMapping.setUpdateTime(new Date());
                            carClueRelationalMappingMapper.insertSelective(carClueRelationalMapping);
                        }
                    }
                }
            }
        });
    }

}
