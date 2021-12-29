package com.br.marketing.service.Impl;


import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.PushInfoListVO;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PushInfoServiceImpl implements PushInfoService {

    @Autowired
    private CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Override
    public PageResultReturn getPushInfoList(int current, int size,String mApiCode,String pushBeginTime,String pushEndTime,String pushInfoId,Integer mStatus) {
        if (StringUtils.isNotEmpty(pushEndTime)){
            pushEndTime = DateUtils.format(addDay(pushEndTime, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        PageHelper.startPage(current, size);
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(mApiCode,pushBeginTime,pushEndTime,pushInfoId,mStatus);
        return PageResultReturn.setPageResult(list, current, size);
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
