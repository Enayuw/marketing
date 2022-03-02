package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskExtendExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    public Map getProducts(String ids) {
        Map map = new HashMap();
        List<String> baseHeadList = new ArrayList<>();
        baseHeadList.add("request_time");
        baseHeadList.add("strategy_id");
        baseHeadList.add("cus_num");
        List<String> fieldsList = new ArrayList<>();
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
                        fieldsList.addAll(fields);
                    }
                }
                if(extendShowTitle!=null && !"".equals(extendShowTitle)){
                    BaseHeadConfigVO baseHeadConfigVO= JSONObject.parseObject(extendShowTitle,BaseHeadConfigVO.class);
                    List<BaseHead> baseHead = baseHeadConfigVO.getBaseHead();
                    if(baseHead!=null && baseHead.size()>0){
                        for(BaseHead single : baseHead){
                            String convert = ifConvert(single.getName());
                            baseHeadList.add(convert);
                        }
                    }
                }
            }else {
                log.info("stra_his_file表获取batch_number为空！");
            }
        }
        map.put("showBaseHead",baseHeadList.stream().distinct().collect(Collectors.toList()));
        map.put("fields",fieldsList.stream().distinct().collect(Collectors.toList()));
        return map;
    }

    public String ifConvert(String s){
        String lowerCase = s.toLowerCase();
        //usertype->user_type,idcard->id_card,strategyId->strategy_id，taskid->task_id
        switch (lowerCase){
            case "usertype": return "user_type";
            case "idcard": return "id_card";
            case "id": return "id_card";
            case "strategyId": return "strategy_id";
            case "taskid": return "task_id";
            default:return s;
        }
    }
}
