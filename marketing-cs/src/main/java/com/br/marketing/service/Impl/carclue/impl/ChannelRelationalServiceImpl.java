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
import com.br.marketing.service.Impl.carclue.ChannelRelationalService;
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
    CarClueInitMappingMapper carClueInitMappingMapper;

    @Override
    public void getProvinceAndCity() {
        //省市信息
        buildZjCity();
        buildYcCity();
        //车辆信息
        buildZjCar();
        buildYcCar();
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
                    carClueProvincesInformation.setProvincesType("1");
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

    private void buildYcCity() {
        Result<JSONArray> ycCityResult = carClueClient.getYcCity();
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
                carClueProvincesInformation.setProvincesType("0");
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
                carClueSeriesInformation.setSeriesType("1");
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

    private void buildYcCar() {
        Result<JSONArray> ycCarResult = carClueClient.getYcCar();

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
                carClueSeriesInformation.setSeriesType("0");
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
        //获取省市集合
        CarClueProvincesInformationExample carClueProvincesInformationExample = new CarClueProvincesInformationExample();
        carClueProvincesInformationExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueProvincesInformation> carClueProvincesInformations = carClueProvincesInformationMapper.selectByExample(carClueProvincesInformationExample);
        Map<String, List<CarClueProvincesInformation>> groupedByProvincesType = carClueProvincesInformations.stream()
                .collect(Collectors.groupingBy(CarClueProvincesInformation::getProvincesType));
        //获取品牌车系集合
        CarClueSeriesInformationExample carClueSeriesInformationExample = new CarClueSeriesInformationExample();
        carClueSeriesInformationExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueSeriesInformation> carClueSeriesInformations = carClueSeriesInformationMapper.selectByExample(carClueSeriesInformationExample);
        Map<String, List<CarClueSeriesInformation>> groupedBySeriesType = carClueSeriesInformations.stream()
                .collect(Collectors.groupingBy(CarClueSeriesInformation::getSeriesType));



        //获取外采初始信息
        CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
        carClueInitMappingExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueInitMapping> carClueInitMappingList = carClueInitMappingMapper.selectByExample(carClueInitMappingExample);
        //匹配初始信息
        for (CarClueInitMapping carClueInitMapping : carClueInitMappingList) {
            carClueInitMapping.getApiCode();
        }
        //校验信息准确性
        //最终映射关系存储外采关系表
    }

}
