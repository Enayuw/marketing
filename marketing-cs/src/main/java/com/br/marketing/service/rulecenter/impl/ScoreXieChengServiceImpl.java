package com.br.marketing.service.rulecenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleExample;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScoreXieChengServiceImpl {

    @Resource
    XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    XiechengCollidingDataPackageRuleMapper xiechengCollidingDataPackageRuleMapper;

    @Resource
    XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    public Boolean isXieCheng(String apiCode,String condition){
        Boolean isXieCheng = Boolean.FALSE;
        JSONArray datas = JSON.parseObject(condition).getJSONArray("data");
        if (!CollectionUtils.isEmpty(datas)) {
            Object result = datas.stream().filter(obj ->("result").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            //api_code为携程且筛选条件传入result
            if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(apiCode) && (!ObjectUtils.isEmpty(result))) {
                isXieCheng = Boolean.TRUE;
            }
        }
        return isXieCheng;
    }


    public int getXieChengDataNum(String mRuleCondition, List<String> batchNumberList, PushViewVO pushViewVO) {
        int total = 0;
        String querySql = "";
        List<String> querySqls = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(mRuleCondition);
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        pushViewVO.setResult(collidingFilterDTO.getResult());
        if ("true".equals(collidingFilterDTO.getResult())) {
            Boolean xcTruePushCustomerPushPreviewOptFlag = marketingCommonConfig.getXcTruePushCustomerPushPreviewOptFlag();
            if (xcTruePushCustomerPushPreviewOptFlag) {
                cycleDataQueryOpt(jsonObject, batchNumberList, collidingFilterDTO, querySqls);
            } else {
                querySql = cycleDataQuery(jsonObject, batchNumberList, collidingFilterDTO);
            }
        } else {
            querySql = falseDataQuery(jsonObject, batchNumberList, collidingFilterDTO.getCleanTime());
        }
        log.warn("规则中心携程={} 的试算量级sql={}",collidingFilterDTO.getResult(),querySql);
        // 查询Doris
        try {
            if (CollectionUtils.isEmpty(querySqls)) {
                log.warn("规则中心携程={} 的试算量级sql={}", collidingFilterDTO.getResult(), querySql);
                total = scoreRecordMapper.getXieChengDataNumdoris_(querySql);
            } else {
                log.warn("规则中心携程={} 的试算量级样例sql={}", collidingFilterDTO.getResult(), querySqls.get(0));
                total = getTotalOpt(querySqls, marketingCommonConfig.getXcFalsePackageOptSoleNum());
            }
        } catch (Exception e) {
            if (CollectionUtils.isEmpty(querySqls)) {
                log.error("规则中心-携程撞库筛选查询Doris异常,sql={}", querySql, e);
            } else {
                log.error("规则中心-携程撞库筛选查询Doris异常,sqls={}", String.join("", querySqls), e);
            }
        }
        return total;
    }

    /**
     * @param querySqls
     * @param xcFalsePackageOptSoleNum
     * @return int
     * @description 多线程获取量级
     * @author hedongshuo
     * @date 2025/1/7 16:01
     **/
    private int getTotalOpt(List<String> querySqls, Integer xcFalsePackageOptSoleNum) {
        AtomicInteger batchCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(xcFalsePackageOptSoleNum, xcFalsePackageOptSoleNum);
        for (String sql : querySqls) {
            futures.add(CompletableFuture.runAsync(() -> {
                batchCount.addAndGet(scoreRecordMapper.getXieChengDataNumdoris_(sql));
            }, threadPool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程自动生成决策任务量级预览线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程自动生成决策任务量级预览线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
        return batchCount.get();
    }

    /**
     * 跑分数据和周期表数据交集量级预览
     *
     * @param jsonObject
     * @param batchNumberList
     * @param collidingFilterDTO
     * @param querySqls
     */
    private void cycleDataQueryOpt(JSONObject jsonObject, List<String> batchNumberList, XieChengCollidingFilterDTO collidingFilterDTO, List<String> querySqls) {
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time< DATE_ADD(CURDATE(), INTERVAL 7 DAY) and is_delete=0";
        //True关联查询
        //true筛选字段处理
        String condition = XieChengEsJsonHandler.zkTrueCondition(collidingFilterDTO);
        if (StringUtils.isNotEmpty(condition)) {
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + condition
                    + " and is_delete=0";
        }
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        for(String batchNumber : batchNumberList){
            if(StringUtils.isEmpty(batchNumber)){
                continue;
            }
            String scoreSql = "select id,cell from b_xiecheng_colliding_".concat(batchNumber)
                    .concat(" where ").concat(sqlCondition).concat(" and is_delete=0");
            StringBuilder cycleAndScoreSql = new StringBuilder();
            cycleAndScoreSql.append("select count(1) from (").append(scoreSql).append(") score inner join (").append(cycleSql).append(") cycle on " +
                    "score.cell = cycle.cell;");
            querySqls.add(cycleAndScoreSql.toString());
        }
    }

    public String cycleDataQuery(JSONObject jsonObject, List<String> batchNumberList, XieChengCollidingFilterDTO xieChengCollidingFilterDTO) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time< DATE_ADD(CURDATE(), INTERVAL 7 DAY) and is_delete=0";
        //True关联查询
        //true筛选字段处理
        String condition = XieChengEsJsonHandler.zkTrueCondition(xieChengCollidingFilterDTO);
        if(StringUtils.isNotEmpty(condition)){
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + condition
                    + " and is_delete=0";
        }
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(scoreSql).append(") score inner join (").append(cycleSql).append(") cycle on " +
                "score.cell = cycle.cell;");
        return cycleAndscoreSql.toString();
    }

    public String falseDataQuery(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        StringBuilder querySql = new StringBuilder();
        String condition = falseDataCondition(jsonObject, batchNumberList, cleanTime);
        querySql.append("select count(1) from (").append(condition).append(") a ;");
        return querySql.toString();

    }

    public String falseDataCondition(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        String cycleDataSql = "select  cell_sha256_code_list as cell,id from  b_xiecheng_colliding_data_loop_cycle where is_delete =0";
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        StringBuilder falseAndscoreSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        //与True的全量数据去重
        falseAndscoreSql.append("select score.cell,score.id from (").append(scoreSql).append(") score left join (").append(cycleDataSql)
                .append(") cycle on score.cell = cycle.cell ");
        //where条件拼接
        whereSql.append(" where cycle.id is null");
        if (StringUtils.isNotEmpty(cleanTime)) {
            XiechengCollidingDataPackageRuleExample packageRuleExample = new XiechengCollidingDataPackageRuleExample();
            packageRuleExample.createCriteria().andCollidingEndTimeGreaterThanOrEqualTo(DateHelper.parseDate(cleanTime)).andIsDeleteEqualTo(0);
            List<XiechengCollidingDataPackageRule> packageRules = xiechengCollidingDataPackageRuleMapper.selectByExample(packageRuleExample);
            String packageId = packageRules.stream().map(xiechengCollidingDataPackageRule -> xiechengCollidingDataPackageRule.getPackageId()
                    .toString()).collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
            //清洗时间在撞库区间内去重，业务应规避此条件
            if (StringUtils.isNotEmpty(packageId)) {
                String FalseDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_rob where package_id in (" +
                        packageId + ") and " + "is_delete=0";
                falseAndscoreSql.append("left join (").append(FalseDataSql).append(") rob on score.cell = rob.cell ");
                whereSql.append(" and rob.id is null");
            }
        }
        //与待清洗去重
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria().andTaskTypeEqualTo(0).andTaskStatusEqualTo(0).andIsDeleteEqualTo(0);
        List<XiechengCollidingDataProcessTask> processTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(processTaskExample);
        processTasks.forEach((XiechengCollidingDataProcessTask processTask) -> {
            falseAndscoreSql.append(" left join (").append(processTask.getTaskExecutionSql()).append(") d").append(processTask.getId())
                    .append(" on score.cell = ").append("d").append(processTask.getId()).append(".cell ");
            whereSql.append(" and  d").append(processTask.getId()).append(".id is null");
        });
        return falseAndscoreSql.append(whereSql).toString();
    }

    /**
     * 组装跑分筛选SQL
     *
     * @param jsonObject      入参jsonsql
     * @param batchNumberList batchNumber集合
     * @return String
     */
    public String scoreSql(JSONObject jsonObject, List<String> batchNumberList) {

        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(sqlCondition).concat(" and is_delete=0 ");
            } else {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(sqlCondition).concat(" and is_delete=0 ").concat(" union all ");
            }

        }
        return scoreSql;
    }
}
