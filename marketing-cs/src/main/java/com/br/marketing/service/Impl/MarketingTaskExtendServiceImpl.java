package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskExtendExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MarketingTaskExtendServiceImpl implements MarketingTaskExtendService {

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Override
    public MarketingTaskExtend getMarketingTaskExtend(Long taskId) {
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andIsDelEqualTo(Integer.valueOf(1)).andTaskIdEqualTo(taskId);
        List<MarketingTaskExtend> extendList = marketingTaskExtendMapper.selectByExample(extendExample);
        if(extendList.size()>0){
            return extendList.get(0);
        }
        return null;
    }

    @Override
    public List<String> getProducts(String ids) {
        List<String> list = new ArrayList<>();
        String[] split = ids.split(",");
        for(String id : split){
            StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(Long.parseLong(id));
            String batchNumber = straHisFile.getBatchNumber();
            if(batchNumber!=null){
                MarketingTaskExtend taskExtend = marketingTaskExtendMapper.getProducts(batchNumber);
                String extendShowTitle = taskExtend.getExtendShowTitle();
                String strategyProductJson = taskExtend.getStrategyProductJson();
                if(strategyProductJson!=null && !"".equals(strategyProductJson)){
                    StrategyProductDetailVO strategyProductDetailVO= JSONObject.parseObject(strategyProductJson,StrategyProductDetailVO.class);
                    List<String> fields = strategyProductDetailVO.getFields();
                    if(fields!=null && fields.size()>0){
                        list.addAll(fields);
                    }
                }
                if(extendShowTitle!=null && !"".equals(extendShowTitle)){
                    BaseHeadConfigVO baseHeadConfigVO= JSONObject.parseObject(extendShowTitle,BaseHeadConfigVO.class);
                    List<String> showBaseHead = baseHeadConfigVO.getShowBaseHead();
                    if(showBaseHead!=null && showBaseHead.size()>0){
                        list.addAll(showBaseHead);
                    }
                }
            }else {
                log.info("stra_his_file表获取batch_number为空！");
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }
}
