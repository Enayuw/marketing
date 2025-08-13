package com.br.marketing.datarelayservice.processor;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrCpaRevokeRecord;
import com.br.marketing.enums.TcCpaDownStatusEnum;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.enums.TcRecordCleanStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaRevokeRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcCpaRevokeProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrCpaRevokeRecordMapper tcyrCpaRevokeRecordMapper;



    @Override
    protected String fetchApiCode() {
        return cpaApiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrCpaRevokeRecord record = new MarketingTcyrCpaRevokeRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrCpaRevokeRecord record = new MarketingTcyrCpaRevokeRecord();
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
            tcyrCpaRevokeRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(TcCpaRecordStatusEnum.ACCESS_FAIL.getValue());
            tcyrCpaRevokeRecordMapper.insertSelective(record);
            return null;
        }
    }
}
