package com.br.marketing.datarelayservice.processor;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.enums.TcCpaDownStatusEnum;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcCpaDataPushProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrCpaSuccessRecordMapper tcyrCpaSuccessRecordMapper;

    @Override
    protected String fetchApiCode() {
        return cpaApiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrCpaSuccessRecord record = new MarketingTcyrCpaSuccessRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrCpaSuccessRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrCpaSuccessRecord record = new MarketingTcyrCpaSuccessRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setStatus(TcCpaRecordStatusEnum.ACCESS_IN.getValue());
        record.setDownStatus(TcCpaDownStatusEnum.DEAL_NO.getValue());
        record.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrCpaSuccessRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(TcCpaRecordStatusEnum.ACCESS_FAIL.getValue());
            tcyrCpaSuccessRecordMapper.insertSelective(record);
            return null;
        }
    }


}
