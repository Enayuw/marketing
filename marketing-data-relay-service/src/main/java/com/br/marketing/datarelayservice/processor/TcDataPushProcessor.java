package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class TcDataPushProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private EmailService systemExceptionServiceImpl;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrSyncRecord record = new MarketingTcyrSyncRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrSyncRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrSyncRecord record = new MarketingTcyrSyncRecord();
        String scene = resolveScene(batchNo);
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setScene(scene);
        record.setData(tcRequestDTO.getData());
        record.setStatus(0);
        record.setDownStatus(0);
        record.setIsDel(1);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrSyncRecordMapper.insertSelective(record);
            notifyNewSceneAlarm(tcRequestDTO, apiCode, batchNo, scene);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警 todo
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrSyncRecordMapper.insertSelective(record);
            return null;
        }
    }

    private String resolveScene(String batchNo) {
        Map<String, String> sceneMap = marketingCommonConfig.getTcBatchNoSuffixToSceneConfig();
        if (StringUtils.isBlank(batchNo) || sceneMap == null || sceneMap.isEmpty()) {
            return "NEW";
        }
        for (Map.Entry<String, String> entry : sceneMap.entrySet()) {
            String prefix = entry.getKey();
            if (StringUtils.isBlank(prefix)) {
                continue;
            }
            if (batchNo.equals(prefix) || batchNo.startsWith(prefix + "_")) {
                return entry.getValue();
            }
        }
        return "NEW";
    }

    private void notifyNewSceneAlarm(TcRequestDTO tcRequestDTO, String apiCode, String batchNo, String scene) {
        if (!"NEW".equals(scene)) {
            return;
        }
        String batchPrefix = extractBatchPrefix(batchNo);
        if (StringUtils.isBlank(batchPrefix)) {
            return;
        }
        try {
            Integer count = tcyrSyncRecordMapper.countTodayByApiCodeAndBatchPrefix(apiCode, batchPrefix);
            if (count == null || count != 1) {
                return;
            }
            String content = String.format("同程NEW前缀告警：apiCode=%s,batchNo=%s,batchPrefix=%s,requestNo=%s,scene=%s",
                    apiCode, batchNo, batchPrefix, tcRequestDTO.getRequestNo(), scene);
            systemExceptionServiceImpl.sendAlarm(content, "Marketing-data-relay-service");
        } catch (Exception ignore) {
            // 告警异常不影响主流程
        }
    }

    private String extractBatchPrefix(String batchNo) {
        if (StringUtils.isBlank(batchNo)) {
            return null;
        }
        int delimiterIndex = batchNo.indexOf('_');
        return delimiterIndex > 0 ? batchNo.substring(0, delimiterIndex) : batchNo;
    }


}
