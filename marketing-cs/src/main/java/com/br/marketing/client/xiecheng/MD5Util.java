package com.br.marketing.client.xiecheng;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.check.utils
 * @Description:
 * @CreateTime: 2022-07-18 16 :36
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public class MD5Util {
    public MD5Util(){

    }
    public static String encode(String source) {
        return Hashing.md5().newHasher().putString(source, Charsets.UTF_8).hash().toString();
    }
}
