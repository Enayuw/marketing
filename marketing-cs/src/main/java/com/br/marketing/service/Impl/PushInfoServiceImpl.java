package com.br.marketing.service.Impl;


import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
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
    public PageResultReturn getPushInfoList(PushInfoFilterDTO dto) {
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(dto);
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
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
