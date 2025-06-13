package com.br.marketing.service.tc.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.service.tc.TcSyncDataDownFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 同城易融拉取GZ文件 TXT信息入库-Service实现
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcSyncDataDownFileServiceImpl implements TcSyncDataDownFileService {

    private final static String TITLE = "【同程易融-DownFile任务】";

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;

    /**
     *  具体的下载文件->TXT信息入库
     * @param syncRecord
     * @return
     */
    @Override
    public Result dealTcyrTxtFileSync(MarketingTcyrSyncRecord syncRecord) {
        Result result = new Result<>().failure();
        try{
            String dataInfo = syncRecord.getData();
            if (StringUtils.isEmpty(dataInfo)) {
                log.warn("apiCode:{},batchNo:{} 下载数据为空",syncRecord.getApiCode(),syncRecord.getBatchNo());
                return result.failure();
            }
            JSONObject dataJson = JSONObject.parseObject(dataInfo);
            String fileUrl = dataJson.getString("fileUrl");
            if (StringUtils.isEmpty(fileUrl)) {
                log.warn("apiCode:{},batchNo:{},fileUrl:{} 下载链接为空",syncRecord.getApiCode(),syncRecord.getBatchNo(),fileUrl);
                return result.failure();
            }
            //文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_customize_upload_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_"+syncRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn("{},batchNo:{} 下载gz包失败",TITLE,syncRecord.getBatchNo());
                return result.failure();
            }
            log.warn("{},batchNo:{} 下载gz包成功",TITLE,syncRecord.getBatchNo());

            // 解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn("{}_batchNo:{} 对应gz文件不存在",TITLE,syncRecord.getBatchNo());
                return result.failure();
            }
            String csvFilePath = dirPath+"csv/"+syncRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            log.warn(TITLE + "解压zip包成功");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(TITLE + "解压csv文件不存在");
                return result.failure();
            }

            //txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                MarketingTcyrSyncFile tcyrSyncFile = new MarketingTcyrSyncFile();
                tcyrSyncFile.setApiCode(syncRecord.getApiCode());
                tcyrSyncFile.setBatchNo(syncRecord.getBatchNo());
                tcyrSyncFile.setFileName(csvFile.getName());
                tcyrSyncFile.setFilePath(csvFilePath+csvFile.getName());
                tcyrSyncFile.setTotalCount(0L);
                tcyrSyncFile.setSuccessCount(0L);
                tcyrSyncFile.setStatus(1);
                tcyrSyncFile.setDealStatus(0);
                tcyrSyncFile.setIsDel(1);
                tcyrSyncFile.setCreateTime(nowDate);
                tcyrSyncFile.setUpdateTime(nowDate);
                tcyrSyncFileMapper.insertSelective(tcyrSyncFile);
            }
            result = result.success();
        }catch (Exception e){
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
        return result;
    }

    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }

}
