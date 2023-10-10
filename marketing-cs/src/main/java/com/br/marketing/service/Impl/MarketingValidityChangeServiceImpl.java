package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * -------------------------------
 *
 * @author guangxiu.li
 * @Description 有效期更改接口类
 * @Date 2023/10/08 10:19 AM
 * ------------------------------
 */
@Service
@Slf4j
public class MarketingValidityChangeServiceImpl implements MarketingDataValidityChangeService {

    @Autowired
    EntityOptServiceImpl entityOptService;
    @Resource
    MarketingValidityChangeMapper marketingValidityChangeMapper;


    @Override
    public PageResultReturn list(int current, int size, int isDel, String createTime, String appletDate,
                                 String apiCode, String userType, String validStartDate, String validEndDate ,
                                 String validDays , int validType , String updateTime) {

        if (StringUtils.isNotEmpty(validStartDate)) {
            validStartDate = DateUtils.format(addDay(validStartDate, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(validEndDate)) {
            validEndDate = DateUtils.format(addDay(validEndDate, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        PageHelper.startPage(current, size);
        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingValidityChangeMapper.selectValidityList(isDel, createTime,
                appletDate, apiCode, userType, validStartDate, validEndDate ,validDays ,validType ,updateTime, null);
        return PageResultReturn.setPageResult(marketingDataValidConfigs, current, size);
    }

    @Override
    public boolean save(MarketingDataValidConfig marketingDataValidConfig) {
        try {
            marketingDataValidConfig.setCreateTime(new Date());
            marketingValidityChangeMapper.insertMarketingDataValidConfig(marketingDataValidConfig);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }


    @Override
    public Result delTask(Long id) {
        MarketingDataValidConfig task = marketingValidityChangeMapper.selectById(id);
        if (task == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该有效期数据不存在");
        }
        MarketingDataValidConfig update = new MarketingDataValidConfig();
        update.setId(id);
        update.setIsDel(9);
        marketingValidityChangeMapper.updateById(update);
        entityOptService.writeOptLog(id, update, task);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("删除成功");

    }

    @Override
    public boolean updateById(Long id, String validStartDate, String validEndDate) {
        try {
            if (StringUtils.isEmpty(validStartDate)){
                log.warn("缺少有效期开始时间");
                return false;
            }
            if (StringUtils.isEmpty(validEndDate)){
                log.warn("缺少有效期结束时间");
                return false;
            }
            MarketingDataValidConfig newData = marketingValidityChangeMapper.selectById(id);
            validStartDate = DateUtils.format(addDay(validStartDate, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
            validEndDate = DateUtils.format(addDay(validEndDate, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
            newData.setValidStartDate(validStartDate);
            newData.setValidEndDate(validEndDate);
            marketingValidityChangeMapper.updateById(newData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }



    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }


}
