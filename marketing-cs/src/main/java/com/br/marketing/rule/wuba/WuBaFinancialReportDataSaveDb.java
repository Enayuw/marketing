package com.br.marketing.rule.wuba;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.InterfaceParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @Description WuBaFinancialSaveReportDB
 * @Author hong.chen
 * @CreateTime 2024/09/12
 */
@Service
@Slf4j
public class WuBaFinancialReportDataSaveDb implements AssembleData<InterfaceParams> {
    @Resource
    private WubaSubmitConversionDataMapper wubaSubmitConversionDataMapper;

    @Override
    public InterfaceParams assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            if (Objects.equals(syncUser.getUserType(), "2")) {
                saveDb(syncUser);
                return true;
            }
        }

        return false;
    }

    @Override
    public String label() {
        return "WuBa_Financial_Report_Data_SaveDB";
    }

    @Override
    public Integer dataDirection() {
        return null;
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    private void saveDb(MarketingSyncUser syncUser) {
        WubaSubmitConversionData data = new WubaSubmitConversionData();
        data.setApiCode(syncUser.getApiCode());
        data.setLocalId(0L);
        data.setCell(syncUser.getCellMd5());
        data.setStatus(1);
        data.setUserType("2");
        data.setExtend("金融场景撞库后自动上报");

        LocalTime start = LocalTime.of(9, 0);
        LocalTime mockTime = start.plusSeconds(RandomUtil.randomLong(3600));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = LocalDateTime.of(LocalDate.now(), mockTime).format(formatter);
        data.setMarketingTime(formattedTime);

        data.setCreateDate(Integer.valueOf(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN)));
        wubaSubmitConversionDataMapper.insertSelective(data);
    }
}
