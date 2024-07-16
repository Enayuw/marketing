import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.Impl.transfertofile.TransferToFileByRongShuServiceImpl;
import com.br.marketing.service.Impl.transfertofile.TransferToFileBySuShangServiceImpl;
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

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private final static String FILE_HEADER = "tskId,custNum,touchType,callTime,pushTime";

    @Test
    public void SuShangWriteTransferToFile() {
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("7492801");
        String myParam = "7492801#2024-06-27";
        String dd = isMyParam("7492801", myParam);
        transferFileTask.setStartDate(dd);
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        boolean isParam = StringUtils.isNotBlank(dd);
        String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        transferFileTask.setFileName(String.format("br_returnlist_%s.txt", dateyyyymmddStr));
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
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            transferToFileBySuShangService.writeSuShangTransferToFile(fw,apiCode,transferFileTask, recordDate);
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

}
