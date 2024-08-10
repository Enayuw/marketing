package com.br.marketing.service.rulecenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.service.rulecenter.IRuleTaskService;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleTaskByScoreServiceImpl implements IRuleTaskService {


    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;

    @Resource
    XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    XiechengCollidingDataPackageRuleMapper xiechengCollidingDataPackageRuleMapper;

    @Resource
    XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;



    @Override
    public Result<PushViewVO> pushPreview(PushCustomerDTO dto) {
        return getTotal(dto);
    }


    @Override
    public PushCustomerDTO buildPreviewDTO(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition) {
        PushCustomerDTO pushCustomerDTO = new PushCustomerDTO();
        pushCustomerDTO.setApiCode(main.getmApiCode());
        pushCustomerDTO.setBatchNumberList(Arrays.stream(main.getmCusBatchNumberList().split(","))
                .collect(Collectors.toList()));
        List<Long> collect = Arrays.stream(scoreSearchCondition.getSourceCondition().split(","))
                .map(t -> Long.valueOf(t)).collect(Collectors.toList());
        pushCustomerDTO.setFileIdList(collect);
        pushCustomerDTO.setmRuleCondition(scoreSearchCondition.getContent());
        pushCustomerDTO.setmRuleConditionShow(scoreSearchCondition.getContentShow());
        return pushCustomerDTO;
    }

    private Result<PushViewVO> getTotal(PushCustomerDTO dto) {
        int total;
        PushViewVO pushViewVO = new PushViewVO();
        if (isXieChengData(dto)) {
            total = getXieChengDataNum(dto.getmRuleCondition(), dto.getBatchNumberList(), pushViewVO);
        } else {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(dto.getApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
            queryBaseBean.setFileIds(Joiner.on(",").join(dto.getFileIdList()));
            queryBaseBean.setJsonData(dto.getmRuleCondition());
            if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
            }
            if (dto.getmPlanNum() != null && dto.getmPlanNum() > 0) {
                queryBaseBean.setAmountTop("0,".concat(dto.getmPlanNum().toString()));
            }
            total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        }
        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }
        if (dto.getmPercentage() != null) {
            if (dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
            }
            Integer res = dto.getmPercentage().multiply(new BigDecimal(total)).setScale(0, RoundingMode.UP).intValue();
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }
        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    private  Boolean isXieChengData(PushCustomerDTO dto) {
        Boolean isXieCheng = Boolean.FALSE;
        JSONArray datas = JSON.parseObject(dto.getmRuleCondition()).getJSONArray("data");
        if (!CollectionUtils.isEmpty(datas)) {
            Object result = datas.stream().filter(obj ->("result").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            //api_code为携程且筛选条件传入result
            if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(dto.getApiCode()) && (!ObjectUtils.isEmpty(result))) {
                isXieCheng = Boolean.TRUE;
            }
        }
        return isXieCheng;

    }


    private int getXieChengDataNum(String mRuleCondition, List<String> batchNumberList, PushViewVO pushViewVO) {
        int total = 0;
        String querySql = "";
        JSONObject jsonObject = JSON.parseObject(mRuleCondition);
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        pushViewVO.setResult(collidingFilterDTO.getResult());
        if ("true".equals(collidingFilterDTO.getResult())) {
            querySql = cycleDataQuery(jsonObject, batchNumberList, collidingFilterDTO);
        } else {
            querySql = falseDataQuery(jsonObject, batchNumberList, collidingFilterDTO.getCleanTime());
        }
        log.warn("规则中心携程={} 的试算量级sql={}",collidingFilterDTO.getResult(),querySql);
        // 查询Doris
        try {
            total = scoreRecordMapper.getXieChengDataNumdoris_(querySql);
        } catch (Exception e) {
            log.error("规则中心-携程撞库筛选查询Doris异常,sql={}", querySql, e);
        }
        return total;
    }

    private String cycleDataQuery(JSONObject jsonObject, List<String> batchNumberList, XieChengCollidingFilterDTO xieChengCollidingFilterDTO) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time< DATE_ADD(CURDATE(), INTERVAL 7 DAY) and is_delete=0";
        //True关联查询
        //true筛选字段处理
        String condition =XieChengEsJsonHandler.zkTrueCondition(xieChengCollidingFilterDTO);
        if(StringUtils.isNotEmpty(condition)){
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + condition
                    + " and is_delete=0";
        }
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(cycleSql).append(") cycle inner join (").append(scoreSql).append(") score on " +
                "score.cell = cycle.cell;");
        return cycleAndscoreSql.toString();
    }

    private String falseDataQuery(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        StringBuilder querySql = new StringBuilder();
        String condition = falseDataCondition(jsonObject, batchNumberList, cleanTime);
        querySql.append("select count(1) from (").append(condition).append(") a ;");
        return querySql.toString();

    }

    private String falseDataCondition(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
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
    private String scoreSql(JSONObject jsonObject, List<String> batchNumberList) {

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


    @Override
    public RuleCenterDataSourceEnum sourceLabel() {
        return RuleCenterDataSourceEnum.SCORE;
    }
}
