import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.Impl.transfertofile.TransferToFileByTongChengGroupServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.google.common.collect.Lists;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 同城集团数据提取单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class TongChengGroupTest {
    protected final static Logger log = LoggerFactory.getLogger(TongChengGroupTest.class);

    @Autowired
    SyncConfigService syncConfigService;
    /**
     * 时间格式 yyyyMMdd
     */
    static final DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);
    /**
     * 时间格式 yyyy-MM-dd
     */
    static final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);
    /**
     * 同城集团
     */
    @Resource
    private TransferToFileByTongChengGroupServiceImpl transferToFileByTongChengGroupService;
    @Autowired
    MarketingCustomerMapper customerMapper;
    @Autowired
    SftpInnerServiceImpl sftpInnerService;

    @Test
    public void testActionTransferToFile(){
        ArrayList<String> apiCodeList = new ArrayList<>();
        apiCodeList.add("7492639");
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"))
                .andApiCodeIn(apiCodeList);
        List<MarketingCustomer> marketingCustomers = customerMapper.selectByExample(customerExample);
        String myParam = null;
        String jobParameter = null;
        jobParameter = "7492639#2024-01-31";
        myParam = transferToFileByTongChengGroupService.isMyParam("7492639", jobParameter);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            Result<List<TransferFileTask>> listResult = new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Lists.newArrayList());
            listResult = transferToFileByTongChengGroupService.buildTransferTask(marketingCustomer.getApiCode(), myParam);
            if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0) {
                List<TransferFileTask> data = listResult.getData();
                for (TransferFileTask datum : data) {
                    Result result = transferToFileByTongChengGroupService.actionTransferToFile(datum, myParam);
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        log.warn("断点");
//                        Result res = sftpInnerService.pushInnerSftp(datum);
//                        if (!ResultCode.SUCCESS.getValue().equals(res.getCode())) {
//                            RetryMainLog retryMainLog = new RetryMainLog();
//                            retryMainLog.setRetryType(1);
//                            retryMainLog.setRetryParam(JSON.toJSONString(datum));
//                            retryMainLog.setRetryParamType(datum.getClass().getName());
//                            retryMainLog.setRetryService("sftpInnerServiceImpl");
//                            retryMainLog.setRetryMethod("pushInnerSftp");
//                            retryMainLog.setRetryNum(0);
//                            retryMainLog.setRetryMaxNum(3);
//                            retryMainLog.setRetryStatus(1);
//                            retryMainLog.setCreateTime(new Date());
//                            retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
//                            retryMainLogMapper.insertSelective(retryMainLog);
//                        } else {
                            //第一次执行，查询为空，不会进行删除，直接返回
                            //第二次执行，删除b_sync_log的记录
//                            List<SyncLog> syncLogList = loanSyncLogMapper.querySyncLog(ImmutableMap.of("apiCode", marketingCustomer.getApiCode(), "fileName", datum.getFileName()));
//                            if (!CollectionUtils.isEmpty(syncLogList)) {
//                                if (syncLogList.size() != 1) {
//                                    log.warn("重新执行数据提取异常，apiCode={},fileName={},syncLogSize={}", marketingCustomer.getApiCode(), datum.getFileName(), syncLogList.size());
//                                    return;
//                                }
//                                SyncLogExample syncLogExample = new SyncLogExample();
//                                syncLogExample.createCriteria().andApiCodeEqualTo(marketingCustomer.getApiCode())
//                                        .andFileNameIn(Lists.newArrayList(datum.getFileName(), datum.getFileName() + ".success"));
//                                loanSyncLogMapper.deleteByExample(syncLogExample);
//                            }
//                        }
                    }
                }
            }
        }
    }

}
