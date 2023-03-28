package com.br.marketing.check.service.Impl;


import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.check.service.RongShuIbuCycleService;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.ibu.IbuAdapDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.RongshuCycleDataMapper;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificalIbuHandler;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 榕树周期性数据推送电销 业务实现
 *
 * @author Lizhen
 * @dateTime 2023/02/14 14:32
 */
@Service
@Slf4j
public class RongShuIbuCycleServiceImpl implements RongShuIbuCycleService {

    @Autowired
    private IRongShuPushDaasService iRongShuPushDaasService;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RongshuCycleDataMapper rongshuCycleDataMapper;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private ArtificalIbuHandler artificalIbuHandler;


    @Override
    public void pushCycleDataToDaas() {
        List<Integer> dayList = marketingCommonConfig.getRongShuCyclePushDays();
        List<String> pushDateList = Lists.newArrayList();
        dayList.forEach(day -> {
            pushDateList.add(LocalDate.now().minusDays(day).toString());
        });
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        while (mark) {
            List<RongshuCycleData> rongshuCycleDataList = rongshuCycleDataMapper.getCycleData(pushDateList, minId);
            if (CollectionUtils.isEmpty(rongshuCycleDataList)) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = rongshuCycleDataList.get(rongshuCycleDataList.size() - 1).getId() + 1;
            String tcId = tableCreateService.getTcId(rongshuCycleDataList.get(0).getApiCode());
            String cId = tableCreateService.getCId(rongshuCycleDataList.get(0).getApiCode());
            String nowDate = LocalDate.now().toString();
            List<IbuAdapDTO> ibuAdapDTOList = new ArrayList<>();
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(rongshuCycleDataList.get(0).getApiCode());
            rongshuCycleDataList.forEach(rongshuCycleData -> {
                //进行条件剔除
                if (iRongShuPushDaasService.isFilter(rongshuCycleData.getApiCode(), rongshuCycleData.getCustNum(), tcId)) {
                    return;
                }
                IbuAdapDTO ibuAdapDTO = new IbuAdapDTO();
                PhoneSaleExtendInfo extendInfo = phoneSaleExtendInfoMapper.selectByPrimaryKey(rongshuCycleData.getPhoneExtendId());
                if (ObjectUtils.isEmpty(extendInfo)) {
                    log.warn("榕树周期性推送人工IBU未查到电销扩展表数据，cycleDataId={},extendId={}", rongshuCycleData.getId(), rongshuCycleData.getPhoneExtendId());
                    return;
                }
                String cell = BrCipherMaker.getInstance().decode(extendInfo.getCell());
                //构造推人工IBU
                IbuReqDTO.Datum datum = JSONObject.parseObject(extendInfo.getRedundancyField(), IbuReqDTO.Datum.class);
                //构造PhoneSaleExtendInfo
                extendInfo.setAppletDate(nowDate);
                extendInfo.setCreateTime(new Date());
                extendInfo.setUpdateTime(new Date());
                //推电销时间置为null
                extendInfo.setPushDxTime(null);
                extendInfo.setStatus("b");
                extendInfo.setInterfaceType((marketingCommonConfig.getRongShuPushNewIbuSwitch() != null && !marketingCommonConfig.getRongShuPushNewIbuSwitch())
                        ? 2 : 1);
                if (StringUtils.isBlank(datum.getGoalsApp())) {
                    datum.setGoalsApp("01");
                }
                //构造推客服数据
                ConversionData conversionData = new ConversionData();
                conversionData.setCid(cId);
                conversionData.setDataId(extendInfo.getSourceId().toString());
                conversionData.setExpireDate(marketingCommonConfig.getRsTransferDataToCustomerExpireDate());
                conversionData.setInversionStatus("0");
                conversionData.setPhone(cell);
                conversionData.setCaseNum(extendInfo.getCustNum());
                conversionData.setPartnerProcessDate(StringUtils.isNotEmpty(extendInfo.getAppletTime()) ? extendInfo.getAppletTime() :
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                ibuAdapDTO.setDatum(datum);
                ibuAdapDTO.setConversionData(conversionData);
                ibuAdapDTO.setPhoneSaleExtendInfo(extendInfo);
                ibuAdapDTO.setPushType("b");
                ibuAdapDTOList.add(ibuAdapDTO);
            });
            //推送
            artificalIbuHandler.call(ibuAdapDTOList, context);
        }
        log.warn("榕树周期性推送人工IBU接口完成");
    }
}
