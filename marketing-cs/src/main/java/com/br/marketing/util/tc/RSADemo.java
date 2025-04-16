package com.br.marketing.util.tc;

public class RSADemo {

    /**
     * 合作方私钥
     */
    private static final String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDJbl3BZUt5N0USArHE+aib+ZPUBPa/uyL14O9Wx7kzOqVKjByHgD87gcoWg7LIDkpx9O+raRuvXE3XzR9v2JHTY5mbzpxmLis4G9dY3ZMhqL1EDzKkrTkgJ0UtDiNknmCPlT1jeO12x7qtsEGvB0ycwv2T7yrPwfsQCA5XSFM64PVavw74wkeGYUlWTIeBDi4+ePQmXLD+S7wO1uPelXtXC1PtH2lgCwWUesw4xodLSKPp1hxqV/owKwz7SZOfsUvpRQOtg6bUvk21NNkrV6TvE0N5BuIXm0tup/JqOIY0MRhPZssHno8qi3WVsrGS/++IsooK3mW6LaWWVz/AUU0xAgMBAAECggEAVNbSUElsv3JC+jH+U9tfY8p7s/ofP2P/dpY12FQoseYKhlp7Das7dsHUryTrIQ2710F1zLlperOi6biwe/g4e1Ng+FnOT880Tt6TFg/FD5E1njvUdb2r6NoBbdedmpyGSRNvlrzxL20yOEObdq/ZWdFa4nsCihzGtNrfx21j+NDVmT7W8vgZmMO9TOlMy2lfYcIVv9gSokKokWSXQL4tIp2atToQP2ydSrsihOsj2JwAueDQ7yuCSKfxSxQgjq+C0c2pNfzU8lt7mNLwpKHkdK1nWLpNzJR5bMh6EdaCPup7iAPt1P5qNgBV2c38D0oczAkjFoTcN828BiD0BEWYHQKBgQDvSc73LvWla9t4e766PTay6nQFH7kJKbgGXUZ9QPuB/QJG/Xmga2SkLRRkyoCk1V2TDZc0FVwD8S0MYqNx4o0mNQhXXgJ0Os+EqLzfmv9OOCk+h79YXOJJfc8aEpgNUpj7ZZwhVhfAcXZLi5Va1oMTExOJztr3f7brDprlWnPL8wKBgQDXf7dJI45cJf3yKUKBjUZGbRMB6L/rXPaC0sPIt3ayDWm98FFFzXBxh1/Agquiv4VZ1QG3k6pcpuZj6kclsQJ8aDXuNUbCtFeWAlOpU3tW3exiUMMdJ9U6uazBlIHzE4CDzF5/W1QVjB/zoCW5XdKjjPcbK5uITTxK4wX/2a1/SwKBgQDo0nHTxBcy6uzSH+UFpIXSM5jLW4BYUAVD/AGX4WBVq+1JUVvEIHn92/bZ0sa0mv2/FRwnixjKpSDlyhuptXsMz9Db4RaWLJq5Qszy7Aa4/p/yvXYJ7eYnB4g4oBrd6536DXWKYhAeKMem3ZtmlQ1Xw9ng3injYpqBvuYrfoTozQKBgQCf0dxApdoChTqcyfTE191yhZo/JDqRd1RrGZyR0154FnKBpyTfO8lUqS1ePIrGHHvtb4fieeUy6aScHTe+wFQzp6W3kFN2OwFqAzfAsGUcu2GAWJ0bllsRPnrc5iKp/6CkHoFGupjVkiJwMClKHEcHfJgW0nR61a0NbOkaInoFmQKBgQChp9BwmmKtjN+T3bm5yPUheKPUE/24EK7an6/JfYYQEiEMYWZGZd2Fx6w0lbDyLCypfH9/kGc0UWs0ZVNw7uaSH5MGvMGB3Pp3oT7euWemAxO8Ohk1Sj0tDnKwZqPNmPh+B+Mr44WyURmDnbDzR14FB+tJSU8lrlbvIBtBl+ya9w==";
    /**
     * 合作方公钥
     */
    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyW5dwWVLeTdFEgKxxPmom/mT1AT2v7si9eDvVse5MzqlSowch4A/O4HKFoOyyA5KcfTvq2kbr1xN180fb9iR02OZm86cZi4rOBvXWN2TIai9RA8ypK05ICdFLQ4jZJ5gj5U9Y3jtdse6rbBBrwdMnML9k+8qz8H7EAgOV0hTOuD1Wr8O+MJHhmFJVkyHgQ4uPnj0Jlyw/ku8Dtbj3pV7VwtT7R9pYAsFlHrMOMaHS0ij6dYcalf6MCsM+0mTn7FL6UUDrYOm1L5NtTTZK1ek7xNDeQbiF5tLbqfyajiGNDEYT2bLB56PKot1lbKxkv/viLKKCt5lui2lllc/wFFNMQIDAQAB";

    /**
     * 同程私钥
     */
    private static final String tcPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";
    /**
     * 同程公钥
     */
    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs9He491hp3zCI70rKQe79mXJglifGas59YLzHvfW6ljp73u1d36U2UnWVTIOwfL4iQ1La1Kg2P2UlrFKI1tDd++G842JRjWWJVpmKomXymWeOAASuvnZMuTA4Z2k6WIoZYcMYiqfD+PGsI8jMYKlsb+3VoUhIFRb4Q5CEK/V2LUH9dRC9rNTem6pGn9brxgEQG+sg8/b4eLaK86MPm5n3tB3ox9UMsOQt3s2vlhJoRXQRRHh+BoZC8Kd8EOpqKUA6cjz186zSSKO82qm2ifrPi049qvBAlfIR53cEh62otH6IJGnQDBfbYEw9lMB2QLbytCvCFtUS9BPJBSGpn5VlQIDAQAB";

    public static void main(String[] args) throws Exception {
        // 同程进行加密加签
        String reqParams = "reqParams";
        // 同程私钥对加密数据encData进行签名
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, reqParams);

        String signTC = sign;

        if (RSAUtil.verifySignByPublicKey(tcPublicKey, signTC, reqParams)) {
            System.out.println("接入方拿到后验签成功");
        }
        System.out.println("签名：" + sign);
        String respParams = "respParams";
        System.out.println("接入方验签成功后，构造返回结果未加密的：" + respParams);

        String respSign = RSAUtil.signByPrivateKey(privateKey, respParams);

        System.out.println("同程收到请求进行验签--------");
        boolean b = RSAUtil.verifySignByPublicKey(publicKey, respSign, respParams);
        System.out.println("同程验签结果：" + b);
    }
}
