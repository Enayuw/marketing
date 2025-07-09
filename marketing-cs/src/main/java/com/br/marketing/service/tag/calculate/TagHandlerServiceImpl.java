package com.br.marketing.service.tag.calculate;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tag.MaterializedViewDTO;
import com.br.marketing.entity.tag.*;
import com.br.marketing.enums.SourceTypeEnum;
import com.br.marketing.enums.tag.DeleteFlagEnum;
import com.br.marketing.enums.tag.TagData;
import com.br.marketing.enums.tag.TagStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.TagDataRuleCalculateMapper;
import com.br.marketing.mapper.tag.*;
import com.br.marketing.service.tag.calculate.strategy.CallFieldStrategy;
import com.br.marketing.service.tag.calculate.strategy.ShortLinkFieldStrategy;
import com.br.marketing.service.tag.calculate.strategy.TransformFieldStrategy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.*;

/**
 * 标签 处理service
 *
 * @author zhen.Li1
 * @dateTime 2025/03/17 17:32
 */
@Service
@Slf4j
public class TagHandlerServiceImpl implements TagHandleService {


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

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public static final String TITLE = "[标签计算]";

    @Override
    public void calculateTagData() {
        TagDataRuleExample example = new TagDataRuleExample();
        example.createCriteria().andStatusEqualTo(TagStatusEnum.ENABLED.getCode()).andDeleteFlagEqualTo(DeleteFlagEnum.NOT_DELETED.getCode());
        List<TagDataRule> tagDataRuleList = tagDataRuleMapper.selectByExample(example);
        TagDataSourceConfigExample sourceConfigExample = new TagDataSourceConfigExample();
        sourceConfigExample.createCriteria().andStatusEqualTo(TagStatusEnum.ENABLED.getCode());
        List<TagDataSourceConfig> sourceConfigList = tagDataSourceConfigMapper.selectByExample(sourceConfigExample);

        String nowDay = LocalDate.now().toString();
        tagDataRuleList.forEach(tagDataRule -> {
            String tagCode = tagDataRule.getTagCode();
            Long start = System.currentTimeMillis();
            log.warn(TITLE + tagCode + "调度开始");
            Integer status;
            Integer number = null;
            // 判断标签记录表是否存在
            List<TagDataRuleCalculate> tagDataRuleCalculateList = getTagCalculateRecord(tagCode, nowDay, null);
            if (!CollectionUtils.isEmpty(tagDataRuleCalculateList)) {
                return;
            }
            // 存在，进入下次循环，不存在，插入记录
            Long recordId = saveTagCalculateRecord(tagDataRule.getTagCode(), nowDay);
            // 标签运算
            if (tagCalculate(tagDataRule, sourceConfigList, nowDay)) {
                status = TagData.TagCalculateStatusEnum.COMPLETE.getCode();
                String querySql = String.format(
                        "select count(1) from t_tag_data_detail where tag_code = '%S' and calculate_date ='%S'",
                        tagCode, nowDay);
                number = tagDataRuleCalculateMapper.getCountbI_(querySql);
                // 计算完成修改记录表状态
                updateTagCalculateRecord(recordId, status, number);
                // 更新规则表量级
                tagDataRule.setTagNumber(number);
                tagDataRule.setUpdateTime(new Date());
                tagDataRuleMapper.updateByPrimaryKeySelective(tagDataRule);
            }
            log.warn(TITLE + tagCode + "调度结束,耗时:{}ms", System.currentTimeMillis() - start);
        });
    }

