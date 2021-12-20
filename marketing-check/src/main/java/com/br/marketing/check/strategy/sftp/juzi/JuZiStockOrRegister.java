package com.br.marketing.check.strategy.sftp.juzi;

import com.alibaba.fastjson.JSONObject;
import com.br.common.validator.CellUtils;
import com.br.marketing.check.strategy.sftp.SftpStrategy;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.entity.PhoneSale;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * D20211215桔子分期转电销api-3710037
 * <tt>
 * 桔子分期-存量复购
 * <p>
 * 文件命名和规则
 * 上传文件1：桔子分期-存量复购场景yyyymmdd.txt
 * 标识文件1：桔子分期-存量复购场景yyyymmdd.txt.success
 * <p>
 * 文件示例：
 * 测试编号,md5手机号,下单时间,客群类型
 * 0,572527c05beec0898790e239bacd9a5b,2021,存量复购
 * </tt>
 * <br>
 * <tt>
 * 桔子分期-注册未认证桔子分期-注册未认证
 * <p>
 * 文件命名和规则
 * 上传文件2：桔子分期-注册未认证场景yyyymmdd.txt
 * 标识文件2：桔子分期-注册未认证场景yyyymmdd.txt.success
 * <p>
 * 文件示例：
 * 测试编号,md5手机号,注册时间,客群类型
 * 100000,19d0aaa0103400db77dab60be8546ff4,2021,注册未认证
 * </tt>
 *
 * @author Guo Zeqiang
 * @dateTime 2021/12/17 16:08
 */
public class JuZiStockOrRegister implements SftpStrategy {

    private final static String phoneReg = "^([\\+]*[0-9]+)$";

    @Override
    public Result<Object> statisticsHead(String head, HashMap<Integer, String> address,
                                         HashMap<Integer, String> extra) {
        List<String> heads = Splitter.on(",").splitToList(head);
        final Result<String> stringResult = new Result<>();
        stringResult.setCode(ResultCode.FAIL.getValue());
        if (heads.size() <= 0) {
            return stringResult.setMessage("head信息不存在");
        }
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if (StringUtils.isBlank(s)) {
                return stringResult.setMessage("head信息不能有空字段");
            }
            switch (s) {
                case "测试编号":
                    s = "uid";
                    break;
                case "md5手机号":
                    s = "phone";
                    break;
                case "下单时间":
                    s = "lent_time";
                    break;
                case "注册时间":
                    s = "register_time";
                    break;
                case "客群类型":
                    s = "user_type";
                    break;
                default:
                    extra.put(i, s);
                    s = "extend";
            }
            address.put(i, s);
        }
        return stringResult.setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<Object> setDataByPhone(String row
            , PhoneSale phoneSale
            , HashMap<Integer
            , String> address
            , HashMap<Integer
            , String> extSetFields
            , AtomicInteger errorMark
            , Integer line
            , String aesKey
            , DecodeClient decodeClient
    ) {
        try {
            List<String> datas = Splitter.on(",").splitToList(row);
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                errorMark.getAndIncrement();
                return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            }
            JSONObject jo = null;
            String error = "uid不能为空;phone不能为空;user_type不能为空;";
            boolean phoneMark = Boolean.TRUE;
            phoneSale.setName("");
            phoneSale.setOrgname("juzi");
            phoneSale.setSource("15");
            phoneSale.setOptype("1");
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
                    case "uid":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("uid不能为空;", "");
                            phoneSale.setUid(datas.get(i));
                        }
                        break;
                    case "phone":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("phone不能为空;", "");
                            Result<String> stringResult = decryptPhone(datas.get(i), aesKey, decodeClient);
                            phoneSale.setPhoneAes(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSale.setPhone(AESUtil.aesEncrypty(stringResult.getData(), aesKey));
                            } else {
                                phoneMark = Boolean.FALSE;
                            }
                        }
                        break;
                    case "name":
                        phoneSale.setName(datas.get(i));
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
        } catch (Exception ex) {
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            errorMark.getAndIncrement();
            throw ex;
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Result<String> decryptPhone(String phone, String aesKey
            , DecodeClient decodeClient) {
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
        if (isNum) {
            objectResult.setDate(phone);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }

        String s = AESUtil.decrypt(phone, aesKey);
        if (com.br.marketing.common.utils.StringUtils.isNotBlank(s) && CellUtils.isValidateCell(s)) {
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
        if (com.br.marketing.common.utils.StringUtils.isBlank(res)) {
            objectResult.setCode(ResultCode.FAIL.getValue());
        } else {
            if (CellUtils.isValidateCell(res)) {
                objectResult.setCode(ResultCode.SUCCESS.getValue());
                objectResult.setDate(res);
            } else {
                objectResult.setCode(ResultCode.FAIL.getValue());
            }
        }
        return objectResult;
    }
}
