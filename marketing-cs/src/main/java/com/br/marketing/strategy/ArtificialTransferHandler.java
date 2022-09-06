package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleTransferInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
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
 * @Description : 人工转化接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:11
 */
@Slf4j
@Service
public class ArtificialTransferHandler extends AbstractExternalInterfaceHandler<DassAssembleTransferDataDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private PhoneSaleTransferInfoMapper phoneSaleTransferInfoMapper;

    @Override
    public JSONObject call(List<DassAssembleTransferDataDTO> transferData, ProcessHandlerContext context) {
        /**
         * 电销转化接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferData.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<DassAssembleTransferDataDTO> subList;
            if (i == pageCount) {
                subList = transferData.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferData.subList((i - 1) * pageSize, pageSize * (i));
            }
            DassTransferDataAdapDTO dassTransferDataAdapDTO = new DassTransferDataAdapDTO();
            List<DassTransferDataDTO> dataDTOS = subList.stream()
                    .map(DassAssembleTransferDataDTO::getDassTransferDataDTO).collect(Collectors.toList());
            dassTransferDataAdapDTO.setDassTransferDataDTOList(dataDTOS);
            List<PhoneSaleTransferInfo> phoneSaleTransferInfoList = subList.stream()
                    .map(DassAssembleTransferDataDTO::getPhoneSaleTransferInfo)
                    .filter(phoneSaleTransferInfo -> !ObjectUtils.isEmpty(phoneSaleTransferInfo))
                    .collect(Collectors.toList());
            if (phoneSaleTransferInfoList.size() > 0) {
                phoneSaleTransferInfoMapper.insertBatch(phoneSaleTransferInfoList);
            }
            methodRetryHandlerService.callDassTransferData(dassTransferDataAdapDTO, 0);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_TRANSFER;
    }
}
