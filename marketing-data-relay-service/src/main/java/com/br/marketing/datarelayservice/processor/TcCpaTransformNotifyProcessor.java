package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrCpaTransferRecord;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.enums.TcRecordCleanStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaTransferRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcCpaTransformNotifyProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrCpaTransferRecordMapper tcyrCpaTransferRecordMapper;

    @Override
    protected String fetchApiCode() {
        return cpaApiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrCpaTransferRecord record = new MarketingTcyrCpaTransferRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrCpaTransferRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrCpaTransferRecord record = new MarketingTcyrCpaTransferRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setStatus(TcCpaRecordStatusEnum.ACCESS_IN.getValue());
        record.setIsClean(TcRecordCleanStatusEnum.CLEAN_WAITED.getValue());
        record.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrCpaTransferRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(TcCpaRecordStatusEnum.ACCESS_FAIL.getValue());
            tcyrCpaTransferRecordMapper.insertSelective(record);
            return null;
        }
    }
}
