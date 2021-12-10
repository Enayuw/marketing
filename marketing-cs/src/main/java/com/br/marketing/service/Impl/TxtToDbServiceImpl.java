package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TwosevenFileMapper;
import com.br.marketing.service.ITxtToDbService;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class TxtToDbServiceImpl implements ITxtToDbService {

    @Autowired
    TwosevenFileMapper twosevenFileMapper;

    @Autowired
    LocalFileMapper localFileMapper;

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
    public Result toDbByCommon(TxtToDbDTO dto) {
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        HashMap<Integer, String> extSetField = dto.getExtSetField();
        Integer line = dto.getLine();
        String error = dto.getErrorMsg();
        String dbName = dto.getDbName().replace("apicode", dto.getApiCode());
        HashSet<String> fieldAll = dto.getFieldAll();
        HashMap<String, String> fieldAllHm = dto.getFieldAllHm();
        HashSet<String> fieldMust = dto.getFieldMust();
        List<String> datas = Splitter.on(",").splitToList(row);
        String sqlTemp = "insert into %s (%s) values ( %s )";

        JSONObject jo = null;
        Integer status = 1;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        try {
            if (datas.size() != address.size()) {
                String value = String.format("'2','%s','%s','%s'"
                        ,String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致")
                ,time,day);
                String sql = String.format(sqlTemp, dbName, "status,data_message,create_time,create_date",value);
                localFileMapper.insertFileData(sql);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            StringBuilder insertFields = new StringBuilder();
            StringBuilder valueFields = new StringBuilder();

            insertFields.append("local_id,api_code,");
            valueFields.append(String.format("'%s','%s',",dto.getLocalId().toString(),dto.getApiCode()));
            for (int i = 0; i < datas.size(); i++) {
                String sureaddress = address.get(i);
                if(fieldAll.contains(sureaddress)){
                    if(StringUtils.isNotNull(datas.get(i))) {
                        insertFields.append(fieldAllHm.get(sureaddress)).append(",");
                        valueFields.append(String.format("'%s'", datas.get(i))).append(",");
                    }
                }
                if(fieldMust.contains(sureaddress)){
                    error = error.replace(String.format("%s不能为空;",sureaddress), "");
                }
                if(sureaddress.equals("extend")){
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
                insertFields.append("extend").append(",");
                valueFields.append(jo.toJSONString()).append(",");
            }
            if (!StringUtils.isEmpty(error)) {
                status=2;
                insertFields.append("status").append(",");
                insertFields.append("data_message").append(",");
                valueFields.append("'2'").append(",");
                valueFields.append(String.format("'行号：%d;报错信息：%s'", line, error)).append(",");
            }

            insertFields.append("create_time,create_date");
            valueFields.append(String.format("'%s','%s'",time,day));
            String sql = String.format(sqlTemp, dbName, insertFields, valueFields);
            localFileMapper.insertFileData(sql);
        }catch (Exception ex){
            status=2;
            log.error(ex.getMessage(),ex);
            String value = String.format("'2','%s','%s','%s'",String.format("行号：%d;报错信息：%s"
                    , line
                    ,ex.getMessage().length()>=450
                            ?ex.getMessage().substring(0,449)
                            :ex.getMessage()),time,day);
            String sql = String.format(sqlTemp, dbName, "status,data_message,create_time,create_date",value);
            localFileMapper.insertFileData(sql);
        }
        return new Result().setCode(new Integer("1").equals(status)
                ?ResultCode.SUCCESS.getValue()
                :ResultCode.FAIL.getValue());
    }
}
