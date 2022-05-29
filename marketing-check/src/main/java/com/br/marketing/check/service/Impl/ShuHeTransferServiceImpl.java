package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.ShuHeTransferService;
import com.br.marketing.client.dassservice.input.transfer.ShuheBlackPhoneTransferDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.service.IShuheBlackPhoneRecordService;
import com.br.marketing.strategy.ArtificialShuHeBlackPushTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: lizhen
 * @Time: 2022/05/29 10:06
 * @Description: 数禾转化service
 */
@Service
@Slf4j
public class ShuHeTransferServiceImpl implements ShuHeTransferService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IShuheBlackPhoneRecordService iShuheBlackPhoneRecordService;
    @Autowired
    private ArtificialShuHeBlackPushTransferHandler artificialShuHeBlackPushTransferHandler;

    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void pushBlackDataToDaas() {
        String endDay = LocalDate.now().format(yyyyMMddDF);
        String StartDay = LocalDate.parse(endDay, yyyyMMddDF).minusDays(30L).format(yyyyMMddDF);
        List<ShuheBlackPhoneTransferDataDTO> shuheBlackPhoneTransferDataDTOList = new ArrayList<>();
        List<CaseShuheUser> blackPhoneDataList = new ArrayList<>();
        //is_black为Y
        List<CaseShuheUser> blackCaseUserList = caseShuheUserMapper.selectIsBlackData(StartDay, endDay);
        blackPhoneDataList.addAll(blackCaseUserList);
        Set<String> blackMap = blackCaseUserList.parallelStream().map(CaseShuheUser::getMobile).collect(Collectors.toSet());
        Set<String> rrtEndMap = new HashSet<>();
        //clc_usr_max_dx_rrt_end>当前日期
        Boolean mark = Boolean.TRUE;
        Integer page = 0;
        while (mark) {
            List<CaseShuheUser> rrtOrderCaseUserList = caseShuheUserMapper.selectOrderRrtEndData(page * 2000);
            if (CollectionUtils.isEmpty(rrtOrderCaseUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            for (CaseShuheUser rrtOrderCaseUser : rrtOrderCaseUserList) {
                if (LocalDate.parse(rrtOrderCaseUser.getClcUsrMaxDxRrtEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).isBefore(LocalDate.now())) {
                    rrtEndMap.add(rrtOrderCaseUser.getMobile());
                    continue;
                }
                //最新的一条>=当前日期，并且不在isBlack=Y中，推送
                if (rrtEndMap.add(rrtOrderCaseUser.getMobile()) && blackMap.add(rrtOrderCaseUser.getMobile())) {
                    blackPhoneDataList.add(rrtOrderCaseUser);
                }
            }
        }
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        //根据数禾黑名单推电销记录表去重
        //封装调用Daas接口参数
        blackPhoneDataList.forEach(blackCaseUser -> {
            ShuheBlackPhoneTransferDataDTO shuheBlackPhoneTransferDataDTO = new ShuheBlackPhoneTransferDataDTO();
            if (!iShuheBlackPhoneRecordService.isRepeatPhone(blackCaseUser.getCell(), todayDate.toString())) {
                shuheBlackPhoneTransferDataDTO.setPhone(blackCaseUser.getMobile());
                shuheBlackPhoneTransferDataDTO.setApiCode(blackCaseUser.getApiCode());
                shuheBlackPhoneTransferDataDTO.setPushDate(todayDate.toString());
                shuheBlackPhoneTransferDataDTO.setCustNum(blackCaseUser.getCustNum());
                shuheBlackPhoneTransferDataDTOList.add(shuheBlackPhoneTransferDataDTO);
            }
            ;
        });
        //调用电销接口
        artificialShuHeBlackPushTransferHandler.call(shuheBlackPhoneTransferDataDTOList, null);

    }
}


