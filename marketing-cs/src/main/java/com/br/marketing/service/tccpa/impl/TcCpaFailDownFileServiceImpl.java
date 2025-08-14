package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrCpaFailFile;
import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.MarketingTcyrCpaFailFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaFailRecordMapper;
import com.br.marketing.service.tccpa.TcCpaFailDownFileService;
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
public class TcCpaFailDownFileServiceImpl implements TcCpaFailDownFileService{

    private final static String TITLE = "【同程易融CPA:FAIL-downFileShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrCpaFailRecordMapper tcyrCpaFailRecordMapper;


    @Resource
    private MarketingTcyrCpaFailFileMapper tcyrCpaFailFileMapper;
    @Resource
    private TcServiceClient tcServiceClient;


    @Override
    public void process(String apiCode) {
        try {
            List<MarketingTcyrCpaFailRecord > failRecordList = tcyrCpaFailRecordMapper.searchTcyrFailRecordList(apiCode,
                    TcCpaRecordStatusEnum.ACCESS_SUCCESS.getValue(), TcCpaDownStatusEnum.DEAL_NO.getValue());
            failRecordList.forEach(this::dealTcyrCpaFailRecordFile);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "failRecord异常"+e.getMessage(), TITLE), e);
        }
    }


    private void dealTcyrCpaFailRecordFile(MarketingTcyrCpaFailRecord failRecord) {
        try{
            JSONObject dataJson = JSONObject.parseObject(failRecord.getData());
            String fileUrl = dataJson.getString("fileUrl");
            //1、gz文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_cpa_fail_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_cpa_fail"+failRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"文件下载失败", TITLE));
                return;
            }
            //2、gz解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"对应gz文件不存在", TITLE));
                return;
            }
            String csvFilePath = dirPath+"csv/"+failRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        failRecord.getBatchNo()+"解压csv文件不存在", TITLE));
                return;
            }
            //3、txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                String filePath = csvFilePath + csvFile.getName();
                MarketingTcyrCpaFailFile oldFile = tcyrCpaFailFileMapper.selectFileByFilePath(failRecord.getApiCode(),filePath);
                if (oldFile == null) {
                    log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                    MarketingTcyrCpaFailFile tcyrCpaFailFile = new MarketingTcyrCpaFailFile();
                    tcyrCpaFailFile.setApiCode(failRecord.getApiCode());
                    tcyrCpaFailFile.setBatchNo(failRecord.getBatchNo());
                    tcyrCpaFailFile.setFileName(csvFile.getName());
                    tcyrCpaFailFile.setFilePath(filePath);
                    tcyrCpaFailFile.setSyncRecordId(failRecord.getId());
                    tcyrCpaFailFile.setCreateTime(nowDate);
                    tcyrCpaFailFile.setUpdateTime(nowDate);
                    tcyrCpaFailFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.DEAL_NO.getValue());
                    tcyrCpaFailFile.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
                    tcyrCpaFailFileMapper.insertSelective(tcyrCpaFailFile);
                }
            }
            //4、更新 syncRecord 状态
            tcyrCpaFailRecordMapper.updateTcyrRecordDownStatus(failRecord.getId(),TcCpaDownStatusEnum.DEAL_SUCCESS.getValue());
        }catch (Exception e){
            tcyrCpaFailRecordMapper.updateTcyrRecordDownStatus(failRecord.getId(), TcCpaDownStatusEnum.DEAL_FAIL.getValue());
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }
}
