package com.br.marketing.service.file.filetodb.assembler;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.service.file.filetodb.AbstractFileToDbAssembler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * CsvToDbAssembler
 *
 * @author xiang.li
 * @date 2024/01/22
 */
@Service
@Slf4j
public class CsvToDbAssembler extends AbstractFileToDbAssembler {

    public String chooseSqlTemp(String tempType) {
        if(StringUtils.isEmpty(tempType)){
            return "insert into #{tableName} (#{columns}) values #{values}";
        }
        return "";
    }

    @Override
    public Result<Map<String, Object>> assembleTempParams(String tempType, TxtToDbDTO toDbDTO) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            HashMap<Integer, String> fieldIndexMap = toDbDTO.getAddress();
            HashSet<String> fieldAll = toDbDTO.getFieldAll();
            HashSet<String> fieldMust = toDbDTO.getFieldMust();

            StringBuilder insertValues = new StringBuilder();
            Integer successNum = 0;
            Integer errorNum = 0;

            String columns = "api_code, local_file_id, file_data, data_status, data_desc, create_date, create_time";
            String valueFormat = "('%s','%d','%s','%s','%s','%s','%s')";

            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 遍历每行数据
            for (Map.Entry<Integer, String> dataEntry : toDbDTO.getDatas().entrySet()) {
                List<String> dataLineList = Splitter.on(",").splitToList(dataEntry.getValue());
                Integer line = dataEntry.getKey();
                String errorMsg = "";

                // region 列数不一致,直接赋值跳出
                if (dataLineList.size() != fieldIndexMap.size()) {
                    String lineValue = String.format(valueFormat,
                            toDbDTO.getApiCode(), toDbDTO.getLocalId(), "", "2", String.format("行号：%d;报错信息：%s", line, "表头和该行数据不一致"),
                            currentDate, currentTime);
                    insertValues.append(lineValue).append(",");
                    errorNum++;
                    continue;
                }

                // 遍历每行所有字段
                StringBuilder fileDataBuilder = new StringBuilder();
                StringBuilder errorBuilder = new StringBuilder();
                for (int i = 0; i < dataLineList.size(); i++) {
                    String field = fieldIndexMap.get(i);
                    String value = dataLineList.get(i);
                    // 当前文件字段不在b_file_db_config配置字段中，忽略
                    if (!fieldAll.contains(field)) {
                        continue;
                    }

                    // 当前字段是必填字段, 校验value是否为空
//                    if (fieldMust.contains(field) && StringUtils.isBlank(value)) {
//                        errorMsg = String.format("%s不能为空;", field);
//                        errorBuilder.append(errorMsg);
//                    }
                    fileDataBuilder.append(StringUtils.isBlank(value) ? "" : String.format("%s", value)).append(",");
                }

                // assemble data_status, data_desc
                String dataStatus = "1";
                String dataDesc = "";
                if (StringUtils.isNotEmpty(errorBuilder.toString())) {
                    errorNum++;
                    dataStatus = "2";
                    dataDesc = String.format("行号：%d;报错信息：%s", line, errorBuilder.toString());
                } else {
                    successNum++;
                    dataStatus = "1";
                    dataDesc = "";
                }

                if (StringUtils.isNotBlank(fileDataBuilder.toString())) {
                    String lineValue = String.format(valueFormat,
                            toDbDTO.getApiCode(), toDbDTO.getLocalId(), StringUtils.removeEnd(fileDataBuilder.toString(),","),
                            dataStatus, dataDesc, currentDate, currentTime);
                    insertValues.append(lineValue).append(",");
                }
            }

            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("tableName", toDbDTO.getDbName().replace("apicode", toDbDTO.getApiCode()));
            placeHolders.put("columns", columns);
            placeHolders.put("values", StringUtils.removeEnd(insertValues.toString(),","));
            resultMap.put("placeHolders", placeHolders);
            resultMap.put("successNum", successNum);
            resultMap.put("errorNum", errorNum);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(e.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultMap);
    }
}
