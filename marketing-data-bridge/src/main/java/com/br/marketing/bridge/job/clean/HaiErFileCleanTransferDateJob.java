package com.br.marketing.bridge.job.clean;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.*;

/**
 * @ClassName HaiErFileCleanTransferDateJob
 * @Description 海尔转化数据每日清洗(sftp->api)-3710018 https://c.100credit.cn/pages/viewpage.action?pageId=204927045
 * @Author kongbx
 * @Date 2025/5/19 16:12
 */
@Component
@Slf4j
public class HaiErFileCleanTransferDateJob extends AbstractSimpleElasticJob {

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;
    @Resource
    private JobManager jobManager;
    @Resource
    SyncConfigMapper syncConfigMapper;
    @Resource
    private PushInfoService pushInfoService;

    private static final String TITLE = "【海尔转化数据每日清洗】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long start = System.currentTimeMillis();

        String apiCode = context.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710018";
        }
        clean(apiCode);

        long end = System.currentTimeMillis();
        log.warn("{}执行完成，耗时{}ms", TITLE, end - start);
    }

    private void clean(String apiCode) {

        //判断今日是否执行过
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().toString())
                .andActionTypeEqualTo(TransferActionFrontActionTypeEnum.ONE.getValue())
                .andStatusEqualTo(2)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (!CollectionUtils.isEmpty(transferActionFronts)) {
            log.warn(TITLE + "今日已执行！");
            return;
        }

        // 新增执行记录 setStatus 任务状态 1-未执行；2-执行结束；3-本地文件已生成
        TransferActionFront transferActionFront = jobManager.saveFront(apiCode, LocalDate.now().toString(), TransferActionFrontActionTypeEnum.ONE.getValue());
        // 获取SFTP配置
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(DataTypeEnum.MARKETINGTRANSFERDATA.getValue())
                .andStatusEqualTo(1)
                .andTypeEqualTo(1);

        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.warn(TITLE + "SFTP配置不存在！");
            return;
        }
        SyncConfig syncConfig = syncConfigs.get(0);
        // 文件处理逻辑
        processFile(apiCode, syncConfig, transferActionFront);
    }

    private void processFile(String apiCode, SyncConfig syncConfig, TransferActionFront transferActionFront) {

        String date = LocalDate.now().toString();
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        String fileName = "BR202501_result_"+ date + ".csv";

        //源文件逻辑处理
        if (srcPath.contains("yyyy-MM-dd")) {
            srcPath = srcPath.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }else if(srcPath.contains("yyyyMMdd")){
            srcPath = srcPath.replace("yyyyMMdd", TimeUtils.getNowDate(TimeUtils.DATE_STRING));
        }

        if (targetPath.contains("yyyy-MM-dd")) {
            targetPath = targetPath.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }else if(targetPath.contains("yyyyMMdd")){
            targetPath = targetPath.replace("yyyyMMdd", TimeUtils.getNowDate(TimeUtils.DATE_STRING));
        }

        syncConfig.setSrcPath(srcPath);
        syncConfig.setTargetPath(targetPath);

        Boolean flag = Boolean.TRUE;
        if(transferActionFront.getStatus() == 1){
            FtpClient ftpClient = new FtpClient(syncConfig, true);
            flag = ftpFileList(ftpClient, syncConfig, fileName, transferActionFront);
        }
        //文件处理
        TransferActionFront front = transferActionFrontMapper.selectByPrimaryKey(transferActionFront.getId());
        if(flag && front.getStatus() == 3){
            workWithFiles(apiCode, syncConfig, fileName, transferActionFront.getId());
        }
    }

    private Boolean ftpFileList(FtpClient ftpClient, SyncConfig syncConfig, String fileName,TransferActionFront transferActionFront) {
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        try {
            ftpClient.connect();
            boolean fileExists = ftpClient.isExistFile(srcPath.concat(fileName));
            if (!fileExists) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HAIER_SERVICEERROR.getCode(), TITLE + "文件不存在："+srcPath.concat(fileName)));
                return Boolean.FALSE;
            }
            // 判断文件创建时间是否超过1分钟
            FTPFile ftpFile = ftpClient.getFtpFile(srcPath, fileName);
            Calendar timestamp = ftpFile.getTimestamp();
            String createFileTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
            long minutes = DateHelper.getDistanceMinutes(createFileTime);
            if (minutes < 1) {
                log.warn(TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理，文件名{},创建时间{}", fileName, createFileTime);
                return Boolean.FALSE;
            }
            //判断.success文件是否存在
            String successName = fileName + ".success";
            boolean successFileExists = ftpClient.isExistFile(srcPath.concat(successName));
            if (!successFileExists) {
                //创建.success文件
                log.warn(TITLE + ".success文件不存在:{}", successName);
                File file = new File(targetPath.concat("success/"));
                if (!file.exists()) {
                    file.mkdirs();
                }
                String targetPathConcat = targetPath.concat("success/").concat(successName);
                File successFile = new File(targetPathConcat);
                if (!successFile.exists()) {
                    successFile.createNewFile();
                }

                log.warn(TITLE + "ftpClient推送success文件本地目录:{},远程目录:{}", targetPathConcat,srcPath+successName);
                ftpClient.uploadFile(Files.newInputStream(Paths.get(targetPathConcat)), srcPath, successName);
                return Boolean.FALSE;
            }
            //判断本地文件是否存在
            File targetFile = new File(targetPath.concat(fileName));
            if (!targetFile.exists()) {
                log.warn(TITLE + "本地文件不存在:{}", targetPath.concat(fileName));
                return Boolean.FALSE;
            }
            //判断本地文件生成时间是否小于2分钟
            Path filePath = targetFile.toPath();
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileTime targetCreationTime = attrs.creationTime();
            String targetCreateFileTime = DateUtils.parseDateTimeByDate(new Date(targetCreationTime.toMillis()), "yyyy-MM-dd HH:mm:ss");
            long targetMinutes = DateHelper.getDistanceMinutes(targetCreateFileTime);
            if (targetMinutes < 1) {
                log.warn(TITLE + "本地文件上传时间距离当前时间小于1分钟，暂时不处理，文件名{},创建时间{}", fileName, targetCreateFileTime);
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error(TITLE + "拉取文件异常", e);
            return Boolean.FALSE;
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error(TITLE + "文件检查任务关闭FTP客户端异常", e);
            }
        }
        jobManager.updateFrontDataStatus(transferActionFront.getId(),3);
        return Boolean.TRUE;
    }


    private void workWithFiles(String apiCode, SyncConfig syncConfig, String fileName, Long jobId) {
        String targetPath = syncConfig.getTargetPath();
        String path = targetPath.concat(fileName);

        //判断文件是否存在
        File tmpFile = new File(path);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        //读取文件并写入数据库
        try (InputStreamReader fReader = new InputStreamReader(Files.newInputStream(Paths.get(path)), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fReader)) {
            String params;
            List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
            while ((params = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(params)) {
                    if (params.contains("登陆日期")) {
                        log.warn(TITLE + "过滤表头params:{}", params);
                        continue;
                    }
                    // 解析数据
                    String[] split = params.split(",");
                    if(split.length < 12){
                        log.warn(TITLE + "列少于12列:{}", split);
                        return;
                    }
                    // UUID,登陆日期,完件日期,是否申请授信,Usertype(场景类型),申请授信日期,授信金额区间,是否授信通过,是否申请提现,申请提现时间,是否提现成功,提现金额区间,
                    String custNum = split[0];
                    //登陆日期
                    String loginTime = split[1];
                    //完件日期
                    String auditTime = split[2];
                    //是否申请授信 1是0否
                    String ifApply = split[3];
                    //Usertype(场景类型)
                    String userType = split[4];
                    //申请授信日期 yyyy-mm-dd hh:mm:ss
                    String applyDt = split[5];
                    //授信金额区间
                    String auditAmount = split[6];
                    //是否授信通过 1是0否
                    String applyResult = split[7];
                    //是否申请提现 1是0否
                    String applyLoan = split[8];
                    //申请提现时间
                    String applyLoanTime = split[9];
                    //是否提现成功 1是0否
                    String ifLent = split[10];
                    //提现金额区间
                    String applyLoanAmount = split[11];


                    TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
                    transferDataItemDTO.setApiCode(apiCode);
                    transferDataItemDTO.setRequestId(apiCode.concat(UUID.randomUUID().toString()));
                    //transferDataItemDTO.setOrgName("");
                    transferDataItemDTO.setCustNum(custNum);
                    //transferDataItemDTO.setSource("");
                    transferDataItemDTO.setUserType(userType);
                    //transferDataItemDTO.setType("");
                    //transferDataItemDTO.setCustomName("");
                    //transferDataItemDTO.setIfRegister("");
                    //transferDataItemDTO.setRegisterTime("");
                    //transferDataItemDTO.setIfLogin("");
                    transferDataItemDTO.setLoginTime(loginTime);
                    transferDataItemDTO.setIfApply(ifApply);
                    transferDataItemDTO.setApplyDt(applyDt);
                    //transferDataItemDTO.setApplyTime("");
                    transferDataItemDTO.setApplyResult(applyResult);
                    //transferDataItemDTO.setRefuseTime("");
                    transferDataItemDTO.setAuditTime(auditTime);
                    transferDataItemDTO.setAuditAmount(auditAmount);
                    transferDataItemDTO.setIfLent(ifLent);
                    //transferDataItemDTO.setLentTime("");
                    //transferDataItemDTO.setLentAmount("");
                    //transferDataItemDTO.setUnlentAmount("");
                    //transferDataItemDTO.setIfSettle("");
                    //transferDataItemDTO.setSettleTime("");
                    //transferDataItemDTO.setActivity("");
                    //transferDataItemDTO.setCaseStatus("");
                    //transferDataItemDTO.setCaseEffective("");
                    //transferDataItemDTO.setIfTransform("");
                    //transferDataItemDTO.setTransformTime("");
                    //transferDataItemDTO.setInsertTime("");
                    transferDataItemDTO.setReserveField1("");
                    //transferDataItemDTO.setReserveField2("");

                    transferDataItemDTOS.add(transferDataItemDTO);

                }
            }
            if (CollectionUtils.isEmpty(transferDataItemDTOS)) {
                return;
            }
            PushTransferDataDetailDTO dto = initTransferData(apiCode,transferDataItemDTOS);
            Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
            log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}",TITLE,pushResult.getCode(),pushResult.isSuccess(),pushResult.getMessage());
        } catch (Exception e) {
            log.error(TITLE + "处理出错", e);
        }
    }

    private PushTransferDataDetailDTO initTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+System.currentTimeMillis()+"_"+randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }

}
