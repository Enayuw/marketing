package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 人工推电销批量接口
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/29 17:31
 */
@Service
public class ArtificialBatchRealTimeDataHandler extends AbstractExternalInterfaceHandler<BatchRealTimeUserDataDTO>{

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public JSONObject call(List<BatchRealTimeUserDataDTO> transferData, ProcessHandlerContext context) {
        /**
         * 批量人工推电销接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = transferData.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BatchRealTimeUserDataDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferData.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferData.subList((i - 1) * pageSize, pageSize * (i));
            }
            DassImportAdapDTO dassImportAdapDTO = new DassImportAdapDTO();
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());

            List<DassImportDataDTO> dataDTOS = subList.stream().map(batchData->batchData.getDassImportDataDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = subList.stream().map(batchData->batchData.getPhoneSaleExtendInfo())
                    .filter(item-> StringUtils.isNotEmpty(item)).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            if (!CollectionUtils.isEmpty(phoneSaleExtendInfos)){
                phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            }
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO,0);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA;
    }
}
