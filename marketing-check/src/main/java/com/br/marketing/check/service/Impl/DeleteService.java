package com.br.marketing.check.service.Impl;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.service.AbstractDataToDbService;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.LoadResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.Map;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class DeleteService extends AbstractDataToDbService {

    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;
    /**
     * The File ckeck servicce.
     */
    @Resource
    FileCheckServiceImpl fileCheckService;
    /**
     * The Redis chg service.
     */
    @Resource
    RedisChgService redisChgService;
    private final static Integer SPLITSIZE=5000;



    @Override
    public Boolean checkTxtFile(FileContext context) {
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!fileCheckService.checkTxtfile(context,errorMessage)){
            return false;
        }
        String txtFilePathAndName=context.getLocalDeleteFilePath().concat(context.getZipFileName());
        StringBuilder head;
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if(totalLines==0){
            errorMessage.append("文件内容为空");
            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
            return false;
        }

        try {
         head=MyFileUtil.gethead(txtFilePathAndName);
        } catch (IOException e) {
            log.error("获取表头失败",e);
            return false;
        }

        if(!SftpToDbUtils.checkHead(context,head.toString())){
            log.error("文件表头异常-文件名-{}，head-{}",context.getTxtFileName(),head);
            errorMessage.append("文件表头异常");
            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
        }

        log.info("================开始去重============");
        int pageSize= (totalLines + SPLITSIZE-1) /SPLITSIZE;
        File[] files = MyFileUtil.splitFileByCusNum(txtFilePathAndName,pageSize);
        MyFileUtil.distinctByCusNum(files,context.getDistinctTxtFilePath(),context.getDistinctTxtFileName(),pageSize,head);
        log.info("================去重结束============");


        fileCheckService.checkSmallDataFile(context);

        dealErrorResultFile(context);
        log.info("parseConfigFile done");
        String s = redisChgService.get(Constants.INSERT_DB_NUMBER + context.getTxtFileName());
        redisChgService.expire(Constants.INSERT_DB_NUMBER + context.getTxtFileName(),60);
        Integer actualNumber =StringUtils.isNotEmpty(s)?Integer.parseInt(s):0 ;
        LoadResult lr=new LoadResult();
        lr.setApiCode(context.getTask().getApiCode());
        lr.setFileName(context.getTxtFileName());
        lr.setBatchNumber(context.getTask().getBatchNumber());
        lr.setStatus("1");
        lr.setActualNumber(actualNumber);
        lr.setTaskNumber(totalLines-1);
        loadResultMapper.insertLoadResult(lr);

        fileCheckService.volidatorDataVolume(context.getTask().getDataVolume(),totalLines-1,context.getTask().getApiCode(),context.getTxtFileName());

        return true;
    }

    @Override
    public void checkConfigFile(FileContext context) {

    }


    /**
     * 处理三要素校验失败的内容
     * @param context 参数对象
     */
    private void dealErrorResultFile(FileContext context) {

    }
}
