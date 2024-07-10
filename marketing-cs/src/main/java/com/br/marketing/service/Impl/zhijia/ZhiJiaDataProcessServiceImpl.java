package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
     * 去除字符串里的空格，并将其转为全大写字符串
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     * @param str
     * @return java.lang.String
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
