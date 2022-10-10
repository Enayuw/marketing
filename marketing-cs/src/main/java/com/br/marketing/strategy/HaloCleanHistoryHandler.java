package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.HaloHistoryCleanService;
import com.br.marketing.service.MarketingSyncReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 哈啰历史数据清洗执行器
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.strategy
 * @Description: 哈啰历史数据清洗执行器
 * @CreateTime: 2022-07-01 13 :41
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Service
@Slf4j
public class HaloCleanHistoryHandler {


    @Autowired
    private MarketingSyncReportService marketingSyncReportService;

    @Autowired
    private MarketingSyncReportService syncReportService;

    @Autowired
    private HaloHistoryCleanService haloHistoryCleanService;
    public Result<Boolean> haluoCleanHistory(String mes) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        //更新数据
        log.warn("开始执行数据更新任务,消息内容={}", mes);

        try {
            Integer integer = haloHistoryCleanService.handlerCleanHistory(mes);
            if(integer>0){
                // 删除统计数据
                log.warn("开始执行删除统计数据任务,消息内容={}", mes);
                marketingSyncReportService.deleteReportByAppletDate(mes);
                // 重新生成统计数据
                log.warn("开始执行重新生成统计数据任务,消息内容={}", mes);
                this.syncReportProcess(mes);
            }
        } catch (Exception e) {
            log.warn("--------------------");
        } finally {
            return result;
        }

    }

    private void syncReportProcess(String mes) {
        JSONObject jsonObject = JSON.parseObject(mes);
        JSONArray dataArray = jsonObject.getJSONArray("dataArray");
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataJson = dataArray.getJSONObject(i);
                String appletDate = dataJson.getString("appletDate");
                String apiCode = dataJson.getString("apiCode");
                // 调用更新接口
                syncReportService.syncReportProcessByApiCode(appletDate, apiCode);
            }
        }
    }


}
