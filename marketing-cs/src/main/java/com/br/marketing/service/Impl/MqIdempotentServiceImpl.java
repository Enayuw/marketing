package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.IdempotentRecordInfo;
import com.br.marketing.entity.MqIdempotentCommon;
import com.br.marketing.entity.MqIdempotentSpecial;
import com.br.marketing.enums.MqIdempotentTableType;
import com.br.marketing.mapper.MqIdempotentCommonMapper;
import com.br.marketing.mapper.MqIdempotentSpecialMapper;
import com.br.marketing.service.MqIdempotentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * MQ幂等性服务实现
 */
@Service
@Slf4j
public class MqIdempotentServiceImpl implements MqIdempotentService {

    @Resource
    private MqIdempotentCommonMapper mqIdempotentCommonMapper;

    @Resource
    private MqIdempotentSpecialMapper mqIdempotentSpecialMapper;

    private static final Integer NOT_DELETED = 0;
    private static final Integer NOT_FINISHED = 0;
    private static final Integer FINISHED = 1;

    @Override
    public Long insertIdempotentRecord(MqIdempotentTableType tableType, Long idempotentKey, String apiCode, String tag) {
        Date now = new Date();

        switch (tableType) {
            case COMMON:
                MqIdempotentCommon commonRecord = createCommonRecord(idempotentKey, apiCode, tag, now);
                mqIdempotentCommonMapper.insertSelective(commonRecord);
                return commonRecord.getId();
            case SPECIAL:
                MqIdempotentSpecial specialRecord = createSpecialRecord(idempotentKey, apiCode, tag, now);
                mqIdempotentSpecialMapper.insertSelective(specialRecord);
                return specialRecord.getId();
            default:
                throw new IllegalArgumentException("不支持的幂等表类型: " + tableType);
        }
    }

    @Override
    public void updateIsFinishedAndApiCode(MqIdempotentTableType tableType, Long recordId, String apiCode) {
        Date now = new Date();

        switch (tableType) {
            case COMMON:
                MqIdempotentCommon commonRecord = new MqIdempotentCommon();
                commonRecord.setId(recordId);
                commonRecord.setIsFinished(FINISHED);
                commonRecord.setApiCode(apiCode);
                commonRecord.setUpdateTime(now);
                mqIdempotentCommonMapper.updateByPrimaryKeySelective(commonRecord);
                break;
            case SPECIAL:
                MqIdempotentSpecial specialRecord = new MqIdempotentSpecial();
                specialRecord.setId(recordId);
                specialRecord.setIsFinished(FINISHED);
                specialRecord.setApiCode(apiCode);
                specialRecord.setUpdateTime(now);
                mqIdempotentSpecialMapper.updateByPrimaryKeySelective(specialRecord);
                break;
            default:
                throw new IllegalArgumentException("不支持的幂等表类型: " + tableType);
        }
    }

    /**
     * 创建通用表记录
     */
    private MqIdempotentCommon createCommonRecord(Long idempotentKey, String apiCode, String tag, Date now) {
        MqIdempotentCommon record = new MqIdempotentCommon();
        record.setIdempotentKey(idempotentKey);
        record.setApiCode(apiCode);
        record.setTag(tag);
        record.setIsDeleted(NOT_DELETED);
        record.setIsFinished(NOT_FINISHED); // 初始化业务未完成
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        record.setCreateDate(Integer.valueOf(yyyyMMdd));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        return record;
    }

    /**
     * 创建特殊表记录
     */
    private MqIdempotentSpecial createSpecialRecord(Long idempotentKey, String apiCode, String tag, Date now) {
        MqIdempotentSpecial record = new MqIdempotentSpecial();
        record.setIdempotentKey(idempotentKey);
        record.setApiCode(apiCode);
        record.setTag(tag);
        record.setIsDeleted(NOT_DELETED);
        record.setIsFinished(NOT_FINISHED); // 初始化业务未完成
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        record.setCreateDate(Integer.valueOf(yyyyMMdd));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        return record;
    }

    @Override
    public IdempotentRecordInfo selectByIdempotentKey(MqIdempotentTableType tableType, Long idempotentKey) {
        return switch (tableType) {
            case COMMON -> mqIdempotentCommonMapper.selectByIdempotentKey(idempotentKey);
            case SPECIAL -> mqIdempotentSpecialMapper.selectByIdempotentKey(idempotentKey);
        };
    }
}

