package com.br.marketing.service.Impl.zhijia;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.dto.zhijia.ZhiJiaCarInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ZhiJiaCarBrandInfoMapper;
import com.br.marketing.mapper.ZhiJiaCarSeriesInfoMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.br.marketing.mapper.ZhijiaCityConfigMapper;
import com.br.marketing.mapper.ZhijiaCountyConfigBMapper;


/**
 * @ClassName ZhiJiaDataProcessServiceImpl
 * @Description 之家数据处理service
 * @Author zhen.li1
 * @Date 2024/7/10 16:40
 */
@Service
@Slf4j
public class ZhiJiaDataProcessServiceImpl implements ZhiJiaDataProcessService {


    @Resource
    private ZhiJiaClient zhiJiaClient;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    ZhiJiaCarBrandInfoMapper zhiJiaCarBrandInfoMapper;

    @Resource
    ZhiJiaCarSeriesInfoMapper zhiJiaCarSeriesInfoMapper;

    @Resource
    ZhijiaCityConfigMapper zhijiaCityConfigMapper;

    @Resource
    ZhijiaCountyConfigBMapper zhijiaCountyConfigBMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public void getCityAndCounty() {
        String token = getToken();
        if (StringUtils.isEmpty(token)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家获取token异常!"));
            return;
        }
        String zhiJiaApiCode = marketingCommonConfig.getZhiJiaApiCode();
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> result = zhiJiaClient.getCityAndCounty(token);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            jsonObject = result.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode()
                    , "之家省市区调用异常,result= " + result.getMessage()));
        }
        JSONObject resultJson = jsonObject.getJSONObject("result");
        JSONArray cityList = resultJson.getJSONArray("city");
        cityList.forEach((Object cityJson) -> {
            JSONObject city = (JSONObject) cityJson;
            Integer cid = city.getInteger("cid");
            String cname = city.getString("cname");
            ZhijiaCityConfigExample zhijiaCityConfigExample = new ZhijiaCityConfigExample();
            zhijiaCityConfigExample.createCriteria()
                    .andCIdEqualTo(cid);
            List<ZhijiaCityConfig> zhijiaCityConfig = zhijiaCityConfigMapper.selectByExample(zhijiaCityConfigExample);
            if (CollectionUtils.isEmpty(zhijiaCityConfig)) {
                ZhijiaCityConfig cityConfig = new ZhijiaCityConfig();
                cityConfig.setApiCode(zhiJiaApiCode);
                cityConfig.setCId(cid);
                cityConfig.setCName(cname);
                cityConfig.setCreateTime(new Date());
                cityConfig.setUpdateTime(new Date());
                cityConfig.setUploadDate(LocalDate.now().toString());
                zhijiaCityConfigMapper.insert(cityConfig);
            } else {
                ZhijiaCityConfig cityConfig = new ZhijiaCityConfig();
                cityConfig.setApiCode(zhiJiaApiCode);
                cityConfig.setCName(cname);
                cityConfig.setUpdateTime(new Date());
                cityConfig.setUploadDate(LocalDate.now().toString());
                cityConfig.setId(zhijiaCityConfig.get(0).getId());
                zhijiaCityConfigMapper.updateByPrimaryKeySelective(cityConfig);
            }
        });
        JSONArray countyList = resultJson.getJSONArray("county");
        countyList.forEach((Object countyJson) -> {
            JSONObject county = (JSONObject) countyJson;
            Integer cid = county.getInteger("cid");
            Integer countyid = county.getInteger("countyid");
            String countyname = county.getString("countyname");
            ZhijiaCountyConfigExample zhijiaCountyConfigExample = new ZhijiaCountyConfigExample();
            zhijiaCountyConfigExample.createCriteria()
                    .andCountyIdEqualTo(countyid);
            List<ZhijiaCountyConfig> countyConfigList = zhijiaCountyConfigBMapper.selectByExample(zhijiaCountyConfigExample);
            if (CollectionUtils.isEmpty(countyConfigList)) {
                ZhijiaCountyConfig countyConfig = new ZhijiaCountyConfig();
                countyConfig.setApiCode(zhiJiaApiCode);
                countyConfig.setCId(cid);
                countyConfig.setCountyId(countyid);
                countyConfig.setCountyName(countyname);
                countyConfig.setCreateTime(new Date());
                countyConfig.setUpdateTime(new Date());
                countyConfig.setUploadDate(LocalDate.now().toString());
                zhijiaCountyConfigBMapper.insert(countyConfig);
            } else {
                ZhijiaCountyConfig countyConfig = new ZhijiaCountyConfig();
                countyConfig.setApiCode(zhiJiaApiCode);
                countyConfig.setCountyName(countyname);
                countyConfig.setCId(cid);
                countyConfig.setUpdateTime(new Date());
                countyConfig.setUploadDate(LocalDate.now().toString());
                countyConfig.setId(countyConfigList.get(0).getId());
                zhijiaCountyConfigBMapper.updateByPrimaryKeySelective(countyConfig);
            }
        });
    }


    @Override
    public void getBrandAndseries() {
        String token = getToken();
        if (StringUtils.isEmpty(token)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家获取token异常!"));
            return;
        }
        String zhiJiaApiCode = marketingCommonConfig.getZhiJiaApiCode();
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> resultBrand = zhiJiaClient.getBrand(token);
        if (ResultCode.SUCCESS.getValue().equals(resultBrand.getCode())) {
            jsonObject = resultBrand.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode()
                    , "之家车辆品牌接口调用异常,result= " + resultBrand.getMessage()));
        }
        JSONObject resultJson = jsonObject.getJSONObject("result");
        JSONArray brandlist = resultJson.getJSONArray("brandlist");
        brandlist.forEach((Object group) -> {
            JSONObject brandGroup = (JSONObject) group;
            JSONArray brandArray = brandGroup.getJSONArray("list");

            brandArray.forEach((Object obj) -> {
                JSONObject brand = (JSONObject) obj;
                Integer brandId = brand.getInteger("id");
                String brandName = brand.getString("name");
                ZhiJiaCarBrandInfoExample zhiJiaCarBrandInfoExample = new ZhiJiaCarBrandInfoExample();
                zhiJiaCarBrandInfoExample.createCriteria()
                        .andBrandIdEqualTo(brandId);
                List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos = zhiJiaCarBrandInfoMapper.selectByExample(zhiJiaCarBrandInfoExample);
                if (zhiJiaCarBrandInfos.isEmpty()) {
                    String newBrandName = removeSpacesAndConvertToUpper(brandName);
                    String appletDate = LocalDate.now().toString();

                    ZhiJiaCarBrandInfo brandInfo = new ZhiJiaCarBrandInfo();
                    brandInfo.setApiCode(zhiJiaApiCode);
                    brandInfo.setBrandId(brandId);
                    brandInfo.setBrandName(brandName);
                    brandInfo.setNewBrandName(newBrandName);
                    brandInfo.setAppletDate(appletDate);
                    brandInfo.setCreateTime(new Date());
                    brandInfo.setUpdateTime(new Date());
                    zhiJiaCarBrandInfoMapper.insertSelective(brandInfo);
                } else {
                    ZhiJiaCarBrandInfo zhiJiaCarBrandInfo = new ZhiJiaCarBrandInfo();
                    zhiJiaCarBrandInfo.setApiCode(zhiJiaApiCode);
                    zhiJiaCarBrandInfo.setBrandName(brandName);
                    zhiJiaCarBrandInfo.setUpdateTime(new Date());
                    zhiJiaCarBrandInfo.setAppletDate(LocalDate.now().toString());
                    zhiJiaCarBrandInfo.setId(zhiJiaCarBrandInfos.get(0).getId());
                    zhiJiaCarBrandInfo.setNewBrandName(removeSpacesAndConvertToUpper(zhiJiaCarBrandInfos.get(0).getBrandName()));
                    zhiJiaCarBrandInfoMapper.updateByPrimaryKeySelective(zhiJiaCarBrandInfo);
                }
            });
        });
        List<ZhiJiaCarBrandInfo> carBrandInfos = getCarBrandInfos();
        Set<Integer> set = carBrandInfos.stream().map(ZhiJiaCarBrandInfo::getBrandId).collect(Collectors.toSet());
            for (Integer brandId : set) {
                getSeries(brandId);
            }
    }

    public void getSeries(Integer brandId) {
        String token = getToken();
        if (StringUtils.isEmpty(token)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家获取token异常!"));
            return;
        }
        String zhiJiaApiCode = marketingCommonConfig.getZhiJiaApiCode();
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> resultBrand = zhiJiaClient.getSeries(token, brandId.toString());
        if (ResultCode.SUCCESS.getValue().equals(resultBrand.getCode())) {
            jsonObject = resultBrand.getData();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode()
                    , "之家车辆车系接口调用异常,result= " + resultBrand.getMessage()));
        }
        JSONObject resultJson = jsonObject.getJSONObject("result");
        JSONArray serieslist = resultJson.getJSONArray("serieslist");
        try {
            serieslist.forEach((Object seriesJson) -> {
                JSONObject seriesInfo = (JSONObject) seriesJson;
                Integer seriesId = seriesInfo.getInteger("id");
                String seriesName = seriesInfo.getString("name");
                String newSeriesName = removeSpacesAndConvertToUpper(seriesName);
                ZhiJiaCarSeriesInfoExample zhiJiaCarSeriesInfoExample = new ZhiJiaCarSeriesInfoExample();
                zhiJiaCarSeriesInfoExample.createCriteria()
                        .andBrandIdEqualTo(brandId).andSeriesIdEqualTo(seriesId);
                List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos = zhiJiaCarSeriesInfoMapper.selectByExample(zhiJiaCarSeriesInfoExample);
                if (CollectionUtils.isEmpty(zhiJiaCarSeriesInfos)) {
                    ZhiJiaCarSeriesInfo zhiJiaCarSeriesInfo = new ZhiJiaCarSeriesInfo();
                    zhiJiaCarSeriesInfo.setApiCode(zhiJiaApiCode);
                    zhiJiaCarSeriesInfo.setBrandId(brandId);
                    zhiJiaCarSeriesInfo.setSeriesId(seriesId);
                    zhiJiaCarSeriesInfo.setSeriesName(seriesName);
                    zhiJiaCarSeriesInfo.setNewSeriesName(newSeriesName);
                    zhiJiaCarSeriesInfo.setCreateTime(new Date());
                    zhiJiaCarSeriesInfo.setUpdateTime(new Date());
                    zhiJiaCarSeriesInfo.setAppletDate(LocalDate.now().toString());
                    zhiJiaCarSeriesInfoMapper.insertSelective(zhiJiaCarSeriesInfo);
                } else {
                    ZhiJiaCarSeriesInfo zhiJiaCarSeriesInfo = new ZhiJiaCarSeriesInfo();
                    zhiJiaCarSeriesInfo.setApiCode(zhiJiaApiCode);
                    zhiJiaCarSeriesInfo.setSeriesName(seriesName);
                    zhiJiaCarSeriesInfo.setNewSeriesName(newSeriesName);
                    zhiJiaCarSeriesInfo.setUpdateTime(new Date());
                    zhiJiaCarSeriesInfo.setAppletDate(LocalDate.now().toString());
                    zhiJiaCarSeriesInfo.setId(zhiJiaCarSeriesInfos.get(0).getId());
                    zhiJiaCarSeriesInfoMapper.updateByPrimaryKeySelective(zhiJiaCarSeriesInfo);
                }
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家车辆车系信息入库线程错误！"), e);
        }
    }


    /**
     * 匹配区县数据逻辑
     *
     * @param cityList
     * @param countyList
     * @param zhiJiaClueBackInfo
     * @return CityCountyDataDTO
     * @author zhen.Li1
     * @date 2024/7/10 19:46
     */
    @Override
    public CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig> cityList, List<ZhijiaCountyConfig> countyList, ZhiJiaClueBackData
            zhiJiaClueBackInfo) {
        String city = zhiJiaClueBackInfo.getCity().replaceAll("\\s*", "");
        String county = zhiJiaClueBackInfo.getContry().replaceAll("\\s*", "");
        CityCountyDataDTO cityCountyDataDTO = new CityCountyDataDTO();
        //精确匹配城市
        List<ZhijiaCityConfig> defineCityList =
                cityList.stream().filter((ZhijiaCityConfig zhijiaCityConfig) -> zhijiaCityConfig.getCName().equals(city))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(defineCityList)) {
            //匹配区县
            cityCountyDataDTO.setCId(defineCityList.get(0).getCId());
            return matchCounty(cityCountyDataDTO, countyList, county);
        }
        //模糊匹配城市
        List<ZhijiaCityConfig> likeCityList =
                cityList.stream().filter((ZhijiaCityConfig zhijiaCityConfig) -> city.contains(zhijiaCityConfig.getCName()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(likeCityList)) {
            //匹配区县
            if (likeCityList.size() == 1) {
                cityCountyDataDTO.setCId(likeCityList.get(0).getCId());
                return matchCounty(cityCountyDataDTO, countyList, county);
            } else {
                cityCountyDataDTO.setIsMatch(Boolean.FALSE);
                cityCountyDataDTO.setErrorMsg("之家城市匹配出多条:city=".concat(city));
                return cityCountyDataDTO;
            }
        }
        //扩展配置匹配
        List<ZhijiaCityConfig> configCnameList = cityList.stream().filter((ZhijiaCityConfig zhijiaCityConfig) -> {
            String cNameExtend = zhijiaCityConfig.getCNameConfig();
            if (StringUtils.isNotEmpty(cNameExtend)) {
                List<String> cNameExtends = Arrays.asList(cNameExtend.split(","));
                return cNameExtends.contains(city);
            }
            return Boolean.FALSE;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(configCnameList)) {
            //匹配区县
            cityCountyDataDTO.setCId(configCnameList.get(0).getCId());
            return matchCounty(cityCountyDataDTO, countyList, county);
        }
        cityCountyDataDTO.setIsMatch(Boolean.FALSE);
        cityCountyDataDTO.setErrorMsg("之家城市未匹配成功:city=".concat(city));
        return cityCountyDataDTO;
    }

    private CityCountyDataDTO matchCounty(CityCountyDataDTO cityCountyDataDTO, List<ZhijiaCountyConfig> countyList, String county) {
        //获取城市下面的区县
        Integer cId = cityCountyDataDTO.getCId();
        List<ZhijiaCountyConfig> countyConfigList =
                countyList.stream().filter((ZhijiaCountyConfig countyConfig) -> countyConfig.getCId().equals(cId))
                .collect(Collectors.toList());

        //精确匹配区县
        List<ZhijiaCountyConfig> defineCountyList =
                countyConfigList.stream().filter((ZhijiaCountyConfig countyConfig) -> countyConfig.getCountyName().equals(county))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(defineCountyList)) {
            cityCountyDataDTO.setIsMatch(Boolean.TRUE);
            cityCountyDataDTO.setCountyId(defineCountyList.get(0).getCountyId());
            return cityCountyDataDTO;
        }
        //模糊匹配区县
        List<ZhijiaCountyConfig> likeCountyList =
                countyConfigList.stream().filter((ZhijiaCountyConfig countyConfig) -> county.contains(countyConfig.getCountyName()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(likeCountyList)) {
            if (likeCountyList.size() == 1) {
                cityCountyDataDTO.setIsMatch(Boolean.TRUE);
                cityCountyDataDTO.setCountyId(likeCountyList.get(0).getCountyId());
                return cityCountyDataDTO;
            } else {
                cityCountyDataDTO.setIsMatch(Boolean.FALSE);
                cityCountyDataDTO.setErrorMsg("之家区县匹配出多条:county=".concat(county));
                return cityCountyDataDTO;
            }
        }
        //扩展配置匹配
        List<ZhijiaCountyConfig> countyNameList = countyConfigList.stream().filter((ZhijiaCountyConfig zhijiaCityConfig) -> {
            String countyName = zhijiaCityConfig.getCountyNameConfig();
            if (StringUtils.isNotEmpty(countyName)) {
                List<String> countyNameExtends = Arrays.asList(countyName.split(","));
                return countyNameExtends.contains(county);
            }
            return Boolean.FALSE;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(countyNameList)) {
            cityCountyDataDTO.setIsMatch(Boolean.TRUE);
            cityCountyDataDTO.setCountyId(countyNameList.get(0).getCountyId());
            return cityCountyDataDTO;
        }
        cityCountyDataDTO.setIsMatch(Boolean.FALSE);
        cityCountyDataDTO.setErrorMsg("之家区县未匹配成功:county=".concat(county));
        return cityCountyDataDTO;
    }

    /**
     * 获取token
     *
     * @return String
     * @author zhen.Li1
     * @date 2024/7/11 10:10
     */
    @Override
    public String getToken() {
        String redisKey = RedisKeyConstant.ZHIJIA_GET_TOKEN_KEY;
        String redisKeyLock = RedisKeyConstant.ZHIJIA_GET_TOKEN_KEY_LOCK;
        String value = UUID.randomUUID().toString();
        String token = null;
        try {
            token = redisChgService.get(redisKey);
            if (StringUtils.isNotEmpty(token)) {
                return token;
            } else {
                redisChgService.lock(redisKeyLock, value);
                //获取锁成功，
                //（多线程处理时）再查一遍
                token = redisChgService.get(redisKey);
                if (StringUtils.isNotEmpty(token)) {
                    return token;
                }
                //调用获取token接口
                Result<String> result = zhiJiaClient.getToken();
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    token = result.getData();
                    //写入redis
                    redisChgService.setex(redisKey, token, 5400);
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode()
                            , "之家获取token调用异常,result= " + result.getMessage()));
                }
                redisChgService.unlock(redisKeyLock, value);
            }

        } catch (Exception e) {
            redisChgService.unlock(redisKeyLock, value);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家获取token程序异常异常!"), e);
        }
        return token;
    }

    @Override
    public List<ZhiJiaCarBrandInfo> getCarBrandInfos() {
        // 查询品牌
        ZhiJiaCarBrandInfoExample zhiJiaCarBrandInfoExample = new ZhiJiaCarBrandInfoExample();
        zhiJiaCarBrandInfoExample.createCriteria().andAppletDateGreaterThanOrEqualTo(LocalDate.now().toString());
        List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos = zhiJiaCarBrandInfoMapper.selectByExample(zhiJiaCarBrandInfoExample);
        if (zhiJiaCarBrandInfos.isEmpty()) {
            // 今日配置表为空,报警，并启用原有配置表！
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家今日车品牌配置表为空!"));
            ZhiJiaCarBrandInfoExample zhiJiaCarBrandInfoExample1 = new ZhiJiaCarBrandInfoExample();
            zhiJiaCarBrandInfoExample.createCriteria().andAppletDateLessThanOrEqualTo(LocalDate.now().toString());
            List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos1 = zhiJiaCarBrandInfoMapper.selectByExample(zhiJiaCarBrandInfoExample1);
            zhiJiaCarBrandInfos.addAll(zhiJiaCarBrandInfos1);

        }
        return zhiJiaCarBrandInfos;
    }

    @Override
    public ZhiJiaCarInfoDTO getZhiJiaCarBrandInfo(ZhiJiaClueBackData zhiJiaClueBackInfo, List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos) {
        ZhiJiaCarInfoDTO zhiJiaCarInfoDTO = new ZhiJiaCarInfoDTO();
        String brandName = removeSpacesAndConvertToUpper(zhiJiaClueBackInfo.getBrandName());
        // 精确匹配
        List<ZhiJiaCarBrandInfo> carBrandInfos = zhiJiaCarBrandInfos.stream()
                .filter((ZhiJiaCarBrandInfo brandInfo) -> preciseMatch(brandInfo.getNewBrandName(), brandName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carBrandInfos)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setBrandId(carBrandInfos.get(0).getBrandId());
            return zhiJiaCarInfoDTO;
        }
        // 模糊匹配
        List<ZhiJiaCarBrandInfo> carBrandInfos1 = zhiJiaCarBrandInfos.stream()
                .filter((ZhiJiaCarBrandInfo brandInfo) -> complexFuzzyMatch(brandInfo.getNewBrandName(), brandName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carBrandInfos1)) {
            if (carBrandInfos1.size() == 1) {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
                zhiJiaCarInfoDTO.setBrandId(carBrandInfos1.get(0).getBrandId());
                return zhiJiaCarInfoDTO;
            } else {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
                zhiJiaCarInfoDTO.setErrorMsg("车辆品牌匹配到多条，品牌名：" + brandName);
                return zhiJiaCarInfoDTO;
            }
        }
        //扩展配置匹配
        List<ZhiJiaCarBrandInfo> carBrandInfos2 = zhiJiaCarBrandInfos.stream()
                .filter((ZhiJiaCarBrandInfo brandInfo) -> {
                    String brandExtend = brandInfo.getBrandExtend();
                    if (brandExtend != null) {
                        List<String> brandList = Arrays.asList(brandExtend.split(","));
                        return brandList.contains(brandName);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carBrandInfos2)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setBrandId(carBrandInfos2.get(0).getBrandId());
            return zhiJiaCarInfoDTO;
        }
        zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
        zhiJiaCarInfoDTO.setErrorMsg("车辆品牌匹配失败，品牌名：" + brandName);
        return zhiJiaCarInfoDTO;
    }


    @Override
    public List<ZhiJiaCarSeriesInfo> getCarSeriesInfos(int brandId) {
        ZhiJiaCarSeriesInfoExample zhiJiaCarSeriesInfoExample = new ZhiJiaCarSeriesInfoExample();
        zhiJiaCarSeriesInfoExample.createCriteria()
                .andAppletDateGreaterThanOrEqualTo(LocalDate.now().toString())
                .andBrandIdEqualTo(brandId);
        List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos = zhiJiaCarSeriesInfoMapper.selectByExample(zhiJiaCarSeriesInfoExample);
        if (zhiJiaCarSeriesInfos.isEmpty()) {
            // 今日车系配置表为空，报警，并启用原有配置表！
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "今日车系配置表为空!"));
            ZhiJiaCarSeriesInfoExample zhiJiaCarSeriesInfoExample1 = new ZhiJiaCarSeriesInfoExample();
            zhiJiaCarSeriesInfoExample1.createCriteria()
                    .andBrandIdEqualTo(brandId);
            List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos1 = zhiJiaCarSeriesInfoMapper.selectByExample(zhiJiaCarSeriesInfoExample1);
            zhiJiaCarSeriesInfos.addAll(zhiJiaCarSeriesInfos1);
        }
        return zhiJiaCarSeriesInfos;
    }


    @Override
    public ZhiJiaCarInfoDTO getZhiJiaCarSeriesInfo(ZhiJiaClueBackData zhiJiaClueBackInfo, List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos) {
        String seriesName = removeSpacesAndConvertToUpper(zhiJiaClueBackInfo.getSeriesName());
        ZhiJiaCarInfoDTO zhiJiaCarInfoDTO = new ZhiJiaCarInfoDTO();
        // 精确匹配
        List<ZhiJiaCarSeriesInfo> carSeriesInfos = zhiJiaCarSeriesInfos.stream()
                .filter((ZhiJiaCarSeriesInfo seriesInfo) -> preciseMatch(seriesInfo.getNewSeriesName(), seriesName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setBrandId(carSeriesInfos.get(0).getBrandId());
            zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos.get(0).getSeriesId());
            return zhiJiaCarInfoDTO;
        }
        // 模糊匹配
        List<ZhiJiaCarSeriesInfo> carSeriesInfos1 = zhiJiaCarSeriesInfos.stream()
                .filter((ZhiJiaCarSeriesInfo seriesInfo) -> seriesInfo.getNewSeriesName().contains(seriesName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos1)) {
            if (carSeriesInfos1.size() == 1) {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
                zhiJiaCarInfoDTO.setBrandId(carSeriesInfos1.get(0).getBrandId());
                zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos1.get(0).getSeriesId());
                return zhiJiaCarInfoDTO;
            } else {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
                zhiJiaCarInfoDTO.setErrorMsg("车辆车系匹配到多条，车系名：" + seriesName);
                return zhiJiaCarInfoDTO;
            }
        }
        //扩展配置匹配
        List<ZhiJiaCarSeriesInfo> carSeriesInfos2 = zhiJiaCarSeriesInfos.stream()
                .filter((ZhiJiaCarSeriesInfo seriesInfo) -> {
                    String seriesExtend = seriesInfo.getSeriesExtend();
                    if (seriesExtend != null) {
                        List<String> seriesList = Arrays.asList(seriesExtend.split(","));
                        return seriesList.contains(seriesName);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos2)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setBrandId(carSeriesInfos2.get(0).getBrandId());
            zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos2.get(0).getSeriesId());
            return zhiJiaCarInfoDTO;
        }
        zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
        zhiJiaCarInfoDTO.setErrorMsg("车辆品牌匹配失败，车系名：" + seriesName);
        return zhiJiaCarInfoDTO;
    }


    /**
     * 精准匹配两个字符串，返回匹配结果
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等则返回true，否则返回false
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     */
    public static boolean preciseMatch(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 车品牌模糊匹配，返回布尔结果
     *
     * @param brand   被匹配车品牌名称
     * @param pattern 匹配模板
     * @return 如果brand匹配pattern则返回true，否则返回false
     */
    public static boolean complexFuzzyMatch(String brand, String pattern) {
        if (brand == null || pattern == null) {
            return false;
        }

        // 忽略大小写的匹配
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(brand);

        return matcher.find();
    }

    /**
     * 将给定的字符串转换为全英文大写，并去除非字母数字字符。
     *
     * @param input 输入的字符串
     * @return 转换后的字符串
     */
    public static String removeSpacesAndConvertToUpper(String input) {
        if (input == null) {
            return null;
        }

        // 去除非字母数字字符，保留中文字符并转换为大写
        StringBuilder normalizedString = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isLetterOrDigit(ch) || Character.isIdeographic(ch)) {
                if (Character.isLowerCase(ch)) {
                    normalizedString.append(Character.toUpperCase(ch));
                } else {
                    normalizedString.append(ch);
                }
            }
        }

        return normalizedString.toString();
    }

    /**
     * 获取当日城市配置
     *
     * @return String
     * @author zhen.Li1
     * @date 2024/7/11 10:10
     */
    @Override
    public List<ZhijiaCityConfig> getCityConfigList() {
        List<ZhijiaCityConfig> zhijiaCityConfig = new ArrayList<>();
        ZhijiaCityConfigExample zhijiaCityConfigExample = new ZhijiaCityConfigExample();
        zhijiaCityConfigExample.createCriteria()
                .andUploadDateEqualTo(LocalDate.now().toString());
        zhijiaCityConfig = zhijiaCityConfigMapper.selectByExample(zhijiaCityConfigExample);
        if (zhijiaCityConfig.isEmpty()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "今日城市配置表为空!"));
            ZhijiaCityConfigExample zhijiaCityConfigExampleYes = new ZhijiaCityConfigExample();
            zhijiaCityConfigExampleYes.createCriteria()
                    .andUploadDateEqualTo(LocalDate.now().minusDays(1).toString());
            zhijiaCityConfig = zhijiaCityConfigMapper.selectByExample(zhijiaCityConfigExampleYes);
        }
        return zhijiaCityConfig;
    }

    /**
     * 获取当日区县配置
     *
     * @return String
     * @author zhen.Li1
     * @date 2024/7/11 10:10
     */
    @Override
    public List<ZhijiaCountyConfig> getCountyConfigList() {
        List<ZhijiaCountyConfig> zhijiaCountyConfigList = new ArrayList<>();
        ZhijiaCountyConfigExample zhijiaCountyConfigExample = new ZhijiaCountyConfigExample();
        zhijiaCountyConfigExample.createCriteria()
                .andUploadDateEqualTo(LocalDate.now().toString());
        zhijiaCountyConfigList = zhijiaCountyConfigBMapper.selectByExample(zhijiaCountyConfigExample);
        if (zhijiaCountyConfigList.isEmpty()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家今日区县配置表为空!"));
            ZhijiaCountyConfigExample zhijiaCountConfigExampleYes = new ZhijiaCountyConfigExample();
            zhijiaCountyConfigExample.createCriteria()
                    .andUploadDateEqualTo(LocalDate.now().minusDays(1).toString());
            zhijiaCountyConfigList = zhijiaCountyConfigBMapper.selectByExample(zhijiaCountConfigExampleYes);
        }
        return zhijiaCountyConfigList;
    }
}
