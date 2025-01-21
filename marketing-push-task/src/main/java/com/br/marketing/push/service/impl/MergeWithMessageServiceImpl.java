package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.bi.BiApiClient;
import com.br.marketing.client.bi.input.OffLineScoreDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.RegexConstants;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.dto.TaskExtendExtendFieldDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.sftp.PushFinishSucService;
import com.br.marketing.service.sftp.PushService;
import com.br.marketing.service.sftp.PushToSftpService;
import com.br.marketing.service.sftp.impl.PushToSftpServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MergeWithMessageServiceImpl {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    LoanFileMapper loanFileMapper;

    @Autowired
    MergeServiceImpl mergeService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    CustomerMapper customerMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Autowired
    BiApiClient biApiClient;

    @Value("${otherConfig.warning.ftpBasePath:00}")
    private String ftpBasePath;
    @Value("${otherConfig.warning.ftpHost:00}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort:00}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUsername:00}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd:00}")
    private String ftpPwd;

    @Autowired
    MarketingHistoryEsService marketingHistoryEsService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MarketingTaskService marketingTaskService;

    @Autowired
    PushService pushService;
    @Autowired
    RedisChgService redisChgService;
    @Autowired
    PushToSftpService pushToSftpService;
    @Resource
    RetryMainLogMapper retryMainLogMapper;

    public Result<Boolean> consumerInitFileMsg(Long fileId) {
        Boolean res = Boolean.FALSE;
        List<LoanFile> loanFiles = loanFileMapper.queryFileById(fileId);
        if (loanFiles.size() <= 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }
        LoanFile file = loanFiles.get(0);
        Customer customer = customerMapper.getCustomerByApiCode(file.getApiCode());
        List<LoanFile> pushList = mergeService.process(loanFiles, customer);
        if (pushList != null && pushList.size() > 0) {
            Result result = pushToSftpService.pushFiles(pushList);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                RetryMainLog retryMainLog = new RetryMainLog();
                retryMainLog.setRetryType(1);
                retryMainLog.setRetryParam(JSON.toJSONString(pushList));
                retryMainLog.setRetryParamType(List.class.getName());
                retryMainLog.setRetryService(PushToSftpServiceImpl.class.getName());
                retryMainLog.setRetryMethod("pushFiles");
                retryMainLog.setServiceType(2);
                retryMainLog.setRetryNum(0);
                retryMainLog.setRetryStatus(1);
                retryMainLog.setCreateTime(new Date());
                retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                retryMainLog.setRetryMaxNum(3);
                retryMainLogMapper.insertSelective(retryMainLog);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }

    /**
     * 消费文件合并信息
     *
     * @param fileId
     * @return
     */
    public Result<Boolean> consumerFileMsg(Long fileId) {
        Boolean res = Boolean.FALSE;
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
        LoanFile loanFile = new LoanFile();
        if (straHisFile != null && straHisFile.getStatus().equals(ScoreStatusEnum.OFFLINEMERGE.getValue())) {
            try {
                Customer customer = customerMapper.getCustomerByApiCode(straHisFile.getApiCode());

                BeanUtils.copyProperties(straHisFile, loanFile);
                loanFile.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(straHisFile.getCreateTime()));
                long start = System.currentTimeMillis();
                String s = mergeService.mergeResultFile(loanFile, customer);
                if (StringUtils.isNotBlank(s)) {
                    log.warn(String.format("离线文件合并统计 文件id：%d,耗时：%d", fileId, System.currentTimeMillis() - start));
                    BeanUtils.copyProperties(loanFile, straHisFile);
                    straHisFile.setStatus(ScoreStatusEnum.OFFLINESFP.getValue());
                    straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        if (straHisFile != null && straHisFile.getStatus() == 5) {
            straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
            Boolean aBoolean = pushToFtp(straHisFile);
            if (aBoolean) {
                Result result = reqOffLine(straHisFile);
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    straHisFile.setStatus(ScoreStatusEnum.OFFLINECALLBACK.getValue());
                    straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
                } else {
                    res = Boolean.TRUE;
                }
            } else {
                res = Boolean.TRUE;
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }


    public Boolean pushToFtp(StraHisFile blf) {
        String apiCode = blf.getApiCode();
        FtpClient ftpClient = new FtpClient(ftpHost, ftpPort, ftpUsername, ftpPwd, ftpBasePath);
        try {
            ftpClient.connect();
            String remotePath = ftpBasePath.concat(apiCode).concat("/output/").concat(DateHelper.getDateAddYyMmDd(0)).concat("/");
            String filePathAndName = blf.getFilePath().concat("/").concat(blf.getFileName());
            File file = new File(filePathAndName);
            if (file.exists()) {
                log.warn("push txt to sftp :{}", blf.getFileName());
                ftpClient.uploadFileAndMk(Files.newInputStream(Paths.get(filePathAndName)), remotePath, blf.getFileName());
                String sueccessFilePath = filePathAndName.concat(".success");
                File successFile = new File(sueccessFilePath);
                if (!successFile.exists()) {
                    successFile.createNewFile();
                }
                ftpClient.uploadFileAndMk(Files.newInputStream(Paths.get(sueccessFilePath)), remotePath, blf.getFileName().concat(".success"));
            }
            blf.setInnerFtpPath(remotePath);
            return true;
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
        return false;
    }

    private Result reqOffLine(StraHisFile file) {
        String batchNumber = file.getBatchNumber();
        MarketingTask task = marketingTaskMapper.getByBatchNumber(batchNumber);
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andTaskIdEqualTo(task.getId());
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(extendExample);
        MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
        Integer encodeType = 1;
        if (StringUtils.isNotBlank(taskExtend.getExtendConfigInfo())) {
            TaskExtendExtendFieldDTO taskExtendExtendFieldDTO = JSON.parseObject(taskExtend.getExtendConfigInfo(), TaskExtendExtendFieldDTO.class);
            encodeType = taskExtendExtendFieldDTO.getThreekEncryptType() != null ? taskExtendExtendFieldDTO.getThreekEncryptType() : 1;
        }

        StringBuilder head = new StringBuilder();
        task.setIsOnline(1);
        iProductResultSimpleService.initHead(head, ",", task);
        OffLineScoreDTO scoreDTO = new OffLineScoreDTO();
        scoreDTO.setRequestId(file.getId().toString());
        scoreDTO.setProductInfo(JSONArray.parseArray(task.getProductInfo()));
        scoreDTO.setHeadInfo(head.toString());
        scoreDTO.setFilePath(file.getInnerFtpPath());
        scoreDTO.setFileName(file.getFileName());
        scoreDTO.setEncodeType(encodeType.toString());
        return biApiClient.reqOffLineJob(scoreDTO);
    }

    public Result consumerFileCallBack(Long fileId) {
        Boolean res = Boolean.FALSE;
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
        if (straHisFile.getStatus().equals(ScoreStatusEnum.OFFLINESUCCESS.getValue())) {
            String s = downFile(straHisFile);
            if (StringUtils.isBlank(s)) {
                res = Boolean.TRUE;
            } else {
                long start = System.currentTimeMillis();
                Boolean aBoolean = fileAction(straHisFile, s);
                if (aBoolean) {
                    log.warn(String.format("离线文件解析入es统计 文件id：%d,耗时：%d", fileId, System.currentTimeMillis() - start));
                    upLoadSuccess(straHisFile);
                }
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }

    private void upLoadSuccess(StraHisFile file) {
        FtpClient ftpClient = new FtpClient(ftpHost, ftpPort, ftpUsername, ftpPwd, ftpBasePath);
        try {
            ftpClient.connect();
            String offlineFilePath = file.getOfflineFilePath();
            String zipfileName = file.getZipfileName();
            String remotePath = offlineFilePath.concat("/");
            String localPathName = file.getFilePath().concat("/").concat(file.getZipfileName()).concat(".success");
            File localSuccess = new File(localPathName);
            if (!localSuccess.exists()) {
                localSuccess.createNewFile();
            }
            ftpClient.uploadFileAndMk(Files.newInputStream(Paths.get(localPathName)), remotePath, zipfileName.concat(".success"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String downFile(StraHisFile file) {
        String offlineFilePath = file.getOfflineFilePath();
        String zipfileName = file.getZipfileName();
        String remotePathName = offlineFilePath.concat("/").concat(zipfileName);
        FtpClient ftpClient = new FtpClient(ftpHost, ftpPort, ftpUsername, ftpPwd, ftpBasePath);
        String localPathName = file.getFilePath().concat("/").concat(file.getZipfileName());
        try {
            ftpClient.connect();
            if (ftpClient.isExsits(remotePathName.concat(".suc"))) {
                boolean download = ftpClient.download(remotePathName, new File(localPathName));
                if (download) {
                    return localPathName;
                } else {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Boolean fileAction(StraHisFile file, String s) {
        File zipFile = new File(s);
        String unZipPath = file.getFilePath().concat("/").concat("unzip").concat("/");
        ZipUtils.unZip(zipFile, unZipPath, "");
        File dic = new File(unZipPath);
        ArrayList<File> files = new ArrayList<>();
        getFiles(dic, files);
        MarketingTask task = marketingTaskMapper.getByBatchNumber(file.getBatchNumber());
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andTaskIdEqualTo(task.getId());
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(extendExample);
        MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
        Integer enc = 1;
        if (StringUtils.isNotBlank(taskExtend.getExtendConfigInfo())) {
            TaskExtendExtendFieldDTO taskExtendExtendFieldDTO = JSON.parseObject(taskExtend.getExtendConfigInfo(), TaskExtendExtendFieldDTO.class);
            enc = taskExtendExtendFieldDTO.getThreekEncryptType() != null ? taskExtendExtendFieldDTO.getThreekEncryptType() : 1;
        }

        final List<String> baseFields;
        final List<String> hxFields;
        Result<String> headRes = iProductResultSimpleService.getCurrentBaseHeadInfoByTaskId(task.getId(),task.getScoreSeparator());
        if (ResultCode.SUCCESS.getValue().equals(headRes.getCode()) && StringUtils.isNotBlank(headRes.getData())) {
            baseFields = Arrays.stream(headRes.getData().split(task.getScoreSeparator())).collect(Collectors.toList());
        } else {
            baseFields = new ArrayList<>();
        }
        Result<List<String>> fieldsInfo = iProductResultSimpleService.getFieldsInfo(file.getApiCode(), file.getBatchNumber());
        if (ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())) {
            hxFields = fieldsInfo.getData();
        } else {
            hxFields = new ArrayList<>();
        }
        Integer offLineInserEsThreadNum = marketingCommonConfig.getOffLineInserEsThreadNum();
        Integer threadNum = offLineInserEsThreadNum != null && offLineInserEsThreadNum > 0 ? offLineInserEsThreadNum : 50;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer sumNum = 0;
        Long index = 0L;
        for (File f : files) {
            try {
                FileReader read = new FileReader(f);
                BufferedReader br = new BufferedReader(read);
                String ss = "";
                Integer number = 0;
                List<String> heads = new ArrayList<>();
                while ((ss = br.readLine()) != null) {
                    number++;
                    sumNum++;
                    index++;
                    if (number == 1) {
                        heads = Arrays.stream(ss.split(",")).collect(Collectors.toList());
                    } else {
                        final String content = ss;
                        final List<String> titles = heads;
                        String part = marketingTaskService.getPart(sumNum, index).toString();
                        threadPool.submit(new EsRun(ss, heads, file, hxFields, baseFields, enc, JSON.parseArray(task.getProductInfo()), part));
                    }
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        String md5 = "";
        try {
            md5 = MyFileUtil.getMd5(new FileInputStream(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StraHisFile updateFile = new StraHisFile();
        updateFile.setStatus(ScoreStatusEnum.PUSH.getValue());
        updateFile.setScoreStatus(2);
        updateFile.setId(file.getId());
        updateFile.setMd5(md5);
        straHisFileMapper.updateByPrimaryKeySelective(updateFile);
        return true;
    }

    public class EsRun implements Runnable {

        private String content;

        private List<String> heads;

        private StraHisFile file;

        private List<String> hxFields;

        private List<String> baseFields;

        private Integer encryptionType;

        private JSONArray products;

        private String part;

        public EsRun(String content, List<String> heads, StraHisFile file
                , List<String> hxFields, List<String> baseFields
                , Integer encryptionType, JSONArray products
                , String part) {
            this.content = content;
            this.heads = heads;
            this.file = file;
            this.hxFields = hxFields;
            this.baseFields = baseFields;
            this.encryptionType = encryptionType;
            this.products = products;
            this.part = part;
        }

        @Override
        public void run() {
            String[] split = content.split(",", -1);
            HashMap<String, String> row = new HashMap<>();
            for (int i = 0; i < split.length; i++) {
                String value = split[i];
                String name = heads.get(i);
                row.put(name.toLowerCase(), value);
            }
            MarketingHistory history = new MarketingHistory();
            history.setApiCode(file.getApiCode());
            history.setIdCard(threeKdec(StringUtils.isBlank(row.get("id"))
                    ? (StringUtils.isBlank(row.get("idcard")) ? "" : row.get("idcard"))
                    : row.get("id"), "id", encryptionType));
            history.setCell(threeKdec(row.get("cell"), "cell", encryptionType));
            history.setName(threeKdec(row.get("name"), "name", encryptionType));
            history.setPart(part);
            history.setScoreTime(System.currentTimeMillis());
            try {
                history.setRequestTime(new SimpleDateFormat("yyyy-MM-dd").parse(row.get("request_time")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            history.setBatchNumber(file.getBatchNumber());
            history.setSwiftNumber("");
            history.setHxSwiftNumber("");
            history.setCusNum(row.get("cus_num"));
            history.setStrategyId(row.get("strategy_id"));
            history.setVersion(row.get("version"));
            ArrayList<MarketingCondition> conditions = new ArrayList<>();
            history.setCondition(conditions);
            history.setFileId(file.getId().toString());
            history.setReserveField("");
            history.setTaskId(row.get("taskid"));
            history.setUserType(row.get("usertype"));

            //region hx字段
            for (String hxField : hxFields) {
                Optional<Object> codeOpt = products.stream().filter(t -> hxField.toLowerCase().equals(((JSONObject) t).getString("code").toLowerCase())).findFirst();
                MarketingCondition marketingCondition = new MarketingCondition();
                marketingCondition.setFieldKey(hxField);
                String s = row.get(hxField);
                marketingCondition.setStrValue(s);
                if (codeOpt.isPresent()) {
                    JSONObject jo = (JSONObject) codeOpt.get();
                    String code = jo.getString("code");
                    String version = jo.getString("version");
                    marketingCondition.setCode(code);
                    marketingCondition.setVersion(version);
                    if (StringUtils.isNotBlank(s)) {
                        if(Pattern.compile(RegexConstants.Numeric).matcher(s).matches()){
                            marketingCondition.setDValue(Double.valueOf(s));
                        }
                        Long date = DateHelper.strToMill(s);
                        if(date !=null){
                            marketingCondition.setLValue(date);
                        }
                    }
                } else {
                    if (StringUtils.isNotBlank(s)) {
                        if(Pattern.compile(RegexConstants.Numeric).matcher(s).matches()){
                            marketingCondition.setDValue(Double.valueOf(s));
                        }
                        Long date = DateHelper.strToMill(s);
                        if(date !=null){
                            marketingCondition.setLValue(date);
                        }
                    }
                }
                conditions.add(marketingCondition);
            }
            //endregion

            //region 用户上传字段
            for (String baseField : baseFields) {
                if (baseField.toLowerCase().equals("cell")
                        || baseField.toLowerCase().equals("id")
                        || baseField.toLowerCase().equals("idcard")
                        || baseField.toLowerCase().equals("name")
                        || baseField.toLowerCase().equals("taskid")
                        || baseField.toLowerCase().equals("usertype")) {
                    continue;
                }
                MarketingCondition marketingCondition = new MarketingCondition();
                marketingCondition.setFieldKey(baseField);
                String s = row.get(baseField.toLowerCase());
                marketingCondition.setStrValue(s);
                if (StringUtils.isNotBlank(s)) {
                    if(Pattern.compile(RegexConstants.Numeric).matcher(s).matches()){
                        marketingCondition.setDValue(Double.valueOf(s));
                    }
                    Long date = DateHelper.strToMill(s);
                    if(date !=null){
                        marketingCondition.setLValue(date);
                    }
                }
                conditions.add(marketingCondition);
            }
            //endregion
            String id = UuidUtils.getUuid();
            marketingHistoryEsService.insert(history, id);
        }


    }

    private String threeKdec(String str, String type, Integer encryptionType) {
        if (StringUtils.isNotBlank(str)) {
            if (encryptionType.equals(1)) {
                String s = RpcClientProxy.decode(str, type, "md5", "");
                return StringUtils.isNotBlank(s) ? BrCipherMaker.getInstance().encode(s) : "";
            } else if (encryptionType.equals(2)) {
                String s = RpcClientProxy.decode(str, type, "sha", "");
                return StringUtils.isNotBlank(s) ? BrCipherMaker.getInstance().encode(s) : "";
            } else {
                return str;
            }
        } else {
            return "";
        }
    }


    private void getFiles(File file, List<File> files) {
        if (file.isFile()) {
            files.add(file);
        }
        if (file.isDirectory()) {
            File[] files1 = file.listFiles();
            for (File file1 : files1) {
                getFiles(file1, files);
            }
        }
    }
}
