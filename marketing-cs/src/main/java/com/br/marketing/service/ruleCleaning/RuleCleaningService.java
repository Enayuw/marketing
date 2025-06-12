package com.br.marketing.service.ruleCleaning;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.rulecleaning.CleanConfigDTO;
import com.br.marketing.client.rulecleaning.FieldCleaningConfigDTO;
import com.br.marketing.client.rulecleaning.FieldSampleDTO;
import com.br.marketing.client.rulecleaning.RuleCleaningConfigDTO;
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
     * 保存或更新规则
     * @param config 规则配置信息
     * @return 操作结果
     */
    boolean saveOrUpdateRule(MarketingDataCleanGeneralConfig config);

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
    List<FieldSampleDTO> getPreviewFieldSamples(@Validated String apiCode, @Validated Integer dataType,
                                                 @Validated Integer acceptType);


    /**
     * 字段样例查询
     * @param apiCode    API编码
     * @param dataType   数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    List<FieldSampleDTO> getFieldSamples(@Validated String apiCode, @Validated Integer dataType, @Validated Integer acceptType);

    /**
     * 字段样例查询
     * @param apiCode    API编码
     * @param dataType   数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    String getpreviewField(@Validated String apiCode, @Validated Integer dataType, @Validated Integer acceptType);

    /**
     * 保存字段清洗配置
     * @param configDTO 字段清洗配置DTO
     * @return 操作结果
     */
    boolean saveFieldCleaningConfig(@Validated FieldCleaningConfigDTO configDTO);

    /**
     * 预览字段清洗结果
     * @param fieldSample  字段样例数据
     * @param cleaningRule 清洗规则（JSON格式）
     * @return 清洗后的数据值
     */
    Object previewFieldCleaning(@Validated String fieldSample, @Validated String cleaningRule);


    MarketingDataCleanGeneralFieldConfig getFieldConfg(Integer dataType, Integer acceptType);

    boolean fieldSaveOrUpdate(CleanFieldConfigVO fieldConfigVO);


    Object executeCleaningRule(JSONObject nodeParse, MarketingDataCleanGeneralRuleConfig cleaningRule);

    /**
     * 保存规则及其清洗配置
     * @param configDTO 包含规则和清洗配置的DTO
     * @return 操作结果
     */
    boolean saveRuleWithConfigs(RuleCleaningConfigDTO configDTO);

    List<String> getLastMonthDataDates(String apiCode,Integer acceptType,String sftpPath);

    boolean saveCleanConfig(CleanConfigDTO configDTO);

    List<String> getFileSftpPath(String apiCode, Integer fileType);

    List<FieldSampleDTO> getRuleDetail(Long configId);
}
