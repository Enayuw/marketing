package com.br.marketing.check.service;

public interface EncryptFileService {
    boolean encryptFile(String apiCode, String fileName,String path);

    boolean encrypt(String file,String password,String path);
}
