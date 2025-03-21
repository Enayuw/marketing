package com.br.marketing.check.service.Impl.tag;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.tag.MaterializedViewDTO;
import com.br.marketing.entity.tag.*;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.TagDataRuleCalculateMapper;
import com.br.marketing.mapper.tag.*;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签 处理service
 *
 * @author zhen.Li1
 * @dateTime 2025/03/17 17:32
 */
@Service
@Slf4j
public class TagHandlerServiceImpl implements TagHandleService {


    private static final Long LOCK_WAIT_THRESHOLD = 30000L;

    @Autowired
    private TagDataRuleMapper tagDataRuleMapper;

    @Autowired
    private TagDataRuleCalculateMapper tagDataRuleCalculateMapper;

    @Autowired
    private TagDataSourceConfigMapper tagDataSourceConfigMapper;

    @Autowired
    private TagDataSourceMappingMapper tagDataSourceMappingMapper;

    @Autowired
    private TagRuleSourceRelationMapper tagRuleSourceRelationMapper;

    @Autowired
    private TagDataFieldConfigMapper tagDataFieldConfigMapper;

    @Resource
    FlagDataMapper flagDataMapper;

    @Override
    public void calculateTagData() {
        TagDataRuleExample example = new TagDataRuleExample();
        example.createCriteria().andStatusEqualTo(1).andIsRepeatEqualTo(1);
        List<TagDataRule> tagDataRuleList = tagDataRuleMapper.selectByExample(example);
        TagDataSourceConfigExample sourceConfigExample = new TagDataSourceConfigExample();
        sourceConfigExample.createCriteria().andStatusEqualTo(1);
        List<TagDataSourceConfig> sourceConfigList = tagDataSourceConfigMapper.selectByExample(sourceConfigExample);

        TagDataFieldConfigExample fieldConfigExample = new TagDataFieldConfigExample();
        fieldConfigExample.createCriteria().andStatusEqualTo(1);
        List<TagDataFieldConfig> tagDataFieldConfigList = tagDataFieldConfigMapper.selectByExample(fieldConfigExample);
        String nowDay = LocalDate.now().toString();
        tagDataRuleList.forEach(tagDataRule -> {
            Integer status;
            Integer number = null;
            String tagCode = tagDataRule.getTagCode();
            //判断标签记录表是否存在
            List<TagDataRuleCalculate> tagDataRuleCalculateList = getTagCalculateRecord(tagCode, nowDay);
            if (!CollectionUtils.isEmpty(tagDataRuleCalculateList)) {
                return;
            }
            //存在，进入下次循环，不存在，插入记录
            Long recordId = saveTagCalculateRecord(tagDataRule.getTagCode(), nowDay);
            //标签运算
            if (tagCalculate(tagDataRule, sourceConfigList, tagDataFieldConfigList, nowDay)) {
                status = 2;
                String querySql = String.format("select count(1) from t_tag_data_detail where tag_code = '%S' and calculate_date ='%S'", tagCode, nowDay);
                number = tagDataRuleCalculateMapper.getCountbI_(querySql);
                //计算完成修改记录表状态
                updateTagCalculateRecord(recordId, status, number);
                //更新规则表量级
                tagDataRule.setTagNumber(number);
                tagDataRuleMapper.updateByPrimaryKeySelective(tagDataRule);
            }
        });
    }

