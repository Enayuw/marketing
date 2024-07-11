package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.entity.ZhijiaCityConfig;
import com.br.marketing.entity.ZhijiaCountyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @ClassName ZhiJiaDataProcessServiceImpl
 * @Description 之家数据处理service
 * @Author zhen.li1
 * @Date 2024/7/10 16:40
 */
@Service
@Slf4j
public class ZhiJiaDataProcessServiceImpl implements ZhiJiaDataProcessService {

    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Override
    public void getCityAndCounty() {

    }

    @Override
    public void getBrandAndseries() {
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
     * @param city
     * @param county
     * @return CityCountyDataDTO
     * @author zhen.Li1
     * @date 2024/7/10 19:46
     */
    @Override
    public CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig> cityList, List<ZhijiaCountyConfig> countyList, String city, String county) {
        CityCountyDataDTO cityCountyDataDTO = new CityCountyDataDTO();
        //精确匹配城市
        List<ZhijiaCityConfig> defineCityList = cityList.stream().filter(zhijiaCityConfig -> zhijiaCityConfig.getCName().equals(city))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(defineCityList)) {
            cityCountyDataDTO.setCountyId(countyList.get(0).getCountyId());
        } else {
            //模糊匹配城市
            List<ZhijiaCityConfig> likeCityList = cityList.stream().filter(zhijiaCityConfig -> city.contains(zhijiaCityConfig.getCNameConfig()))
                    .collect(Collectors.toList());
            if ((CollectionUtils.isEmpty(likeCityList))) {
                {

                }
            }
        }

        return cityCountyDataDTO;
    }

    /**
     * 去除字符串里的空格，并将其转为全大写字符串
     *
     * @param str
     * @return java.lang.String
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     */
    public static String removeSpacesAndConvertToUpper(String str) {
        if (str == null) {
            return null;
        }
        // 去除所有空格
        String noSpaces = str.replaceAll("\\s+", "");
        // 转为大写
        String upperCase = noSpaces.toUpperCase();
        return upperCase;
    }
}
