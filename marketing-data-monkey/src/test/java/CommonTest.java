import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SaveReachDeleteRecordReqBO;
import com.br.marketing.client.qifu.SaveReachDeleteRecordReq;
import com.br.marketing.client.qifu.SaveReachDeleteRecordResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.zhijia.ZhiJiaCarInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.monkey.MarketingDataMonkeyApplication;
import com.br.marketing.monkey.job.dewu.DewuCollidingDataToSendJob;
import com.br.marketing.monkey.job.tongcheng.TongChengOperationPushToCustomerJob;
import com.br.marketing.service.Impl.zhijia.ZhiJiaDataProcessService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MarketingDataMonkeyApplication.class})
@WebAppConfiguration
@Slf4j
public class CommonTest {

    @Resource
    private TongChengOperationPushToCustomerJob tongchengJob;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private DewuCollidingDataToSendJob job;

    @Test
    public void testActionTransferToFile(){
        List<Long> localIdList = new ArrayList<>();
        localIdList.add(8610047L);
        localIdList.add(8610048L);
//        service.refreshLocalFile(localIdList);
    }

    @Test
    public void testDewuCollidingDataToSendJob(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        context.setJobParameter("8820073");
        job.process(context);
    }

    @Test
    public void testTongchengJob(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        tongchengJob.process(context);
    }

    @Test
    public void test3(){
        SaveReachDeleteRecordReqBO bo = new SaveReachDeleteRecordReqBO();
        SaveReachDeleteRecordReq saveReachDeleteRecordReq = new SaveReachDeleteRecordReq();
        saveReachDeleteRecordReq.setBatchNo("3710143_RE6522969066196701184_AGOP6521067035855687912");
        saveReachDeleteRecordReq.setAgentOperator("bairong");
        bo.setReq(saveReachDeleteRecordReq);
        Result<SaveReachDeleteRecordResp> saveReachDeleteRecordRespResult = methodRetryHandlerService.callDeleteReachRecordCuDongZhi(bo, null);
        log.warn(JSONObject.toJSONString(saveReachDeleteRecordRespResult));
    }

    @Resource
    ZhiJiaDataProcessService zhiJiaDataProcessService;

    @Test
    public void testZhiJiaCarInfoGetService(){
        Integer brandId = null;
        Integer seriesId = null;
        ZhiJiaClueBackData zhiJiaClueBackInfo = new ZhiJiaClueBackData();
        zhiJiaClueBackInfo.setBrandName("一汽奥迪");
        zhiJiaClueBackInfo.setSeriesName("一汽奥迪a4l");
        // 查询品牌
        List<ZhiJiaCarBrandInfo> carBrandInfos = zhiJiaDataProcessService.getCarBrandInfos();
        ZhiJiaCarInfoDTO zhiJiaCarInfo = zhiJiaDataProcessService.getZhiJiaCarBrandInfo(zhiJiaClueBackInfo, carBrandInfos);
        if (zhiJiaCarInfo.getIsMatch().equals(Boolean.TRUE)){
            brandId = zhiJiaCarInfo.getBrandId();
        }

        List<ZhiJiaCarSeriesInfo> carSeriesInfos = zhiJiaDataProcessService.getCarSeriesInfos(brandId);
        ZhiJiaCarInfoDTO zhiJiaCarSeriesInfo = zhiJiaDataProcessService.getZhiJiaCarSeriesInfo(zhiJiaClueBackInfo, carSeriesInfos);
        if (zhiJiaCarSeriesInfo.getIsMatch().equals(Boolean.TRUE)){
            seriesId = zhiJiaCarSeriesInfo.getSeriesId();
        }

        System.err.println(brandId + "------------" + seriesId);
    }

    @Test
    public void testGetBrandAndseries(){
        zhiJiaDataProcessService.getBrandAndseries();
    }

    @Test
    public void testGetCityAndCounty(){
        zhiJiaDataProcessService.getCityAndCounty();
    }


}
