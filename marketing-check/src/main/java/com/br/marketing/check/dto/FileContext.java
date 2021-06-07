package com.br.marketing.check.dto;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import lombok.Data;

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
 * @Date 2021/6/1 11:31
 * @Description:
 **/
@Data
public class FileContext {
    private String  sftpZipFilePath;
    private String zipFileName;
    private String localZipFilePath;
    private MarketingTask task;
    private BaseFtpClient baseFtpClient;
    private MerchantParam merchantParam;
    private String txtFileName;
    private String configFileName;
    private String localTxtFilePath;
    private String distinctTxtFileName;
    private String distinctTxtFilePath;
    private String errorFileName;
    private String errorDataFileName;
    private String errorFilePath;
    private String errorConfigFileName;


    public void init(){
        if(StringUtils.isNotBlank(localZipFilePath) &&StringUtils.isNotBlank(zipFileName)){
            String name= Constants.MYREGEX.split(zipFileName)[0];
            this.localTxtFilePath=localZipFilePath.concat(name).concat("/");
            this.txtFileName=this.zipFileName.replace(".zip",".txt");
            this.configFileName=this.zipFileName.replace(".zip",".config");
            this.distinctTxtFileName=this.txtFileName;
            this.distinctTxtFilePath=this.localTxtFilePath.concat("distinct/");
            this.errorFilePath=this.localTxtFilePath.concat("error/");
            this.errorFileName=this.getTask().getApiCode().concat("_").concat(name).concat(Constants.ERRORFILE).concat(DateHelper.getDateAddYyMmDd(0)).concat(".txt");
            this.errorConfigFileName=this.errorFileName;
            String[] split =name.split("_");
            if(split.length>=3){
                errorDataFileName=split[0].concat("_").concat(split[1]).concat("_error_").concat(split[2]).concat(".txt");
            }

        }


    }
}
