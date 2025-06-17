import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class DataCleanTest {

    @Resource
    GeneralDataCleanService generalDataCleanService;

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Test
    public void uploadClean() throws NoSuchFieldException {
        MarketingTcyrSync marketingTcyrSync = new MarketingTcyrSync();
        marketingTcyrSync.setCell("123");
        marketingTcyrSync.setTerminal("0");
        marketingTcyrSync.setUserKey("456");
        List<MarketingTcyrSync> list = new ArrayList<>();
        list.add(marketingTcyrSync);
        List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(list), JSONObject.class);
        Result callResult = generalDataCleanService.uploadClean(jsonObjectList, "7492773");
    }

    @Test
    public void uploadClean01() throws NoSuchFieldException {
        List<MarketingTcyrSync> tcyrSyncList =
                tcyrSyncMapper.selectTcSyncList("No_B559B13A29D7408BB51F094774A85952", 0, 71640L, 2000);
        List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncList), JSONObject.class);
        Result callResult = generalDataCleanService.uploadClean(jsonObjectList, "7492773");
    }


    @Test
    public void transferClean01() {
        JSONObject jsonObject = JSONObject.parseObject("{\"batchNo\":\"CP20250429000000000014015\",\"nodeStatus\":\"SUCCESS\",\"transformNode\":\"CREDIT\",\"transformAmount\":\"6666.699\",\"transformTime\":\"2025-05-01 12:47:00\",\"userKey\":\"86192406\"}");
        List<JSONObject> jsons = new ArrayList<>();
        jsons.add(jsonObject);
        Result callResult = generalDataCleanService.transferClean(jsons,"7492773");
        System.out.println(callResult);
    }
}
