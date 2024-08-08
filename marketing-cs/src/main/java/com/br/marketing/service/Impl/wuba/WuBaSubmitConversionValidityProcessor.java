package com.br.marketing.service.Impl.wuba;

import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WuBaSubmitConversionValidityProcessor {

    private final static String TITLE = "【58新客提交营销名单】";

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    public List<Long> validate(List<WubaSubmitConversionData> pushList, WubaSubmitConversionData param){
        List<Long> notValidIds = new ArrayList<>();
        if(CollectionUtils.isEmpty(pushList)) {
            return notValidIds;
        }
        String apiCode = param.getApiCode();
        LocalDate curLocalDate = LocalDate.now();
        LocalDate startLocalDate = curLocalDate.plusDays(-6);

        String appletDateStart = startLocalDate.toString();
        String appletDateEnd = curLocalDate.toString();

        List<String> custNums = pushList.stream().map(WubaSubmitConversionData::getCell).collect(Collectors.toList());
        Set<String> validCustNums = marketingSyncUserMapper.getCustNumSetByAppletDateInterval(
                apiCode, custNums, appletDateStart, appletDateEnd);

        Iterator<WubaSubmitConversionData> iterator = pushList.iterator();
        while (iterator.hasNext()){
            WubaSubmitConversionData next = iterator.next();
            String cell = next.getCell();
            if(!validCustNums.contains(cell)){
                notValidIds.add(next.getId());
                iterator.remove();
            }
        }
        return notValidIds;
    }
}
