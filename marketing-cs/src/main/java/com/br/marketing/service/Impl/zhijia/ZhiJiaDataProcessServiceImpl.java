package com.br.marketing.service.Impl.zhijia;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.dto.zhijia.ZhiJiaCarInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ZhiJiaCarBrandInfoMapper;
import com.br.marketing.mapper.ZhiJiaCarSeriesInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Resource
    ZhijiaCityConfigMapper zhijiaCityConfigMapper;

    @Resource
    ZhijiaCountyConfigBMapper zhijiaCountyConfigBMapper;

    @Resource
    HttpProxyClient httpProxyClient;

    @Value("${api.zhijiaCarInfo.isProxy:false}")
    Boolean isProxy;

    @Value("${api.zhijiaCarInfo.brandUrl:00}")
    private String brandUrl;

    @Value("${api.zhijiaCarInfo.seriesUrl:00}")
    private String seriesUrl;

    @Value("${api.zhijiaCarInfo.appId:0}")
    private String appId;

    @Value("${api.zhijiaCarInfo.querykey:0}")
    private String querykey;

    @Override
    public void getCityAndCounty() {

    }

    @Override
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public void getBrandAndseries() {
        String url = brandUrl + "?access_token=" + getToken() + "&appid=" + appId + "&querykey=" + querykey;
        HashMap<String, String> stringStringHashMap = httpProxyClient.get(url, isProxy);
        String s = stringStringHashMap.get("content");

//        String brandBame = zhiJiaClueBackInf.getBrandBame();
//        String seriesName = zhiJiaClueBackInf.getSeriesName();
//        String newBrandBame = removeSpacesAndConvertToUpper(brandBame);
//        String newSeriesName = removeSpacesAndConvertToUpper(seriesName);
        String appletDate = LocalDate.now().toString();
        String localDateTime = LocalDateTime.now().format(YYYYMMDDSHORTLINE);
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
    public CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig> cityList, List<ZhijiaCountyConfig> countyList, ZhiJiaClueBackData zhiJiaClueBackInfo) {
        String city = zhiJiaClueBackInfo.getCity();
        String county = zhiJiaClueBackInfo.getContry();
        CityCountyDataDTO cityCountyDataDTO = new CityCountyDataDTO();
        //精确匹配城市
        List<ZhijiaCityConfig> defineCityList = cityList.stream().filter(zhijiaCityConfig -> zhijiaCityConfig.getCName().equals(city))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(defineCityList)) {
            //匹配区县
            cityCountyDataDTO.setCId(defineCityList.get(0).getCId());
            return matchCounty(cityCountyDataDTO, countyList, county);
        }
        //模糊匹配城市
        List<ZhijiaCityConfig> likeCityList = cityList.stream().filter(zhijiaCityConfig -> city.contains(zhijiaCityConfig.getCName()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(likeCityList)) {
            //匹配区县
            if (likeCityList.size() == 1) {
                cityCountyDataDTO.setCId(likeCityList.get(0).getCId());
                return matchCounty(cityCountyDataDTO, countyList, county);
            } else {
                cityCountyDataDTO.setIsMatch(Boolean.FALSE);
                cityCountyDataDTO.setErrorMsg("城市匹配出多条:city=".concat(city));
                return cityCountyDataDTO;
            }
        }
        //扩展配置匹配
        List<ZhijiaCityConfig> configCnameList = cityList.stream().filter(zhijiaCityConfig ->
                Arrays.asList(zhijiaCityConfig.getCNameConfig().split(",")).contains(city)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(configCnameList)) {
            //匹配区县
            cityCountyDataDTO.setCId(configCnameList.get(0).getCId());
            return matchCounty(cityCountyDataDTO, countyList, county);
        }
        cityCountyDataDTO.setIsMatch(Boolean.FALSE);
        cityCountyDataDTO.setErrorMsg("城市未匹配成功:city=".concat(city));
        return cityCountyDataDTO;
    }

    private CityCountyDataDTO matchCounty(CityCountyDataDTO cityCountyDataDTO, List<ZhijiaCountyConfig> countyList, String county) {
        //获取城市下面的区县
        Integer cId = cityCountyDataDTO.getCId();
        List<ZhijiaCountyConfig> countyConfigList = countyList.stream().filter(countyConfig -> countyConfig.getCId().equals(cId))
                .collect(Collectors.toList());

        //精确匹配区县
        List<ZhijiaCountyConfig> defineCountyList = countyConfigList.stream().filter(countyConfig -> countyConfig.getCountyName().equals(county))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(defineCountyList)) {
            cityCountyDataDTO.setIsMatch(Boolean.TRUE);
            cityCountyDataDTO.setCountyId(defineCountyList.get(0).getCountyId());
            return cityCountyDataDTO;
        }
        //模糊匹配区县
        List<ZhijiaCountyConfig> likeCountyList = countyConfigList.stream().filter(countyConfig -> county.contains(countyConfig.getCountyName()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(likeCountyList)) {
            if (likeCountyList.size() == 1) {
                cityCountyDataDTO.setIsMatch(Boolean.TRUE);
                cityCountyDataDTO.setCountyId(likeCountyList.get(0).getCountyId());
                return cityCountyDataDTO;
            } else {
                cityCountyDataDTO.setIsMatch(Boolean.FALSE);
                cityCountyDataDTO.setErrorMsg("区县匹配出多条:county=".concat(county));
                return cityCountyDataDTO;
            }
        }
        //扩展配置匹配
        List<ZhijiaCountyConfig> countyNameList = countyConfigList.stream().filter(zhijiaCityConfig ->
                Arrays.asList(zhijiaCityConfig.getCountyNameConfig().split(",")).contains(county)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(countyNameList)) {
            cityCountyDataDTO.setIsMatch(Boolean.TRUE);
            cityCountyDataDTO.setCountyId(countyNameList.get(0).getCountyId());
            return cityCountyDataDTO;
        }
        cityCountyDataDTO.setIsMatch(Boolean.FALSE);
        cityCountyDataDTO.setErrorMsg("区县未匹配成功:county=".concat(county));
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
                boolean lock = redisChgService.lock(redisKeyLock, value, 3000L);
                if (lock == true) {
                    //获取锁成功，调接口
                    Result<String> result = zhiJiaClient.getToken();
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        token = result.getData();
                        //写入redis
                        redisChgService.setex(redisKey, token, 5400);
                    } else {
                        log.error("之家获取token调用异常,result= {}", result.getMessage());
                    }
                    redisChgService.unlock(redisKeyLock, value);
                }
            }
        } catch (Exception e) {
            redisChgService.unlock(redisKeyLock, value);
            log.error("之家获取token程序异常异常", e);
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(),"今日车品牌配置表为空!"));
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
                .filter(brandInfo -> preciseMatch(brandInfo.getNewBrandName(), brandName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carBrandInfos)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setBrandId(carBrandInfos.get(0).getBrandId());
            return zhiJiaCarInfoDTO;
        }
        // 模糊匹配
        List<ZhiJiaCarBrandInfo> carBrandInfos1 = zhiJiaCarBrandInfos.stream()
                .filter(brandInfo -> complexFuzzyMatch(brandInfo.getNewBrandName(), brandName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carBrandInfos1)) {
            if (carBrandInfos1.size() == 1) {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
                zhiJiaCarInfoDTO.setBrandId(carBrandInfos1.get(0).getBrandId());
                return zhiJiaCarInfoDTO;
            } else {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
                zhiJiaCarInfoDTO.setErrorMsg("车辆品牌匹配到多条，品牌名：" +  brandName);
                return zhiJiaCarInfoDTO;
            }
        }
        //扩展配置匹配
        List<ZhiJiaCarBrandInfo> carBrandInfos2 = zhiJiaCarBrandInfos.stream().filter(brandInfo ->
                Arrays.asList(brandInfo.getBrandExtend().split(",")).contains(brandName)).collect(Collectors.toList());
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
        if (zhiJiaCarSeriesInfos.isEmpty()){
            // 今日车系配置表为空，报警，并启用原有配置表！
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(),"今日车系配置表为空!"));
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
                .filter(seriesInfo -> preciseMatch(seriesInfo.getNewSeriesName(), seriesName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos.get(0).getSeriesId());
            return zhiJiaCarInfoDTO;
        }
        // 模糊匹配
        List<ZhiJiaCarSeriesInfo> carSeriesInfos1 = zhiJiaCarSeriesInfos.stream()
                .filter(seriesInfo -> seriesInfo.getNewSeriesName().contains(seriesName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos1)) {
            if (carSeriesInfos1.size() == 1) {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
                zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos1.get(0).getSeriesId());
                return zhiJiaCarInfoDTO;
            } else {
                zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
                zhiJiaCarInfoDTO.setErrorMsg("车辆车系匹配到多条，车系名：" +  seriesName);
                return zhiJiaCarInfoDTO;
            }
        }
        //扩展配置匹配
        List<ZhiJiaCarSeriesInfo> carSeriesInfos2 = zhiJiaCarSeriesInfos.stream().filter(seriesInfo ->
                Arrays.asList(seriesInfo.getSeriesExtend().split(",")).contains(seriesName)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(carSeriesInfos2)) {
            zhiJiaCarInfoDTO.setIsMatch(Boolean.TRUE);
            zhiJiaCarInfoDTO.setSeriesId(carSeriesInfos2.get(0).getSeriesId());
            return zhiJiaCarInfoDTO;
        }
        zhiJiaCarInfoDTO.setIsMatch(Boolean.FALSE);
        zhiJiaCarInfoDTO.setErrorMsg("车辆品牌匹配失败，车系名：" + seriesName);
        return zhiJiaCarInfoDTO;
    }



    /**
     * 精准匹配两个字符串，返回匹配结果
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等则返回true，否则返回false
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
     * @param brand 被匹配车品牌名称
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(), "今日城市配置表为空!"));
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(), "今日区县配置表为空!"));
            ZhijiaCountyConfigExample zhijiaCountConfigExampleYes = new ZhijiaCountyConfigExample();
            zhijiaCountyConfigExample.createCriteria()
                    .andUploadDateEqualTo(LocalDate.now().minusDays(1).toString());
            zhijiaCountyConfigList = zhijiaCountyConfigBMapper.selectByExample(zhijiaCountConfigExampleYes);
        }
        return zhijiaCountyConfigList;
    }
}
