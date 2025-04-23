import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.tc.TcDataPushDto;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * xiechengTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class XieChengTest {
    protected final static Logger log = LoggerFactory.getLogger(XieChengTest.class);

    @Autowired
    SyncConfigService syncConfigService;

    @Resource
    TransferToFileByXieChengServiceImpl TransferToFileByXieChengServiceImpl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String brPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGnJwI+EI96Lb7+33AiUug3g7aZTr9gpkLjM3w9Gu3PaSigsF8DNaugV8cMAPJfi9QGZ3t5qGGwLW/N5AFknedZvyGzOEmwk1ezimPtYH0ToEz1OKID0uriFGqrF7lzE7l/rsvpRv6TU07ztg1eDSckGZwyHSDgQD7E5HkqHt1wdpW+aqR5y3xtg9viYfI+0BBgduthJ9mPrX1l/26MKvZIeXAxGm84Fvs/LA7nJqJi64YhYx9jbhVPgHwsE057H33Vi5UZUyseM1cZc2QfqtWVJHfJW06b5ZW73MVSK3MxdNZX6dgT9bkHfxzeFOM0BNJm4n6Ykhcgg8sRMUAvDjnAgMBAAECggEAHxKXkhp8b/3//zqWVJNcuc2IcDFd5Jb47QmboDtLggjgsAKu1wu";


    static {
        // 配置ObjectMapper
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不序列化null值
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期不转为时间戳
        objectMapper.registerModule(new JavaTimeModule()); // 支持Java 8日期类型
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 允许序列化空对象
    }

    @Test
    public void testActionTransferToFile(){
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("3710058");
        transferFileTask.setFileType(2);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        transferFileTask.setStartDate(yyyyMMdd);
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        // TransferToFileByXieChengServiceImpl.actionTransferToFile(transferFileTask,"");
    }

    @Resource
    private DatabaseOperationService dbService;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Test
    public void test04() {

        try {
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    List<XiechengCollidingDataProcessTask> xiechengCollidingDataProcessTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(taskExample);
                }

                @Override
                public Object getParams() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    return taskExample;
                }

                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapperBase";
                }

                @Override
                public String getMapperMethod() {
                    return "selectByExample";
                }
            },"",config);
        } catch (Exception e) {

        }
    }

    @Test
    public void test05() {
        TcDataPushDto tcDataPushDto = new TcDataPushDto();
        tcDataPushDto.setBatchNo("123");
        tcDataPushDto.setFileUrl("https://oss.17usoft.com/public-nova/xz8KKPBV-AgencyCP20250411000000000041044.gz");
        tcDataPushDto.setFileExpirationTime("2025-03-14 13:58:47");
        tcDataPushDto.setStartDate("2025-03-14");
        tcDataPushDto.setEndDate("2025-04-14");
        tcDataPushDto.setTotal(100l);
        String data;
        try {
            data = objectMapper.writeValueAsString(tcDataPushDto);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("对象转JSON失败", e);
        }
        TcRequestDTO tcRequestDTO = new TcRequestDTO();
        tcRequestDTO.setRequestNo("1234");
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        tcRequestDTO.setData(data);
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        String sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
        tcRequestDTO.setSign(sign);


    }

}
