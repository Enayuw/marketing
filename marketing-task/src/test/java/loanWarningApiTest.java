/*
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.DtbStrategyClient;
import com.br.marketing.client.LoanWarningClient;
import com.br.marketing.common.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DecodeClient.class})
@WebAppConfiguration
public class loanWarningApiTest {

    @Resource
    DecodeClient decodeClient;

    @Test
    public void testDtb(){
        Map<String,String> proMap=new HashMap<>();
        String  id=decodeClient.decode("id","28ef991ac68531a272724ee415e9156f","","","");
        System.out.println(id);
       */
/* JSONArray dtbStrategyArray=new JSONArray();
            JSONObject object = JSONObject.parseObject(dtb0000052);
            if("000000".equals(object.getString("code"))){
                JSONObject data = object.getJSONObject("data");
                if(data!=null && StringUtils.isNotEmpty(data.getString("dataProdList"))
                        && data.getInteger("status")==1 && data.getInteger("canUse")==1){
                    JSONObject dtbStrategy = object.getJSONObject("data");

                    JSONArray jsonArray = JSON.parseObject(dtbStrategy.getString("dataProdList")).getJSONArray("dataProdList");
                    if(jsonArray.toString().indexOf("BadInfo")!=-1||
                            jsonArray.toString().indexOf("CourtDetail")!=-1){
                        for(int i=0;i<jsonArray.size();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if(!"BadInfo".equals(jsonObject.getString("code"))&&
                                    !"CourtDetail".equals(jsonObject.getString("code"))){
                                dtbStrategyArray.add(jsonObject);
                            }
                        }
                    }else {
                        dtbStrategyArray= jsonArray;
                    }
                }else{
                }
            }else{
            }
        System.out.println(dtbStrategyArray);*//*

    }
    @Test
    public void testApi(){
     */
/*   String apiCode="4002055";
        String param="{'strategyId': 'STRB0000151', 'jsonData': '{\"batch_number\": \"4002055_201908201536_0001\",\"cusNum\": \"12\", \"idCard\": \"0A3B7CC991F61CE354DFB53F84D510A2\", \"name\": \"a77d474a27c1c7ea71a732f113b51498\", \"cell\":\"2fc1a39b0c7cbaf835cbdcefe318a7b0\",\"passDate\": \"2019-08-19\", \"loanMaturityDate\": \"2019-08-19\", \"approveResult\": \"1\"}', 'deleteTime': '2020-08-20'}";
        //String param="{'strategyId': 'DTB0000052', 'jsonData': '{\"batch_number\": \"100092_201908201536_0001\",\"cusNum\": \"12\", \"idCard\": \"11010019801224326x\", \"name\": \"雨露\", \"cell\":\"17035767564\",\"passDate\": \"2017-09-18\", \"loanMaturityDate\": \"2020-06-13\", \"approveResult\": \"1\"}', 'deleteTime': '2020-08-20'}";
        JSONObject jsonObject = JSONObject.parseObject(param);
        String s = null;
            s = loanWarningClient.queryApi(jsonObject, apiCode);
 JSONObject jsonObject1 = JSONObject.parseObject(s);
        String hxResult = HxClient.hauXiangFlat(jsonObject1.getString("hxResult"));
        JSONObject hxJson=JSONObject.parseObject(hxResult);

        System.out.println(s);*//*

    }


}
*/
