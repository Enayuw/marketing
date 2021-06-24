package com.br.marketing.check.service;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.service.Impl.FileCheckServiceImpl;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.common.utils.file.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/6/1 11:39
 * @Description:
 **/
@Service
@Slf4j
public abstract class AbstractDataToDbService implements BaseDataToDbService {
    @Resource
    private FileCheckServiceImpl fileCheckService;
    @Override
    public Boolean dowloadFile(FileContext context) {
        String localFilePath=context.getLocalZipFilePath();
        String zipFileName=context.getZipFileName();
        SftpClient client =(SftpClient)context.getBaseFtpClient();
        File dir=new File(localFilePath);
        if(!dir.exists()||!dir.isDirectory()){
            boolean mkdirs = dir.mkdirs();
            if(!mkdirs){
                log.error("创建文件夹失败-{}",context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb=new StringBuilder().append(localFilePath).append(zipFileName);
        boolean download = client.downloadFile(context.getSftpZipFilePath() , zipFileName, sb.toString());
        if(!download){
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}",context.getSftpZipFilePath(),zipFileName);
            return false;
        }
        return true;
    }

    @Override
    public Boolean unZipFile(FileContext context) {
        File localUnZipFile=new File(context.getLocalTxtFilePath());
        if(localUnZipFile.exists() && localUnZipFile.isDirectory()){
            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
            errorMessage.append("文件名重复");
            fileCheckService.errorDetail(context,errorMessage.toString(), ErrorFileTypeEnum.ERROR_FILE);
            log.error("文件名重复-{}",context.getZipFileName());
            return false;
        }

        File file=new File(context.getLocalZipFilePath().concat(context.getZipFileName()));
        try {
            if("0".equals(context.getMerchantParam().getFileEncryptionMethods())){
                ZipUtil.unZip(file,context.getLocalTxtFilePath());
            }else if("2".equals(context.getMerchantParam().getFileEncryptionMethods())){
                ZipUtils.unZip(file,context.getLocalTxtFilePath(),context.getMerchantParam().getFileEncryptionKey());
            }
        }catch (Exception e){
            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
            errorMessage.append("压缩文件解密异常");
            fileCheckService.errorDetail(context,errorMessage.toString(), ErrorFileTypeEnum.ERROR_FILE);
            log.error("压缩文件解密异常",e);
            return false;
        }
        return true;
    }



    @Override
    public void execute(FileContext context) {
            if(!dowloadFile(context)){
                return ;
            }
            if(!unZipFile(context)){
                return;
            }
            if(!checkTxtFile(context)){
                return;
            }
            checkConfigFile(context);
    }

}
