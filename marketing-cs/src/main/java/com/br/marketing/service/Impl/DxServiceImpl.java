package com.br.marketing.service.Impl;

import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.IDxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DxServiceImpl implements IDxService {

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Override
    public Set<String> getCustNumByPhoneDx(Collection custNums,String _tApicode,String _startDay,String _endDay,String _transferType) {
        PhoneSaleRecordInfoDTO _7recordInfoDTO = new PhoneSaleRecordInfoDTO();
        _7recordInfoDTO.setCustNums(custNums);
        _7recordInfoDTO.setApiCode(_tApicode);
        _7recordInfoDTO.setStartDate(_startDay);
        _7recordInfoDTO.setEndDate(_endDay);
        _7recordInfoDTO.setTransferType(_transferType);
        List<String> _records = phoneSaleExtendInfoMapper.getDxRecordCustByTransferType(_7recordInfoDTO);
        Set<String> _filerCustNumSet = _records.stream().collect(Collectors.toSet());
        return _filerCustNumSet;
    }

    @Override
    public Result<Map<String, String>> getBlackByTransfer(List<MarketingTransferSyncUser> transferSyncUsers,String apiCode) {
        List<BlackQueryDetailDTO> blackQueryDetailDTOS = new ArrayList<>();
        ReqBlackPhoneQueryDTO dto = new ReqBlackPhoneQueryDTO();
        dto.setApiCode(apiCode);
        dto.setDetailBlackPhoneDTO(blackQueryDetailDTOS);
        transferSyncUsers.forEach(k -> {
            BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
            blackQueryDetailDTO.setDataId(k.getId().toString());
            blackQueryDetailDTO.setApiCode(apiCode);
            blackQueryDetailDTO.setCaseNum(k.getCustNum());
            blackQueryDetailDTOS.add(blackQueryDetailDTO);
        });
        return robotaiApiServiceClient.queryBlackPhone(dto);
    }

    @Override
    public Result<Map<String, String>> getBlackByDXfile(List<PhoneSale> phoneSales, String apiCode) {
        List<BlackQueryDetailDTO> blackQueryDetailDTOS = new ArrayList<>();
        ReqBlackPhoneQueryDTO dto = new ReqBlackPhoneQueryDTO();
        dto.setApiCode(apiCode);
        dto.setDetailBlackPhoneDTO(blackQueryDetailDTOS);
        phoneSales.forEach(k -> {
            BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
            blackQueryDetailDTO.setDataId(k.getId().toString());
            blackQueryDetailDTO.setApiCode(apiCode);
            blackQueryDetailDTO.setCaseNum(k.getUid());
            blackQueryDetailDTOS.add(blackQueryDetailDTO);
        });
        return robotaiApiServiceClient.queryBlackPhone(dto);
    }
}
