package com.br.marketing.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserExample;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            CaseShuheUser user = caseShuheUserMapper
                    .getByCellOrClcUsrMaxDxRrtEndOrUsrForbidCallEndTim(
                            isLog ? mobile : BrCipherMaker.getInstance().encode(mobile));
            if (user == null || user.getId() == null) {
                return false;
            }
            LocalDate localDate = LocalDate.now();
            String clcUsrMaxDxRrtEnd = user.getClcUsrMaxDxRrtEnd();
            if (StringUtils.isNotBlank(clcUsrMaxDxRrtEnd)) {
                String date = clcUsrMaxDxRrtEnd.substring(0, 10);
                LocalDate endDate = LocalDate.parse(date, ymd);
                if (endDate.compareTo(localDate) >= 0) {
                    return true;
                }
            }
            String usrForbidCallEndTim = user.getUsrForbidCallEndTim();
            if (StringUtils.isNotBlank(usrForbidCallEndTim)) {
                LocalDate forbidCallEndTimDate;
                try {
                    forbidCallEndTimDate = LocalDateTime.parse(usrForbidCallEndTim
                            , DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT))
                            .toLocalDate();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        forbidCallEndTimDate = LocalDate.parse(usrForbidCallEndTim
                                , DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (Exception exception) {
                        log.error(exception.getMessage(), e);
                        return false;
                    }
                }
                if (localDate.isBefore(forbidCallEndTimDate) || localDate.isEqual(forbidCallEndTimDate)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.error("判断rrt时间有错误" + ex.getMessage(), ex);
        }
        return false;
    }

}
