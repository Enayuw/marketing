import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class DataCleanTest {

    @Resource
    GeneralDataCleanService generalDataCleanService;

    @Test
    public void uploadClean() throws NoSuchFieldException {
        MarketingTcyrSync marketingTcyrSync = new MarketingTcyrSync();
        marketingTcyrSync.setCell("123");
        marketingTcyrSync.setTerminal(0);
        marketingTcyrSync.setUserKey("456");
        List<MarketingTcyrSync> list = new ArrayList<>();
        list.add(marketingTcyrSync);
        List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(list), JSONObject.class);
        Result callResult = generalDataCleanService.uploadClean(jsonObjectList, "7492773");
    }
}
