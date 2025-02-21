package com.br.marketing.service.mark.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.mark.DataMarkCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DataMarkCommonServiceImpl implements DataMarkCommonService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;


    @Override
    public StraHisFile getStraHisFile(String apiCode) {
        Date beginTimeOfDay = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(2)
                .andTypeEqualTo(2)
                .andCreateTimeGreaterThanOrEqualTo(beginTimeOfDay);
        straHisFileExample.setOrderByClause("create_time desc limit 1");
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        if (CollectionUtils.isEmpty(straHisFiles)) {
            return null;
        }
        return straHisFiles.get(0);
    }

    @Override
    public List<MarketingHistory> getScoreWithEs(String apiCode, String batchNumber, Long id, List<String> cellLogs, Integer esPageSize) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(id.toString());
        JSONObject cellCondition = new JSONObject();
        cellCondition.put("type", "operation");
        cellCondition.put("key", "cell");
        cellCondition.put("operation", "in");
        cellCondition.put("value", cellLogs);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(cellCondition);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", jsonArray);
        queryBaseBean.setJsonData(jsonObject.toString());
        queryBaseBean.setPageSize(esPageSize);
        return marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
    }
}
