package com.br.marketing.innerapi.service.impl.dataclean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.dataclean.DataCleanConfigDTO;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import com.br.marketing.vo.dataclean.DataCleanTaskVO;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.grpc.ClientStreamTracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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


    @Resource
    MarketingUserMapper marketingUserMapper;

    @Autowired
    PushRuleService pushRuleService;

    public static final List<String> UPLOAD_FIELD = Lists.newArrayList("custNum", "cell", "id", "name", "userType");
    public static final List<String> TRANSFER_FIELD = Lists.newArrayList("custNum", "userType");


    @Override
    public Result<List<MarketingCleanDataFile>> getfileMsg(String fileIdList, String apiCode) {

        List<String> fieldIds = Arrays.asList(fileIdList.split(","));
        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andIdIn(fieldIds.stream().map(Long::valueOf).collect(Collectors.toList()));
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample);
        Set<String> headerSet = cleanDataFiles.stream().map(MarketingCleanDataFile::getFileHeader).collect(Collectors.toSet());
        if (headerSet.size() > 1) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("多个文件存在表头不一致");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(cleanDataFiles);

    }

    @Override
    public List<MarketingCleanDataFile> getfileNames(Integer fileType, String apiCode) {
        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andCleanTypeEqualTo(fileType);
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample);
        return cleanDataFiles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> saveOrUpdateTask(DataCleanRuleDetailDTO dto) {
        String apiCode = dto.getApiCode();
        Long configId = dto.getRuleId();
        Integer fileId = Integer.valueOf(Arrays.asList(dto.getFileIds().split(",")).get(0));
        MarketingDataFileConfig marketingDataFileConfig = new MarketingDataFileConfig();
        String ruleConfigShow = dto.getRuleCondition();
        //json处理
        List<FileToMarketingFieldVO> marketingFieldVOList = JSON.parseObject(ruleConfigShow, new TypeReference<List<FileToMarketingFieldVO>>() {
        }.getType());
        List<String> mustFieldList = getInterFaceMustField(dto.getFileType());
        List<String> interFaceFieldList = marketingFieldVOList.stream().map(FileToMarketingFieldVO::getInterfaceField).collect(Collectors.toList());
        if (!interFaceFieldList.containsAll(mustFieldList)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("必填字段未配置，请检查");
        }
        String ruleConfig = ruleTransferHandler(ruleConfigShow, dto.getFileType());
        marketingDataFileConfig.setFieldConfig(ruleConfig);
        marketingDataFileConfig.setFieldConfigShow(ruleConfigShow);
        marketingDataFileConfig.setCleanType(dto.getFileType());
        marketingDataFileConfig.setServiceName("defaultFileToMarketingRuleServiceImpl");
        marketingDataFileConfig.setFileId(fileId);
        if (StringUtils.isEmpty(dto.getRuleName())) {
            marketingDataFileConfig.setRuleName(apiCode + "_" + LocalDate.now().toString() + fileId);
        }else{
            marketingDataFileConfig.setRuleName(dto.getRuleName());
        }
        marketingDataFileConfig.setApiCode(apiCode);
        marketingDataFileConfig.setCreateTime(new Date());
        marketingDataFileConfig.setUpdateTime(new Date());
        if (Objects.isNull(configId)) {
            marketingDataFileConfigMapper.insertSelective(marketingDataFileConfig);
            configId = marketingDataFileConfig.getId();
        } else {
            marketingDataFileConfig.setId(configId);
            marketingDataFileConfigMapper.updateByPrimaryKeySelective(marketingDataFileConfig);
        }
        //保存任务
        MarketingCleanDataTask task = new MarketingCleanDataTask();
        task.setConfigId(configId.intValue());
        task.setFileId(dto.getFileIds());
        task.setCleanType(dto.getFileType());
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setCleanStatus(0);
        task.setApiCode(apiCode);
        if (Objects.isNull(dto.getId())) {
            marketingCleanDataTaskMapper.insertSelective(task);
        } else {
            task.setId(dto.getId());
            marketingCleanDataTaskMapper.updateByPrimaryKeySelective(task);
        }
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(task.getId());
    }


    private String ruleTransferHandler(String ruleConfig, Integer fileType) {
        List<FileToMarketingFieldVO> ruleList = new ArrayList<>();
        List<FileToMarketingFieldVO> marketingFieldVOList = JSON.parseObject(ruleConfig, new TypeReference<List<FileToMarketingFieldVO>>() {
        }.getType());
        marketingFieldVOList.forEach((FileToMarketingFieldVO fileToMarketingFieldVO) -> {
                List<String> interfaceFields = Arrays.asList(fileToMarketingFieldVO.getInterfaceField().split(","));
                interfaceFields.forEach((String field) -> {
                    FileToMarketingFieldVO fieldVO = new FileToMarketingFieldVO();
                    BeanUtils.copyProperties(fileToMarketingFieldVO, fieldVO);
                    if (StringUtils.isBlank(field)) {
                        fieldVO.setInterfaceField(fileToMarketingFieldVO.getHeadField());
                    } else {
                        fieldVO.setInterfaceField(field);
                    }
                    fieldVO.setIsExtend(isExtend(fileType, field));
                    fieldVO.setIsMust(isFileMust(fileType, field));
                    ruleList.add(fieldVO);
                });
        });
        return JSON.toJSONString(ruleList);
    }

    private Boolean isFileMust(Integer fileType, String field) {
        List<String> mustList = new ArrayList<>();
        if (fileType.equals(0)) {
            mustList = Lists.newArrayList("custNum", "cell");
        } else {
            mustList = Lists.newArrayList("custNum");
        }
        return mustList.contains(field);

    }

    private Boolean isExtend(Integer fileType, String field) {
        List<String> fieldList = getfieldMap(fileType);
        if (fieldList.contains(field)) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public List<String> getfieldMap(Integer fileType) {
        List<String> fieldList = new ArrayList<>();
        if (fileType.equals(0)) {
            fieldList = UPLOAD_FIELD;
        } else {
            fieldList = TRANSFER_FIELD;
        }
        return fieldList;
    }


    private List<String> getInterFaceMustField(Integer fileType) {
        List<String> mustfieldList = new ArrayList<>();
        if (fileType.equals(0)) {
            mustfieldList = Lists.newArrayList("custNum", "cell", "userType");
        } else {
            mustfieldList = Lists.newArrayList("custNum", "userType");
        }
        return mustfieldList;
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

        MarketingDataFileConfig marketingDataFileConfig  = marketingDataFileConfigMapper.selectByPrimaryKey(dto.getId());
        if(marketingDataFileConfig==null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("清洗配置规则不存在");
        }
        MarketingDataFileConfig update = new MarketingDataFileConfig();
        String ruleConfig = ruleTransferHandler(dto.getRuleConfig(), marketingDataFileConfig.getCleanType());
        update.setFieldConfig(ruleConfig);
        update.setFieldConfigShow(dto.getRuleConfig());
        update.setRuleName(dto.getRuleName());
        update.setId(dto.getId());
        update.setUpdateTime(new Date());
        marketingDataFileConfigMapper.updateByPrimaryKeySelective(update);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result saveConfig(DataCleanConfigDTO dto) {

        MarketingDataFileConfig marketingDataFileConfig = new MarketingDataFileConfig();
        marketingDataFileConfig.setApiCode(dto.getApiCode());
        String ruleConfig = ruleTransferHandler(dto.getRuleConfig(), dto.getFileType());
        marketingDataFileConfig.setFieldConfig(ruleConfig);
        marketingDataFileConfig.setFieldConfigShow(dto.getRuleConfig());
        marketingDataFileConfig.setCleanType(dto.getFileType());
        marketingDataFileConfig.setCreateTime(new Date());
        marketingDataFileConfig.setUpdateTime(new Date());
        marketingDataFileConfig.setRuleName(dto.getRuleName());
        marketingDataFileConfig.setServiceName("defaultFileToMarketingRuleServiceImpl");
        marketingDataFileConfigMapper.insertSelective(marketingDataFileConfig);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<Long> runTask(DataCleanRuleDetailDTO dto) {
        MarketingCleanDataTask task = new MarketingCleanDataTask();
        task.setUpdateTime(new Date());
        task.setCleanStatus(1);
        task.setId(dto.getId());
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(task);
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(task.getId());
    }

    @Override
    public Result getfileRules(String fileHeader, String apiCode, String fileType) {

        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andFileHeaderEqualTo(fileHeader).andCleanTypeEqualTo(Integer.valueOf(fileType));
        List<Long> fileIds = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample).stream().map(MarketingCleanDataFile::getId)
                .collect(Collectors.toList());
        MarketingDataFileConfigExample configExample = new MarketingDataFileConfigExample();
        MarketingDataFileConfigExample.Criteria configExampleCriteria = configExample.createCriteria();
        configExampleCriteria.andApiCodeEqualTo(apiCode).andFileIdIn(fileIds.stream().map(Long::intValue).collect(Collectors.toList()));
        List<MarketingDataFileConfig> marketingDataFileConfigs = marketingDataFileConfigMapper.selectByExample(configExample);
        return new Result<List<MarketingDataFileConfig>>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingDataFileConfigs);

    }

    @Override
    public Result<Long> testTask(DataCleanRuleDetailDTO dto) {
        Long fieldId = Long.valueOf(Arrays.asList(dto.getFileIds().split(",")).get(0));
        MarketingCleanDataFile marketingCleanDataFile = marketingCleanDataFileMapper.selectByPrimaryKey(fieldId);
        String data = marketingCleanDataFile.getFileData();
        MarketingDataFileConfig marketingDataFileConfig =marketingDataFileConfigMapper.selectByPrimaryKey(dto.getRuleId());
        String ruleConfig = marketingDataFileConfig.getFieldConfig();
        //组装数据 TODO
        //插入上传info表
        MarketingPreUserDTO uploadDataDTO = new MarketingPreUserDTO();
        MarketingSyncInfo syncInfo = new MarketingSyncInfo();
        try {
            syncInfo.setApiCode(dto.getApiCode());
            syncInfo.setCusBatch(uploadDataDTO.getTaskId());
            syncInfo.setRequestBatch(uploadDataDTO.getRequestId());
            syncInfo.setCreateTime(new Date());
            syncInfo.setJsonData(JSON.toJSONString(uploadDataDTO));
            syncInfo.setActualNum(uploadDataDTO.getDataItems().size());
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        } catch (DuplicateKeyException keyException) {
            log.error("数据清洗上传数据request_batch重复，requestBatch = {}", uploadDataDTO.getRequestId());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮上传数据request_batch重复");
        } catch (Exception ex) {
            log.error("中邮上传数据插入异常", ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮上传数据插入异常");
        }
        //插入上传明细表
        pushRuleService.insertMarketingPreUserSync(syncInfo.getId());
        //查询明细表

        return null;
    }

    @Override
    public Result getRuleByID(Long id) {

        MarketingDataFileConfig config = marketingDataFileConfigMapper.selectByPrimaryKey(id);
        if (Objects.isNull(config)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗配置");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(config);
    }

    @Override
    public Result getTaskByID(Long id) {

        MarketingCleanDataTask task = marketingCleanDataTaskMapper.selectByPrimaryKey(id);
        if (Objects.isNull(task)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗任务");
        }
        MarketingDataFileConfig config = marketingDataFileConfigMapper.selectByPrimaryKey(task.getConfigId().longValue());
        List<String> fieldIds = Arrays.asList(task.getFileId().split(","));
        MarketingCleanDataFileExample cleanDataFileExample = new MarketingCleanDataFileExample();
        MarketingCleanDataFileExample.Criteria criteria = cleanDataFileExample.createCriteria();
        criteria.andApiCodeEqualTo(task.getApiCode()).andIdIn(fieldIds.stream().map(Long::valueOf).collect(Collectors.toList()));
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(cleanDataFileExample);
        //组装返参
        DataCleanTaskVO dataCleanTaskVO = new DataCleanTaskVO();
        BeanUtils.copyProperties(task, dataCleanTaskVO);
        dataCleanTaskVO.setFileType(task.getCleanType());
        dataCleanTaskVO.setRuleCondition(config.getFieldConfigShow());
        dataCleanTaskVO.setFileName(cleanDataFiles.stream().map(MarketingCleanDataFile::getFileName).collect(Collectors.joining(",")));
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(dataCleanTaskVO);

    }

}
