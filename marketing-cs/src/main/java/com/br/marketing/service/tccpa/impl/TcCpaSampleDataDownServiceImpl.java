package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.ZipFileClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrCpaSampleRecord;
import com.br.marketing.enums.TcCpaDownStatusEnum;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaSampleRecordMapper;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.tc.TcSampleDataDownService;
import com.br.marketing.service.tccpa.TcCpaSampleDataDownService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description TcSampleDataDownService
 * @Author xiong.luo
 * @CreateTime 2025/08/13
 */
@Service
@Slf4j
public class TcCpaSampleDataDownServiceImpl implements TcCpaSampleDataDownService {
    private static final String TITLE = "【同程易融-CPA-正负样本数据下载】";

    @Autowired
    private MarketingTcyrCpaSampleRecordMapper tcyrCpaSampleRecordMapper;

    @Autowired
    private SyncConfigService syncConfigService;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private SftpInnerServiceImpl sftpInnerService;

    @Resource
    private ZipFileClient zipFileClient;

    @Override
    public void process(String apiCode) {
        List<MarketingTcyrCpaSampleRecord> sampleRecords = tcyrCpaSampleRecordMapper.searchTcyrSyncList(apiCode,
                TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue(), getStartOfDay(), getEndOfDay());

        for (MarketingTcyrCpaSampleRecord sampleRecord : sampleRecords) {
            String filePath = apiCode.concat(marketingCommonConfig.getTongChengCpaSampleZipFilePath());
            // 状态置为下载中
            tcyrCpaSampleRecordMapper.updageTcyrSampleRecordDownStatus(sampleRecord.getBatchNo(), TcCpaDownStatusEnum.DEALING.getValue());

            try {
                String dataInfo = sampleRecord.getData();
                if (StringUtils.isEmpty(dataInfo)) {
                    log.warn("apiCode:{},batchNo:{} 下载数据为空", sampleRecord.getApiCode(), sampleRecord.getBatchNo());
                    return;
                }
                JSONObject dataJson = JSONObject.parseObject(dataInfo);
                String fileUrl = dataJson.getString("fileUrl");
                if (StringUtils.isEmpty(fileUrl)) {
                    log.warn("apiCode:{},batchNo:{},fileUrl:{} 下载链接为空", sampleRecord.getApiCode(), sampleRecord.getBatchNo(), fileUrl);
                    return;
                }

                //文件下载
                String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
                String dirPath = syncConfigService.getPath().concat(filePath).concat(yyyyMMdd).concat("/");
                String orgFileName = extractFileNameFromUrl(fileUrl);
                String gzFileName = StringUtils.isEmpty(orgFileName) ? marketingCommonConfig.getTongChengCpaDefaultFileName()
                        + sampleRecord.getBatchNo() + ".csv.zip" : orgFileName + ".zip";
                String gzFilePath = dirPath.concat(gzFileName);
                Result callFileResult = zipFileClient.downloadFile(fileUrl, gzFilePath, true);
                if (callFileResult == null || !callFileResult.isSuccess()) {
                    log.warn("{},batchNo:{} 下载gz包失败", TITLE, sampleRecord.getBatchNo());
                    return;
                }
                log.warn("{},batchNo:{} 下载gz包成功", TITLE, sampleRecord.getBatchNo());


                File gzFile = new File(gzFilePath);
                if (!gzFile.exists() || !gzFile.getName().contains(".zip")) {
                    log.warn("{}_batchNo:{} 对应zip文件不存在", TITLE, sampleRecord.getBatchNo());
                    return;
                }
                sftpInnerService.pushInnerSftp(gzFilePath, filePath, gzFileName);
                tcyrCpaSampleRecordMapper.updageTcyrSampleRecordDownStatus(sampleRecord.getBatchNo(), TcCpaDownStatusEnum.DEAL_SUCCESS.getValue());
            } catch (Exception e) {
                tcyrCpaSampleRecordMapper.updageTcyrSampleRecordDownStatus(sampleRecord.getBatchNo(), TcCpaDownStatusEnum.DEAL_FAIL.getValue());
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            }
        }
    }

    public static Date getStartOfDay() {
        return TcSampleDataDownService.getStartOfDay();
    }

    // 获取当天的结束时间 (23:59:59.999999999)
    public static Date getEndOfDay() {
        return TcSampleDataDownService.getEndOfDay();
    }

    private String extractFileNameFromUrl(String url) {
        try {
            // 移除预签名url参数
            String cleanUrl = url.split("\\?")[0];
            // 获取最后一个斜杠后的内容
            String fileName = cleanUrl.substring(url.lastIndexOf('/') + 1);

            // 验证文件名是否有效
            if (!fileName.isEmpty() && !fileName.equals("/")) {
                return fileName;
            }
        } catch (Exception e) {
            log.warn("Failed to extract filename from URL: {}", url);
        }
        return null;
    }
}
