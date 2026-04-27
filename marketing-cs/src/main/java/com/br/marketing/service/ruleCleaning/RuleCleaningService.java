package com.br.marketing.service.ruleCleaning;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.rulecleaning.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 规则数据清洗接口
 * @author guangxiu.li
 * @date 2025/5/6
 */
public interface RuleCleaningService {

    /**
     * 规则列表查询
     * @param current     当前页
     * @param size        每页条数
     * @param apiCode     API编码
     * @param accountType 账号类型
     * @param acceptType  接口类型
     * @return 分页查询结果
     */
    PageResultReturn getRuleList(@Validated int current, @Validated int size,  String apiCode, String accountType,  Integer acceptType);

    /**
     * 根据规则ID查询规则明细
     * @param ruleId 规则ID
     * @return 规则明细信息
     */
    MarketingDataCleanGeneralConfig getRuleDetailById(Long ruleId);

    /**
     * 删除不用的清洗规则
     * @param config 规则配置信息
     * @param cleanFields 要删除的清洗字段列表
     * @return 操作结果
     */
    boolean deleteRule(MarketingDataCleanGeneralConfig config, List<String> cleanFields);

    /**
     * 字段样例查询
     * @param apiCode    API编码
     * @param dataType   数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    List<FieldSampleDTO> getPreviewFieldSamples(@Validated String apiCode, @Validated Integer systemType, @Validated Integer dataType,
                                                 @Validated Integer acceptType);


    /**
     * 字段样例查询
     * @param apiCode    API编码
     * @param dataType   数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    List<FieldSampleDTO> getFieldSamples(@Validated String apiCode, @Validated Integer systemType,
                                         @Validated Integer dataType, @Validated Integer acceptType);

    /**
     * 字段样例查询
     * @param apiCode    API编码
     * @param dataType   数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    String getpreviewField(@Validated String apiCode,@Validated Integer systemType, @Validated Integer dataType, @Validated Integer acceptType);

    /**
     * 预览字段清洗结果
     * @param fieldSample  字段样例数据
     * @param cleaningRule 清洗规则（JSON格式）
     * @return 清洗后的数据值
     */
    Object previewFieldCleaning(@Validated String fieldSample, @Validated String cleaningRule, Object nodeParse);


    MarketingDataCleanGeneralFieldConfig getFieldConfg(Integer dataType, Integer acceptType, Integer systemType);

    boolean fieldSaveOrUpdate(CleanFieldConfigVO fieldConfigVO);


    Object executeCleaningRule(JSONObject nodeParse, MarketingDataCleanGeneralRuleConfig cleaningRule);


    List<String> getLastMonthDataDates(String apiCode,Integer acceptType,String sftpPath);

    boolean saveCleanConfig(CleanConfigDTO configDTO);

    List<String> getFileSftpPath(String apiCode, Integer fileType);

    List<FieldSampleDTO> getRuleDetail(Long configId);

    boolean saveCleanRule(RuleCleaningConfigDTO ruleCleaningConfigDTO);

    Result<List<List<RuleCleaningResult>>> trialProcess(RuleTrialConfigDTO ruleTrialConfigDTO);

    boolean ruleEffect(Long ruleId);

    List<MarketingDataCleanGeneralConfig> queryCleanConfigCommon(String apiCode, Integer systemType, Integer dataType, Integer acceptType);

    String generateAviatorScriptRule(String question);
}
