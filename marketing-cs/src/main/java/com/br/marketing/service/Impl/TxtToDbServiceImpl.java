package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.validator.CellUtils;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.mapper.TwosevenFileMapper;
import com.br.marketing.service.ITxtToDbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TxtToDbServiceImpl implements ITxtToDbService {

    @Autowired
    TwosevenFileMapper twosevenFileMapper;

    @Autowired
    PhoneSaleMapper phoneSaleMapper;

    @Autowired
    DecodeClient decodeClient;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    private static String phoneReg = "^([\\+]*[0-9]+)$";

    @Override
    public Result TwoSevenToDb(TxtToDbDTO dto) {
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetField = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "mobile不能为空;";
        TwosevenFile twosevenFile = new TwosevenFile();
        twosevenFile.setCid(dto.getCid());
        twosevenFile.setApiCode(dto.getApiCode());
        twosevenFile.setLocalId(dto.getLocalId());
        twosevenFile.setUserType("1");
        twosevenFile.setOrgName("qiqi");
        twosevenFile.setStatus(1);
        try {
            if (datas.size() != address.size()) {
                twosevenFile.setStatus(2);
                twosevenFile.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                twosevenFileMapper.insertSelective(twosevenFile);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
                    case "mobile":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("mobile不能为空;", "");
                        }
                        twosevenFile.setMobile(datas.get(i));
                        twosevenFile.setCustNum(datas.get(i));
                        break;
                    case "extend":
                        String s = extSetField.get(i);
                        if (StringUtils.isNotBlank(s)) {
                            if (jo == null) {
                                jo = new JSONObject();
                            }
                            jo.put(s, datas.get(i));
                        }
                        break;
                }
                if (jo != null) {
                    twosevenFile.setExtend(jo.toJSONString());
                }
            }
            if (!StringUtils.isEmpty(error)) {
                twosevenFile.setStatus(2);
                twosevenFile.setDataMessage(String.format("行号：%d;报错信息：%s", line, error));
            }
            Date date = new Date();
            twosevenFile.setCreateTime(date);
            twosevenFile.setUpdateTime(date);
            twosevenFileMapper.insertSelective(twosevenFile);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            twosevenFile.setStatus(2);
            twosevenFile.setDataMessage(String.format("行号：%d;报错信息：%s"
                    , line
                    ,ex.getMessage().length()>=450
                            ?ex.getMessage().substring(0,449)
                            :ex.getMessage()));
            twosevenFileMapper.insertSelective(twosevenFile);
        }
        return new Result().setCode(new Integer("1").equals(twosevenFile.getStatus())
                ?ResultCode.SUCCESS.getValue()
                :ResultCode.FAIL.getValue());
    }

    @Override
    public Result phoneTodb(TxtToDbDTO dto) {
        PhoneSale phoneSale = new PhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;phone不能为空;orgName不能为空;user_type不能为空;name不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setStatus(1);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                phoneSaleMapper.insertSelective(phoneSale);
                return new Result().setCode(ResultCode.FAIL.getValue());
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
                            String s = datas.get(i);
                            phoneSale.setName(s);
                            if(DecodeClient.isMd5(s)){
                                String content = decodeClient.query(s, "name", "md5", "");
                                if(StringUtils.isBlank(content)){
                                    error = error.concat("姓名解密失败;");
                                }else{
                                    phoneSale.setName(content);
                                }
                            }
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
                    case "yx_flag_3d":
                        phoneSale.setYxFlag3d(datas.get(i));
                        break;
                    case "yx_flag_7d":
                        phoneSale.setYxFlag7d(datas.get(i));
                        break;
                    case "yx_flag_15d":
                        phoneSale.setYxFlag15d(datas.get(i));
                        break;
                    case "yx_flag_1m":
                        phoneSale.setYxFlag1m(datas.get(i));
                        break;
                    case "person_flag_house":
                        phoneSale.setPersonFlagHouse(datas.get(i));
                        break;
                    case "person_flag_car":
                        phoneSale.setPersonFlagCar(datas.get(i));
                        break;
                    case "person_flag_insur":
                        phoneSale.setPersonFlagInsur(datas.get(i));
                        break;
                    case "white_list_gw":
                        phoneSale.setWhiteListGw(datas.get(i));
                        break;
                    case "white_list_fp":
                        phoneSale.setWhiteListFp(datas.get(i));
                        break;
                    case "white_list_yc":
                        phoneSale.setWhiteListYc(datas.get(i));
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
            } else if (!phoneMark) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            }
            Date date = new Date();
            phoneSale.setCreateTime(date);
            phoneSale.setUpdateTime(date);
            phoneSaleMapper.insertSelective(phoneSale);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            phoneSaleMapper.insertSelective(phoneSale);
        }
        return new Result().setCode(new Integer("1").equals(phoneSale.getStatus())
                ?ResultCode.SUCCESS.getValue()
                :ResultCode.FAIL.getValue());
    }

    @Override
    public Result phoneTodbByXW(TxtToDbDTO dto) {
        PhoneSale phoneSale = new PhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;phone不能为空;name不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setOrgname("xiaowei");
        phoneSale.setUserType("A");
        phoneSale.setSource("17");
        phoneSale.setStatus(1);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                phoneSaleMapper.insertSelective(phoneSale);
                return new Result().setCode(ResultCode.FAIL.getValue());
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
                            Result<String> stringResult = decryptMd5Phone(datas.get(i));
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
                            String s = datas.get(i);
                            phoneSale.setName(s);
                            if(DecodeClient.isMd5(s)){
                                String content = decodeClient.query(s, "name", "md5", "");
                                if(StringUtils.isBlank(content)){
                                    error = error.concat("姓名解密失败;");
                                }else{
                                    phoneSale.setName(content);
                                }
                            }
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
                    case "yx_flag_3d":
                        phoneSale.setYxFlag3d(datas.get(i));
                        break;
                    case "yx_flag_7d":
                        phoneSale.setYxFlag7d(datas.get(i));
                        break;
                    case "yx_flag_15d":
                        phoneSale.setYxFlag15d(datas.get(i));
                        break;
                    case "yx_flag_1m":
                        phoneSale.setYxFlag1m(datas.get(i));
                        break;
                    case "person_flag_house":
                        phoneSale.setPersonFlagHouse(datas.get(i));
                        break;
                    case "person_flag_car":
                        phoneSale.setPersonFlagCar(datas.get(i));
                        break;
                    case "person_flag_insur":
                        phoneSale.setPersonFlagInsur(datas.get(i));
                        break;
                    case "white_list_gw":
                        phoneSale.setWhiteListGw(datas.get(i));
                        break;
                    case "white_list_fp":
                        phoneSale.setWhiteListFp(datas.get(i));
                        break;
                    case "white_list_yc":
                        phoneSale.setWhiteListYc(datas.get(i));
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
            } else if (!phoneMark) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            }
            Date date = new Date();
            phoneSale.setCreateTime(date);
            phoneSale.setUpdateTime(date);
            phoneSaleMapper.insertSelective(phoneSale);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            phoneSaleMapper.insertSelective(phoneSale);
        }
        return new Result().setCode(new Integer("1").equals(phoneSale.getStatus())
                ?ResultCode.SUCCESS.getValue()
                :ResultCode.FAIL.getValue());
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
        if(StringUtils.isNotBlank(s)&& CellUtils.isValidateCell(s)){
            objectResult.setDate(s);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }

        String res = "";
        if (DecodeClient.isMd5(phone)) {
            //cell md5
            res = decodeClient.query(phone, "cell", "md5", "");
        } else {
            //cell sha256
            res = decodeClient.query(phone, "cell", "sha", "");
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
    Result<String> decryptMd5Phone(String phone){
        Result<String> objectResult = new Result<>();
        boolean isNum = Pattern.matches(phoneReg, phone);
        if(isNum){
            objectResult.setDate(phone);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }
        String res = decodeClient.query(phone, "cell", "md5", "");

        if(StringUtils.isBlank(res)){
            objectResult.setCode(ResultCode.FAIL.getValue());
        }else{
            if (CellUtils.isValidateCell(res)) {
                objectResult.setCode(ResultCode.SUCCESS.getValue());
                objectResult.setDate(res);
            } else {
                objectResult.setCode(ResultCode.FAIL.getValue());
            }
        }
        return objectResult;
    }

    @Override
    public Result phoneTodbByJuZi(TxtToDbDTO dto) {
        PhoneSale phoneSale = new PhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;phone不能为空;user_type不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setName("");
        phoneSale.setOrgname("juzi");
        phoneSale.setSource("15");
        phoneSale.setOptype("1");
        phoneSale.setStatus(1);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                phoneSaleMapper.insertSelective(phoneSale);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
//                    case "uid":
                    case "测试编号":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("uid不能为空;", "");
                        }
                        phoneSale.setUid(datas.get(i));
                        break;
//                    case "phone":
                    case "md5手机号":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("phone不能为空;", "");
                            Result<String> stringResult = decryptMd5Phone(datas.get(i));
                            phoneSale.setPhoneAes(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSale.setPhone(AESUtil.aesEncrypty(stringResult.getData(), aesKey));
                            } else {
                                phoneMark = Boolean.FALSE;
                            }
                        }
                        break;
