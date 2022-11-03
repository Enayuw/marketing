package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.EncryptFileService;
import com.br.marketing.common.utils.file.AesCrpyt;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class EncryptFileServiceImpl implements EncryptFileService {

    @Override
    public boolean encryptFile(String apiCode, String fileName,String path) {
        File file=new File(path+fileName);
        if(!file.exists()){
            return false;
        }
        MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        if("1".equals(merchantParam.getFileEncryptionMethods())){
            String[] split = fileName.split("\\.");
            if(split.length>=2){
                String destFileName=split[0]+"_cipherdecrypt."+split[1];
                AesCrpyt.decrypt(path+fileName,path+destFileName,merchantParam.getFileEncryptionKey());
            }

            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean encrypt(String filePath,String password,String path) {
        String[] split = filePath.split("\\.");
        if(split.length>=2){
            String destFileName=split[0]+"_cipherencrypt."+split[1];
            log.info(destFileName);
            AesCrpyt.crypt(path+filePath,path+destFileName,password);


            String destFileName1=split[0]+"_cipherdecrypt."+split[1];
            AesCrpyt.decrypt(path+destFileName,path+destFileName1,password);
        }
        return true;
    }
}
