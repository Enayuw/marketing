import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.dto.rsxk.CallStatusDTO;
import com.br.marketing.dto.rsxk.Resp;
import com.br.marketing.rpcclient.RpcClientProxy;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class RsxkTest {

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private HttpProxyClient httpProxyClient;

    @Test
    public void test() {
        double v = System.currentTimeMillis() / 1000.0;

        DecimalFormat df = new DecimalFormat("#.###");
        String ts = df.format(System.currentTimeMillis() / 1000.0);
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer your-token-here");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String yyyyMMdd = DigestUtils.md5Hex(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        String urlWithParams = "https://dymapi.shurongdai.cn/mdp-thinker-facade/api/queryCallStatus?&uid={uid}&planId={planId}&bizSource={bizSource}&operateType={operateType}";
        Map<String, String> params = new HashMap<>();
        params.put("uid", "48004166");
        params.put("planId", "3985");
        params.put("bizSource", "01");
        params.put("operateType", "2");
        params.put("sign", "ed3c84007e3d48511aa8dfd421fe14c5");

        ResponseEntity<Object> userResponse = restTemplate.exchange(
                urlWithParams,
                HttpMethod.GET,
                entity,
                Object.class,
                params
        );
        Object obj = userResponse.getBody();
    }

    @Test
    public void test01() {
        String str = "{\"4004739\":{\"userTypes\":[\"1\",\"2\"],\"orgname\":\"rongshuxinke\",\"source \":\"45\"},\"4004713\":{\"userTypes\":[\"1\",\"2\"],\"orgname\":\"rongshuxinke\",\"source \":\"45\"}}";
        JSONObject jsonObject = JSON.parseObject(str);
        JSONObject jsonObject1 = jsonObject.getJSONObject("4004739");
        JSONArray userTypes = jsonObject1.getJSONArray("userTypes");
        userTypes.contains("1");

    }

    @Test
    public void test02() {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", "48004166");
        data.put("planId", 3985);
        data.put("bizSource", "01");
        data.put("operateType", 2);
        data.put("sign", DigestUtils.md5Hex(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        String reqUrl = String.format("%s?%s", "https://dymapi.shurongdai.cn/mdp-thinker-facade/api/queryCallStatus", param(data));
        HashMap<String, String> resMap = httpProxyClient.get(reqUrl, false, null);
        Resp<CallStatusDTO> resp = JSON.parseObject(resMap.get("content"), new TypeReference<Resp<CallStatusDTO>>() {
        }.getType());
    }

    private static String param(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key)
                    .append("=")
                    .append(data.get(key));
        }
        return sb.toString();
    }

    @Test
    public void test03() {
        String decode = RpcClientProxy.decode("1b5d7f7aa5149354c647a2ac5bbd7b12", "cell", "md5", "");
        String encode = BrCipherMaker.getInstance().encode(decode);
    }
}
