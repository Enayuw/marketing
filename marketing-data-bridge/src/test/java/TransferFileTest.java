import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.Impl.PushDataServiceImpl;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.SyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * TransferFileTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DataBridgeApplication.class})
@WebAppConfiguration
@Slf4j
public class TransferFileTest implements ApplicationContextAware {

//    @Autowired
//    FileSyncService fileSyncService;
    @Resource
    SyncConfigMapper syncConfigMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DataBridgeApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }

    @Test
    public void test(){
//        SyncConfig config = new SyncConfig();
//        config.setType(1);
//        config.setApiCode("3710065");
//        config.setDataType(6);
//        SyncConfig queryConfig = syncConfigMapper.queryConfigByConditaion(config);
//        List<SyncConfig> syncConfigList = new ArrayList<>();
//        syncConfigList.add(queryConfig);
//        fileSyncService.pullFromSftp();
    }

    @Resource
    TransferToFileByYiShiServiceImpl transferToFileByYiShiService;
    private final static String TABLE_HEAD_TRANSFER_YiShi = "custNum,userType,callId,isBlack,extend01,extend02";
    @Test
    public void YiShiTransferFileTest() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7410716");
        String apiCode = "7410716";
        String myParam = "7410716#2024-07-29";
        String dd = isMyParam("7410716", myParam);
        String date = LocalDate.now().toString();
        date = date.replace("-", "");
        transferFileTask.setStartDate(dd);
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? dd.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        transferFileTask.setFileName(String.format("transform_yishi_%s.txt", dateyyyymmddStr));
        log.warn("医时化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append(TABLE_HEAD_TRANSFER_YiShi);
            fw.append("\r\n");
            transferToFileByYiShiService.writeYiShiTransferToFile(fw,apiCode,transferFileTask, recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private final static String FILE_HEADER_PPD = "requestId,requestTime,custNum,cell,userType,userType1,registerTime,ifApply,applyDt,applyResult,"
            + "auditTime,auditAmount,ifLent,lentTime,lentAmount,applyLoan,applyLoanTime,applyLoanAmount,"
            + "ifActivity,activityTime,unlentAmount,caseEffective,isBlack";
    @Resource
    TransferToFileByRongShuServiceImpl transferToFileByRongShuService;

    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    @Test
    public void RSWriteTransferToFile() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7492801");
        String myParam = "7492801#2024-06-27";
        String dd = isMyParam("7492801", myParam);
        transferFileTask.setStartDate(dd);
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? dd : LocalDate.now().toString();
        LocalDate localDate = LocalDate.parse(dateyyyymmddStr, YYYYMMDDSHORTLINE);
        String yesterday = localDate.minusDays(1).toString();
        String newYesterday = yesterday.replace("-", "");
        String fileName = apiCode + "_zhuanhua_" + newYesterday + ".txt";
        transferFileTask.setFileName(fileName);
        log.warn("榕树转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append(FILE_HEADER_PPD);
            fw.append("\r\n");
            transferToFileByRongShuService.writeTransferToFile(fw,apiCode,transferFileTask, recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }


    @Resource
    TransferToFileBySuShangServiceImpl transferToFileBySuShangService;

    private final static String FILE_HEADER = "taskId,custNum,touchType,callTime,pushTime";

    @Test
    public void SuShangWriteTransferToFile() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7491726");
        String myParam = "7491726#2024-07-17";
        String dd = isMyParam("7491726", myParam);
        transferFileTask.setStartDate(dd);
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        transferFileTask.setFileName(String.format("br_returnlist_%s_01.txt", dateyyyymmddStr));
        log.warn("苏商自动化回传-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            transferToFileBySuShangService.writeSuShangTransferToFile(fw,apiCode,transferFileTask, recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Resource
    TransferToFileByCuDongZhiServiceImpl transferToFileByCuDongZhiService;

    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,loginTime,applyDt,applyResult,auditAmount,ifLent,firstName" +
            ",cell,stopMarketingSign,gender,isLightMarkting,operationScene,applyLoan,succAmtType";


    @Test
    public void CuDongZhiWriteTransferToFile() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7491635");
        String myParam = "7491635#2024-08-21";
        String dd = isMyParam("7491635", myParam);
        transferFileTask.setStartDate(dd);
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        transferFileTask.setFileName(String.format("%s_360cudong_zhuahua_%s.txt", apiCode, dateyyyymmddStr));
        log.warn("奇富360促动支转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            transferToFileByCuDongZhiService.writeCuDongZhiTransferToFile(fw,apiCode,transferFileTask, recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Resource
    TransferToFileByGuoMeiServiceImpl transferToFileByGuoMeiService;

    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;

    @Test
    public void GuoMeiWriteTransferToFile() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7492805");
        String myParam = "7492805#2024-09-20";
        String dd = isMyParam("7492805", myParam);
        transferFileTask.setStartDate(dd);
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        TransferFileTaskExample taskExample = new TransferFileTaskExample();
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                .andFileTypeEqualTo(1).andFileNameLike("transform_guomei_%");
        List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
        if (CollectionUtils.isEmpty(transferFileTasks)) {
            System.err.println("国美转化数据提取-开始执行,");
        }
        transferFileTask.setFileName(String.format("transform_qifujuxin_%s.txt", dateyyyymmddStr));
        log.warn("国美转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append(transferToFileByGuoMeiService.TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            transferToFileByGuoMeiService.writeGuoMeiTransferToFile(fw,apiCode,transferFileTask, recordDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }


    public String isMyParam(String apiCode, String jobParameter) {
        if (jobParameter.contains(apiCode)) {
            String[] split = jobParameter.split(";");
            for (String s : split) {
                if (s.contains(apiCode)) {
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Resource
    PushDataServiceImpl pushDataService;

    @Test
    public void testPushDataService() {
        pushDataService.pushUpdateDassData(9360060L);
    }
}
