package com.br.marketing.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserExample;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class CaseUserServiceImpl {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    CaseShuheUserMapper caseShuheUserMapper;

    public static final DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public boolean isY(String mobile) {
        return isY(mobile, false);
    }

    public boolean isY(String mobile, boolean isLog) {
        if (!isLog) {
            mobile = BrCipherMaker.getInstance().encode(mobile);
        }
        Integer days = 29;
        HashMap<String, Integer> shuhePushBlackDay = marketingCommonConfig.getShuhePushBlackDay();
        if (shuhePushBlackDay != null) {
            days = shuhePushBlackDay.getOrDefault("dassBlack", 30) - 1;
        }
        Date startTime = new Date();
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse(LocalDate.now().minusDays(days).format(ymd) + " 00:00:00");
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        CaseShuheUserExample userExample = new CaseShuheUserExample();
        userExample.createCriteria()
                .andCellEqualTo(mobile)
                .andIsBlackEqualTo("Y")
                .andCreateTimeGreaterThanOrEqualTo(startTime);
        List<CaseShuheUser> caseShuheUsers = caseShuheUserMapper.selectByExample(userExample);
        if (caseShuheUsers.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRrtEnd(String mobile) {
        return isRrtEnd(mobile, false);
    }

    public boolean isRrtEnd(String mobile, boolean isLog) {
        try {
            Long count = caseShuheUserMapper
                    .getByCellOrClcUsrMaxDxRrtEndOrUsrForbidCallEndTim(isLog
                                    ? mobile : BrCipherMaker.getInstance().encode(mobile)
                            , LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            return count != null && count > 0L;
        } catch (Exception ex) {
            log.error("判断rrt时间有错误" + ex.getMessage(), ex);
        }
        return false;
    }

}
