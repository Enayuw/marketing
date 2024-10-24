package com.br.marketing.bridge.service.todb.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.validator.CellUtils;
import com.br.marketing.bridge.model.dto.FileContext;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class SftpToDbByDXService {

    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;

    @Resource
    RabbitMqProducter producter;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    private static String phoneReg = "^([\\+]*[0-9]+)$";

    private final static Integer SPLITSIZE = 5000;

    public Boolean dowloadFile(FileContext context) {
        String localFilePath = context.getLocalTxtFilePath();
        String zipFileName = context.getTxtFileName();
        SftpClient client = (SftpClient) context.getBaseFtpClient();
        File dir = new File(localFilePath);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                log.error("创建文件夹失败-{}", context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb = new StringBuilder().append(localFilePath).append(zipFileName);
        boolean download = client.downloadFile(context.getSftpZipFilePath(), zipFileName, sb.toString());
        if (!download) {
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}", context.getSftpZipFilePath(), zipFileName);
            return false;
        }
        return true;
    }

//    public void checkConfigFile(FileContext context) {
//        MarketingTask task = context.getTask();
//        String configFilePathAndName = context.getLocalTxtFilePath().concat(context.getConfigFileName());
//        File configFile = new File(configFilePathAndName);
//        if (configFile.exists() && configFile.isFile()) {
//
//            Map<String, String> configMap = new HashedMap();
//            try (FileReader read = new FileReader(configFilePathAndName);
//                 BufferedReader br = new BufferedReader(read)) {
//                String row;
//                while ((row = br.readLine()) != null) {
//                    String trim = row.trim();
//                    if (StringUtils.isNotEmpty(trim)) {
//                        String[] split = trim.split("=");
//                        if (split.length >= 2) {
//                            configMap.put(split[0], split[1]);
//                        }
//                    }
//                }
//                String dataVolume = configMap.get("dataVolume");
//                if (StringUtils.isNotEmpty(dataVolume)) {
//                    try {
//                        int count = Integer.parseInt(dataVolume);
//                        task.setDataVolume(count);
//                    } catch (Exception e) {
//                        log.error("dataVolume error", e);
//                    }
//                }
//                log.warn("{}，内容为{}", context.getConfigFileName(), configMap);
//                if (task.getMonitorType() == 1) {
//                    if (StringUtils.isNotEmpty(configMap.get("strategyId")) && fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")) {
//                        task.setStrategyId(configMap.get("strategyId"));
//                        task.setFrequency(0 + "");
//                        task.setCloseDate(DateHelper.getDateAdd(2));
//                        task.setStartDate(DateHelper.getDateAdd(0));
//                    } else {
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("配置文件异常,策略编号异常");
//                        fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//                } else if (task.getMonitorType() == 2 || task.getMonitorType() == 3 || task.getMonitorType() == 4) {
//                    if (StringUtils.isNotEmpty(configMap.get("strategyId")) && fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")) {
//                        task.setStrategyId(configMap.get("strategyId"));
//                    } else {
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("配置文件异常,策略编号异常");
//                        fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//                    if (StringUtils.isNotEmpty(configMap.get("monitorFrequency")) && fileCheckService.checkConfig("monitorFrequency", configMap.get("monitorFrequency"), task.getApiCode(), "")) {
//                        task.setFrequency(configMap.get("monitorFrequency"));
//                    } else {
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("配置文件异常,监控周期异常");
//                        fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//                    if (StringUtils.isNotEmpty(configMap.get("monitorStartTime")) && fileCheckService.checkConfig("monitorStartTime", configMap.get("monitorStartTime"), task.getApiCode(), "")) {
//                        task.setStartDate(configMap.get("monitorStartTime"));
//                    } else {
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("配置文件异常,监控开始日期异常");
//                        fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//                    if (StringUtils.isNotEmpty(configMap.get("monitorStartTime")) && fileCheckService.checkConfig("monitorendTime", configMap.get("monitorendTime"), task.getApiCode(), configMap.get("monitorStartTime"))) {
//                        task.setCloseDate(configMap.get("monitorStartTime"));
//                    } else {
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("配置文件异常,监控截止日期异常");
//                        fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//                } else {
//                    task.setMonitorStatus(3);
//                    task.setStatus(1);
//                    task.setErrorMessage("监控模式异常");
//                    fileCheckService.errorDetail(context, task.getErrorMessage(), ErrorFileTypeEnum.ERROR_CONFIG);
//                    return;
//                }
//
//            } catch (FileNotFoundException e) {
//                log.error("FileNotFoundException", e);
//            } catch (IOException e) {
//                log.error("IOException", e);
//            }
//            LoadResult lr = new LoadResult();
//            lr.setApiCode(task.getApiCode());
//            lr.setFileName(context.getConfigFileName());
//            lr.setBatchNumber(task.getBatchNumber());
//            lr.setStatus("1");
//            loadResultMapper.insertLoadResult(lr);
//            task.setMonitorStatus(1);
//        }
//        task.setStatus(1);
//    }


    private Result setDataByPhone(String row,PhoneSale phoneSale,HashMap<Integer,String> address,HashMap<Integer,String> extSetFields,AtomicInteger errorMark,Integer line){
        try {
            List<String> datas = Splitter.on(",").splitToList(row);
            JSONObject jo = null;
            String error = "uid不能为空;phone不能为空;orgName不能为空;user_type不能为空;name不能为空;";
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                errorMark.getAndIncrement();
                phoneSaleMapper.insertSelective(phoneSale);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
                    case "uid":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("uid不能为空;", "");
                        }
                        phoneSale.setUid(datas.get(i));
                        break;
                    case "phone":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("phone不能为空;", "");
                            Result<String> stringResult = decryptPhone(datas.get(i));
                            phoneSale.setPhoneAes(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSale.setPhone(AESUtil.aesEncrypty(stringResult.getData(), aesKey));
                            } else {
                                phoneMark = Boolean.FALSE;
                            }
                        }
                        break;
                    case "name":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("name不能为空;", "");
                            phoneSale.setName(datas.get(i));
                        }
                        break;
                    case "gender":
                        phoneSale.setGender(datas.get(i));
                        break;
                    case "marketscore":
                        phoneSale.setMarketscore(datas.get(i));
                        break;
                    case "riskscore":
                        phoneSale.setRiskscore(datas.get(i));
                        break;
                    case "orgname":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("orgName不能为空;", "");
                            phoneSale.setOrgname(datas.get(i));
                        }
                        break;
                    case "source":
                        phoneSale.setSource(datas.get(i));
                        break;
                    case "user_type":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("user_type不能为空;", "");
                            phoneSale.setUserType(datas.get(i));
                        }
                        break;
                    case "product_name":
                        phoneSale.setProductName(datas.get(i));
                        break;
                    case "flag_type":
                        phoneSale.setFlagType(datas.get(i));
                        break;
                    case "type":
                        phoneSale.setType(datas.get(i));
                        break;
                    case "level":
                        phoneSale.setLevel(datas.get(i));
                        break;
                    case "if_register":
                        phoneSale.setIfRegister(datas.get(i));
                        break;
                    case "register_time":
                        phoneSale.setRegisterTime(datas.get(i));
                        break;
                    case "if_login":
                        phoneSale.setIfLogin(datas.get(i));
                        break;
                    case "login_time":
                        phoneSale.setLoginTime(datas.get(i));
                        break;
                    case "if_apply":
                        phoneSale.setIfApply(datas.get(i));
                        break;
                    case "apply_dt":
                        phoneSale.setApplyDt(datas.get(i));
                        break;
                    case "apply_time":
                        phoneSale.setApplyTime(datas.get(i));
                        break;
                    case "apply_result":
                        phoneSale.setApplyResult(datas.get(i));
                        break;
                    case "pagenode":
                        phoneSale.setPagenode(datas.get(i));
                        break;
                    case "optype":
                        phoneSale.setOptype(datas.get(i));
                        break;
                    case "refuse_time":
                        phoneSale.setRefuseTime(datas.get(i));
                        break;
                    case "audit_time":
                        phoneSale.setAuditTime(datas.get(i));
                        break;
                    case "audit_amount":
                        phoneSale.setAuditAmount(datas.get(i));
                        break;
                    case "if_lent":
                        phoneSale.setIfLent(datas.get(i));
                        break;
                    case "lent_time":
                        phoneSale.setLentTime(datas.get(i));
                        break;
                    case "lent_amount":
                        phoneSale.setLentAmount(datas.get(i));
                        break;
                    case "unlent_amount":
                        phoneSale.setUnlentAmount(datas.get(i));
                        break;
                    case "if_settle":
                        phoneSale.setIfSettle(datas.get(i));
                        break;
                    case "settle_time":
                        phoneSale.setSettleTime(datas.get(i));
                        break;
                    case "activity":
                        phoneSale.setActivity(datas.get(i));
                        break;
                    case "production":
                        phoneSale.setProduction(datas.get(i));
                        break;
                    case "region":
                        phoneSale.setRegion(datas.get(i));
                        break;
                    case "extend":
                        String s = extSetFields.get(i);
                        if (StringUtils.isNotBlank(s)) {
                            if (jo == null) {
                                jo = new JSONObject();
                            }
                            jo.put(s, datas.get(i));
                        }
                        break;
                }
                if (jo != null) {
                    phoneSale.setExtend(jo.toJSONString());
                }
            }
            if (!StringUtils.isEmpty(error)) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, error));
                errorMark.getAndIncrement();
            } else if (!phoneMark) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
                errorMark.getAndIncrement();
            }
            Date date = new Date();
            phoneSale.setCreateTime(date);
            phoneSale.setUpdateTime(date);
            phoneSaleMapper.insertSelective(phoneSale);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            errorMark.getAndIncrement();
            phoneSaleMapper.insertSelective(phoneSale);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    Result<String> decryptPhone(String phone){

        /**
         * 判断是否全是数字格式
         *      是数字格式 成功
         *      不是数字 进行aes解密
         *          判断解密后的文本是否是手机号
         *              是手机号 成功
         *              不是手机号 进行md5 ，sha256解密 判断是密文是否是手机号
         *                  是手机号 成功
         *                  不是 失败
         */
        Result<String> objectResult = new Result<>();
        boolean isNum = Pattern.matches(phoneReg, phone);
        if(isNum){
            objectResult.setDate(phone);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }

        String s = AESUtil.decrypt(phone, aesKey);
        if(StringUtils.isNotBlank(s)&&CellUtils.isValidateCell(s)){
            objectResult.setDate(s);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }

        String res = "";
        if (DecodeGrpcClient.isMd5(phone)) {
            //cell md5
            res = RpcClientProxy.decode(phone, "cell", "md5", "");
        } else {
            //cell sha256
            res = RpcClientProxy.decode(phone, "cell", "sha", "");
        }
        if(StringUtils.isBlank(res)){
            objectResult.setCode(ResultCode.FAIL.getValue());
        }else{
            if(CellUtils.isValidateCell(res)){
                objectResult.setCode(ResultCode.SUCCESS.getValue());
                objectResult.setDate(res);
            }else{
                objectResult.setCode(ResultCode.FAIL.getValue());
            }
        }
        return objectResult;
    }
}