    private Boolean tagCalculate(TagDataRule tagDataRule, List<TagDataSourceConfig> sourceConfigList, List<TagDataFieldConfig> tagDataFieldConfigList,
                                 String nowDay) {
        Boolean calculateStatus = Boolean.FALSE;
        String tagCode = tagDataRule.getTagCode();
        try {
            //查看是否存在数据源配置
            List<String> sourceCodes = Arrays.asList(tagDataRule.getSourceCode().split(","));
            Collections.sort(sourceCodes);
            //圈选的ApiCode范围
            List<String> apiCodes = Arrays.asList(tagDataRule.getApiCodeScope().split(","));
            apiCodes.forEach(apiCode -> {
                Integer sourceType;
                String sourceName;
                if (sourceCodes.size() > 1) {
                    sourceType = 2;
                    sourceName = "view_".concat(apiCode).concat("_").concat(String.join("_", sourceCodes));
                } else {
                    sourceType = 1;
                    sourceName = sourceConfigList.stream().filter(sourceConfig -> sourceConfig.getSourceCode()
                            .equals(sourceCodes.get(0))).findFirst().get().getSourceName().replace("${apiCode}", apiCode);
                }
                //查询t_tag_data_source_mapping 是否存在，不存在插入，存在判断t_tag_rule_source_relation，存在 跳过，不存在插入
                TagDataSourceMappingExample sourceMappingExample = new TagDataSourceMappingExample();
                sourceMappingExample.createCriteria().andStatusEqualTo(1).andSourceNameEqualTo(sourceName).andSourceTypeEqualTo(sourceType);
                List<TagDataSourceMapping> tagDataSourceMappingList = tagDataSourceMappingMapper.selectByExample(sourceMappingExample);
                String sourceMappingCode = apiCode.concat("_").concat(LocalDate.now().toString()).concat(UUID.randomUUID().toString());
                if (CollectionUtils.isEmpty(tagDataSourceMappingList)) {
                    if (sourceCodes.size() > 1) {
                        Boolean viewIsSuccess = createDorisView(apiCode, sourceName, sourceCodes, sourceConfigList);
                        if (!viewIsSuccess) {
                            log.error("创建物化视图失败");
                        }
                    }
                    TagDataSourceMapping dataSourceMapping = new TagDataSourceMapping();
                    dataSourceMapping.setSourceMappingCode(sourceMappingCode);
                    dataSourceMapping.setSourceName(sourceName);
                    dataSourceMapping.setSourceType(sourceType);
                    dataSourceMapping.setApiCode(apiCode);
                    dataSourceMapping.setStatus(1);
                    dataSourceMapping.setCreateTime(new Date());
                    tagDataSourceMappingMapper.insertSelective(dataSourceMapping);
                } else {
                    sourceMappingCode = tagDataSourceMappingList.get(0).getSourceMappingCode();
                }
                //保存到t_tag_rule_source_relation 中
                TagRuleSourceRelationExample sourceRelationExample = new TagRuleSourceRelationExample();
                sourceRelationExample.createCriteria().andTagCodeEqualTo(tagCode).andSourceMappingCodeEqualTo(sourceMappingCode)
                        .andApiCodeEqualTo(apiCode).andStatusEqualTo(1);
                List<TagRuleSourceRelation> tagRuleSourceRelationList = tagRuleSourceRelationMapper.selectByExample(sourceRelationExample);
                if (CollectionUtils.isEmpty(tagRuleSourceRelationList)) {
                    TagRuleSourceRelation tagRuleSourceRelation = new TagRuleSourceRelation();
                    tagRuleSourceRelation.setApiCode(apiCode);
                    tagRuleSourceRelation.setTagCode(tagCode);
                    tagRuleSourceRelation.setSourceMappingCode(sourceMappingCode);
                    tagRuleSourceRelation.setStatus(1);
                    tagRuleSourceRelation.setCreateTime(new Date());
                    tagRuleSourceRelationMapper.insertSelective(tagRuleSourceRelation);
                }
                // 写入数据到doris
                insertDataDoris(sourceName, sourceType, sourceCodes, tagDataRule, tagDataFieldConfigList);
            });
            //同步数据到TiDB表
            String SyncTiDBSql = String.format("insert into jdbc_yf_tidb.marketing.t_tag_data_detail (tag_code,calculate_date,cell,cust_num,create_time," +
                    "update_time)  select tag_code,calculate_date,cell,cust_num,create_time,update_time from marketing.t_tag_data_detail where tag_code = '%S' and calculate_date ='%S'", tagCode, nowDay);
            flagDataMapper.insertbI_(SyncTiDBSql);
            calculateStatus = Boolean.TRUE;
        } catch (Exception e) {
            log.error(tagCode.concat(":标签计算异常"), e);

        }
        return calculateStatus;
    }

    private void insertDataDoris(String sourceName, Integer sourceType, List<String> sourceCodes,
                                 TagDataRule tagDataRule, List<TagDataFieldConfig> tagDataFieldConfigList) {
        Map<String, String> filedCodeMap =
                tagDataFieldConfigList.stream().collect(Collectors.toMap(TagDataFieldConfig::getFieldCode, TagDataFieldConfig::getSourceCode));
        String sourcecode = sourceCodes.get(0);
        StringBuilder insertBuilder = new StringBuilder();
        String cell;
        String custNum;
        String contiditionSql;
        String timeField;
        //基础表
        if (1 == sourceType) {
            cell = sourcecode.equals("CALL") ? "phone_num_encoded" : "cell";
            custNum = sourcecode.equals("CALL") ? "case_num" : "cust_num";
            timeField = sourcecode.equals("CALL") ? "case_log_create_time" : "create_time";
            contiditionSql = EsConditionTransferSqlUtil.jsonTransferSql(JSON.parseObject(tagDataRule.getContent()), "");
        } else {
            cell = sourcecode.equals("CALL") ? "CALL".concat("_").concat("phone_num_encoded") : "CALL".concat("_").concat("cell");
            custNum = sourcecode.equals("CALL") ? "CALL".concat("_").concat("case_num") : "CALL".concat("_").concat("cust_num");
            timeField = sourcecode.equals("CALL") ? "CALL".concat("_").concat("case_log_create_time") : "CALL".concat("_").concat("create_time");
            contiditionSql = EsConditionTransferSqlUtil.jsonTransferSqlByFillKey(JSON.parseObject(tagDataRule.getContent())
                    , "", filedCodeMap);
        }
        insertBuilder.append(String.format("insert into t_tag_data_detail(tag_code,calculate_date,cell,cust_num,create_time,update_time) " +
                        "SELECT \"%s\" AS tag_code, CURDATE() AS calculate_date, %s AS cell, %s AS cust_num, now(), now() from ".concat(sourceName),
                tagDataRule.getTagCode(), cell, custNum));
        insertBuilder.append(" where ");
        //TODO 时间范围
        String beforeDate = DateHelper.getPreviousDate(tagDataRule.getTimeUnit(), tagDataRule.getTimeNumber()).toString();
        insertBuilder.append(timeField).append(">=\"").append(beforeDate).append("\" and ");
        insertBuilder.append("(").append(contiditionSql).append(")");
        //插入数据到Doris表
        flagDataMapper.insertbI_(insertBuilder.toString());

    }

