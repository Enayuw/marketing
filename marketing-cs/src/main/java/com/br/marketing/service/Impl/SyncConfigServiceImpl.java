package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.vo.SyncConfigVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


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


    @Override
    public PageResultReturn getSftpList(int page, int pageSize, String apiCode) {
        PageHelper.startPage(page, pageSize);
        try {
            List<SyncConfigVO> list = syncConfigMapper.getSftpList(apiCode);
            return PageResultReturn.setPageResult(list, page,pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> copySftp(String id, String apiCode, String srcPath, String targePath) {
        SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(Long.parseLong(id));
        syncConfig.setId(null);
        syncConfig.setCreateTime(null);
        syncConfig.setUpdateTime(null);
        SyncConfig syncConfigNew = new SyncConfig();

        try {
            ConvertUtils.register(new DateConverter(null), java.util.Date.class);
            BeanUtils.copyProperties(syncConfigNew,syncConfig);
            syncConfigNew.setApiCode(apiCode);
            syncConfigNew.setSrcPath(srcPath);
            syncConfigNew.setTargetPath(targePath);
            syncConfigNew.setCreateTime(new Date());
            syncConfigNew.setUpdateTime(new Date());
        }catch (Exception e){
            log.error("复制sftp配置信息失败！");
            e.printStackTrace();
        }
        int insert = syncConfigMapper.insert(syncConfigNew);

        if (StringUtils.isEmpty(insert)){
            log.error("复制sftp配置信息失败！");
        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> editSftp(String id, String apiCode, String srcPath, String targePath) {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setId(Long.parseLong(id));
        syncConfig.setApiCode(apiCode);
        syncConfig.setSrcPath(srcPath);
        syncConfig.setTargetPath(targePath);
        syncConfig.setUpdateTime(new Date());
        int update = syncConfigMapper.updateByPrimaryKeySelective(syncConfig);
        if (StringUtils.isEmpty(update) || update<=0){
            log.error("编辑sftp配置信息失败！");
        }
        return new ApiResult<Boolean>().success(true);
    }

}
