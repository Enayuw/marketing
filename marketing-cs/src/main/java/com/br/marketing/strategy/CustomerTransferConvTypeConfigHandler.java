package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.ConvTypeConfigConversionData;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * @Description : 推送客服转化Handler(支持一个apicode分发到多个apicode)
 * ---------------------------------
 * @Author : hong.chen
 * @Date : Create in 2023/7/4 18:03
 */

@Service
@Slf4j
public class CustomerTransferConvTypeConfigHandler extends AbstractExternalInterfaceHandler<ConversionData> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 客服标准接口 每500条数据一个批次
     */
    @Override
    public JSONObject call(List<ConversionData> transferList, ProcessHandlerContext context) {
        // 将转化数据根据convType分组
        Map<String, List<ConvTypeConfigConversionData>> groupByConvDataList =
                transferList.parallelStream().filter(ConvTypeConfigConversionData.class::isInstance)
                        .map(ConvTypeConfigConversionData.class::cast)
                        .collect(Collectors.groupingBy(t -> t.getConvType()));

        Set<Map.Entry<String, Object>> entries = marketingCommonConfig.getPushConvTypeConfig().get(context.getApiCode()).entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String convType = entry.getKey();
            List<ConvTypeConfigConversionData> dataList = groupByConvDataList.get(convType);
            // 没有配置数据对应的convType，则不推送客服
            if (CollectionUtils.isEmpty(dataList)) {
                continue;
            }

            // 推送客服之前移除convType
            List<ConversionData> pushDataList = dataList.stream().map((ConversionData data) -> {
                ConversionData pushData = new ConversionData();
                BeanUtils.copyProperties(data, pushData);
                return pushData;
            }).collect(Collectors.toList());

            // 获取该convType下配置的apicode集合
            List<String> pushApiCodes = (List<String>) entry.getValue();
            batchData(context, pushApiCodes, pushDataList);
        }

        return null;
    }

    private void batchData(ProcessHandlerContext context, List<String> pushApiCodes, List<ConversionData> pushDataList) {
        // 每500条数据一个批次
        int pageSize = 500;
        int totalCount = pushDataList.size();
        int pageCount = totalCount % pageSize == 0 ? (totalCount / pageSize) : totalCount / pageSize + 1;
        String last = context.getLast();
        String lastRep;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList;

            if (i == pageCount) {
                subList = pushDataList.subList((i - 1) * pageSize, totalCount);
                lastRep = last;
            } else {
                subList = pushDataList.subList((i - 1) * pageSize, pageSize * (i));
                lastRep = last != null ? "0" : null;
            }

            assembleAndPushData(context, pushApiCodes, lastRep, subList);
        }
    }

    private void assembleAndPushData(ProcessHandlerContext context, List<String> pushApiCodes, String lastRep, List<ConversionData> subList) {
        // 同一批次数据，推送多个apiCode
        for (String pushApiCode : pushApiCodes) {
            TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList, lastRep));
            robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());
            robotOutboundDTO.setApiCode(pushApiCode);
            methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, 0);
        }
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_BY_CONVTYPE;
    }
}