    private List<TagDataRuleCalculate> getTagCalculateRecord(String tagCode, String calculateDate) {
        TagDataRuleCalculateExample example = new TagDataRuleCalculateExample();
        TagDataRuleCalculateExample.Criteria criteria = example.createCriteria();
        criteria.andTagCodeEqualTo(tagCode)
                .andCalculateDateEqualTo(calculateDate);
        return tagDataRuleCalculateMapper.selectByExample(example);
    }


    public Long saveTagCalculateRecord(String tagCode, String calculateDate) {
        TagDataRuleCalculate tagDataRuleCalculate = new TagDataRuleCalculate();
        tagDataRuleCalculate.setTagCode(tagCode);
        tagDataRuleCalculate.setCalculateDate(calculateDate);
        tagDataRuleCalculate.setCreateTime(new Date());
        tagDataRuleCalculate.setUpdateTime(new Date());
        tagDataRuleCalculate.setStatus(1);
        tagDataRuleCalculateMapper.insertSelective(tagDataRuleCalculate);
        return tagDataRuleCalculate.getId();
    }


    private int updateTagCalculateRecord(Long Id, Integer status, Integer number) {
        TagDataRuleCalculate tagDataRuleCalculate = new TagDataRuleCalculate();
        tagDataRuleCalculate.setId(Id);
        tagDataRuleCalculate.setStatus(status);
        tagDataRuleCalculate.setTagNumber(number);
        return tagDataRuleCalculateMapper.updateByPrimaryKeySelective(tagDataRuleCalculate);
    }


    private Boolean createDorisView(String apiCode, String viewName, List<String> sourceCodes, List<TagDataSourceConfig> sourceConfigList) {
        Boolean isSuccess = Boolean.FALSE;
        StringBuilder viewSql = new StringBuilder();
        viewSql.append("create MATERIALIZED VIEW " + viewName +
                "BUILD IMMEDIATE\n" +
                "REFRESH AUTO\n" +
                "ON COMMIT\n" +
                "DISTRIBUTED BY RANDOM BUCKETS 2\n" +
                "PROPERTIES ('replication_num' = '2')  \n" +
                "AS\n" +
                "select ");
        StringBuilder tableSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        String relateStr = "";
        for (int i = 0; i < sourceCodes.size(); i++) {
            String sourceCode = sourceCodes.get(i);
            String sourceName = sourceConfigList.stream().filter(sourceConfig -> sourceConfig.getSourceCode()
                    .equals(sourceCodes.get(0))).findFirst().get().getSourceName().replace("${apiCode}", apiCode);
            List<String> fieldNameList = flagDataMapper.queryColumnNamebI_(sourceName);
            fieldNameList.forEach(field -> {
                viewSql.append(sourceCode).append(".").append(field).append(" as ").append(sourceCode).append("_").append(field).append(",");
            });
            if (i == 0) {
                tableSql.append(" from ").append(sourceName).append(" ").append(sourceCode);
                relateStr = sourceCode.concat(".").concat(sourceCode.equals("CALL") ? "phone_num_encoded" : "cell");
            } else {
                tableSql.append("FULL JOIN ").append(sourceName).append(" ").append(sourceCode).append(" on").append(sourceCode).append(".")
                        .append(sourceCode.equals("CALL") ? "phone_num_encoded" : "cell").append("=").append(relateStr);
            }
            whereSql.append(" and ").append(sourceCode).append(".").append(sourceCode.equals("CALL") ? "case_log_create_time" : "create_time").append(">=")
                    .append("2025-01-01 00:00:00");
        }
        //创建物化视图
        flagDataMapper.insertbI_(new StringBuilder(viewSql.substring(0, viewSql.length() - 1)).append(tableSql).append(whereSql).toString());

        long begin = System.currentTimeMillis();

        while (System.currentTimeMillis() - begin < LOCK_WAIT_THRESHOLD) {
            MaterializedViewDTO materializedView = tagDataRuleCalculateMapper.getMViewInfobI_(viewName);
            if ("NORMAL".equals(materializedView.getState()) && "SUCCESS".equals(materializedView.getRefreshState()) && "1".equals(materializedView.getSyncWithBaseTables())) {
                isSuccess = Boolean.TRUE;
                break;
            }
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return isSuccess;

    }

}
