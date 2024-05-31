package com.br.marketing.bridge.job.jinmeixin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.FtpClient;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 金美信黑名单文件检查任务
 *
 * @author senyang.zheng
 * @date 2024/05/27
 */
@Component
@Slf4j
public class JinMeiXinBlackListFileCheckJob extends AbstractSimpleElasticJob {

    private final static String FILE_HEADER = "custNum,type";
    public static final int MAX_RETRY_COUNT = 3;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private SyncConfigService syncConfigService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String param = context.getJobParameter();
        String date = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_FORMAT);

        if (StringUtils.isNotEmpty(param)) {
            try {
                date = DateUtil.format(DateUtil.parse(param), "yyyyMMdd");
            } catch (Exception e) {
                log.error("金美信黑名单文件检查任务 输入参数非日期格式，输入参数:{}", param);
            }
        }
        JSONObject config = JSONObject.parseObject(marketingCommonConfig.getJinMeiXinBlackListFileCheckConfig());
        FtpClient client = new FtpClient(config.getString("host"), config.getInteger("port"), config.getString("username"),
            config.getString("password"), config.getString("filePath"));
        String filePath = config.getString("filePath") + date + "/";
        String fileName = "tousu_" + date + ".txt";
        String successName = fileName + ".success";
        String descPath = syncConfigService.getPath().concat("blackList/jinmeixin/").concat(date).concat("/");
        boolean success = false;
        for (int retry = 0; retry <= MAX_RETRY_COUNT; retry++) {
            try {
                client.connect();
                if (!client.isExist(filePath)) {
                    log.warn("金美信黑名单文件路径不存在，创建路径并上传空文件");
                    client.mkdir(filePath);
                    uploadEmptyAndSuccessFile(descPath, fileName, successName, client, filePath);
                    success = true;
                    break;
                }
                boolean fileExists = client.isExsits(filePath.concat(fileName));
                boolean successFileExists = client.isExsits(filePath.concat(successName));
                if (!fileExists || !successFileExists) {
                    log.warn("黑名单文件:{}, 标识文件:{}", fileExists ? "存在" : "不存在", successFileExists ? "存在" : "不存在");
                    uploadEmptyAndSuccessFile(descPath, fileName, successName, client, filePath);
                }
                success = true;
                break;
            } catch (Exception e) {
                log.warn("第{}次任务异常", retry + 1, e);
                try {
                    Thread.sleep(config.getInteger("interval"));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                try {
                    client.disconnect();
                } catch (Exception e) {
                    log.error("金美信黑名单文件检查任务关闭FTP客户端异常", e);
                }
            }
        }

        if (!success) {
            log.error("金美信黑名单文件检查任务失败，重试三次后仍然失败");
        }
    }

    private static void uploadEmptyAndSuccessFile(String descPath, String fileName, String successName, FtpClient client, String filePath)
        throws Exception {
        File dir = new File(descPath);
        File file = new File(descPath + fileName);
        File successFile = new File(descPath + successName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            log.warn("创建本地文件路径{}", mkdirs);
        }
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            fw.append(FILE_HEADER);
        } catch (Exception e) {
            log.error("写入金美信黑名单空文件表头错误！", e);
        }
        client.uploadFile(Files.newInputStream(Paths.get(descPath + fileName)), filePath, fileName);
        if (!successFile.exists()) {
            successFile.createNewFile();
        }
        client.uploadFile(Files.newInputStream(Paths.get(descPath + successName)), filePath, successName);
    }
}
