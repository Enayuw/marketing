package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.ShuheBlackPhoneTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.ShuheBlackPhoneRecord;
import com.br.marketing.service.IShuheBlackPhoneRecordService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
/**
 * @Description : 数禾黑名单数据推人工转化
 * @Author : lizhen
 * @Date : Create in 2022/05/28 16:11
 */
public class ArtificialShuHeBlackPushTransferHandler extends AbstractExternalInterfaceHandler<ShuheBlackPhoneTransferDataDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Autowired
    private IShuheBlackPhoneRecordService iShuheBlackPhoneRecordService;

    private static final String ORGNAME = "orgname";
    private static final String SOURCE = "source";
    private static final String TYPE = "type";
    private static final String USER_TYPE = "user_type";

    @Override
    public JSONObject call(List<ShuheBlackPhoneTransferDataDTO> transferData, ProcessHandlerContext context) {
        /**
         * 电销转化接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferData.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<ShuheBlackPhoneTransferDataDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferData.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferData.subList((i - 1) * pageSize, pageSize * (i));
            }
            List<Map<String, String>> listType = getShuHeType();
            for (Map<String, String> typeMap : listType) {
                List<DassTransferDataDTO> dassTransferDataDTOList = copyList(subList, DassTransferDataDTO::new);
                List<ShuheBlackPhoneRecord> shuheBlackPhoneRecordList = copyList(subList, ShuheBlackPhoneRecord::new);
                dassTransferDataDTOList.forEach(
                        dassTransferDataDTO -> {
                            dassTransferDataDTO.setType(typeMap.get(TYPE));
                            dassTransferDataDTO.setSource(typeMap.get(SOURCE));
                            dassTransferDataDTO.setUserType(typeMap.get(USER_TYPE));
                            dassTransferDataDTO.setTransformStatus("3");
                            dassTransferDataDTO.setOrgName(typeMap.get(ORGNAME));
                        }
                );
                shuheBlackPhoneRecordList.forEach(
                        shuheBlackPhoneRecord -> {
                            shuheBlackPhoneRecord.setPhone(BrCipherMaker.getInstance().encode(shuheBlackPhoneRecord.getPhone()));
                            shuheBlackPhoneRecord.setCreateTime(new Date());
                            shuheBlackPhoneRecord.setType(typeMap.get(TYPE));
                            shuheBlackPhoneRecord.setSource(typeMap.get(SOURCE));
                            shuheBlackPhoneRecord.setUserType(typeMap.get(USER_TYPE));
                            shuheBlackPhoneRecord.setOrgname(typeMap.get(ORGNAME));
                            shuheBlackPhoneRecord.setPStatus(1);
                        }
                );
                DassTransferDataAdapDTO dassTransferDataAdapDTO = new DassTransferDataAdapDTO();
                dassTransferDataAdapDTO.setDassTransferDataDTOList(dassTransferDataDTOList);
                iShuheBlackPhoneRecordService.saveBatch(shuheBlackPhoneRecordList);
                methodRetryHandlerService.callDassTransferData(dassTransferDataAdapDTO, 0);
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_SHUHE_BLACK_DATA;
    }

    private List<Map<String, String>> getShuHeType() {
        Map<String, String> fujieMap = ImmutableMap.of(ORGNAME, "shuhefujie", SOURCE, "16", USER_TYPE, "1", TYPE, "4");
        Map<String, String> shenwanMap = ImmutableMap.of(ORGNAME, "shuheshenwan", SOURCE, "16", USER_TYPE, "2", TYPE, "2");
        Map<String, String> shoujieMap = ImmutableMap.of(ORGNAME, "shuheshoujie", SOURCE, "18", USER_TYPE, "1", TYPE, "3");
        Map<String, String> shoujieTwoMap = ImmutableMap.of(ORGNAME, "shuheshoujie", SOURCE, "18", USER_TYPE, "1", TYPE, "4");
        return Lists.newArrayList(fujieMap, shenwanMap, shoujieMap, shoujieTwoMap);
    }

    public static <S, T> List<T> copyList(List<S> sources, Supplier<T> target) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            BeanUtils.copyProperties(source, t);
            list.add(t);
        }
        return list;
    }
}
