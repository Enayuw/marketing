package com.br.marketing.datarelayservice.test.tc;

import com.br.marketing.dto.tc.TcResponseDTO;
import org.junit.Assert;
import org.junit.Test;

/**
 * 仅承载 JUnit 入口；加签/验签实现见 {@link TcRelaySignAndVerifyHelper}（该类为 final 工具类且构造私有，不能作为 JUnit 被测类）。
 */
public class TcRelaySignAndVerifyHelperTest {

    /**
     * 将 Postman 调用 relay 后得到的<strong>整段响应 JSON</strong>粘到此处（含 code、msg、sign、timestamp、data），再运行
     * {@link #verifyRelayResponseFromPostmanJson()}。
     */
    private static final String RESPONSE_JSON_FROM_POSTMAN = "{\n" +
            "    \"code\": \"0000\",\n" +
            "    \"msg\": \"成功\",\n" +
            "    \"sign\": \"SUARXPTb4O4rA6Xd1px/7YV3QOpwKu3H/xUu9XiHM7z72sld2iEu9W4sphWTN53/WN0frB4FVSewStty741pkvn8jceBpskig3vNCXL4oxpUQK7VHIvCl2X7rdAvqv7chRvrqDdpzrk9GXdiOlkJSufwEKq3BlHZ4SFo6eoY1K3zxWpRa9LksKdBVpRJ1Mzj/8TfvMhMqYmsoP0DjqlQfx6NcLc/eEZHV+Q3F2mwSjBgIPdp/YnDLtUFtSqkJ6T1SFOcOawmFWVkQVvsoI/RurJ0md6clrVOptGJ7/OF0kSd0EXbb+fZ62yrWX3yfCBN7bv1M+52WFM93ptJE/lq5g==\",\n" +
            "    \"timestamp\": \"1778748556368\",\n" +
            "    \"data\": \"{\\\"status\\\":\\\"SUCCESS\\\"}\"\n" +
            "}";

    @Test
    public void runConsolePostmanSamples() throws Exception {
        TcRelaySignAndVerifyHelper.printPostmanMarketDataPushSamples();
    }

    /**
     * 模拟同程：用 {@link TcRelaySignAndVerifyHelper} 里配置的 {@code brPublicKey} 校验接口返回签名。
     * <ul>
     *   <li>代码里：{@link TcRelaySignAndVerifyHelper#verifyRelayResponseJsonAsTcPartner(String)}</li>
     *   <li>对象入参：{@link TcRelaySignAndVerifyHelper#verifyRelayResponseAsTcPartner(TcResponseDTO)}</li>
     *   <li>指定公钥：{@link TcRelaySignAndVerifyHelper#verifyRelayResponseAsTcPartner(TcResponseDTO, String)}</li>
     * </ul>
     */
    @Test
    public void verifyRelayResponseFromPostmanJson() {
        if (RESPONSE_JSON_FROM_POSTMAN.isBlank()) {
            System.out.println("跳过：请先在 TcRelaySignAndVerifyHelperTest.RESPONSE_JSON_FROM_POSTMAN 粘贴 Postman 返回的 JSON");
            return;
        }
        boolean ok = TcRelaySignAndVerifyHelper.verifyRelayResponseJsonAsTcPartner(RESPONSE_JSON_FROM_POSTMAN);
        Assert.assertTrue("relay 响应验签失败：请核对 brPublicKey 与 Speed 中 brPrivateKey 是否成对，且 JSON 为完整原始响应", ok);
    }
}
