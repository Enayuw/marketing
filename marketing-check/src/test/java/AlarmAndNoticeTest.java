import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.RsTransferServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by Bairong on 2020/7/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class AlarmAndNoticeTest {
    protected final static Logger log = LoggerFactory.getLogger(AlarmAndNoticeTest.class);
    @Resource
    EmailService businessAlarmServiceImpl;
    @Resource
    EmailService reportServiceImpl;

    @Resource
    EmailService validDataAlarmServiceImpl;

    @Resource
    LoanFileMapper loanFileMapper;

    @Resource
    NewTransferToFileByXieChengServiceImpl newTransferToFileByXieChengService;

    @Resource
    TransferToFileByDiDiServiceImpl transferToFileByDiDiService;

    @Autowired
    SyncConfigService syncConfigService;

    @Resource
    TransferFileTaskMapper transferFileTaskMapper;

    final static String FILE_HEADER = "taskId,custNum,userType,customName,registerTime,ifLogin,loginTime," +
            "ifApply,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount," +
            "pushTime,loginChannel,auditRate,couponType,validityAmt,rateType,lentRate,validityRate,applyLentTime,cps," +
            "lentAmountFirst,lentTimeFirst,cpsRate,fileName,firstName,gender,cell";

    @Resource
    ZhongYuanService zhongYuanService;

    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Test
    public void testParamerter(){
        Integer yxToDX = dynamicParameterService.getPageSize("yxToDx");
        Integer yxToCustomer = dynamicParameterService.getPageSize("yxToCustomer");
        Integer yhGet = dynamicParameterService.getPageSize("yhGet");
        Integer aaa = dynamicParameterService.getPageSize("aaa");
        System.out.println(yxToDX);
        System.out.println(yxToCustomer);
        System.out.println(yhGet);
        System.out.println(aaa);
    }

    @Test
    public void testYxSqlAndYhSql(){
        String tcId = tableCreateService.getTcId("7410437");
        Integer limitStart = 1 * dynamicParameterService.getPageSize("yxToDx");
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, "2023-10-18", limitStart,dynamicParameterService.getPageSize("yxToDx"));
        System.out.println(transferOrderInsertTime.toString());
    }

    @Test
    public void pushOutBoundDataTest(){
        Long id = Long.valueOf(11);
        Result result = zhongYuanService.pushOutBoundData(id);
        System.out.println(result.getMessage());
    }

    @Test
    public void testNew(){
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7434432");
        transferFileTask.setStartDate("2023-07-09 ");
        transferFileTask.setFileName("file");
        log.warn("滴滴转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,data,extend");
            fw.append("\r\n");
            transferToFileByDiDiService.newWriteDiDiTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }

    @Test
    public void testAes(){
        String phone = AESUtil.aesDecrypt("p5ho9PDsqrnJz9CJYNHyqA==", "ovksl39fcl13m9dF");
        System.out.println("解密："+phone);
        String s = AESUtil.aesEncrypty("18822755999","ovksl39fcl13m9dF");
        String s1 = DigestUtils.md5DigestAsHex("王晓二".getBytes());
        String s2 = DigestUtils.md5DigestAsHex("120222199007077719".getBytes());
        System.out.println(s+"。。。"+s1+"。。。"+s2);
    }

    @Test
    public void test(){
        try {
            Map<String, BigDecimal> stringBigDecimalMap = loanFileMapper.queryTotalDataNum("4200777");
            BigDecimal expecteData=stringBigDecimalMap.get("expecteDataNum");
            BigDecimal actualData=stringBigDecimalMap.get("actualDataNum");
            int expecteDataNum =0;
            int actualDataNum=0;
            if(expecteData!=null){
                expecteDataNum=Integer.parseInt(expecteData.toString());
            }
            if(expecteData!=null){
                actualDataNum=Integer.parseInt(actualData.toString());
            }
            System.out.println(expecteDataNum);
            System.out.println(actualDataNum);
        }catch (Exception e){
            log.error("{}",e);
        }

    }

    @Test
    public void resultVolumeCheckTest(){
        businessAlarmServiceImpl.resultVolumeCheck("4200333");
    }
    @Test
    public void ftpToSftpCheckTest(){
        businessAlarmServiceImpl.ftpToSftpCheck("4200777");
    }
    @Test
    public void fileSizeTest(){
        businessAlarmServiceImpl.fileSizeException("4200777","4200777_ceshi_2020071412312.zip,2097154");
    }
    @Test
    public void report(){
        reportServiceImpl.report();
    }
    @Test
    public void progressReport(){
        reportServiceImpl.progressReport();
    }
    @Test
    public void fileUploadFtpException(){
        businessAlarmServiceImpl.fileUploadFtpException("4200777","4200777_ceshi_2020071412312.zip,2097154,2097778");
    }

    @Test
    public void fileUpload(){
        validDataAlarmServiceImpl.fileUpload("4200777","4200777_20200806142007_8932");
    }
    @Test
    public void dataFileVolumn(){
        validDataAlarmServiceImpl.dataFileVolumn("4200333","4200333_ceshi_2020071412312.txt,123,456");
    }

    @Test
    public void deleteMonitor(){
        validDataAlarmServiceImpl.deleteMonitorFileUpload("4200333","4200333_p4_DeleteMonitor_202008284200333");
    }

    @Resource
    private TransferToFileBySamoyeServiveImpl transferToFileService;

    @Resource
    private NewTransferToFileByXieChengServiceImpl newTransferToFileByXieChengServiceImpl;

    @Resource
    private TransferToFileByZhongYouServiceImpl transferToFileByZhongYouService;

    @Resource
    private TransferToFileByZhongBangServiceImpl transferToFileByZhongBangService;

    final static String ZHONGYOU_TRANSFER_FILE = "transform_";

    final static String ZHONGBANG_TRANSFER_FILE = "caifu_transform_";

    @Test
    public void newTransferFileTest() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7410990");
        transferFileTask.setStartDate("2023-07-20 ");
        transferFileTask.setFileName("file");
        log.warn("滴滴转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,convType,requestTime,result,orgChannel,mktLevel");
            fw.append("\r\n");
            newTransferToFileByXieChengServiceImpl.writeXieChengTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }

    @Test
    public void ZhongYouTransferFileTest() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7434636");
        transferFileTask.setStartDate("2023-08-15 ");
        String recordDate = transferFileTask.getStartDate();
        StringBuilder fileName = new StringBuilder();
        fileName.append(ZHONGYOU_TRANSFER_FILE).append(recordDate).append(".txt");
        transferFileTask.setFileName(fileName.toString());
        log.warn("中邮转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            transferToFileByZhongYouService.writeXieChengTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }

    @Test
    public void ZhongBangTransferFileTest() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7433800");
        transferFileTask.setStartDate("2023-11-21 ");
        String recordDate = transferFileTask.getStartDate();
        String dateyyyymmddStr =  LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder fileName = new StringBuilder();
        fileName.append(ZHONGBANG_TRANSFER_FILE).append(dateyyyymmddStr).append(".txt");
        transferFileTask.setFileName(fileName.toString());
        log.warn("众邦财富转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,ifLogin,ifApply,applyTime,applyproductName,applyAmount,ifLent1,lentTime,lentAmount,pushTime,userType,fileName");
            fw.append("\r\n");
            transferToFileByZhongBangService.writeTransferToFile(fw,apiCode,transferFileTask,recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }


    @Test
    public void transferFileTest(){
        String jobParameter = "7410787#2022-05-21";
        String myParam = transferToFileService.isMyParam("7434432", jobParameter);
        Result<List<TransferFileTask>> listResult = transferToFileService.buildTransferTask("7434432",myParam);
        if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0){
            List<TransferFileTask> data = listResult.getData();
            for (TransferFileTask datum : data){
                Result result = transferToFileService.actionTransferToFile(datum,myParam);
                System.out.println(result.getCode());
            }
        }
    }

    @Autowired
    private PushDataService pushDataService;

    @Test
    public void pushData(){
        JSONObject msg = new JSONObject();
        msg.put("localId", 1972439l);
        msg.put("isNewFile", false);
        pushDataService.pushXieChengSmsCollidingToDbData(msg.toJSONString());
    }

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    JobManager jobManager;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RsTransferServiceImpl rsTransferService;
    @Test
    public void actionTest(){
        String apiCode = "7492800";
        apiCode = StringUtils.isNotBlank(apiCode)?apiCode:"7492800";
        String tcId = tableCreateService.getTcId(apiCode);
        String date = "2023-08-20";
        LocalDate now = LocalDate.now();
        String actionDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = StringUtils.isNotBlank(date) ? date : now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Result<TransferActionFront> frontData = jobManager.getFrontData(apiCode, actionDay, 1);
        if(!ResultCode.SUCCESS.getValue().equals(frontData.getCode())){
            System.err.println(new Result().setCode(ResultCode.FAIL.getValue()));
        }
        Long jobId  = 0L;
        TransferActionFront actionFront = frontData.getData();
        if(actionFront ==null){
            jobId = jobManager.saveFrontData(apiCode,date,1);
        }else{
            jobId = actionFront.getId();
        }
        HashSet cellSet = new HashSet();
        HashMap<String, JSONObject> rsStrategyCodes = marketingCommonConfig.getRsStrategyCodes();
        JSONObject strategyCode = rsStrategyCodes.get(apiCode);
        rsTransferService.action(date,apiCode,tcId,cellSet,"1",date,"c",strategyCode.getString("c"));
        rsTransferService.action(date,apiCode,tcId,cellSet,"0",null,"d",strategyCode.getString("d"));
        int size = cellSet.size();
        log.warn("榕树推送决策推送了"+size+"条");
        jobManager.updateFrontDataStatus(jobId,2);
        System.err.println(new Result().setCode(ResultCode.SUCCESS.getValue()));;
    }

}
