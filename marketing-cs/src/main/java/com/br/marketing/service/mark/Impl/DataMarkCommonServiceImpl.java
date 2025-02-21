package com.br.marketing.service.mark.Impl;

import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.mark.DataMarkCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DataMarkCommonServiceImpl implements DataMarkCommonService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Override
    public StraHisFile getStraHisFile(String apiCode) {
        Date beginTimeOfDay = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(2)
                .andTypeEqualTo(2)
                .andCreateTimeGreaterThanOrEqualTo(beginTimeOfDay);
        straHisFileExample.setOrderByClause("create_time desc limit 1");
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        if (CollectionUtils.isEmpty(straHisFiles)) {
            return null;
        }
        return straHisFiles.get(0);
    }
}
