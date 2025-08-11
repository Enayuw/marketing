package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import com.br.marketing.entity.MarketingTcyrCpaFile;
import com.br.marketing.entity.MarketingTcyrCpaSyncRecord;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.enums.TcCpaRecordTypeEnum;
import com.br.marketing.mapper.MarketingTcyrCpaFailRecordMapper;
import com.br.marketing.mapper.MarketingTcyrCpaFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSyncRecordMapper;
import com.br.marketing.service.tccpa.TcCpaSyncDataDownFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TcCpaSyncDataDownFileServiceImpl implements TcCpaSyncDataDownFileService {

    private final static String TITLE = "【同程易融CPA-downFileShard任务】";


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingTcyrCpaSyncRecordMapper tcyrCpaSyncRecordMapper;

    @Resource
    private MarketingTcyrCpaFailRecordMapper tcyrFailRecordMapper;

    @Resource
    private MarketingTcyrCpaFileMapper tcyrCpaFileMapper;

    @Override
    public void process(String apiCode) {
        //1.syncRecord
        try {
            List<MarketingTcyrCpaSyncRecord> syncRecordList = tcyrCpaSyncRecordMapper.searchTcyrSyncRecordList(
                    apiCode, TcCpaRecordStatusEnum.ACCESS_IN.getValue());
            if (!CollectionUtils.isEmpty(syncRecordList)) {
                syncRecordList.forEach(this::dealTcyrCpaSyncRecordFile);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "syncRecord异常"+e.getMessage(), TITLE), e);
        }

        //2.failRecord
        try {
            List<MarketingTcyrCpaFailRecord> failRecordList = tcyrFailRecordMapper.searchTcyrFailRecordList(
                    apiCode, TcCpaRecordStatusEnum.ACCESS_IN.getValue());
            failRecordList.forEach(this::dealTcyrCpaFailRecordFile);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "failRecord异常"+e.getMessage(), TITLE), e);
        }
    }



    private void dealTcyrCpaSyncRecordFile(MarketingTcyrCpaSyncRecord syncRecord) {
        try{
            JSONObject dataJson = JSONObject.parseObject(syncRecord.getData());
            String fileUrl = dataJson.getString("fileUrl");
            //1、gz文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_cpa_sync_upload_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_cpa_sync_"+syncRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"文件下载失败", TITLE));
                return;
            }
            //2、gz解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"对应gz文件不存在", TITLE));
                return;
            }
            String csvFilePath = dirPath+"csv/"+syncRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"解压csv文件不存在", TITLE));
                return;
            }
            //3、txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                MarketingTcyrCpaFile tcyrCpaFile = new MarketingTcyrCpaFile();
                tcyrCpaFile.setApiCode(syncRecord.getApiCode());
                tcyrCpaFile.setBatchNo(syncRecord.getBatchNo());
                tcyrCpaFile.setFileName(csvFile.getName());
                tcyrCpaFile.setFilePath(csvFilePath+csvFile.getName());
                tcyrCpaFile.setSyncRecordId(syncRecord.getId());
                tcyrCpaFile.setCreateTime(nowDate);
                tcyrCpaFile.setType(TcCpaRecordTypeEnum.SYNC_RECORD.getValue());
                tcyrCpaFileMapper.insertSelective(tcyrCpaFile);
            }
            //4、更新 syncRecord 状态
            tcyrCpaSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getId(), 2);
        }catch (Exception e){
            tcyrCpaSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getId(), 3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private void dealTcyrCpaFailRecordFile(MarketingTcyrCpaFailRecord failRecord) {
        try{
            JSONObject dataJson = JSONObject.parseObject(failRecord.getData());
            String fileUrl = dataJson.getString("fileUrl");
            //1、gz文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_cpa_fail_upload_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_cpa_fail"+failRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"文件下载失败", TITLE));
                return;
            }
            //2、gz解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"对应gz文件不存在", TITLE));
                return;
            }
            String csvFilePath = dirPath+"csv/"+failRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"解压csv文件不存在", TITLE));
                return;
            }
            //3、txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                MarketingTcyrCpaFile tcyrCpaFile = new MarketingTcyrCpaFile();
                tcyrCpaFile.setApiCode(failRecord.getApiCode());
                tcyrCpaFile.setBatchNo(failRecord.getBatchNo());
                tcyrCpaFile.setFileName(csvFile.getName());
                tcyrCpaFile.setFilePath(csvFilePath+csvFile.getName());
                tcyrCpaFile.setSyncRecordId(failRecord.getId());
                tcyrCpaFile.setCreateTime(nowDate);
                tcyrCpaFile.setType(TcCpaRecordTypeEnum.FAIL_RECORD.getValue());
                tcyrCpaFileMapper.insertSelective(tcyrCpaFile);
            }
            //4、更新 syncRecord 状态
            tcyrFailRecordMapper.updateTcyrRecordDownStatus(failRecord.getId(), 2);
        }catch (Exception e){
            tcyrFailRecordMapper.updateTcyrRecordDownStatus(failRecord.getId(), 3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }
}
