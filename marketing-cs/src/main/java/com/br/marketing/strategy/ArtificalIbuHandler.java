package com.br.marketing.strategy;


import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.ibu.IbuAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.entity.RongshuCycleData;
import com.br.marketing.entity.RongshuCycleDataExample;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.RongshuCycleDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description : ibu人工定制接口(批量)
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2023/02/15 17:09
 */
@Slf4j
@Service
public class ArtificalIbuHandler extends AbstractExternalInterfaceHandler<IbuAdapDTO> {

    @Resource
    private RedisChgService redisChgService;

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RongshuCycleDataMapper rongshuCycleDataMapper;

    @Resource
    private CustomerTransferSoleHandler customerTransferSoleHandler;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;
    @Resource
    private ArtificialBatchRealTimeDataHandler artificialBatchRealTimeDataHandler;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;


    @Override
    public JSONObject call(List<IbuAdapDTO> ibuAdapDTOS, ProcessHandlerContext context) {
        String nowDate = LocalDate.now().toString();
        Date todayDate = null;
        try {
            todayDate = DateUtils.parseDate(nowDate.concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (Iterator<IbuAdapDTO> iterator = ibuAdapDTOS.iterator(); iterator.hasNext(); ) {
            IbuAdapDTO ibuAdapDTO = iterator.next();
            //进行条件剔除
            PhoneSaleExtendInfo phoneSaleExtendInfo = ibuAdapDTO.getPhoneSaleExtendInfo();
            String cell = phoneSaleExtendInfo.getCell();
            String apiCode = phoneSaleExtendInfo.getApiCode();
            //分布式锁，控制cell并发推送
            String key = RedisKeyConstant.pushRongShuDaasIbuKey.concat(":")
                    .concat(apiCode).concat(":")
                    .concat(cell);
            String value = UUID.randomUUID().toString();
            redisChgService.lock(key, value);
            PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
            extendInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andCellEqualTo(cell).andCreateTimeGreaterThanOrEqualTo(todayDate);
            if (phoneSaleExtendInfoMapper.countByExample(extendInfoExample) > 0) {
                //今日已经推送,删除集合中数据
                redisChgService.unlock(key, value);
                //更新周期数据表
                if ("a".equals(ibuAdapDTO.getPushType())) {
                    RongshuCycleDataExample cycleDataExample = new RongshuCycleDataExample();
                    cycleDataExample.createCriteria().andApiCodeEqualTo(phoneSaleExtendInfo.getApiCode()).andCellEqualTo(phoneSaleExtendInfo.getCell());
                    List<RongshuCycleData> rongshuCycleDataList = rongshuCycleDataMapper.selectByExample(cycleDataExample);
                    if (!CollectionUtils.isEmpty(rongshuCycleDataList)) {
                        //update
                        RongshuCycleData update = new RongshuCycleData();
                        update.setId(rongshuCycleDataList.get(0).getId());
                        update.setPushDaasDate(nowDate);
                        update.setPushDaasTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        if (marketingCommonConfig.getRongShuPushDaasSwitch()) {
                            update.setPStatus(1);
                        } else {
                            update.setPStatus(0);
                        }
                        update.setUpdateTime(new Date());
                        rongshuCycleDataMapper.updateByPrimaryKeySelective(update);
                    }
                }
                iterator.remove();
            } else {
                //开关打开，状态为1,开关关闭，状态为4标识挡板数据状态
                if (marketingCommonConfig.getRongShuPushDaasSwitch()) {
                    phoneSaleExtendInfo.setPStatus(1);
                } else {
                    phoneSaleExtendInfo.setPStatus(4);
                }
                phoneSaleExtendInfo.setCreateTime(new Date());
                phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
                redisChgService.unlock(key, value);

                //a情况，需要insert or update 周期表
                if ("a".equals(ibuAdapDTO.getPushType())) {
                    insertOrUpdateCycleData(ibuAdapDTO, nowDate);
                }
            }
        }
        if (!CollectionUtils.isEmpty(ibuAdapDTOS)) {
            //开关打开，进行推送
            if (marketingCommonConfig.getRongShuPushDaasSwitch()) {
                //推客服
                List<ConversionData> conversionDataList = ibuAdapDTOS.stream().map(IbuAdapDTO::getConversionData).collect(Collectors.toList());
                customerTransferSoleHandler.call(conversionDataList, context);
                //推人工ibu
                callDaasIbu(ibuAdapDTOS.stream().map(IbuAdapDTO::getDatum).collect(Collectors.toList()), context);
            }
        }
        return null;
    }

    private void insertOrUpdateCycleData(IbuAdapDTO ibuAdapDTO, String nowDate) {
        PhoneSaleExtendInfo extendInfo = ibuAdapDTO.getPhoneSaleExtendInfo();
        RongshuCycleDataExample cycleDataExample = new RongshuCycleDataExample();
        cycleDataExample.createCriteria().andApiCodeEqualTo(extendInfo.getApiCode()).andCellEqualTo(extendInfo.getCell());
        List<RongshuCycleData> rongshuCycleDataList = rongshuCycleDataMapper.selectByExample(cycleDataExample);
        if (CollectionUtils.isEmpty(rongshuCycleDataList)) {
            //insert
            RongshuCycleData insert = new RongshuCycleData();
            insert.setPhoneExtendId(extendInfo.getId());
            insert.setApiCode(extendInfo.getApiCode());
            insert.setCell(extendInfo.getCell());
            insert.setCustNum(extendInfo.getCustNum());
            insert.setPushDaasDate(nowDate);
            insert.setPushDaasTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (marketingCommonConfig.getRongShuPushDaasSwitch()) {
                insert.setPStatus(1);
            } else {
                insert.setPStatus(0);
            }
            insert.setCreateTime(new Date());
            insert.setUpdateTime(new Date());
            rongshuCycleDataMapper.insert(insert);
        } else {
            //update
            RongshuCycleData update = new RongshuCycleData();
            update.setId(rongshuCycleDataList.get(0).getId());
            update.setPushDaasDate(nowDate);
            update.setPhoneExtendId(extendInfo.getId());
            update.setPushDaasTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (marketingCommonConfig.getRongShuPushDaasSwitch()) {
                update.setPStatus(1);
            } else {
                update.setPStatus(0);
            }
            update.setUpdateTime(new Date());
            rongshuCycleDataMapper.updateByPrimaryKeySelective(update);
        }
    }

    //推送人工ibu
    private void callDaasIbu(List<IbuReqDTO.Datum> ibuReqList, ProcessHandlerContext context) {
        /**
         * 人工ibu批量接口 每50条数据一个批次
         */
        //开关打开，重新构造参数,调用通用接口（电销批量接口）
        if (marketingCommonConfig.getRongShuPushNewIbuSwitch()) {
            List<BatchRealTimeUserDataDTO> batchRealTimeUserDataDTOList = new ArrayList<>();
            ibuReqList.forEach(daum -> {
                BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
                DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
                dassImportDataDTO.setPhone(AESUtil.aesEncrypty(daum.getPhone(), aesKey));
                dassImportDataDTO.setName("1");
                dassImportDataDTO.setOrgname("rongshu");
                dassImportDataDTO.setSource("26");
                dassImportDataDTO.setUid(daum.getUid());
                dassImportDataDTO.setUserType("2");
                dassImportDataDTO.setId(daum.getId());
                batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
                batchRealTimeUserDataDTOList.add(batchRealTimeUserDataDTO);
            });
            artificialBatchRealTimeDataHandler.call(batchRealTimeUserDataDTOList, new ProcessHandlerContext());
            return;
        }

        int pageSize = 50;
        int totalCount = ibuReqList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<IbuReqDTO.Datum> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = ibuReqList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = ibuReqList.subList((i - 1) * pageSize, pageSize * (i));
            }
            methodRetryHandlerService.callDassIbuBatchData(new ArrayList<>(subList), 0);
        }
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_IBU_BATCH_DATA;
    }
}
