package com.br.marketing.service.Impl;

import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.service.PushDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PushDataServiceImpl implements PushDataService{

    @Autowired
    PhoneSaleMapper phoneSaleMapper;
    
    @Autowired
    DassServiceClient dassServiceClient;
    
    @Override
    public Result pushDassData(Long id) {
        Boolean actionMark = true;
        Long minId = null;
        while(actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                minId = phoneSale.getId();
                Result result = dassServiceClient.postHermesUserData(phoneSales, 0);
            }else{
                actionMark=false;
            }
        }

        return null;
    }

}
