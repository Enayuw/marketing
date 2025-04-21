package demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpDemo {

//    private static final String tcPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";
//    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs9He491hp3zCI70rKQe79mXJglifGas59YLzHvfW6ljp73u1d36U2UnWVTIOwfL4iQ1La1Kg2P2UlrFKI1tDd++G842JRjWWJVpmKomXymWeOAASuvnZMuTA4Z2k6WIoZYcMYiqfD+PGsI8jMYKlsb+3VoUhIFRb4Q5CEK/V2LUH9dRC9rNTem6pGn9brxgEQG+sg8/b4eLaK86MPm5n3tB3ox9UMsOQt3s2vlhJoRXQRRHh+BoZC8Kd8EOpqKUA6cjz186zSSKO82qm2ifrPi049qvBAlfIR53cEh62otH6IJGnQDBfbYEw9lMB2QLbytCvCFtUS9BPJBSGpn5VlQIDAQAB";

//    private static final String tcPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCdTQU4CjxeDuX7kMTU10HQV6gMI0cmRT25McvA4/k+zv5PrvpqDWitUwZm9R1ylW6Rm81oGegr2yMTBWUOfa2ha21Qy5vfr+5e2MTZqYeTj5yvUbtgpU449N5UTwYriAlQeEObx+w0kYRpFY9u7R4rK8uBn92KUSJMaMaHY6CyGnNCUCB6378qPMfSy/osPKWLanv1ptrNMx/bsR7Uqrntka9QTkx/Z48grW4wAB6V583hVlQWrhpcB2WYubp8PBgh7IyB+3Dk4uTuoAulMCT+nsAE4ktHiqGHRyW2jhnN31pgboLhSr1Ky34t134zsOchPz7pNgCMBbQiJ+78xjwVAgMBAAECggEAFDnyINmYF5Qc2HMuDBw9tzd+QUlL/czLegPAKmZmB0CAjOh4BKkCu77ARP7hsXkrhYeeKvOh2fnMkcaQzwM0D1yL6uazfVjW8tU2wrI836mIwTZmPLAk1cgOypJ6vaA5amJ8dwyG/99yQjzme6H7zhtMiLdwujdijKawWHpkEGKIquwiT49T1oPSkb1SEbUxjWVymxuMynnjjsEtfqkN6aEc9DmSn3LW+OWjzUsRsE3ssN0qnAXYHWcOcnRwNSn9s/tDWOo5vEg1o+I9XjQpO/cDO7KCVELydBSd2ESCHNSSOieBliEJDXSuIxx/3i5TKoV1zNKWRX4pLVY+Tc/WxQKBgQDPbbJQzb1FPeeUB/yk1m1lN4yz1ug/ByseRvoOaaTLI0zyFzXiY1BiI/Ab0lnRWdxaGamFDOJ/zkVhaUKLqf3kK29Vgltn3Ld032x5XhpGs6sxmOU4xvAfa88XySW/LJ6SDeV+CvAjshvl+NuRhSsJ8zjxoKFQQo3GYB+w9xlLFwKBgQDCImwDgutjS8Mb/R6/qMPxS4roswxCVpzMnI+Qf7mDaVw0PGQAPWbYivvRzkwT9atddUrx7/17KksWw\n";
//    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnU0FOAo8Xg7l+5DE1NdB0FeoDCNHJkU9uTHLwOP5Ps7+T676ag1orVMGZvUdcpVukZvNaBnoK9sjEwVlDn2toWttUMub36/uXtjE2amHk4+cr1G7YKVOOPTeVE8GK4gJUHhDm8fsNJGEaRWPbu0eKyvLgZ/dilEiTGjGh2OgshpzQlAget+/KjzH0sv6LDyli2p79abazTMf27Ee1Kq57ZGvUE5Mf2ePIK1uMAAelefN4VZUFq4aXAdlmLm6fDwYIeyMgftw5OLk7qALpTAk/p7ABOJLR4qhh0clto4Zzd9aYG6C4Uq9Sst+Ldd+M7DnIT8+6TYAjAW0Iifu/MY8FQIDAQAB";


    private static final String tcPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6JbKUPsahMZ/9S2ZRRc8d9sFOy+0CNyBQNurhUHOvEpONfVhx1BjE+HGZl64Cha8PObX2tD9Btx4wa03k+/AhPi7dNd38G9r+YbsRbSohzPSl9akCDaf1xiKw7m9lH/GN/Lk/vtpRHWoPJ5wu6SLfTRn+FzYqxhipp725OA3vEk7R2fB9mUXy4vW/E59hD9fuixbid1m7c1Aaejgwku+BKRfmtcLvaHsvTn3rDUSxdT4jkXMwWwIVA4M33ssi5Z+9s1VVuxsokN9g6XsT4K0M9GioBHDwnEiOshiRyM4oPFZp8Gmu5l1EJ73Sc6+4iS8q1cBW03ePELBon3i/h8knAgMBAAECggEBAKTlVg9amMwcULSpwUaHh5TsjItHvHl06ewE6gaUJRZWZ100R0/2aca6qq87nUrgr5XWMqoLO+nz1AtiUstgnnRkSFFvMWjuKA2l93fVczgj/iixuHh4Lmxai0qevREgvfNgh52/bFfkrZolJYaswVZ8T2U1nKdBeoF3dWqJDFbVJs7EMZ1ypEat0K437bU6JqxMpILQ2hSJdrmAmmm9AjBPEZiDdmxtJsQ9g9ZC5HwVcCKKAiTSHeapGNLN/Lkvr0W5rUADUy7Ek4CBOxMeX+zShMNFV2a2jp90spVoJ+4agXBoOrabCBqA+VcBMHs3tV+DhEt2L7tfBz/ohA/NDZkCgYEA5K2KuBidVfc5JxTgnNC52h3pzW0Jz+7tNngJ7v5iAjxnu92r5uOWR8GNqJHD6P3XIIHpH4r3+zRktCPvMdWN0ULATycnIrYzUqJMi3w2sJNK0wcKkJp3F8QrMpe8nKPSv01GC8m7/zRSLUZgkQgDKsD/jnaCq/WsY4nDv8Ze89sCgYEA0GNKu+mnrOXyEOWlntyfcDqqo/xq7+N28pMHrVUZo9acY8apDj3EJ8o/fqcC9bq+cQ+5p0wQiHfTcJx3l+UKaDOx0uCitgG38zXawZA5F1Z1Y8ZQ8y/2bxpw4GcMyqlyZfZ77/yAld83yhe6qlOxB8gJCdrNLSaLZca7/bn+56UCgYBBcRSIuKqWBmj5sTTSS71UGUlme3TaZ7LE6rdVCMF9iFHbZoWiTrEcGdzzR7u7+qDM8cCIQVnULts+3iW+qjGqmCK2xCqj+WZYmI+1PzfbcltwZsx0M3Avgfkmwlu8q/lMu8125CWD1DJMOJ68AoH9gzvfRjUBBw5tcehuAlP8DwKBgF+ZzcVbslL9wwnBcTPqXzLrlzFYMe8P2Zf7oAADFJo3cNPNZe1kpMLkZDDEifUV0RypbDC2Ereo0VXOUodaymV3odLuv3bkXvGy+ULn2Wk9fulhJ+4JSPM7nCE25YVsK1FfvQgiPROErmGGdVqCvqqlOJBO0uYt0rHEdKY4WBsJAoGAOb6Y+aY2YalJTfg4BgLuV2TRAmNOA5ITK2sAtPqfAIQZVT39EqJUCvc1bGaFt+EzlWXts171kcgcr7N2quZkT2L1syyP6TEwU1345brJtV4Ux/c4fBNxD8vrBeZAmEA9myl/41fFQSX5MXZzskoNG5aazFUpDnDl7F0Vhw4Q4/k=";
    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuiWylD7GoTGf/UtmUUXPHfbBTsvtAjcgUDbq4VBzrxKTjX1YcdQYxPhxmZeuAoWvDzm19rQ/QbceMGtN5PvwIT4u3TXd/Bva/mG7EW0qIcz0pfWpAg2n9cYisO5vZR/xjfy5P77aUR1qDyecLuki300Z/hc2KsYYqae9uTgN7xJO0dnwfZlF8uL1vxOfYQ/X7osW4ndZu3NQGno4MJLvgSkX5rXC72h7L0596w1EsXU+I5FzMFsCFQODN97LIuWfvbNVVbsbKJDfYOl7E+CtDPRoqARw8JxIjrIYkcjOKDxWafBpruZdRCe90nOvuIkvKtXAVtN3jxCwaJ94v4fJJwIDAQAB";
    public static void main(String[] args) {
        String requestNo = "requestNo";
        String data = "{\"batchNo\":\"12123\"}";
        // 组装标准格式的入参
        Map<String, Object> reqData = new LinkedHashMap<>();
        reqData.put("requestNo", requestNo);
        reqData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        reqData.put("data", data);
        // SHA256withRSA加签
        String signature = RSAUtil.generateContent(reqData);
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        reqData.put("sign", sign);
//        SimpleHttpClient.HttpReq httpReq = new SimpleHttpClient.HttpReq();
//        httpReq.setUrl("http://test.com");
//        httpReq.addHeader("Content-Type", "application/json");
//        httpReq.setContent(JSON.toJSONString(reqData));
//        System.out.println("请求入参：" + httpReq.getContent());
//        try (SimpleHttpClient simpleHttpClient = new SimpleHttpClient()) {
//            SimpleHttpClient.HttpResp httpResp = simpleHttpClient.doPost(httpReq);
//        }
        // 接收到验签
        Map<String, Object> respContent = JSON.parseObject(JSON.toJSONString(reqData), LinkedHashMap.class);
        String verifySignContent = RSAUtil.generateContent(respContent);
        System.out.println("同程验签：" + RSAUtil.verifySignByPublicKey(tcPublicKey, sign, verifySignContent));

        Map<String, String> stringStringMap = RSAUtil.generateKeyPair();
    }


}