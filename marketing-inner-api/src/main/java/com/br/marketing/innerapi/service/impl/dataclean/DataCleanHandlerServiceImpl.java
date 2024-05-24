package com.br.marketing.innerapi.service.impl.dataclean;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanConfigDTO;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import com.br.marketing.vo.dataclean.DataCleanTaskVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 数据清洗service实现
 * @Author zhen.Li1
 * @CreateTime 2024/05/22
 */
@Service
@Slf4j
public class DataCleanHandlerServiceImpl implements DataCleanHandlerService {


    @Autowired
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    private MarketingDataFileConfigMapper marketingDataFileConfigMapper;

    @Resource
    private MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;


    @Override
    public Result<MarketingCleanDataFile> getfileMsg(String fileNames, String apiCode) {

        List<String> fileNameList = Arrays.asList(fileNames.split(","));
        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andFileNameIn(fileNameList);
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample);
        Set<String> headerSet = cleanDataFiles.stream().map(MarketingCleanDataFile::getFileHeader).collect(Collectors.toSet());
        if (headerSet.size() > 1) {
            return new Result<MarketingCleanDataFile>().setCode(ResultCode.FAIL.getValue()).setMessage("多个文件存在表头不一致");
        }
        return new Result<MarketingCleanDataFile>().setCode(ResultCode.SUCCESS.getValue()).setDate(cleanDataFiles.get(0));

    }

    @Override
    public List<String> getfileNames(Integer fileType, String apiCode) {
        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andCleanTypeEqualTo(fileType);
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample);
        return cleanDataFiles.stream().map(MarketingCleanDataFile::getFileName).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> saveTask(DataCleanRuleDetailDTO dto) {
        String apiCode = dto.getApiCode();
        Long configId = dto.getRuleId();
        if (Objects.isNull(configId)) {
            Integer fileId = Integer.valueOf(Arrays.asList(dto.getFileIds().split(",")).get(0));
            MarketingDataFileConfig marketingDataFileConfig = new MarketingDataFileConfig();
            String ruleConfig = dto.getRuleCondition();
            //TODO json处理
            marketingDataFileConfig.setFieldConfig(ruleConfig);
            marketingDataFileConfig.setFieldConfigShow(ruleConfig);
            marketingDataFileConfig.setCleanType(dto.getFileType());
            marketingDataFileConfig.setServiceName("defaultFileToMarketingRuleServiceImpl");
            marketingDataFileConfig.setFileId(fileId);
            if (StringUtils.isEmpty(dto.getRuleName())) {
                marketingDataFileConfig.setRuleName(apiCode + "_" + LocalDate.now().toString() + fileId);
            }
            marketingDataFileConfig.setApiCode(apiCode);
            marketingDataFileConfigMapper.insertSelective(marketingDataFileConfig);
            configId = marketingDataFileConfig.getId();
        }
        //保存任务
        MarketingCleanDataTask task = new MarketingCleanDataTask();
        task.setConfigId(configId.intValue());
        task.setFileId(dto.getFileIds());
        task.setCleanType(dto.getFileType());
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        marketingCleanDataTaskMapper.insertSelective(task);
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(task.getId());
    }

    @Override
    public List<String> getfieldMap(Integer fileType) {
        List<String> fieldList = new ArrayList<>();
        if (fileType.equals(0)) {
            fieldList.add("custNum");
            fieldList.add("cell");
            fieldList.add("id");
            fieldList.add("name");
            fieldList.add("userType");
        } else {
            fieldList.add("custNum");
            fieldList.add("userType");
        }
        return fieldList;
    }

    @Override
    public PageResultReturn taskList(int page, int pageSize, String apiCode, String fileType, String status) {
        PageHelper.startPage(page, pageSize);
        try {
            List<DataCleanTaskVO> list = marketingCleanDataTaskMapper.getTaskList(apiCode, fileType, status);
            list.stream().map(dataCleanTaskVO -> {
                MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
                MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
                List<String> fieldIds = Arrays.asList(dataCleanTaskVO.getFileId().split(","));
                criteria.andIdIn(fieldIds.stream().map(Long::valueOf).collect(Collectors.toList()));
                dataCleanTaskVO.setFileName(marketingCleanDataFileMapper.selectByExample(cleanDataFileExample).stream().map(MarketingCleanDataFile::getFileName).
                        collect(Collectors.joining(",")));
                return dataCleanTaskVO;
            }).collect(Collectors.toList());

            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public PageResultReturn configList(int page, int pageSize, String apiCode, String fileType) {
        PageHelper.startPage(page, pageSize);
        try {
            List<DataCleanConfigVO> list = marketingDataFileConfigMapper.getList(apiCode, fileType);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Result updateConfig(DataCleanConfigDTO dto) {
        MarketingDataFileConfig marketingDataFileConfig = new MarketingDataFileConfig();
        marketingDataFileConfig.setFieldConfigShow(dto.getRuleConfig());
        marketingDataFileConfig.setRuleName(dto.getRuleName());
        marketingDataFileConfig.setId(dto.getId());
        marketingDataFileConfigMapper.updateByPrimaryKeySelective(marketingDataFileConfig);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result saveConfig(DataCleanConfigDTO dto) {

        MarketingDataFileConfig marketingDataFileConfig = new MarketingDataFileConfig();
        marketingDataFileConfig.setApiCode(dto.getApiCode());
        marketingDataFileConfig.setFieldConfigShow(dto.getRuleConfig());
        marketingDataFileConfig.setCleanType(dto.getFileType());
        marketingDataFileConfig.setCreateTime(new Date());
        marketingDataFileConfig.setUpdateTime(new Date());
        marketingDataFileConfig.setServiceName("defaultFileToMarketingRuleServiceImpl");
        marketingDataFileConfigMapper.insertSelective(marketingDataFileConfig);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

}