    /**
     * 标签是否可用
     *
     * @param apiCode 客户编码
     * @param tagCode 标签Code
     * @return 是否可用
     */
    @Override
    public Boolean tagIsEnabled(String apiCode, String tagCode) {
        //标签状态判断
        TagDataRuleExample example = new TagDataRuleExample();
        example.createCriteria().andTagCodeEqualTo(tagCode).andStatusEqualTo(TagStatusEnum.ENABLED.getCode())
                .andDeleteFlagEqualTo(DeleteFlagEnum.NOT_DELETED.getCode());
        List<TagDataRule> tagDataRuleList = tagDataRuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tagDataRuleList)) {
            log.warn(TITLE + tagCode + "标签已失效或已删除");
            return Boolean.FALSE;
        }
        TagDataRule tagDataRule = tagDataRuleList.get(0);
        if (!tagDataRule.getApiCodeLicense().contains(apiCode)) {
            log.warn(TITLE + tagCode + "apiCode={}标签未授权", apiCode);
            return Boolean.FALSE;
        }
        //标签计算完成状态判断
        List<TagDataRuleCalculate> records = getTagCalculateRecord(tagCode, LocalDate.now().toString(),
                TagData.TagCalculateStatusEnum.COMPLETE.getCode());
        if (CollectionUtils.isEmpty(records)) {
            log.warn(TITLE + tagCode + "标签计算未完成");
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


    private Boolean tagCalculate(TagDataRule tagDataRule, List<TagDataSourceConfig> sourceConfigList,
                                 String nowDay) {
        String tagCode = tagDataRule.getTagCode();
        log.warn(TITLE + "开始计算标签: {}", tagCode);
        try {
            // 解析数据源配置
            List<String> sourceCodes = Arrays.asList(tagDataRule.getSourceCode().split(","));
            Collections.sort(sourceCodes);

            // 圈选的ApiCode范围
            List<String> apiCodes = Arrays.asList(tagDataRule.getApiCodeScope().split(","));

            // 处理每个apiCode的标签
            for (String apiCode : apiCodes) {
                if (!processTagByapiCode(tagDataRule, apiCode, sourceCodes, sourceConfigList)) {
                    return Boolean.FALSE;
                }
            }
            // 同步数据到TiDB表
            syncDataToTiDB(tagCode, nowDay);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    TITLE + tagCode + "标签计算异常请关注"), e);
            return Boolean.FALSE;
        }
    }

    /**
     * 处理单个apiCode的标签计算
     */
    private Boolean processTagByapiCode(TagDataRule tagDataRule, String apiCode, List<String> sourceCodes,
                                        List<TagDataSourceConfig> sourceConfigList) {
        String tagCode = tagDataRule.getTagCode();
        // 确定数据源类型和名称
        Integer sourceType;
        String sourceName;
        if (sourceCodes.size() > 1) {
            sourceType = TagData.TableTypeEnum.MATERIALIZED_VIEW.getLabel();
            sourceName = "view_".concat(apiCode).concat("_").concat(String.join("_", sourceCodes));
        } else {
            sourceType = TagData.TableTypeEnum.BASE.getLabel();
            sourceName = sourceConfigList.stream()
                    .filter(sourceConfig -> sourceConfig.getSourceCode().equals(sourceCodes.get(0)))
                    .findFirst()
                    .get()
                    .getSourceName()
                    .replace("${apiCode}", apiCode);
        }
        //生产环境视图名称为小写，此处对视图名称进行小写处理
        String lowerSourceName = sourceName.toLowerCase();
        // 处理数据源映射
        String sourceMappingCode = handleSourceMapping(tagCode, apiCode, lowerSourceName, sourceType, sourceCodes, sourceConfigList);
        if (sourceMappingCode == null) {
            return Boolean.FALSE;
        }

        // 保存标签规则与数据源的关系
        saveTagRuleSourceRelation(tagCode, apiCode, sourceMappingCode);

        // 写入数据到Doris
        Long start = System.currentTimeMillis();
        insertDataDoris(apiCode, lowerSourceName, sourceType, sourceCodes, tagDataRule);
        log.warn(TITLE + "tagCode={},apiCode={},写入数据到Doris明细表,耗时={}ms", tagCode, apiCode,
                System.currentTimeMillis() - start);

        return Boolean.TRUE;
    }

    /**
     * 处理数据源映射
     */
    private String handleSourceMapping(String tagCode, String apiCode, String sourceName, Integer sourceType,
                                       List<String> sourceCodes, List<TagDataSourceConfig> sourceConfigList) {
        // 查询数据源映射是否存在
        TagDataSourceMappingExample sourceMappingExample = new TagDataSourceMappingExample();
        sourceMappingExample.createCriteria()
                .andStatusEqualTo(TagStatusEnum.ENABLED.getCode())
                .andSourceNameEqualTo(sourceName)
                .andSourceTypeEqualTo(sourceType);

        List<TagDataSourceMapping> tagDataSourceMappingList = tagDataSourceMappingMapper
                .selectByExample(sourceMappingExample);

        String sourceMappingCode = apiCode.concat("_").concat(LocalDate.now().toString())
                .concat(UUID.randomUUID().toString());

        if (CollectionUtils.isEmpty(tagDataSourceMappingList)) {
            // 需要创建物化视图
            if (TagData.TableTypeEnum.MATERIALIZED_VIEW.getLabel().equals(sourceType)) {
                Boolean viewIsSuccess = createDorisView(apiCode, sourceName, sourceCodes, sourceConfigList);
                if (!viewIsSuccess) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                            TITLE + tagCode + "创建物化视图异常,apiCode=" + apiCode + "请关注"));
                    return null;
                }
            }
            // 创建新的数据源映射
            TagDataSourceMapping dataSourceMapping = new TagDataSourceMapping();
            dataSourceMapping.setSourceMappingCode(sourceMappingCode);
            dataSourceMapping.setSourceName(sourceName);
            dataSourceMapping.setSourceType(sourceType);
            dataSourceMapping.setApiCode(apiCode);
            dataSourceMapping.setStatus(TagStatusEnum.ENABLED.getCode());
            dataSourceMapping.setCreateTime(new Date());
            tagDataSourceMappingMapper.insertSelective(dataSourceMapping);
        } else {
            sourceMappingCode = tagDataSourceMappingList.get(0).getSourceMappingCode();
        }

        return sourceMappingCode;
    }

    /**
     * 保存标签规则与数据源的关系
     */
    private void saveTagRuleSourceRelation(String tagCode, String apiCode, String sourceMappingCode) {
        TagRuleSourceRelationExample sourceRelationExample = new TagRuleSourceRelationExample();
        sourceRelationExample.createCriteria()
                .andTagCodeEqualTo(tagCode)
                .andSourceMappingCodeEqualTo(sourceMappingCode)
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(TagStatusEnum.ENABLED.getCode());

        List<TagRuleSourceRelation> tagRuleSourceRelationList = tagRuleSourceRelationMapper
                .selectByExample(sourceRelationExample);

        if (CollectionUtils.isEmpty(tagRuleSourceRelationList)) {
            TagRuleSourceRelation tagRuleSourceRelation = new TagRuleSourceRelation();
            tagRuleSourceRelation.setApiCode(apiCode);
            tagRuleSourceRelation.setTagCode(tagCode);
            tagRuleSourceRelation.setSourceMappingCode(sourceMappingCode);
            tagRuleSourceRelation.setStatus(TagStatusEnum.ENABLED.getCode());
            tagRuleSourceRelation.setCreateTime(new Date());
            tagRuleSourceRelationMapper.insertSelective(tagRuleSourceRelation);
        }
    }

    /**
     * 同步数据到TiDB表
     */
    private void syncDataToTiDB(String tagCode, String nowDay) {
        Long start = System.currentTimeMillis();
        String syncDBName = marketingCommonConfig.getTagCalculateConfig().get("syncDBName");
        String fromDBName = marketingCommonConfig.getTagCalculateConfig().get("fromDBName");
        String syncTiDBSql = String.format(
                "insert into %s.marketing.t_tag_data_detail (tag_code,calculate_date,cell,cust_num,create_time,"
                        + "update_time) select tag_code,calculate_date,cell,cust_num,create_time,update_time from %s.t_tag_data_detail where tag_code = '%S' and calculate_date ='%S'",
                syncDBName, fromDBName, tagCode, nowDay);
        flagDataMapper.insertbI_(syncTiDBSql);
        log.warn(TITLE + "tagCode={},同步数据到Tidb明细表,耗时={}ms", tagCode, System.currentTimeMillis() - start);
    }


    /**
     * 将数据插入到Doris数据库
     *
     * @param apiCode     apiCode
     * @param sourceName  数据源名称
     * @param sourceType  数据源类型
     * @param sourceCodes 数据源代码列表
     * @param tagDataRule 标签数据规则
     */
    private void insertDataDoris(String apiCode, String sourceName, Integer sourceType, List<String> sourceCodes,
                                 TagDataRule tagDataRule) {

        String sourceCode = "";
        String conditionSql = "";
        String groupSql = "";

        if (TagData.TableTypeEnum.BASE.getLabel().equals(sourceType)) {
            sourceCode = sourceCodes.get(0);
            conditionSql = EsConditionTransferSqlUtil.jsonTransferSql(JSON.parseObject(tagDataRule.getContent()), "");
            if (sourceCode.equals(SourceTypeEnum.SHORTLINK.getCode())) {
                //如果数据源为短链，还需要对cell去重
                groupSql = " group by cell";
            }
        } else {
            //如果是多表查询，以CALL或TRANSFORM作为sourceCode进行查询
            for (String code : sourceCodes) {
                if (SourceTypeEnum.CALL.getCode().equals(code) || SourceTypeEnum.TRANSFORM.getCode().equals(code)) {
                    sourceCode = code;
                    break;
                }
            }
            conditionSql = EsConditionTransferSqlUtil.jsonTransferSqlByFillKey(JSON.parseObject(tagDataRule.getContent()), "");
        }

        // 构建插入SQL
        StringBuilder insertBuilder = new StringBuilder();
        SourceTypeEnum sourceCodeEnum = SourceTypeEnum.fromCode(sourceCode);
        if (sourceCodeEnum == null) {
            throw new BusinessException("当前数据源编码不存在！");
        }

        // 根据表类型确定字段名
        SourceFieldStrategy sourceFieldStrategy = null;
        try {
            sourceFieldStrategy = getFieldMappingStrategy(SourceTypeEnum.valueOf(sourceCode));
            if (sourceFieldStrategy != null) {
                insertBuilder.append(sourceFieldStrategy.mapFields(apiCode, sourceType, sourceCode, sourceCodeEnum, sourceName, tagDataRule));
            }
        } catch (Exception e) {
            log.error("获取数据源失败，sourceCode:{}", sourceCode);
            throw new BusinessException(e.getMessage());
        }

        // 添加其他条件
        insertBuilder.append("(").append(conditionSql).append(")").append(groupSql);

        // 执行插入操作
        log.warn(TITLE + "tagCode={},插入Doris明细表的sql={}", tagDataRule.getTagCode(), insertBuilder);
        flagDataMapper.insertbI_(insertBuilder.toString());
    }

    private SourceFieldStrategy getFieldMappingStrategy(SourceTypeEnum sourceTypeEnum) {
        SourceFieldStrategy sourceFieldStrategy;
        switch (sourceTypeEnum) {
            case SHORTLINK:
                sourceFieldStrategy = new ShortLinkFieldStrategy();
                break;
            case CALL:
                sourceFieldStrategy = new CallFieldStrategy();
                break;
            case TRANSFORM:
                sourceFieldStrategy = new TransformFieldStrategy();
                break;
            default:
                sourceFieldStrategy = null;
                break;
        }
        return sourceFieldStrategy;
    }

    private List<TagDataRuleCalculate> getTagCalculateRecord(String tagCode, String calculateDate, Integer status) {
        TagDataRuleCalculateExample example = new TagDataRuleCalculateExample();
        TagDataRuleCalculateExample.Criteria criteria = example.createCriteria();
        criteria.andTagCodeEqualTo(tagCode)
                .andCalculateDateEqualTo(calculateDate);
        if (StringUtils.isNotEmpty(status)) {
            criteria.andStatusEqualTo(status);
        }
        return tagDataRuleCalculateMapper.selectByExample(example);
    }

    public Long saveTagCalculateRecord(String tagCode, String calculateDate) {
        TagDataRuleCalculate tagDataRuleCalculate = new TagDataRuleCalculate();
        tagDataRuleCalculate.setTagCode(tagCode);
        tagDataRuleCalculate.setCalculateDate(calculateDate);
        tagDataRuleCalculate.setCreateTime(new Date());
        tagDataRuleCalculate.setUpdateTime(new Date());
        tagDataRuleCalculate.setStatus(TagData.TagCalculateStatusEnum.RUNNING.getCode());
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

    /**
     * 创建Doris物化视图
     *
     * @param apiCode          客户编码
     * @param viewName         视图名称
     * @param sourceCodes      数据源代码列表
     * @param sourceConfigList 数据源配置列表
     * @return 是否创建成功
     */
    private Boolean createDorisView(String apiCode, String viewName, List<String> sourceCodes,
                                    List<TagDataSourceConfig> sourceConfigList) {

        Long start = System.currentTimeMillis();
        Boolean isSuccess = Boolean.FALSE;
        // 视图基本定义
        String viewSqlPrefix = new StringBuilder(500)
                .append("CREATE MATERIALIZED VIEW ").append(viewName)
                .append(" BUILD IMMEDIATE\n")
                .append("REFRESH AUTO\n")
                .append("ON COMMIT\n")
                .append("DISTRIBUTED BY RANDOM BUCKETS 2\n")
                .append("PROPERTIES ('replication_num' = '2')\n")
                .append("AS\nSELECT ").toString();

        try {
            if (StringUtils.isNotEmpty(marketingCommonConfig.getTagCalculateConfig().get("viewSqlPrefix"))) {
                //解码，speed不能填充空格行
                viewSqlPrefix = URLDecoder.decode(marketingCommonConfig.getTagCalculateConfig().get("viewSqlPrefix"), "UTF-8");
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    TITLE + "创建物化视图前缀urldecode解码异常,apiCode=" + apiCode + "请关注"), e);
        }
        // 视图基本定义
        StringBuilder viewSql = new StringBuilder(500)
                .append(String.format(viewSqlPrefix, viewName));
        StringBuilder joinBuilder = new StringBuilder();
        String relateField = "";
        for (int i = 0; i < sourceCodes.size(); i++) {
            String sourceCode = sourceCodes.get(i);
            SourceTypeEnum sourceCodeEnum = SourceTypeEnum.fromCode(sourceCode);
            if (sourceCodeEnum == null) {
                throw new BusinessException("当前数据源编码不存在！");
            }

            String sourceName = sourceConfigList.stream().filter(sourceConfig -> sourceConfig.getSourceCode()
                    .equals(sourceCode)).findFirst().get().getSourceName().replace("${apiCode}", apiCode);
            // 时间条件
            StringBuilder whereSql = new StringBuilder().append(sourceCodeEnum.getTimeField())
                    .append(">=").append("\"").append(DateHelper.getPreviousDate("m", 3)).append("\"");
            if (SourceTypeEnum.SHORTLINK.getCode().equals(sourceCode)) {
                whereSql.append(" and api_code = \"").append(apiCode).append("\"");
            }
            // 添加字段
            List<String> fieldNameList = flagDataMapper.queryColumnNamebI_(sourceName);
            fieldNameList.forEach(field -> {
                viewSql.append(sourceCode).append(".").append(field).append(" as ").append(sourceCode).append("_")
                        .append(field).append(",");
            });
            // 构建FROM和JOIN
            if (i == 0) {
                joinBuilder.append(" from ( select * from ").append(sourceName).append(" where ").append(whereSql).append(") ").append(sourceCode);
                relateField = sourceCode.concat(".")
                        .concat(sourceCodeEnum.getCellField());
            } else {
                joinBuilder.append(" FULL JOIN ( select * from ").append(sourceName).append(" where ").append(whereSql).append(") ").append(sourceCode)
                        .append(" on ")
                        .append(sourceCode).append(".")
                        .append(sourceCodeEnum.getCellField())
                        .append("=").append(relateField);
            }
        }
        String createViewSql = new StringBuilder(viewSql.substring(0, viewSql.length() - 1)).append(joinBuilder).toString();
        log.warn(TITLE + "apiCode={},创建物化视图sql={}", apiCode, createViewSql);
        // 执行创建视图SQL
        flagDataMapper.insertbI_(createViewSql);
        // 等待视图创建完成
        isSuccess = waitForViewCreationComplete(viewName);
        log.warn(TITLE + "创建物化视图完成: apiCode={}, viewName={}, 是否成功={}, 耗时={}ms",
                apiCode, viewName, isSuccess, System.currentTimeMillis() - start);
        return isSuccess;

    }

    /**
     * 等待视图创建完成
     */
    private Boolean waitForViewCreationComplete(String viewName) {
        long beginTime = System.currentTimeMillis();
        Long maxTime = marketingCommonConfig.getCreateMVMaxWaitTime();
        String database = marketingCommonConfig.getDatabase();
        while (System.currentTimeMillis() - beginTime < maxTime) {
            try {
                MaterializedViewDTO materializedView = tagDataRuleCalculateMapper.getMViewInfobI_(viewName, database);

                if (materializedView != null &&
                        "NORMAL".equals(materializedView.getState()) && "SUCCESS".equals(materializedView.getRefreshState()) &&
                        "1".equals(materializedView.getSyncWithBaseTables())) {
                    return Boolean.TRUE;
                }
                // 等待5秒再次检查
                Thread.sleep(5000L);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                        TITLE + viewName + "等待物化视图创建过程被中断"), e);
            }
        }

        log.warn(TITLE + "等待物化视图创建超时: viewName={}", viewName);
        return Boolean.FALSE;
    }

}
