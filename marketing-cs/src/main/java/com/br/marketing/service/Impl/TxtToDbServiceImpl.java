package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.validator.CellUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.commonmethod.YiXinUtils;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.IDxService;
import com.br.marketing.service.ITxtToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TxtToDbServiceImpl implements ITxtToDbService {

    @Resource
    TwosevenFileMapper twosevenFileMapper;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Resource
    PhoneSaleTransferMapper phoneSaleTransferMapper;

    @Resource
    PhoneSaleIbuMapper phoneSaleIbuMapper;

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingSyncUserMapper syncUserMapper;

    @Resource
    CallRecordMapper callRecordMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    DecodeGrpcClient decodeClient;

    @Autowired
    IDxService iDxService;

    @Resource
    CsosPhoneSaleMapper csosPhoneSaleMapper;

    @Resource
    UpdatePhoneSaleMapper updatePhoneSaleMapper;

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
                return new Result().setCode(ResultCode.FAIL.getValue());
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
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            twosevenFile.setStatus(2);
            twosevenFile.setDataMessage(String.format("行号：%d;报错信息：%s"
                    , line
                    , ex.getMessage().length() >= 450
                            ? ex.getMessage().substring(0, 449)
                            : ex.getMessage()));
            twosevenFileMapper.insertSelective(twosevenFile);
        }
        return new Result().setCode(new Integer("1").equals(twosevenFile.getStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
    }

    @Override
    public Result toDbByCommon(TxtToDbDTO dto) {
        try {
            HashMap<Integer, String> address = dto.getAddress();
            HashMap<Integer, String> extSetField = dto.getExtSetField();

            String dbName = dto.getDbName().replace("apicode", dto.getApiCode());
            HashSet<String> fieldAll = dto.getFieldAll();
            HashMap<String, String> fieldAllHm = dto.getFieldAllHm();
            HashSet<String> fieldMust = dto.getFieldMust();

            String sqlTemp = "insert into %s (%s) values %s";
            StringBuilder errorValues = new StringBuilder();
            StringBuilder insertFields = new StringBuilder();
            StringBuilder insertValues = new StringBuilder();
            Integer errorNum = 0;
            Integer successNum = 0;
            for (int i = 0; i < address.size(); i++) {
                String s = address.get(i);
                if (!"extend".equals(s) && StringUtils.isNotBlank(fieldAllHm.get(s))) {
                    insertFields.append(fieldAllHm.get(s)).append(",");
                }
            }
            insertFields.append("extend,status,data_message,create_time,create_date,local_id,api_code").append(",");
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            for (Map.Entry<Integer, String> data : dto.getDatas().entrySet()) {
                List<String> datas = Splitter.on(",").splitToList(data.getValue());
                Integer line = data.getKey();
                String error = dto.getErrorMsg();
                //region 列数不一致,直接赋值跳出
                if (datas.size() != address.size()) {
                    String value = String.format("('2','%s','%s','%s',%d,'%s')"
                            , String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致")
                            , time, day,dto.getLocalId(),dto.getApiCode());
                    errorValues.append(value).append(",");
                    errorNum++;
                    continue;
                }
                //endregion

                //region 字段处理
                JSONObject jo = null;
                StringBuilder valueSb = new StringBuilder();
                for (int i = 0; i < datas.size(); i++) {
                    String field = address.get(i);
                    String value = datas.get(i);
                    //上传文件字段不在配置的（b_file_db_config）字段中，忽略掉
                    if (!fieldAll.contains(field)) {
                        continue;
                    }
                    if (fieldMust.contains(field)) {
                        if (StringUtils.isNotBlank(value)) {
                            error = error.replace(String.format("%s不能为空;", field), "");
                        }
                    }
                    valueSb.append(StringUtils.isBlank(value) ? "''" : String.format("'%s'", value)).append(",");
                    if (field.equals("extend")) {
                        String s = extSetField.get(i);
                        if (StringUtils.isNotBlank(s)) {
                            if (jo == null) {
                                jo = new JSONObject();
                            }
                            jo.put(s, datas.get(i));
                        }
                    }
                }

                if (jo != null) {
                    valueSb.append(String.format("'%s'",jo.toJSONString())).append(",");
                } else {
                    valueSb.append("'',");
                }

                if (StringUtils.isNotEmpty(error)) {
                    errorNum++;
                    valueSb.append("'2'").append(",");
                    valueSb.append(String.format("'行号：%d;报错信息：%s'", line, error)).append(",");
                } else {
                    successNum++;
                    valueSb.append("'1'").append(",");
                    valueSb.append("''").append(",");
                }

                //endregion

                //拼接 插入的数据
                if (StringUtils.isNotBlank(valueSb.toString())) {
                    valueSb.append(String.format("'%s','%s',%d,'%s'", time, day,dto.getLocalId(),dto.getApiCode())).append(",");
                    insertValues.append(String.format("(%s),", org.apache.commons.lang3.StringUtils.removeEnd(valueSb.toString(), ",")));
                }
            }

            //插入列和表头不一致的错误数据
            if (StringUtils.isNotBlank(errorValues.toString())) {
                String sql = String.format(sqlTemp, dbName, "status,data_message,create_time,create_date,local_id,api_code", org.apache.commons.lang3.StringUtils.removeEnd(errorValues.toString(), ","));
                localFileMapper.insertFileData(sql);
            }

            //插入成功和变天字段缺少值得数据
            if (StringUtils.isNotBlank(insertValues.toString())) {
                String sql = String.format(sqlTemp, dbName, org.apache.commons.lang3.StringUtils.removeEnd(insertFields.toString(), ","), org.apache.commons.lang3.StringUtils.removeEnd(insertValues.toString(), ","));
                localFileMapper.insertFileData(sql);
            }

            JSONObject resMsg = new JSONObject();
            resMsg.put("successNum", successNum);
            resMsg.put("errorNum", errorNum);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(JSON.toJSONString(resMsg));
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
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
                            phoneSale.setNameAes(s);
                            phoneSale.setName(decryptName(s));
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

    @Override
    public Result phoneTodbByTransfer(TxtToDbDTO dto) {
        PhoneSaleTransfer phoneSaleTransfer = new PhoneSaleTransfer();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
//        String error = "uid不能为空;phone不能为空;orgName不能为空;user_type不能为空;name不能为空;";
        phoneSaleTransfer.setApiCode(dto.getApiCode());
        phoneSaleTransfer.setLocalId(dto.getLocalId().toString());
        phoneSaleTransfer.setmStatus(1);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSaleTransfer.setmStatus(2);
                phoneSaleTransfer.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                phoneSaleTransferMapper.insertSelective(phoneSaleTransfer);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            Integer nullMark = 0;
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
                    case "uid":
//                        if (StringUtils.isNotBlank(datas.get(i))) {
//                            error = error.replace("uid不能为空;", "");
//                        }
                        phoneSaleTransfer.setUid(datas.get(i));
                        break;
                    case "phone":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            Result<String> stringResult = decryptPhone(datas.get(i));
                            phoneSaleTransfer.setPhoneAes(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSaleTransfer.setPhone(AESUtil.aesEncrypty(stringResult.getData(), aesKey));
                            } else {
                                phoneMark = Boolean.FALSE;
                            }
                        }
                        break;
                    case "orgName":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            phoneSaleTransfer.setOrgName(datas.get(i));
                        }
                        break;
                    case "source":
                        phoneSaleTransfer.setSource(datas.get(i));
                        break;
                    case "userType":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            phoneSaleTransfer.setUserType(datas.get(i));
                        }
                        break;
                    case "type":
                        phoneSaleTransfer.setType(datas.get(i));
                        break;
                    case "ifRegister":
                        phoneSaleTransfer.setIfRegister(datas.get(i));
                        break;
                    case "registerTime":
                        phoneSaleTransfer.setRegisterTime(datas.get(i));
                        break;
                    case "ifLogin":
                        phoneSaleTransfer.setIfLogin(datas.get(i));
                        break;
                    case "loginTime":
                        phoneSaleTransfer.setLoginTime(datas.get(i));
                        break;
                    case "ifApply":
                        phoneSaleTransfer.setIfApply(datas.get(i));
                        break;
                    case "applyDt":
                        phoneSaleTransfer.setApplyDt(datas.get(i));
                        break;
                    case "applyTime":
                        phoneSaleTransfer.setApplyTime(datas.get(i));
                        break;
                    case "applyResult":
                        phoneSaleTransfer.setApplyResult(datas.get(i));
                        break;
                    case "refuseTime":
                        phoneSaleTransfer.setRefuseTime(datas.get(i));
                        break;
                    case "auditTime":
                        phoneSaleTransfer.setAuditTime(datas.get(i));
                        break;
                    case "auditAmount":
                        phoneSaleTransfer.setAuditAmount(datas.get(i));
                        break;
                    case "ifLent":
                        phoneSaleTransfer.setIfLent(datas.get(i));
                        break;
                    case "lentTime":
                        phoneSaleTransfer.setLentTime(datas.get(i));
                        break;
                    case "lentAmount":
                        phoneSaleTransfer.setLentAmount(datas.get(i));
                        break;
                    case "unlentAmount":
                        phoneSaleTransfer.setUnlentAmount(datas.get(i));
                        break;
                    case "ifSettle":
                        phoneSaleTransfer.setIfSettle(datas.get(i));
                        break;
                    case "settleTime":
                        phoneSaleTransfer.setSettleTime(datas.get(i));
                        break;
                    case "activity":
                        phoneSaleTransfer.setActivity(datas.get(i));
                        break;
                    case "caseStatus":
                        phoneSaleTransfer.setCaseStatus(datas.get(i));
                        break;
                    case "caseEffective":
                        phoneSaleTransfer.setCaseEffective(datas.get(i));
                        break;
                    case "ifTransform":
                        phoneSaleTransfer.setIfTransform(datas.get(i));
                        break;
                    case "transformTime":
                        phoneSaleTransfer.setTransformTime(datas.get(i));
                        break;
                    case "status":
                        phoneSaleTransfer.setStatus(datas.get(i));
                        break;
                    case "insertTime":
                        phoneSaleTransfer.setInsertTime(datas.get(i));
                        break;
                    case "transformStatus":
                        phoneSaleTransfer.setTransformStatus(datas.get(i));
                        break;
                    default:
                        nullMark++;
                        break;
                }
            }

            if (nullMark.equals(datas.size())) {
                phoneSaleTransfer.setmStatus(2);
                phoneSaleTransfer.setDataMessage(String.format("行号：%d;报错信息：%s", line, "该行数据不包含有效字段数据"));
            }
