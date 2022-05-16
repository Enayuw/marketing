import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.TransferToFileByJiuFuServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
    private TransferToFileByJiuFuServiceImpl transferToFileByJiuFuService;

    @Test
    public void transferFileTest(){
        Result<List<TransferFileTask>> listResult = transferToFileByJiuFuService.buildTransferTask("7412002","2022-05-01");
        if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0){
            List<TransferFileTask> data = listResult.getData();
            for (TransferFileTask datum : data){
                Result result = transferToFileByJiuFuService.actionTransferToFile(datum);
                System.out.println(result.getCode());
            }
        }
    }

}
