package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.enums.sync.SyncConfigIsUnzipEnum;
import com.br.marketing.enums.sync.UnzipFilenameCharsetEnum;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.SyncConfigEditVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * sftp账号配置业务逻辑实现
 *
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
 */
@Service
@Slf4j
public class SyncConfigServiceImpl implements SyncConfigService {

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    EntityOptServiceImpl entityOptService;

    @Override
    public PageResultReturn getSftpList(int page, int pageSize, String apiCode, Integer dataType) {
        PageHelper.startPage(page, pageSize);
        try {
            List<SyncConfigEditVO> list = syncConfigMapper.getSftpList(apiCode, dataType);
            list.stream().map(syncConfigEditVO -> {
                String s = DataTypeEnum.fromDescByValue(syncConfigEditVO.getDataType());
                syncConfigEditVO.setDataTypeValue(s);
                return syncConfigEditVO;
            }).collect(Collectors.toList());

            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public JSONArray getDataTypeList() {
        JSONArray typeList = new JSONArray();
        for (DataTypeEnum typeEnum : DataTypeEnum.values()) {
            JSONObject type = new JSONObject();
            type.put("value", typeEnum.getValue());
            type.put("desc", typeEnum.getDesc());
            typeList.add(type);
        }
        return typeList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> copySftp(String id, String apiCode, String srcPath, String targePath
        , Integer type, Integer dataType,
                                       String suffix, String srcSftpHost, Integer srcSftpPort, String srcSftpUser, String srcSftpPwd,
                                       String targetSftpHost, Integer targetSftpPort, String targetSftpUser, String targetSftpPwd,String srcType,
                                       String targetType, Integer isUnzip, String unzipFilenameCharset) {
        SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(Long.parseLong(id));
        syncConfig.setId(null);
        syncConfig.setCreateTime(null);
        syncConfig.setUpdateTime(null);
        SyncConfig syncConfigNew = new SyncConfig();

        //判重
       /* boolean only = sftpOnly(null, apiCode, dataType!=null?dataType:syncConfig.getDataType(),
                type!=null?type:syncConfig.getType());
        if(!only){
            return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS_4);
        }*/

        try {
            ConvertUtils.register(new DateConverter(null), java.util.Date.class);
            BeanUtils.copyProperties(syncConfigNew, syncConfig);
            syncConfigNew.setApiCode(apiCode);
            syncConfigNew.setSrcPath(srcPath);
            syncConfigNew.setTargetPath(targePath);
            syncConfigNew.setType(type);
            syncConfigNew.setDataType(dataType);
            syncConfigNew.setSuffix(suffix);
            syncConfigNew.setSrcSftpHost(srcSftpHost);
            syncConfigNew.setSrcSftpPort(srcSftpPort);
            syncConfigNew.setSrcSftpUser(srcSftpUser);
            syncConfigNew.setSrcSftpPwd(srcSftpPwd);
            syncConfigNew.setTargetSftpHost(targetSftpHost);
            syncConfigNew.setTargetSftpPort(targetSftpPort);
            syncConfigNew.setTargetSftpUser(targetSftpUser);
            syncConfigNew.setTargetSftpPwd(targetSftpPwd);
            syncConfigNew.setSrcType(srcType);
            syncConfigNew.setTargetType(targetType);
            syncConfigNew.setIsUnzip(SyncConfigIsUnzipEnum.defaultIfNull(isUnzip));
            syncConfigNew.setUnzipFilenameCharset(UnzipFilenameCharsetEnum.defaultIfBlank(unzipFilenameCharset));
            syncConfigNew.setCreateTime(new Date());
            syncConfigNew.setUpdateTime(new Date());
        } catch (Exception e) {
            log.error("复制sftp配置信息失败！");
            e.printStackTrace();
        }
        int insert = syncConfigMapper.insert(syncConfigNew);

        if (StringUtils.isEmpty(insert)) {
            log.error("复制sftp配置信息失败！");
        }

        entityOptService.writeOptLog(syncConfigNew.getId(), syncConfigNew, null);
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> editSftp(SyncConfigEditVO vo) {
        SyncConfig select = syncConfigMapper.selectByPrimaryKey(vo.getId());
        //判重
      /*  boolean only = sftpOnly(vo.getId().toString(), vo.getApiCode(), select.getDataType(), select.getType());
        if(!only){
            return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS_4);
        }*/

        SyncConfig syncConfig = new SyncConfig();

        try {
            BeanUtils.copyProperties(syncConfig, vo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //copyProperties方法Integer如果为null，赋值0，需要单独处理
        syncConfig.setSrcSftpPort(vo.getSrcSftpPort());
        syncConfig.setTargetSftpPort(vo.getTargetSftpPort());
        syncConfig.setDataType(vo.getDataType());
        syncConfig.setType(vo.getType());
        syncConfig.setIsUnzip(SyncConfigIsUnzipEnum.defaultIfNull(vo.getIsUnzip()));
        syncConfig.setUnzipFilenameCharset(UnzipFilenameCharsetEnum.defaultIfBlank(vo.getUnzipFilenameCharset()));
        syncConfig.setUpdateTime(new Date());
        int update = syncConfigMapper.updateByPrimaryKeySelective(syncConfig);
        if (StringUtils.isEmpty(update) || update <= 0) {
            log.error("编辑sftp配置信息失败！");
        }
        SyncConfig newConfig = syncConfigMapper.selectByPrimaryKey(syncConfig.getId());
        entityOptService.writeOptLog(syncConfig.getId(), newConfig, syncConfig);
        return new ApiResult<Boolean>().success(true);
    }

    //对aipCode,dataType,type判重
    public boolean sftpOnly(String id, String aipCode, Integer dataType, Integer type) {
        SyncConfigExample example = new SyncConfigExample();
        example.createCriteria().andApiCodeEqualTo(aipCode)
            .andDataTypeEqualTo(dataType)
            .andTypeEqualTo(type)
            .andStatusEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(example);
        if (StringUtils.isEmpty(id)) {
            //新增
            if (syncConfigs.size() == 0) {
                return true;
            }
            return false;
        }
        //编辑
        if (syncConfigs != null && syncConfigs.size() > 1) {
            log.error("apicode=" + aipCode + ",dataType=" + dataType + ",type=" + type + "的配置存在多条！");
            return false;
        }
        for (SyncConfig s : syncConfigs) {
            if (StringUtils.isNotEmpty(id) && id.equals(s.getId().toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }

    @Override
    public String getPullCustomerFilePath(String apiCode) {
        return getPath().concat("pullCustomerFile")
                .concat(File.separator).concat(apiCode).concat(File.separator);
    }

    @Override
    public ApiResult<Boolean> batchDeleteSftpList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ApiResult<Boolean>().fail("删除失败，参数为空！");
        }

        try {
            SyncConfigExample syncConfigExample = new SyncConfigExample();
            syncConfigExample.createCriteria().andIdIn(ids);

            // 查询要删除的记录（原始数据）
            List<SyncConfig> oldSyncConfigs = syncConfigMapper.selectByExample(syncConfigExample);

            if (oldSyncConfigs.isEmpty()) {
                return new ApiResult<Boolean>().fail("删除失败，未找到对应记录！");
            }

            // 执行软删除
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setStatus(Constants.STATUS_DELETE);
            int updateCount = syncConfigMapper.updateByExampleSelective(syncConfig, syncConfigExample);

            if (updateCount == 0) {
                return new ApiResult<Boolean>().fail("删除失败，未更新任何记录！");
            }

            // 查询更新后的记录
            List<SyncConfig> newSyncConfigs = syncConfigMapper.selectByExample(syncConfigExample);

            // 为每条记录记录操作日志
            for (int i = 0; i < oldSyncConfigs.size(); i++) {
                SyncConfig oldConfig = oldSyncConfigs.get(i);
                SyncConfig newConfig = newSyncConfigs.get(i);

                entityOptService.writeOptLog(oldConfig.getId(), newConfig, oldConfig);
            }

            return new ApiResult<Boolean>().success(true);

        } catch (Exception e) {
            log.error("批量删除SFTP配置失败", e);
            return new ApiResult<Boolean>().fail("删除失败: {}", e.getMessage());
        }
    }

    @Override
    public List<String> getSftpSuffixConfigs() {
        List<String> list = marketingCommonConfig.getSftpFileSuffixOptions();
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

}