//            if (!StringUtils.isEmpty(error)) {
//                phoneSaleTransfer.setmStatus(2);
//                phoneSaleTransfer.setDataMessage(String.format("行号：%d;报错信息：%s", line, error));
//            } else
            if (!phoneMark) {
                phoneSaleTransfer.setmStatus(2);
                phoneSaleTransfer.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            }
            Date date = new Date();
            phoneSaleTransfer.setCreateTime(date);
            phoneSaleTransfer.setUpdateTime(date);
            phoneSaleTransferMapper.insertSelective(phoneSaleTransfer);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            phoneSaleTransfer.setmStatus(2);
            phoneSaleTransfer.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            phoneSaleTransferMapper.insertSelective(phoneSaleTransfer);
        }
        return new Result().setCode(new Integer("1").equals(phoneSaleTransfer.getmStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
    }

    @Override
    public Result phoneTodbByIbu(TxtToDbDTO dto) {
        PhoneSaleIbu phoneSaleIbu = new PhoneSaleIbu();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;userType不能为空;userCode不能为空;userName不能为空;phone不能为空;source不能为空;";
        phoneSaleIbu.setApiCode(dto.getApiCode());
        phoneSaleIbu.setLocalId(dto.getLocalId().toString());
        phoneSaleIbu.setmStatus(1);
        Date date = new Date();
        phoneSaleIbu.setCreateTime(date);
        phoneSaleIbu.setUpdateTime(date);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSaleIbu.setmStatus(2);
                phoneSaleIbu.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                phoneSaleIbuMapper.insertSelective(phoneSaleIbu);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            Integer nullMark = 0;
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                switch (sureaddress) {
                    case "uid":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("uid不能为空;", "");
                            phoneSaleIbu.setUid(datas.get(i));
                        }
                        break;
                    case "userType":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("userType不能为空;", "");
                            phoneSaleIbu.setUserType(datas.get(i));
                        }
                        break;
                    case "userCode":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("userCode不能为空;", "");
                            phoneSaleIbu.setUserCode(datas.get(i));
                        }
                        break;
                    case "source":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("source不能为空;", "");
                            phoneSaleIbu.setSource(datas.get(i));
                        }
                        break;

                    case "phone":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("phone不能为空;", "");
                            Result<String> stringResult = decryptPhone(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSaleIbu.setPhone(BrCipherMaker.getInstance().encode(stringResult.getData()));
                            } else {
                                phoneMark = Boolean.FALSE;
                            }
                        }
                        break;
                    case "userName":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("userName不能为空;", "");
                            phoneSaleIbu.setUserName(BrCipherMaker.getInstance().encode(decryptName(datas.get(i))));
                        }
                        break;
                    case "planId":
                        phoneSaleIbu.setPlanId(datas.get(i));
                        break;
                    case "pid":
                        phoneSaleIbu.setPid(datas.get(i));
                        break;
                    case "purpose":
                        phoneSaleIbu.setPurpose(datas.get(i));
                        break;
                    case "gender":
                        phoneSaleIbu.setGender(datas.get(i));
                        break;
                    case "signInTimeStr":
                        phoneSaleIbu.setSignInTimeStr(datas.get(i));
                        break;
                    case "clickProductName":
                        phoneSaleIbu.setClickProductName(datas.get(i));
                        break;
                    case "clickTimeStr":
                        phoneSaleIbu.setClickTimeStr(datas.get(i));
                        break;
                    case "recommendList":
                        phoneSaleIbu.setRecommendList(datas.get(i));
                        break;
                    case "recommendH5List":
                        phoneSaleIbu.setRecommendH5List(datas.get(i));
                        break;
                    case "basicInfo":
                        phoneSaleIbu.setBasicInfo(datas.get(i));
                        break;
                    case "realName":
                        phoneSaleIbu.setRealName(datas.get(i));
                        break;
                    case "supplement":
                        phoneSaleIbu.setSupplement(datas.get(i));
                        break;
                    case "contract":
                        phoneSaleIbu.setContract(datas.get(i));
                        break;
                    case "operator":
                        phoneSaleIbu.setOperator(datas.get(i));
                        break;
                    case "loanProductName":
                        phoneSaleIbu.setLoanProductName(datas.get(i));
                        break;
                    case "loanTimeStr":
                        phoneSaleIbu.setLoanTimeStr(datas.get(i));
                        break;
                    case "createTimeStr":
                        phoneSaleIbu.setCreateTimeStr(datas.get(i));
                        break;
                    case "diffAmount":
                        phoneSaleIbu.setDiffAmount(datas.get(i));
                        break;
                    case "faceRecognition":
                        phoneSaleIbu.setFaceRecognition(datas.get(i));
                        break;
                    case "firstApproveResult":
                        phoneSaleIbu.setFirstApproveResult(datas.get(i));
                        break;
                    case "firstApproveTimeStr":
                        phoneSaleIbu.setFirstApproveTimeStr(datas.get(i));
                        break;
                    case "hasBindCard":
                        phoneSaleIbu.setHasBindCard(datas.get(i));
                        break;
                    case "hasEverBorrow":
                        phoneSaleIbu.setHasEverBorrow(datas.get(i));
                        break;
                    case "hasWithdraw":
                        phoneSaleIbu.setHasWithdraw(datas.get(i));
                        break;
                    case "insteadCommitFlag":
                        phoneSaleIbu.setInsteadCommitFlag(datas.get(i));
                        break;
                    case "insteadCommitPname":
                        phoneSaleIbu.setInsteadCommitPname(datas.get(i));
                        break;
                    case "isTimely":
                        phoneSaleIbu.setIsTimely(datas.get(i));
                        break;
                    case "loanFailedTimeStr":
                        phoneSaleIbu.setLoanFailedTimeStr(datas.get(i));
                        break;
                    case "loanSuccessTimeStr":
                        phoneSaleIbu.setLoanSuccessTimeStr(datas.get(i));
                        break;
                    case "loanWillingness":
                        phoneSaleIbu.setLoanWillingness(datas.get(i));
                        break;
                    case "aCardScore":
                        phoneSaleIbu.setaCardScore(datas.get(i));
                        break;
                    case "bucketName":
                        phoneSaleIbu.setBucketName(datas.get(i));
                        break;
                    case "overdueDays":
                        phoneSaleIbu.setOverdueDays(datas.get(i));
                        break;
                    case "prepayAmount":
                        phoneSaleIbu.setPrepayAmount(datas.get(i));
                        break;
                    case "prepayPname":
                        phoneSaleIbu.setPrepayPname(datas.get(i));
                        break;
                    case "prepayTimeStr":
                        phoneSaleIbu.setPrepayTimeStr(datas.get(i));
                        break;
                    case "repayPname":
                        phoneSaleIbu.setRepayPname(datas.get(i));
                        break;
                    case "repayAmount":
                        phoneSaleIbu.setRepayAmount(datas.get(i));
                        break;
                    case "repayTimeStr":
                        phoneSaleIbu.setRepayTimeStr(datas.get(i));
                        break;
                    case "secondApproveResult":
                        phoneSaleIbu.setSecondApproveResult(datas.get(i));
                        break;
                    case "secondApproveTimeStr":
                        phoneSaleIbu.setSecondApproveTimeStr(datas.get(i));
                        break;
                    case "applyAmount":
                        phoneSaleIbu.setApplyAmount(datas.get(i));
                        break;
                    case "approveAmount":
                        phoneSaleIbu.setApproveAmount(datas.get(i));
                        break;
                    case "prodType":
                        phoneSaleIbu.setProdType(datas.get(i));
                        break;
                    case "score":
                        phoneSaleIbu.setScore(datas.get(i));
                        break;
                    case "callTimes":
                        phoneSaleIbu.setCallTimes(datas.get(i));
                        break;
                    case "callAccessScore":
                        phoneSaleIbu.setCallAccessScore(datas.get(i));
                        break;
                    case "remark":
                        phoneSaleIbu.setRemark(datas.get(i));
                        break;
                    case "grade":
                        phoneSaleIbu.setGrade(datas.get(i));
                        break;
                    case "totalAmount":
                        phoneSaleIbu.setTotalAmount(datas.get(i));
                        break;
                    case "surplusAmount":
                        phoneSaleIbu.setSurplusAmount(datas.get(i));
                        break;
                    case "pchannel":
                        phoneSaleIbu.setPchannel(datas.get(i));
                        break;
                    case "channelName":
                        phoneSaleIbu.setChannelName(datas.get(i));
                        break;
                    case "marketPurpose":
                        phoneSaleIbu.setMarketPurpose(datas.get(i));
                        break;
                    case "riskControlLabel":
                        phoneSaleIbu.setRiskControlLabel(datas.get(i));
                        break;
                    case "firstLoginTimeStr":
                        phoneSaleIbu.setFirstLoginTimeStr(datas.get(i));
                        break;
                    case "goalsApp":
                        phoneSaleIbu.setGoalsApp(datas.get(i));
                        break;
                    case "flowSideName":
                        phoneSaleIbu.setFlowSideName(datas.get(i));
                        break;
                    case "flowSidePath":
                        phoneSaleIbu.setFlowSidePath(datas.get(i));
                        break;
                    case "cusTag":
                        phoneSaleIbu.setCusTag(datas.get(i));
                        break;
                    case "abgroupPushOffsetStr":
                        phoneSaleIbu.setAbgroupPushOffsetStr(datas.get(i));
                        break;
                    case "extra1":
                        phoneSaleIbu.setExtra1(datas.get(i));
                        break;
                    case "extra2":
                        phoneSaleIbu.setExtra2(datas.get(i));
                        break;
                    case "extra3":
                        phoneSaleIbu.setExtra3(datas.get(i));
                        break;
                    case "creditTimeStr":
                        phoneSaleIbu.setCreditTimeStr(datas.get(i));
                        break;
                    case "creditChannel":
                        phoneSaleIbu.setCreditChannel(datas.get(i));
                        break;
                    case "amountStatus":
                        phoneSaleIbu.setAmountStatus(datas.get(i));
                        break;
                    case "connectTimes":
                        phoneSaleIbu.setConnectTimes(datas.get(i));
                        break;
                    case "zyApplyFlag":
                        phoneSaleIbu.setZyApplyFlag(datas.get(i));
                        break;
                    case "zyApplySuccessFlag":
                        phoneSaleIbu.setZyApplySuccessFlag(datas.get(i));
                        break;
                    case "zyAmountStatus":
                        phoneSaleIbu.setZyAmountStatus(datas.get(i));
                        break;
                    case "zyTotalUsableAmount":
                        phoneSaleIbu.setZyTotalUsableAmount(datas.get(i));
                        break;
                    case "isIdnumber":
                        phoneSaleIbu.setIsIdnumber(datas.get(i));
                        break;
                    case "isTaobao":
                        phoneSaleIbu.setIsTaobao(datas.get(i));
                        break;
                    case "isNuclearapproval":
                        phoneSaleIbu.setIsNuclearapproval(datas.get(i));
                        break;
                    case "callaccessscore":
                        phoneSaleIbu.setCallaccessscore(datas.get(i));
                        break;
                    case "marketingScore":
                        phoneSaleIbu.setMarketingScore(datas.get(i));
                        break;
                    case "noWithdrawOrders":
                        phoneSaleIbu.setNoWithdrawOrders(datas.get(i));
                        break;
                    case "planData":
                        phoneSaleIbu.setPlanData(datas.get(i));
                        break;
                    case "priorityScore":
                        phoneSaleIbu.setPriorityScore(datas.get(i));
                        break;
                    case "callType":
                        phoneSaleIbu.setCallType(datas.get(i));
                        break;
                    case "extend":
                        String s = extSetFields.get(i);
                        if (StringUtils.isNotBlank(s)) {
                            if (jo == null) {
                                jo = new JSONObject();
                            }
                            if (!"extend".equals(s)) {
                                jo.put(s, datas.get(i));
                            }
                        }
                        break;
                    default:
                        nullMark++;
                        break;
                }
                if (jo != null) {
                    phoneSaleIbu.setReserveField1(jo.toJSONString());
                }
            }

            if (nullMark.equals(datas.size())) {
                phoneSaleIbu.setmStatus(2);
                phoneSaleIbu.setDataMessage(String.format("行号：%d;报错信息：%s", line, "该行数据不包含有效字段数据"));
            }
            if (!StringUtils.isEmpty(error)) {
                phoneSaleIbu.setmStatus(2);
                phoneSaleIbu.setDataMessage(String.format("行号：%d;报错信息：%s", line, error));
            }
            if (!phoneMark) {
                phoneSaleIbu.setmStatus(2);
                phoneSaleIbu.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            }
            phoneSaleIbuMapper.insertSelective(phoneSaleIbu);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            phoneSaleIbu.setmStatus(2);
            phoneSaleIbu.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            phoneSaleIbuMapper.insertSelective(phoneSaleIbu);
        }
        return new Result().setCode(new Integer("1").equals(phoneSaleIbu.getmStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
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
                            phoneSale.setNameAes(s);
                            if (DecodeGrpcClient.isMd5(s)) {
                                String content = RpcClientProxy.decode(s, "name", "md5", "");
                                phoneSale.setName(StringUtils.isNotBlank(content) ? content : "1");
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
                    default:
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

    Result<String> decryptPhone(String phone) {

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
        if (StringUtils.isNotBlank(s) && CellUtils.isValidateCell(s)) {
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
        if (StringUtils.isBlank(res)) {
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

    Result<String> decryptMd5Phone(String phone) {
        Result<String> objectResult = new Result<>();
        boolean isNum = Pattern.matches(phoneReg, phone);
        if (isNum) {
            objectResult.setDate(phone);
            objectResult.setCode(ResultCode.SUCCESS.getValue());
            return objectResult;
        }
        String res = RpcClientProxy.decode(phone, "cell", "md5", "");

        if (StringUtils.isBlank(res)) {
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

    String decryptName(String name) {
        UserValidator userValidator = new UserValidator(2);
        if (userValidator.validateName(name)) {
            return name;
        }
        String res = "";
        if (DecodeGrpcClient.isMd5(name)) {
            //cell md5
            res = RpcClientProxy.decode(name, "name", "md5", "");
        } else if (name.length() == 64) {
            //cell sha256
            res = RpcClientProxy.decode(name, "name", "sha", "");
        }
        if (!StringUtils.isEmpty(res)) {
            return res;
        }
        if(StringUtils.isNotBlank(name)){
            return name;
        }
        return "1";
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

    @Override
    public Result phoneTodbByYiXin(TxtToDbDTO dto) {
        PhoneSale phoneSale = new PhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;type不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setOrgname("yixin");
        phoneSale.setUserType("A");
        phoneSale.setSource("16");
        phoneSale.setStatus(1);
        try {
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
                            Result<String> stringResult = decryptMd5Phone(datas.get(i));
                            phoneSale.setPhoneAes(datas.get(i));
                            if (ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                                phoneSale.setPhone(AESUtil.aesEncrypty(stringResult.getData(), aesKey));
                            }
                        }
                        break;
                    case "name":
                        if (StringUtils.isNotBlank(datas.get(i))) {
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
                            phoneSale.setOrgname(datas.get(i));
                        }
                        break;
                    case "source":
                        phoneSale.setSource(datas.get(i));
                        break;
                    case "user_type":
                        if (StringUtils.isNotBlank(datas.get(i))) {
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
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("type不能为空;", "");
                        }
                        phoneSale.setType(YiXinUtils.getDxType(datas.get(i)));
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

    @Override
    public Result<Integer> phoneTodbByYiXinAfterAction(LocalFile file) {
        if (file == null || file.getId() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        AtomicInteger errorNum = new AtomicInteger();
        String apiCode = file.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        String cid = tableCreateService.getCId(apiCode);
        Boolean actionMark = Boolean.TRUE;
        String _7start = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String _7end = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long minId = null;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        while (actionMark) {
            Result<List<PhoneSale>> dataRes = getPhoneSaleDataByfileWithPage(file.getId(), minId);
            if (!ResultCode.SUCCESS.getValue().equals(dataRes.getCode())) {
                actionMark = Boolean.FALSE;
                continue;
            }
            List<PhoneSale> datas = dataRes.getData();
            minId = datas.get(datas.size() - 1).getId() + 1;
            List<List<PhoneSale>> partition = Lists.partition(datas, 500);
            for (List<PhoneSale> phoneSales : partition) {
                threadPool.submit(() -> {
                    try {
                        List<String> custNums = phoneSales.stream().map(t -> t.getUid()).distinct().collect(Collectors.toList());
                        //根据custNum获取当天的数据情况
                        Map<String, List<MarketingTransferSyncUser>> nowTimetransferUserMap = transferSyncUserMapper
                                .getTransferOrderInsertTimeByCustNum(tcId, custNums, null)
                                .stream().collect(Collectors.groupingBy(MarketingTransferSyncUser::getCustNum));
                        //根据custNum获取最新转化数据
                        List<MarketingTransferSyncUser> _lastTransferSyncUsers = transferSyncUserMapper
                                .getTransferOrderRequestTimeByCustNum(tcId, custNums, null);
                        Map<String, List<MarketingTransferSyncUser>> lastTransferUserMap = _lastTransferSyncUsers
                                .stream().collect(Collectors.groupingBy(MarketingTransferSyncUser::getCustNum));

                        //根据custNum获取最新上传数据
                        Map<String, List<MarketingSyncUser>> syncUser = syncUserMapper.getSyncUserLastByCustNums(apiCode, custNums)
                                .stream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum));
                        //获取caseEffective=0的案件
                        Set<String> caseEffectiveCust=transferSyncUserMapper.getByInCustAndCaseEffective(tcId,apiCode, new HashSet<>(custNums))
                                .stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                        //获取最新通话记录
                        Map<String, List<CallRecord>> callrecord = callRecordMapper.getLastCallRecordByCustNum(custNums, cid)
                                .stream().collect(Collectors.groupingBy(CallRecord::getCaseNum));

                        //获取7天实时数据
                        Set<String> custNumByPhoneDx = iDxService.getCustNumByPhoneDx(custNums, apiCode, _7start, _7end, "1");
                        //获取黑名单
                        HashMap<String, String> black = new HashMap<>();
                        Result<Map<String, String>> blackByTransfer = iDxService.getBlackByDXfile(phoneSales, apiCode);
                        if (ResultCode.SUCCESS.getValue().equals(blackByTransfer.getCode())) {
                            black.putAll(blackByTransfer.getData());
                        }

                        for (PhoneSale phoneSale : phoneSales) {
                            String uid = phoneSale.getUid();
                            PhoneSale computeSale = new PhoneSale();
                            computeSale.setId(phoneSale.getId());
                            List<MarketingTransferSyncUser> marketingTransferSyncUsers = nowTimetransferUserMap.get(uid);
                            if (marketingTransferSyncUsers != null
                                    && marketingTransferSyncUsers.size() > 0
                                    && marketingTransferSyncUsers.stream()
                                    .anyMatch(t -> marketingCommonConfig.getYixinNoRealTimeType().contains(t.getType()))) {
                                computeSale.setDataMessage("命中当天非实时数据");
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            if (custNumByPhoneDx != null && custNumByPhoneDx.contains(uid)) {
                                computeSale.setDataMessage("命中7天内实时数据");
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            if (caseEffectiveCust != null && caseEffectiveCust.contains(uid)) {
                                computeSale.setDataMessage("命中caseEffective等于0的数据");
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            List<MarketingSyncUser> marketingSyncUsers = syncUser.get(uid);
                            if (marketingSyncUsers == null || marketingSyncUsers.size() <= 0) {
                                computeSale.setDataMessage("没有命中原始上传数据");
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            MarketingSyncUser _marketingSyncUser = marketingSyncUsers.get(0);
                            String cell = BrCipherMaker.getInstance().decode(_marketingSyncUser.getCell());
                            if (StringUtils.isBlank(cell)) {
                                computeSale.setDataMessage(String.format("原始上传数据手机号解密失败 sync_id:%d", _marketingSyncUser.getId()));
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            MarketingTransferSyncUser _transferSyncUser = new MarketingTransferSyncUser();
                            List<MarketingTransferSyncUser> transferSyncUsers = lastTransferUserMap.get(uid);
                            if (transferSyncUsers != null && transferSyncUsers.size() > 0) {
                                _transferSyncUser = transferSyncUsers.get(0);
                            }
                            if (black.containsKey(phoneSale.getId().toString())
                                    && "Y".equals(black.get(phoneSale.getId().toString()))) {
                                computeSale.setDataMessage(String.format("该数据属于黑名单 phone_sale_id:%d", phoneSale.getId()));
                                computeSale.setStatus(2);
                                phoneSaleMapper.updateByPrimaryKeySelective(computeSale);
                                errorNum.getAndIncrement();
                                continue;
                            }
                            updatePhoneSale(_marketingSyncUser,_transferSyncUser,phoneSale,computeSale,callrecord,cell);

                        }
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });
            }
        }
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(errorNum.get());
    }


    private void updatePhoneSale(MarketingSyncUser _marketingSyncUser, MarketingTransferSyncUser _transferSyncUser, PhoneSale phoneSale,
                                 PhoneSale computeSale,Map<String, List<CallRecord>> callrecord,String cell){
        String name = "";
        String gender = null;
        String activity = null;
        JSONObject extend = new JSONObject();
        if (StringUtils.isNotBlank(_marketingSyncUser.getName())) {
            name = BrCipherMaker.getInstance().decode(_marketingSyncUser.getName());
        }
        if (StringUtils.isNotBlank(_marketingSyncUser.getReserveField1())) {
            JSONObject jsonObject = JSON.parseObject(_marketingSyncUser.getReserveField1());
            gender = YiXinUtils.getGender(jsonObject.getString("gender"));
        }
        if (StringUtils.isNotBlank(_transferSyncUser.getReserveField1())) {
            JSONObject jsonObject = JSON.parseObject(_transferSyncUser.getReserveField1());
            activity = YiXinUtils.getActivity(jsonObject.getString("rate"));
            String raiseLimiSuccess = jsonObject.getString("raiseLimiSuccess");
            String raiseLimiType = jsonObject.getString("raiseLimiType");
            String availableAmount = jsonObject.getString("availableAmount");
            String recommendType = jsonObject.getString("recommendType");
            if (StringUtils.isNotBlank(raiseLimiSuccess)) {
                extend.put("raiseLimiSuccess", raiseLimiSuccess);
            }
            if (StringUtils.isNotBlank(raiseLimiType)) {
                extend.put("raiseLimiType", raiseLimiType);
            }
            if (StringUtils.isNotBlank(availableAmount)) {
                extend.put("availableAmount", availableAmount);
            }
            if (StringUtils.isNotBlank(recommendType)) {
                extend.put("recommendType", recommendType);
            }
        }
        CallRecord _callRecord = new CallRecord();
        List<CallRecord> callRecords = callrecord.get(phoneSale.getUid());
        if (callRecords != null && callRecords.size() > 0) {
            _callRecord = callRecords.get(0);
        }
        computeSale.setPhone(AESUtil.aesEncrypty(cell, aesKey));
        computeSale.setPhoneAes(_marketingSyncUser.getCell());
        computeSale.setName(name);
        computeSale.setNameAes(_marketingSyncUser.getName());
        computeSale.setGender(gender);
        computeSale.setLevel(YiXinUtils.getLevel(_callRecord.getIntentionGrade()));
        computeSale.setAuditAmount(_transferSyncUser.getAuditAmount());
        computeSale.setApplyTime(StringUtils.isBlank(_transferSyncUser.getApplyDt())
                ? _transferSyncUser.getApplyDt()
                : _transferSyncUser.getApplyDt().replaceAll(":\\d{3}", ""));
        computeSale.setActivity(activity);
        computeSale.setPrioritysymbol(YiXinUtils.getPrioritySymbol(phoneSale.getType()));
        computeSale.setExtend(JSON.toJSONString(extend));
        phoneSaleMapper.updateByPrimaryKeySelective(computeSale);

    }

    private Result<List<PhoneSale>> getPhoneSaleDataByfileWithPage(Long fileId, Long dataId) {
        PhoneSaleExample saleExample = new PhoneSaleExample();
        saleExample.setOrderByClause(" id asc limit 5000");
        PhoneSaleExample.Criteria criteria = saleExample.createCriteria()
                .andLocalIdEqualTo(fileId.toString())
                .andStatusEqualTo(1);
        if (dataId != null) {
            criteria.andIdGreaterThanOrEqualTo(dataId);
        }
        List<PhoneSale> phoneSales = phoneSaleMapper.selectByExample(saleExample);
        if (phoneSales.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(phoneSales);
    }


    @Override
    public Result csosPhoneTodb(TxtToDbDTO dto) {
        CsosPhoneSale phoneSale = new CsosPhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;phone不能为空;orgName不能为空;user_type不能为空;source不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setStatus(1);
        try {
            Boolean phoneMark = Boolean.TRUE;
            if (datas.size() != address.size()) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"));
                csosPhoneSaleMapper.insertSelective(phoneSale);
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
                            String name = datas.get(i);
                            phoneSale.setNameAes(name);
                            phoneSale.setName(decryptCsosName(name));
                        break;
                    case "gender":
                        phoneSale.setGender(datas.get(i));
                        break;
                    case "orgname":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("orgName不能为空;", "");
                            phoneSale.setOrgname(datas.get(i));
                        }
                        break;
                    case "source":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("source不能为空;", "");
                            phoneSale.setSource(datas.get(i));
                        }
                        break;
                    case "user_type":
                        if (StringUtils.isNotBlank(datas.get(i))) {
                            error = error.replace("user_type不能为空;", "");
                            phoneSale.setUserType(datas.get(i));
                        }
                        break;
                    case "household_registration":
                        phoneSale.setHouseholdRegistration(datas.get(i));
                        break;
                    case "age":
                        phoneSale.setAge(datas.get(i));
                        break;
                    case "account_open_date":
                        phoneSale.setAccountOpenDate(datas.get(i));
                        break;
                    case "account_channel":
                        phoneSale.setAccountChannel(datas.get(i));
                        break;
                    case "last_login_app":
                        phoneSale.setLastLoginApp(datas.get(i));
                        break;
                    case "invest_level":
                        phoneSale.setInvestLevel(datas.get(i));
                        break;
                    case "purchase_intent_score":
                        phoneSale.setPurchaseIntentScore(datas.get(i));
                        break;
                    case "marketing_acceptance_level":
                        phoneSale.setMarketingAcceptanceLevel(datas.get(i));
                        break;
                    case "multi_position_count":
                        phoneSale.setMultiPositionCount(datas.get(i));
                        break;
                    case "available_balance":
                        phoneSale.setAvailableBalance(datas.get(i));
                        break;
                    case "account_assets":
                        phoneSale.setAccountAssets(datas.get(i));
                        break;
                    case "deposit_position":
                        phoneSale.setDepositPosition(datas.get(i));
                        break;
                    case "wealth_position":
                        phoneSale.setWealthPosition(datas.get(i));
                        break;
                    case "fund_position":
                        phoneSale.setFundPosition(datas.get(i));
                        break;
                    case "insurance_position":
                        phoneSale.setInsurancePosition(datas.get(i));
                        break;
                    case "buy_and_redeem_record":
                        phoneSale.setBuyAndRedeemRecord(datas.get(i));
                        break;
                    case "has_loan":
                        phoneSale.setHasLoan(datas.get(i));
                        break;
                    case "has_wechat":
                        phoneSale.setHasWechat(datas.get(i));
                        break;
                    case "risk_level":
                        phoneSale.setRiskLevel(datas.get(i));
                        break;
                    case "third_party_custody":
                        phoneSale.setThirdPartyCustody(datas.get(i));
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
            csosPhoneSaleMapper.insertSelective(phoneSale);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, "手机号解密失败"));
            csosPhoneSaleMapper.insertSelective(phoneSale);
        }
        return new Result().setCode(new Integer("1").equals(phoneSale.getStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
    }



    public String decryptCsosName(String name) {
        if(StringUtils.isEmpty(name)){
            return "1";
        }
        String res = "";
        if (DecodeGrpcClient.isMd5(name)) {
            //cell md5
            res = RpcClientProxy.decode(name, "name", "md5", "");
        } else if (name.length() == 64) {
            //cell sha256
            res = RpcClientProxy.decode(name, "name", "sha", "");
        }
        if (!StringUtils.isEmpty(res)) {
            return res;
        }
        if(StringUtils.isNotBlank(name)){
            return name;
        }
        return "1";
    }

    @Override
    public Result updateFileTodb(TxtToDbDTO dto) {
        long startTime = System.currentTimeMillis();
        UpdatePhoneSale phoneSale = new UpdatePhoneSale();
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetFields = dto.getExtSetField();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;orgname不能为空;user_type不能为空;source不能为空;";
        phoneSale.setApiCode(dto.getApiCode());
        phoneSale.setLocalId(dto.getLocalId().toString());
        phoneSale.setStatus(1);
        
        try {

            // 数据处理
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                String dataValue = datas.get(i) != null ? datas.get(i).trim() : "";
                
                switch (sureaddress) {
                    case "uid":
                        if (StringUtils.isNotBlank(dataValue)) {
                            error = error.replace("uid不能为空;", "");
                            // 验证uid长度和格式
                            if (dataValue.length() > 255) {
                                error += "uid长度超过255字符;";
                            }
                        }
                        phoneSale.setUid(dataValue);
                        break;
                    case "orgname":
                        if (StringUtils.isNotBlank(dataValue)) {
                            error = error.replace("orgname不能为空;", "");
                            if (dataValue.length() > 100) {
                                error += "orgname长度超过100字符;";
                            }
                            phoneSale.setOrgname(dataValue);
                        }
                        break;
                    case "source":
                        if (StringUtils.isNotBlank(dataValue)) {
                            error = error.replace("source不能为空;", "");
                            if (dataValue.length() > 100) {
                                error += "source长度超过100字符;";
                            }
                            phoneSale.setSource(dataValue);
                        }
                        break;
                    case "user_type":
                        if (StringUtils.isNotBlank(dataValue)) {
                            error = error.replace("user_type不能为空;", "");
                            if (dataValue.length() > 100) {
                                error += "user_type长度超过100字符;";
                            }
                            phoneSale.setUserType(dataValue);
                        }
                        break;
                    case "extend":
                        String s = extSetFields.get(i);
                        if (StringUtils.isNotBlank(s) && StringUtils.isNotBlank(dataValue)) {
                            if (jo == null) {
                                jo = new JSONObject();
                            }
                            jo.put(s, dataValue);
                        }
                        break;
                }
            }
            
            // 在循环外设置扩展字段
            if (jo != null) {
                phoneSale.setExtend(jo.toJSONString());
            }
            
            // 数据验证结果处理
            if (!StringUtils.isEmpty(error)) {
                phoneSale.setStatus(2);
                phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, error));
                log.warn("updateFileTodb - 数据验证失败，行号：{}, 错误：{}", line, error);
            } else {
                log.debug("updateFileTodb - 数据处理成功，行号：{}, uid：{}, orgname：{}", 
                    line, phoneSale.getUid(), phoneSale.getOrgname());
            }
            
            // 设置时间戳
            Date date = new Date();
            phoneSale.setCreateTime(date);
            phoneSale.setUpdateTime(date);
            
            // 数据库插入
            updatePhoneSaleMapper.insertSelective(phoneSale);
            
            // 性能监控
            long endTime = System.currentTimeMillis();
            // 超过1秒记录警告
            if (endTime - startTime > 1000) {
                log.error("updateFileTodb - 处理耗时过长，行号：{}, 耗时：{}ms", line, endTime - startTime);
            }
            
        } catch (Exception ex) {
            log.error("updateFileTodb - 处理异常，行号：{}, 错误：{}", line, ex.getMessage(), ex);
            phoneSale.setStatus(2);
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.length() > 200) {
                errorMsg = errorMsg.substring(0, 200) + "...";
            }
            phoneSale.setDataMessage(String.format("行号：%d;报错信息：%s", line, errorMsg));
            
            try {
                updatePhoneSaleMapper.insertSelective(phoneSale);
            } catch (Exception insertEx) {
                log.error("updateFileTodb - 插入错误记录失败，行号：{}, 错误：{}", line, insertEx.getMessage());
            }
        }
        
        return new Result().setCode(new Integer("1").equals(phoneSale.getStatus())
                ? ResultCode.SUCCESS.getValue()
                : ResultCode.FAIL.getValue());
    }

}
