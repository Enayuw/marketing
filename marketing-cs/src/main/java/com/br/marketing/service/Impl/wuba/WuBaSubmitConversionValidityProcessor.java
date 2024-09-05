package com.br.marketing.service.Impl.wuba;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WuBaSubmitConversionValidityProcessor {

    private final static String TITLE = "【58新客提交营销名单】";

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    public List<Long> validate(List<WubaSubmitConversionData> pushList, WubaSubmitConversionData param) {
        List<Long> notValidIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(pushList)) {
            return notValidIds;
        }
        String apiCode = param.getApiCode();
        LocalDate curLocalDate = LocalDate.now();

        Set<String> custNumSet = pushList.stream().map(WubaSubmitConversionData::getCell).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsMap = transferDataValidityPeriodService
                .getValidityPeriodsByCustNum(custNumSet, apiCode, curLocalDate);

        if (CollectionUtils.isEmpty(validityPeriodsMap)) {
            log.warn(TITLE + "未获取到上传数据或未配置有效期, apiCode: {}, requestData: {}", apiCode, curLocalDate);
            validityPeriodsMap = new HashMap<>();
        }

        log.warn(TITLE + "判断有效期, apiCode: {}, requestData: {}", apiCode, curLocalDate);
        Iterator<WubaSubmitConversionData> iterator = pushList.iterator();
        while (iterator.hasNext()) {
            WubaSubmitConversionData next = iterator.next();
            String cell = next.getCell();
            if (validityPeriodsMap.get(cell) == null) {
                notValidIds.add(next.getId());
                iterator.remove();
            }
        }
        return notValidIds;
    }
}
