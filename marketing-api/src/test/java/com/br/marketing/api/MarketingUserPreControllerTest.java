package com.br.marketing.api;

import com.alibaba.fastjson.JSON;
import com.br.common.encryption.Md5Utils;
import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.api.controller.MarketingUserPreController;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
import com.br.marketing.dto.ReserveField1DTO;
import com.br.marketing.vo.MarketingPreUserSyncDetailVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MarketingUserPreControllerTest {

    @Autowired
    private MarketingUserPreController marketingUserPreController;

    //测试场景
    //1 模拟没有传输last和total--模拟数禾、萨摩耶、宜信
    //2 模拟传输last和total--新客户
    //3 模拟只传groupType
    //4 模拟只传userType
    //5 模拟既传groupType又传userType


    //模拟数禾数据传输
    //@Test
    public void testReceiveMarketingPreUserSync0() throws UnsupportedEncodingException {
        //content内容是AES加密过的内容
        //pwdKey是用户原始的appKey
        String s = AESAlgorithmUtil.decrypt("ndVryl-a0j7mX1Z4ALMn_Lu3vlUY-NNEcMriLojLXljuUgR2r4w2fjINTSJ0AcxCMWbhzPJjhIMV8CoLHoKxO7YdNoDGilGeTGr1kDKbUSYfGjrlEoJp1EYULNrbNGyVZdXjtWaSVcaJkBlZmUAhpZy1TonfyrSlynuJhRD_mKxs7oJbnvB_HLZSFh5CwfmgLBgMSNKM4EVCm0uGxa-wgWJRYPeoQt3MkWPB-rB_WMHgj1S0btR5a_xP0TccTzskVQ2bLkhNIsu3oWAhO2iyL7QZIyqnh1Ge8hHopDSwAbbzNR1oHXyxvR9Q3_4OlyEclghVKy0NAYJkLNEDWwJeD7Sm5m7jKqgjqiBj0fpVMN2Ti20MG8oGvMzKJcjuOWz7mz3RYr2Z4RHLrsMEXz3ZG8_Pqy5CP9rIAW1ZVWYiQ-Ye5-DzgWPVR-GJmmRksFoHRFv8okbYuHff7Aqh20I-ZLukHaKjJ_AVVJ3SLTqtOT2KxdY0g58jZTiliashJQRegZv_SP2KBLXghat72KKHhk9gzqHzyJ4-ObumWQ0BiGfQV2NGNgY7aguBj7iqDv09wrCGzqz7XyFTlGpaQewIbQ", "31ba331c8e9ccbb7e0ee5c91b41f6e790cdb60f100777aea24b6f1986f8dc92e");
        //输出的是URLEncode加密过的数据
        System.out.println(s);
        //此时才是真正的数据
        String jsonData = URLDecoder.decode(s,"UTF-8");
        String apiCode = "123";
        ApiNoDataResult apiNoDataResult = marketingUserPreController.receiveMarketingPreUserSync(apiCode, jsonData);
        Assert.assertEquals(apiNoDataResult.getCode(), "00");
    }

    //模拟没有传输last和total
    //@Test
    public void testReceiveMarketingPreUserSync1() {
        String apiCode = "123";
        String jsonData = mockMarketingPreUserDTO1(apiCode);
        ApiNoDataResult apiNoDataResult = marketingUserPreController.receiveMarketingPreUserSync(apiCode, jsonData);
        Assert.assertEquals(apiNoDataResult.getCode(), "00");
    }

    //模拟没有传输last和total
    /**
     * {
     *   "dataItems": [
     *     {
     *       "cell": "e070737a08d19eea40dacc5e60716e56",
     *       "custNum": "1",
     *       "groupType": "S01"
     *     }
     *   ],
     *   "requestId": "123_1_b1b3ed14-4194-486f-b7c1-59e069abe0d0",
     *   "taskId": "1"
     * }
     * */
    private String mockMarketingPreUserDTO1(String apiCode) {
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        String taskId = "1";
        dto.setTaskId(taskId);
        dto.setRequestId(apiCode+"_"+taskId+"_"+ UUID.randomUUID());
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
        detailDTO.setCell(Md5Utils.cell32("18810987654"));
        detailDTO.setGroupType("S01");
        detailDTO.setCustNum("1");
        dataItems.add(detailDTO);
        dto.setDataItems(dataItems);
        System.out.println(JSON.toJSONString(dto));
        return JSON.toJSONString(dto);
    }

    //模拟没有传输last和total
    //@Test
    public void testReceiveMarketingPreUserSync2() {
        String apiCode = "123";
        String jsonData = mockMarketingPreUserDTO2(apiCode);
        ApiNoDataResult apiNoDataResult = marketingUserPreController.receiveMarketingPreUserSync(apiCode, jsonData);
        Assert.assertEquals(apiNoDataResult.getCode(), "00");
    }

    //模拟传输last和total
    //{
    //  "dataItems": [
    //    {
    //      "cell": "e070737a08d19eea40dacc5e60716e56",
    //      "custNum": "1",
    //      "groupType": "S01"
    //    }
    //  ],
    //  "last": 1,
    //  "requestId": "123_1_ded27b1f-7e00-453c-8e8f-132592393266",
    //  "taskId": "1",
    //  "total": 1
    //}
    private String mockMarketingPreUserDTO2(String apiCode) {
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        String taskId = "1";
        dto.setTaskId(taskId);
        dto.setRequestId(apiCode+"_"+taskId+"_"+ UUID.randomUUID());
        dto.setTotal("1");
        dto.setLast("1");
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
        detailDTO.setCell(Md5Utils.cell32("18810987654"));
        detailDTO.setGroupType("S01");
        detailDTO.setCustNum("1");
        dataItems.add(detailDTO);
        dto.setDataItems(dataItems);
        System.out.println(JSON.toJSONString(dto));
        return JSON.toJSONString(dto);
    }

    //3 模拟只传groupType
    //@Test
    public void testReceiveMarketingPreUserSync3() {
        String apiCode = "123";
        MarketingPreUserSyncStatusDTO dto = new MarketingPreUserSyncStatusDTO();
        dto.setApiCode(apiCode);
        dto.setRequestId("123_1_89645a50-493c-4d43-94a2-0b332df4436f");
        dto.setTaskId("1");

        ApiResult marketingPreUserStauts = marketingUserPreController.getMarketingPreUserStauts(apiCode, JSON.toJSONString(dto));
        Assert.assertEquals(marketingPreUserStauts.getCode(), "00");
        MarketingPreUserSyncDetailVO data = (MarketingPreUserSyncDetailVO) marketingPreUserStauts.getData();
        System.out.println(JSON.toJSONString(data));
        Assert.assertTrue(data.getStatus()==2);
    }
    //4 模拟只传userType
    //@Test
    public void testReceiveMarketingPreUserSync4() throws InterruptedException {
        String apiCode = "123";
        String taskId = "1";
        String requestId = apiCode+"_"+taskId+"_"+ UUID.randomUUID();
        String jsonData = mockMarketingPreUserDTO4(taskId, requestId);
        ApiNoDataResult apiNoDataResult = marketingUserPreController.receiveMarketingPreUserSync(apiCode, jsonData);
        Assert.assertEquals(apiNoDataResult.getCode(), "00");

        MarketingPreUserSyncStatusDTO dto = new MarketingPreUserSyncStatusDTO();
        dto.setApiCode(apiCode);
        dto.setRequestId(requestId);
        dto.setTaskId(taskId);

        Thread.sleep(3*1000);
        ApiResult marketingPreUserStauts = marketingUserPreController.getMarketingPreUserStauts(apiCode, JSON.toJSONString(dto));
        Assert.assertEquals(marketingPreUserStauts.getCode(), "00");
        MarketingPreUserSyncDetailVO data = (MarketingPreUserSyncDetailVO) marketingPreUserStauts.getData();
        System.out.println(JSON.toJSONString(data));
        Assert.assertTrue(data.getStatus()==2);
    }

    private String mockMarketingPreUserDTO4(String taskId, String requestId) {
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        dto.setTaskId(taskId);
        dto.setRequestId(requestId);
        dto.setTotal("1");
        dto.setLast("1");
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
        detailDTO.setCell(Md5Utils.cell32("18810987662"));
        detailDTO.setCustNum(new Random().nextInt(10000)+"");
        ReserveField1DTO reserveField1DTO = new ReserveField1DTO();
        reserveField1DTO.setUserType("S01");
        detailDTO.setReserveField1(JSON.toJSONString(reserveField1DTO));
        dataItems.add(detailDTO);
        dto.setDataItems(dataItems);
        System.out.println(JSON.toJSONString(dto));
        return JSON.toJSONString(dto);
    }
    //5 模拟既传groupType又传userType
}
