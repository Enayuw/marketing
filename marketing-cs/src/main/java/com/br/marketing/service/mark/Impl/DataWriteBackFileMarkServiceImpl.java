package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataDTO;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.tools.Generate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName DataWriteBackFileMarkServiceImpl
 * @Description pp停车文件数据回写跑分文件与Doris实现
 * @Author kongbx
 * @Date 2025/2/19 19:12
 */
@Service
@Slf4j
public class DataWriteBackFileMarkServiceImpl implements DataWriteBackFileMarkService {

    @Autowired
    RedisChgService redisChgService;
    @Resource
    FlagDataMapper flagDataMapper;
    private static final String TITLE = "【pp停车文件数据回写】";

    @Override
    public void process() {
        if(checkEsStatus()){
            // 同步数据写入doris
            syncData();
            // 生成文件
            generateFile();
        }
    }

    /**
     * 判断es数据是否补充完毕
     * @return
     */
    private boolean checkEsStatus() {
        Boolean aFalse = Boolean.TRUE;
        FlagDataExample flagDataExample = new FlagDataExample();
        FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                .andApiCodeEqualTo("7410717")
                .andCreateDateEqualTo(1)
                .andEsSyncStatusIsNull();
        int i = flagDataMapper.countByExample(flagDataExample);
        if(i >0){
            log.warn(TITLE + "es数据未补充完毕");
            aFalse = Boolean.FALSE;
        }
        return aFalse;
    }

    private void syncData() {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            // 分页查询打标数据
            FlagDataExample flagDataExample = new FlagDataExample();
            flagDataExample.setOrderByClause("id limit 2000");

            FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                    .andApiCodeEqualTo("7410717")
                    .andCreateDateEqualTo(1)
                    .andEsSyncStatusEqualTo(EsSyncStatusEnum.COMPLETE.getValue());
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<FlagData> flagDataList = flagDataMapper.selectByExample(flagDataExample);
            if (CollectionUtil.isEmpty(flagDataList)) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = flagDataList.get(flagDataList.size() - 1).getId();
            // 更新ES
            threadPool.submit(() -> insertMarkData(flagDataList));
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void generateFile() {

    }

    private void insertMarkData(List<FlagData> flagDataList) {
        try {
            List<String> cellMd5List = flagDataList.stream().map(FlagData::getCellMd5).collect(Collectors.toList());
            Map<String, FlagData> groupedByCellMd5 = flagDataList.stream()
                    .collect(Collectors.toMap(FlagData::getCellMd5, data -> data, (oldValue, newValue) -> newValue));

            // 根据cells查询doris数据
            String scoreSql = "select * from b_score_".concat("batchNumber").concat("where cell_md5 in(").concat(cellMd5List.toString()).concat(")");
            List<FlagDataDTO> flagDataDTOS = flagDataMapper.queryDataByCellbI_(scoreSql);
            Map<Integer, FlagDataDTO> groupedByMd5Phone = flagDataDTOS.stream()
                    .collect(Collectors.toMap(FlagDataDTO::getMd5Phone, data -> data, (oldValue, newValue) -> newValue));

            List<FlagDataDTO> list = new ArrayList<>();

            for (String string : cellMd5List) {
                FlagData flagData = groupedByCellMd5.get(string);
                FlagDataDTO flagDataDTO = groupedByMd5Phone.get(Integer.parseInt(string));

                flagDataDTO.setFlagNewCust(flagData.getFlagNewCust());

                flagDataDTO.setFlagRiskgroup(flagData.getFlagRiskgroup());
                flagDataDTO.setFlagInterest(flagData.getFlagInterest());

                flagDataDTO.setFlagAge(flagData.getFlagAge());
                flagDataDTO.setFlagProvince(flagData.getFlagProvince());
                flagDataDTO.setFlagSpecialSmall(flagData.getFlagSpecialSmall());
                flagDataDTO.setFlagSpecialrisklevel(flagData.getFlagSpecialrisklevelRule());
                flagDataDTO.setFlagIndexcs(flagData.getFlagIndexcs());
                flagDataDTO.setFlagApplyloan(flagData.getFlagApplyloan());

                flagDataDTO.setFlagIntellaudioBlacklist(flagData.getFlagIntellaudioBlacklist());
                flagDataDTO.setFlagWithoutWillingness(flagData.getFlagWithoutWillingness());

                flagDataDTO.setFlagScoreWhitelist(flagData.getFlagScoreWhitelist());
                flagDataDTO.setFlagWhitelist(flagData.getFlagWhitelist());
                list.add(flagDataDTO);
            }
            // 写入doris
            flagDataMapper.insertbI_(list);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE+"出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
    }

}