//                    case "name":
//                        if (StringUtils.isNotBlank(datas.get(i))) {
//                            error = error.replace("name不能为空;", "");
//                            String s = datas.get(i);
//                            phoneSale.setName(s);
//                            if(DecodeClient.isMd5(s)){
//                                String content = decodeClient.query(s, "name", "md5", "");
//                                if(StringUtils.isBlank(content)){
//                                    error = error.concat("姓名解密失败;");
//                                }else{
//                                    phoneSale.setName(content);
//                                }
//                            }
//                        }
//                        break;
                    case "gender":
                        phoneSale.setGender(datas.get(i));
                        break;
                    case "marketscore":
                        phoneSale.setMarketscore(datas.get(i));
                        break;
                    case "riskscore":
                        phoneSale.setRiskscore(datas.get(i));
                        break;
//                    case "orgname":
//                        if (StringUtils.isNotBlank(datas.get(i))) {
//                            error = error.replace("orgName不能为空;", "");
//                            phoneSale.setOrgname(datas.get(i));
//                        }
//                        break;
                    case "source":
                        phoneSale.setSource(datas.get(i));
                        break;
//                    case "user_type":
                    case "客群类型":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("user_type不能为空;", "");
                            String userType = datas.get(i);
                            switch (userType) {
                                case "注册未认证":
                                    userType = "A";
                                    break;
                                case "存量复购":
                                    userType = "C";
                                    break;
                                default:
                            }
                            phoneSale.setUserType(userType);
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
//                    case "register_time":
                    case "注册时间":
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
//                    case "lent_time":
                    case "下单时间":
                        final String yyyy = datas.get(i);
                        if (StringUtils.isNotEmpty(yyyy)) {
                            final String substring = yyyy.substring(0, 4);
                            phoneSale.setLentTime(substring.concat("-01-01 00:00:00"));
                        }
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
                    case "yx_flag_3d":
                        phoneSale.setYxFlag3d(datas.get(i));
                        break;
                    case "yx_flag_7d":
                        phoneSale.setYxFlag7d(datas.get(i));
                        break;
                    case "yx_flag_15d":
                        phoneSale.setYxFlag15d(datas.get(i));
                        break;
                    case "yx_flag_1m":
                        phoneSale.setYxFlag1m(datas.get(i));
                        break;
                    case "person_flag_house":
                        phoneSale.setPersonFlagHouse(datas.get(i));
                        break;
                    case "person_flag_car":
                        phoneSale.setPersonFlagCar(datas.get(i));
                        break;
                    case "person_flag_insur":
                        phoneSale.setPersonFlagInsur(datas.get(i));
                        break;
                    case "white_list_gw":
                        phoneSale.setWhiteListGw(datas.get(i));
                        break;
                    case "white_list_fp":
                        phoneSale.setWhiteListFp(datas.get(i));
                        break;
                    case "white_list_yc":
                        phoneSale.setWhiteListYc(datas.get(i));
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
            } else if (!phoneMark) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            }
            Date date = new Date();
            phoneSale.setCreateTime(date);
            phoneSale.setUpdateTime(date);
            phoneSaleMapper.insertSelective(phoneSale);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            phoneSaleMapper.insertSelective(phoneSale);
        }
        return new Result().setCode(new Integer("1").equals(phoneSale.getStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
    }
}
