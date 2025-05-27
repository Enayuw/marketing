package com.br.marketing.check.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 16:57
 * @Company：百融
 * @Description: 功能描述
 */
@Slf4j
public class SftpToDbUtils {
    private static final Pattern REMARK_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_REMARK);
    private static final Pattern FILE_NAME_REGEX = Pattern.compile("^[0-9]{7}");


    /**
     * 遍历sftp目录下所有的文件，获取需要处理的文件名称
     *
     * @param path       sftp目录
     * @param map        结果
     * @param sftpClient sftp连接
     */
    public static void listStpFile(String path, Map<String, Set<String>> map, SftpClient sftpClient) {
        try {
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(path);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if (attrs.isDir()) {
                    log.debug("fileName:{}", fileName);
                    if (FILE_NAME_REGEX.matcher(fileName).matches() || "input".equals(fileName)) {
                        log.debug("isDirectory file:{}", fileName);
                        listStpFile(path + fileName + "/", map, sftpClient);
                    }
                } else {
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    long minutes = DateHelper.getDistanceMinutes(createFileTime);
                    if (minutes < 1) {
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理");
                        continue;
                    }
                    if (StringUtils.isNotEmpty(fileName) && (fileName.endsWith(".zip")
                            || fileName.endsWith(".finish") || fileName.endsWith(".success"))) {
                        Set<String> set = map.get(path);
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(path, set);
                        }
                        set.add(fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
        if(!map.isEmpty()){
            log.warn("map :{}", map);
        }
    }


    public static void listStpFile(String path, Map<String, Set<String>> map, SftpClient sftpClient, SyncConfig syncConfig) {
        try {
            List<String> suffixs = Splitter.on(",").splitToList(syncConfig.getSuffix());
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(path);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if (attrs.isDir()) {
                    listStpFile(path + fileName + "/", map, sftpClient);
                } else {
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    long minutes = DateHelper.getDistanceMinutes(createFileTime);
                    if (minutes < 1) {
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理");
                        continue;
                    }
                    String[] names = fileName.split("\\.");
                    String name = ".".concat(names[names.length - 1]);
                    if (StringUtils.isNotEmpty(fileName) && (suffixs.contains(name))) {
                        Set<String> set = map.get(path);
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(path, set);
                        }
                        set.add(fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
        if(!map.isEmpty()){
            log.warn("map :{}", map);
        }
    }

    /**
     * 校验sftp目录中的apiCode是否正确
     *
     * @param key sftp目录 loanwarn/4200333/input
     * @return
     */
    public static MerchantParam vaildApicode(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String[] split = key.split("/");
        if (split.length != 5) {
            return null;
        }
        String apiCode = split[3];
        MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        if (merchantParam == null) {
            log.error("merchantParam is null,{}", apiCode);
            return null;
        }
        return merchantParam;
    }

    /**
     * 校验文件名称是否正确
     *
     * @param zipFileName 文件名称
     *                    数据文件：
     *                    360:3005390_UploadCustomFileName20200923_00003001_20200923.zip
     *                    其他：3004761_bairongniankuanguserinfo20200820_20200821.zip
     *                    剔除文件：
     *                    360：4000100_2020080401_001_DeleteMonitor_20200710.zip
     *                    其他：4000100_2020080401001_DeleteMonitor_20200710.zip
     * @param apiCode     apiCode
     */
    public static boolean vaildFileName(String zipFileName, String apiCode, StringBuilder errorMessage) {
        if (StringUtils.isNotEmpty(zipFileName)) {
            String[] zipFileNameArr = zipFileName.split("\\.");
            if (zipFileNameArr.length < 2) {
                errorMessage.append("文件名称命名异常");
                return false;
            }
            String name = zipFileNameArr[0];
            String[] s1 = name.split("_");
            boolean isDelete=name.contains("DeleteMonitor");
            int length = isDelete ? 4 : 3;
            if (s1.length != length) {
                errorMessage.append("文件名称命名异常");
                return false;
            } else {
                if (!apiCode.equals(s1[0])) {
                    errorMessage.append("apicode异常");
                    return false;
                } else if (!REMARK_REGEX.matcher(s1[1]).matches()) {
                    errorMessage.append("文件批次命名异常");
                    return false;
                } else if (isDelete && !"DeleteMonitor".equals(s1[2])) {
                    errorMessage.append("文件名称命名异常");
                    return false;
                } else {
                    String s2 = isDelete ? s1[3] : s1[2];
                    try {
                        DateHelper.parseDate(s2);
                    } catch (IllegalArgumentException e) {
                        log.error("日期异常", e);
                        errorMessage.append("日期异常");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 返回文件校验失败错误文件
     *
     * @param context      参数信息
     * @param errorMessage 校验出错提示信息
     */
    public static void returnErrorFile(FileContext context, String errorMessage, ErrorFileTypeEnum errorFileTypeEnum) {
        SftpClient client = (SftpClient) context.getBaseFtpClient();
        String errorFilePath = context.getErrorFilePath();
        String errorFileName="";
        switch (errorFileTypeEnum){
            case ERROR_FILE:
                errorFileName=context.getErrorFileName();
                break;
            case ERROR_DATA:
                errorFileName=context.getErrorDataFileName();
                break;
            case ERROR_CONFIG:
                errorFileName=context.getErrorConfigFileName();
                break;
            default:
        }
        String errorFilePathAndName=errorFilePath.concat(errorFileName);
        File dir = new File(context.getErrorFilePath());
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        log.info("errorFilePath:{},errorFileName：{}", errorFilePath, errorFileName);
        File errorFile = new File(errorFilePathAndName);
        if (errorFile.exists()) {
            log.error("errorFileName {}文件已存在", errorFilePathAndName);
        }
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(errorFilePathAndName)), StandardCharsets.UTF_8));) {
            fw.append("errorType,message\n");
            fw.append(errorMessage + "\n");
        } catch (Exception e) {
            log.error("生成错误文件出错", e);
        }
        if (errorFile.isFile()) {
            try {
                String remotePath = Constants.SFTP_IN_ERROR_PATH.replace("apiCode", context.getApiCode());
                boolean upload = client.uploadFile(remotePath, errorFileName, errorFilePathAndName);
                if (upload) {
                    File successFile = new File(errorFilePathAndName + ".success");
                    successFile.createNewFile();
                    if (successFile.exists()) {
                        client.uploadFile(remotePath, errorFileName + ".success", errorFilePathAndName + ".success");
                    }
                }
            } catch (Exception e) {
                log.error("上传错误文件到sftp出错", e);
            }
        }
    }


    public static boolean checkHead(FileContext context,String head) {
        MerchantParam merchantParam =context.getMerchantParam();
        boolean headFlag = true;
        if (StringUtils.isEmpty(head)) {
            headFlag = false;
        } else {
            String[] headSplit = head.split(",");
            List<String> list = Arrays.asList(headSplit);
            if (!list.contains("cus_num")) {
                headFlag = false;
            }
            if (merchantParam.getIsCheck() == 0 || merchantParam.getIsCheck() == 2 || merchantParam.getIsCheck() == 4) {
                if (!list.contains("id") && !list.contains("cell") && !list.contains("name")) {
                    headFlag = false;
                }
            } else if (merchantParam.getIsCheck() == 1 || merchantParam.getIsCheck() == 3 || merchantParam.getIsCheck() == 5) {
                if (!list.contains("id") || !list.contains("cell") || !list.contains("name")) {
                    headFlag = false;
                }
            }
        }
        return headFlag;
    }
    public static boolean checkDeleteFileHead(String head) {
        boolean headFlag = true;
        if (StringUtils.isEmpty(head)) {
            headFlag = false;
        } else {
            String[] headSplit = head.split(",");
            List<String> list = Arrays.asList(headSplit);
            if (!list.contains("cus_num")||(!list.contains("id")&&!list.contains("cell")&&!list.contains("name"))) {
                headFlag = false;
            }
        }
        return headFlag;
    }
    public static String getBatchNumber(String apiCode) {
        String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
        int i = (int) ((Math.random()*9+1)*1000);
        return apiCode+"_"+dateAddYyMmDdHhMmSs+"_"+i;
    }
    public static Result statisticsHead(String head,HashMap<Integer, String> address,HashMap<Integer, String> extra){
        List<String> heads = Splitter.on(",").splitToList(head);
        if(heads.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不存在");
        }
        Integer extraMark = null;
        Boolean uidMark = false;
        Boolean phoneMark = false;
        Boolean orgNameMark = false;
        Boolean userTypeMark = false;
        Boolean nameMark = false;
        Boolean startExt = false;
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if(StringUtils.isBlank(s)){
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不能有空字段");
            }
            switch (s){
                case "uid":
                    uidMark = true;
                    break;
                case "phone":
                    phoneMark = true;
                    break;
                case "orgname":
                    orgNameMark = true;
                    break;
                case "user_type":
                    userTypeMark = true;
                    break;
                case "name":
                    nameMark = true;
                    break;
            }

            if(s.equals("extend")){
                startExt = true;
            }
            if(startExt){
                address.put(i,"extend");
                extra.put(i,s);
            }else{
                address.put(i,s);
            }
        }
        if(!(uidMark&&phoneMark&&orgNameMark&&userTypeMark&&nameMark)){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("表头缺少必填字段");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    public static Result statisticsHeadByCommon(String head,HashMap<Integer, String> address,HashMap<Integer, String> extra,List<String> baseHeads){
        List<String> heads = Splitter.on(",").splitToList(head);
        if(heads.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不存在");
        }
        Boolean startExt = false;
        if(!heads.containsAll(baseHeads)){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("表头缺少必填字段");
        }
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if(StringUtils.isBlank(s)){
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不能有空字段");
            }
            if(s.equals("extend")){
                startExt = true;
            }
            if(startExt){
                address.put(i,"extend");
                extra.put(i,s);
            }else{
                address.put(i, s);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     *
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/21 17:47
     * @param head 文件表头
     * @param address 空值, <位置,表头字段名>
     * @param extra 空值, 扩展子段包含的表头字段名
     * @param baseHeads 必填字段
     * @param fieldVosMap <表头字段,处理规则配置>
     * @return Result
     */
    public static Result statisticsHeadByCommon(String head,HashMap<Integer, String> address,HashSet extra,
                                                List<String> baseHeads,Map<String, List<FileToMarketingFieldVO>> fieldVosMap){
        List<String> heads = Splitter.on(",").splitToList(head);
        if(heads.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不存在");
        }
        if(!heads.containsAll(baseHeads)){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("表头缺少必填字段,必填字段：" + baseHeads);
        }
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if(StringUtils.isBlank(s)){
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不能有空字段");
            }
            FileToMarketingFieldVO fieldVO = null;
            //根据当前表头名获取配置信息
            List<FileToMarketingFieldVO> fileToMarketingFieldVOS = fieldVosMap.get(s);
            if (fileToMarketingFieldVOS != null && fileToMarketingFieldVOS.size() > 0) {
                fieldVO = fileToMarketingFieldVOS.get(0);
            }
            if (fieldVO != null && fieldVO.getIsExtend() == null) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未配置该字段是否为扩展字段的属性信息"+ JSONObject.toJSONString(fieldVO));
            }
            // 扩展字段
            if(fieldVO == null || fieldVO.getIsExtend()){
                extra.add(s);
            }
            address.put(i, s);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
