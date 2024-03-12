package com.br.marketing.client.didi.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 *
 */
public class MD5Util {
    public static String encode(String source) {
        return Hashing.md5().newHasher().putString(source, Charsets.UTF_8).hash().toString();
    }
}
