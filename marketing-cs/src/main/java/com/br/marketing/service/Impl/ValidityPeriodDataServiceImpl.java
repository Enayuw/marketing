package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ValidityPeriodDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 描述：： 根据有效期框定数据范围实现
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ValidityPeriodDataServiceImpl
 * @author: it-yml
 * @create: 2023-08-25 21:24
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ValidityPeriodDataServiceImpl implements ValidityPeriodDataService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Override
    public Boolean getMarketingTransferDataWithValidityPeriod(String apiCode, String custNum) {
        String tcId = tableCreateService.getTcId(apiCode);
        // isBlack = 1
        Integer countIsBlackByCustNum = marketingTransferSyncUserMapper.getCountIsBlackByCustNum(tcId, custNum);
        if (countIsBlackByCustNum > 0) {
            return Boolean.TRUE;
        }
        MarketingDataValidConfig marketingTransferDataWithValidityPeriod = marketingDataValidConfigMapper.getMarketingTransferDataWithValidityPeriod(apiCode);
        if (marketingTransferDataWithValidityPeriod != null) {
            String validStartDate = marketingTransferDataWithValidityPeriod.getValidStartDate();
            String validEndDate = marketingTransferDataWithValidityPeriod.getValidEndDate();
            String dateStartStr = getDateStr(validStartDate, -1);
            String dateEndStr = getDateStr(validEndDate, 1);
            Integer countIfApplyByCustNum = marketingTransferSyncUserMapper.getCountIfApplyByCustNum(tcId, custNum, dateStartStr, dateEndStr);
            if(countIfApplyByCustNum>0){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /* 获取指定日后 后 dayAddNum 天的 日期
     * @param day  日期，格式为String："2013-9-3";
     * @param dayAddNum 增加天数 格式为int;
     * @return
     */
    public static String getDateStr(String day, int dayAddNum) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); //定义日期格式化的格式
        String stringDate = null;
        //需要加减的字符串型日期
        try {
            Date classDate = format.parse(day);
            //把字符串转化成指定格式的日期
            Calendar calendar = Calendar.getInstance(); //使用Calendar日历类对日期进行加减
            calendar.setTime(classDate);
            calendar.add(Calendar.DAY_OF_MONTH, dayAddNum);
            classDate = calendar.getTime();//获取加减以后的Date类型日期
            stringDate = format.format(classDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return stringDate;
    }

}
