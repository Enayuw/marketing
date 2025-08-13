package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessRecordMapper;
import com.br.marketing.service.tccpa.TcCpaSuccessDownFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TcCpaSuccessDownFileServiceImpl implements TcCpaSuccessDownFileService {

    private final static String TITLE = "【同程易融CPA:SUCCESS-downFileShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingTcyrCpaSuccessRecordMapper tcyrCpaSuccessRecordMapper;
    @Resource
    private MarketingTcyrCpaSuccessFileMapper tcyrCpaSuccessFileMapper;


    @Override
    public void process(String apiCode) {
        try {
            List<MarketingTcyrCpaSuccessRecord> successRecordList = tcyrCpaSuccessRecordMapper.searchTcyrSyncRecordList(apiCode,
                    TcCpaRecordStatusEnum.ACCESS_SUCCESS.getValue(), TcCpaDownStatusEnum.DEAL_NO.getValue());
            successRecordList.forEach(this::dealTcyrCpaSyncRecordFile);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "syncRecord异常"+e.getMessage(), TITLE), e);
        }
    }

    private void dealTcyrCpaSyncRecordFile(MarketingTcyrCpaSuccessRecord successRecord) {
        try{
            JSONObject dataJson = JSONObject.parseObject(successRecord.getData());
            String fileUrl = dataJson.getString("fileUrl");
            //1、gz文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_cpa_success_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_cpa_success_"+successRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        successRecord.getBatchNo()+"文件下载失败", TITLE));
                return;
            }
            //2、gz解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        successRecord.getBatchNo()+"对应gz文件不存在", TITLE));
                return;
            }
            String csvFilePath = dirPath+"csv/"+successRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        successRecord.getBatchNo()+"解压csv文件不存在", TITLE));
                return;
            }
            //3、txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                String filePath = csvFilePath + csvFile.getName();
                MarketingTcyrCpaSuccessFile oldFile = tcyrCpaSuccessFileMapper.selectFileByFilePath(successRecord.getApiCode(),filePath);
                if (oldFile == null) {
                    log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                    MarketingTcyrCpaSuccessFile tcyrCpaSuccessFile = new MarketingTcyrCpaSuccessFile();
                    tcyrCpaSuccessFile.setApiCode(successRecord.getApiCode());
                    tcyrCpaSuccessFile.setBatchNo(successRecord.getBatchNo());
                    tcyrCpaSuccessFile.setFileName(csvFile.getName());
                    tcyrCpaSuccessFile.setFilePath(filePath);
                    tcyrCpaSuccessFile.setSyncRecordId(successRecord.getId());
                    tcyrCpaSuccessFile.setCreateTime(nowDate);
                    tcyrCpaSuccessFile.setUpdateTime(nowDate);
                    tcyrCpaSuccessFile.setSyncDataDealStatus(TcCpaSyncDealStatusEnum.DEAL_NO.getValue());
                    tcyrCpaSuccessFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.DEAL_NO.getValue());
                    tcyrCpaSuccessFile.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
                    tcyrCpaSuccessFileMapper.insertSelective(tcyrCpaSuccessFile);
                }
            }
            //4、更新 syncRecord 状态
            tcyrCpaSuccessRecordMapper.updateTcyrRecordDownStatus(successRecord.getId(), TcCpaDownStatusEnum.DEAL_SUCCESS.getValue());
        }catch (Exception e){
            tcyrCpaSuccessRecordMapper.updateTcyrRecordDownStatus(successRecord.getId(), TcCpaDownStatusEnum.DEAL_FAIL.getValue());
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }
}
