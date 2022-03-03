package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.PushRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
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
 *
 * @Description : 客服转化接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:03
 */

@Service
public class CustomerTransferHandler extends AbstractExternalInterfaceHandler<ConversionData> {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private PushRuleService ruleService;

    @Override
    public JSONObject call(List<ConversionData> transferList, MarketingTransferInfo transferInfo) {

        Set<String> set = transferList.stream().map(ConversionData::getCaseNum).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(transferInfo.getApiCode(), set);
        Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, syncUser -> syncUser
                , (v1, v2) -> StringUtils.isNotBlank(v2.getCell()) && !ObjectUtils.isEmpty(v2.getCreateTime())
                        && v2.getCreateTime().after(v1.getCreateTime()) ? v2 : v1));
        for (ConversionData conversionData : transferList) {
            if (map.containsKey(conversionData.getCaseNum())) {
                MarketingSyncUser marketingSyncUser = map.get(conversionData.getCaseNum());
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
                conversionData.setTaskId(marketingSyncUser.getCusBatch());
            } else {
                conversionData.setPhone("");
                conversionData.setTaskId("");
            }
        }

        /**
         * 客服标准接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList = new ArrayList<>();
            TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
            }
            robotOutboundDTO.setApiCode(transferInfo.getApiCode());
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList));
            ruleService.pushTransferData(robotOutboundDTO, transferInfo);
        }
        return null;
    }

        @Override
        public InterfaceHandlerEnum handlerEnum () {
            return InterfaceHandlerEnum.CUSTOMER_TRANSFER;
        }
    }
