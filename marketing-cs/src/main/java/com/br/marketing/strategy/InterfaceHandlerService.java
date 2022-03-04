package com.br.marketing.strategy;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.InterfaceParams;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class InterfaceHandlerService {

    @Resource
    private InterfaceHandlerFactory interfaceHandlerFactory;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    /**
     *  处理数据流向
     *  1、根据原始表id，查询该批次中传送数据
     *  2、遍历所有数据，按照不同调用接口逻辑将数据分类
     *  3、不同数据调用不同的接口处理
     */

    public Result<Boolean> handleDataDirection(long infoId){

        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);

        try {
            // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
            List<MarketingTransferInfo> list = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(infoId);
            MarketingTransferInfo transferInfo = list.get(0);
            transferInfo.setId(infoId);


            /**
             * 2  遍历数据 根据客户apiCode 及原始详情表数据封装到 map <具体的接口枚举,接口所需对应的参数类列表>
             *     如 { 1:List<BlackListDTO>,4:List<ConversionData>}
             */

            String tcId = tableCreateService.getTcId(transferInfo.getApiCode());
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).
                    andRequestIdEqualTo(transferInfo.getRequestId());
            example.settCid(tcId);
            List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
            ;
            Map<Integer, List<InterfaceParams>> map =
                    interfaceHandlerFactory.assembleData(transferInfo.getApiCode(), transferList);

            /**
             *  3  根据2获取的map key -> 具体的三方接口，value -> 三方接口入参
             */
            Set<Integer> set = map.keySet();
            for (Integer enumFlag : set) {
                interfaceHandlerFactory.handler(enumFlag,map.get(enumFlag), transferInfo);
            }


        } catch (Exception e) {
            log.error("通用转化逻辑处理数据 infoId:{} 失败 -- ",infoId,e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

}
