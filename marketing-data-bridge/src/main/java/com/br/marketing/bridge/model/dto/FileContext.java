package com.br.marketing.bridge.model.dto;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import lombok.Data;

/**
 * FileContext
 */
@Data
public class FileContext {
    /**
     * 内部sftp上文件路径
     */
    private String  sftpZipFilePath;
    /**
     * zip文件名字，包括数据文件和剔除文件
     */
    private String zipFileName;
    /**
     * 本地数据文件路径
     */
    private String localZipFilePath;
    /**
     * 任务实体对象
     */
    private MarketingTask task;
    /**
     * sftp客户端
     */
    private BaseFtpClient baseFtpClient;
    /**
     * apiCode账号信息
     */
    private MerchantParam merchantParam;
    /**
     * txt文件名字
     */
    private String txtFileName;
    /**
     * config文件名字
     */
    private String configFileName;
    /**
     * 本地txt文件路径
     */
    private String localTxtFilePath;
    /**
     * 本地txt文件去重后名字
     */
    private String distinctTxtFileName;
    /**
     * 本地txt文件去重后路径
     */
    private String distinctTxtFilePath;
    /**
     * 错误文件名称，文件校验错误
     */
    private String errorFileName;
    /**
     * 数据校验错误的错误文件名称
     */
    private String errorDataFileName;
    /**
     * 错误文件路径
     */
    private String errorFilePath;
    /**
     * 错误配置文件名称
     */
    private String errorConfigFileName;
    /**
     * apicode
     */
    private String apiCode;

    private String batchNumber;
    private String cusBatch;
    private String type;

    public void init(){
        if(StringUtils.isNotBlank(localZipFilePath) &&StringUtils.isNotBlank(zipFileName)){
            String name= Constants.MYREGEX.split(zipFileName)[0];
            this.localTxtFilePath=localZipFilePath.concat(name).concat("/");
            this.txtFileName=this.zipFileName.replace(".zip",".txt");
            this.configFileName=this.zipFileName.replace(".zip",".config");
            this.distinctTxtFileName=this.txtFileName;
            this.distinctTxtFilePath=this.localTxtFilePath.concat("distinct/");
            this.errorFilePath=this.localTxtFilePath.concat("error/");
            this.errorFileName=this.getApiCode().concat("_").concat(name).concat(Constants.ERRORFILE)
                    .concat(DateHelper.getDateAddYyMmDdHhMmSs(0)).concat(".txt");
            this.errorConfigFileName=this.errorFileName;
            String[] split =name.split("_");
            if(split.length>=3){
                if(StringUtils.isNotEmpty(this.type)&&"delete".equals(this.type)){
                    errorDataFileName=apiCode.concat("_").concat(name).concat("_error_").concat( DateHelper.getDateAddYyMmDd(0)).concat(".txt");
                }else {
                    errorDataFileName=split[0].concat("_").concat(split[1]).concat("_error_").concat(split[2]).concat(".txt");
                }

            }
        }
    }
}
