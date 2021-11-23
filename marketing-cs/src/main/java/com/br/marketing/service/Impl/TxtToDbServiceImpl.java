package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.mapper.TwosevenFileMapper;
import com.br.marketing.service.ITxtToDbService;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class TxtToDbServiceImpl implements ITxtToDbService {

    @Autowired
    TwosevenFileMapper twosevenFileMapper;

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
}
