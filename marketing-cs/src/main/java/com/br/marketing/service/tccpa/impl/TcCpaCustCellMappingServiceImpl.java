package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.MarketingTcyrCustCellMappingMapper;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TcCpaCustCellMappingServiceImpl implements TcCpaCustCellMappingService {

    @Resource
    private MarketingTcyrCustCellMappingMapper tcyrCustCellMappingMapper;

    @Override
    public String selectCell(String userKey) {
        String cell = tcyrCustCellMappingMapper.selectNumUserKeyCellBytikv_(userKey);
        if (StringUtils.isBlank(cell)) {
            cell = tcyrCustCellMappingMapper.selectStrUserKeyCellBytikv_(userKey);
        }
        return cell;
    }

    @Override
    public List<Map<String, Object>> selectCellInfo(List<String> userKeyList) {
        return  tcyrCustCellMappingMapper.selectCellInfotikv_(userKeyList);
    }

    @Override
    public List<Map<String, String>> selectCellByStrCustNum(List<String> notExistUserKeyList) {
        return tcyrCustCellMappingMapper.selectCellByStrCustNumtikv_(notExistUserKeyList);
    }
}
