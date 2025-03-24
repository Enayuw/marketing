package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.carclue.CarClueClient;
import com.br.marketing.client.carclue.dto.HxClueCommitDTO;
import com.br.marketing.client.hxchannel.HxChannelClient;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.tag.AntaiosResourceClient;
import com.br.marketing.client.tag.dto.AntaiosResourceDTO;
import com.br.marketing.client.tag.vo.AntaiosResourceVo;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.ProductResultByConfigSimpleServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.UserCenterHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("redis")
@Slf4j
public class RedisController {

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IProductResultSimpleService productResultSimpleService;

    @Autowired
    UserCenterHandler userCenterHandler;

    @GetMapping("testM")
    public String testM(String msg) {
        Result<Boolean> booleanResult = userCenterHandler.handleDataUserCenter(msg);
        return JSONObject.toJSONString(booleanResult);
    }

    @GetMapping("testNX")
    public String testNX(String taskId) {
        try {
            String taskByPushRuleGetLock = RedisKeyConstant.TASK_PUSH_RULE_GET_LOCK.concat(":" + taskId);
            UUID uuid = UUID.randomUUID();
            Boolean setnx = redisChgService.setnx(taskByPushRuleGetLock, uuid.toString(), 3);
            if (!setnx) {
                return null;
            }
            return uuid.toString();
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
            return null;
        }
    }

    @GetMapping("get")
    public String get(@RequestParam("key") String key, @RequestParam(value = "type", required = false) String type) {
        if (!redisChgService.exists(key)) {
            return "key不存在";
        }
        if ("Set".equals(type)) {
            return JSON.toJSONString(redisChgService.smembers(key));
        } else {
            return redisChgService.get(key);
        }
    }

    @GetMapping("del")
    public String del(@RequestParam("key") String key) {
        long del = redisChgService.del(key);
        return String.valueOf(del);
    }

    @GetMapping("set")
    public String set(@RequestParam("key") String key, @RequestParam("value") String value) {
        redisChgService.set(key, value);
        return "success";
    }

    @GetMapping("/clearInnerCache")
    public String clearInnerCache(@RequestParam("type") Integer type) {
        if (Integer.valueOf(1).equals(type)) {
            ProductResultByConfigSimpleServiceImpl.flagScoreByinnerList.clear();
        }
        return "success";
    }

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @GetMapping("/test")
    public String test() {
        String ab = "{\"apiCode\":\"7410438\",\"jsonData\":{\"accessNumber\":\"juman_20220905_01\",\"batchNumber\":\"juman_20220905\",\"data\":[{\"caseNumber\":\"20220905_01\",\"phone\":\"AgsNΒ7VlVSWwkAVwY\",\"variables\":{\"groupType\":\"促首登\",\"score\":\"83.0\",\"scoreDate\":\"2021-07-26\",\"scoreName\":\"scorencashonshcdlyxf\",\"taskId\":\"82021072601\",\"update\":\"\",\"sleepGroup\":\"540+\"}}],\"extendData\":{\"sampleTotal\":\"1\",\"scoreName\":\"scorencashonshcdlyxf\"},\"method\":\"caseAdd\"},\"platApiCode\":\"7410438\"}";
        PushMarketingUserDTO o = JSON.parseObject(ab, new TypeReference<PushMarketingUserDTO>() {
        }.getType());
        Result<Integer> integerResult = intelligentCustomerServiceClient.pushRuleCenterToPolicy(o, 123L, "123", 1);
        System.out.println(integerResult.getMessage());
        return "";
    }

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取speed的配置信息 方便验证speed是否推送成功
     *
     * @return
     */
    @GetMapping("/getSpeedInfo")
    public String getSpeedInfo() {
        return marketingCommonConfig.toString();
    }

    @GetMapping("/getScoreToCustomerBigKey")
    public String getScoreToCustomerBigKey(Long fileId) {
        HashMap<String, Boolean> res = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                    .concat(":").concat(fileId.toString())
                    .concat(":").concat("" + i);
            Boolean exists = redisChgService.exists(key);
            res.put(key, exists);
        }
        return JSON.toJSONString(res);
    }

    @Resource
    CarClueClient carClueClient;

    @GetMapping("/testCarInterface")
    public String testCarInterface(@RequestParam("carType") String carType, @RequestParam("interfaceType") String interfaceType) {
        if ("yc-ka".equals(carType)) {
            if ("city".equals(interfaceType)) {
                Result<JSONArray> ycCity = carClueClient.getYcCity("7-1");
                return JSON.toJSONString(ycCity);
            } else if ("car".equals(interfaceType)) {
                Result<JSONArray> ycCar = carClueClient.getYcCar("7-1");
                return JSON.toJSONString(ycCar);
            } else if ("commit".equals(interfaceType)) {
                HxClueCommitDTO hxClueCommitDTO = new HxClueCommitDTO();
                hxClueCommitDTO.setChannelId("umOFo6Lmtx7z8Xpk");
                hxClueCommitDTO.setPhone("13000000000");
                hxClueCommitDTO.setMember("马");
                hxClueCommitDTO.setProvince("贵州省");
                hxClueCommitDTO.setCity("贵阳");
                hxClueCommitDTO.setBrand("东风奕派");
                hxClueCommitDTO.setSeries("eπ007");
                hxClueCommitDTO.setSeriesId(10553);
                hxClueCommitDTO.setPushTask("7-1");
                hxClueCommitDTO.setSoundUrl("123");
                hxClueCommitDTO.setBuyTime("2025-04-20");
                Result<String> clueRes = carClueClient.commitClue(hxClueCommitDTO);
                return JSON.toJSONString(clueRes);
            }
        } else  if ("yc-member".equals(carType)) {
            if ("city".equals(interfaceType)) {
                Result<JSONArray> ycCity = carClueClient.getYcCity("6+");
                return JSON.toJSONString(ycCity);
            } else if ("car".equals(interfaceType)) {
                Result<JSONArray> ycCar = carClueClient.getYcCar("6+");
                return JSON.toJSONString(ycCar);
            } else if ("commit".equals(interfaceType)) {
                HxClueCommitDTO hxClueCommitDTO = new HxClueCommitDTO();
                hxClueCommitDTO.setChannelId("umOFo6Lmtx7z8Xpk");
                hxClueCommitDTO.setPhone("13000000000");
                hxClueCommitDTO.setMember("马");
                hxClueCommitDTO.setProvince("贵州省");
                hxClueCommitDTO.setCity("贵阳");
                hxClueCommitDTO.setBrand("东风奕派");
                hxClueCommitDTO.setSeries("eπ007");
                hxClueCommitDTO.setSeriesId(10553);
                hxClueCommitDTO.setPushTask("6+");
                hxClueCommitDTO.setSoundUrl("123");
                hxClueCommitDTO.setBuyTime("2025-04-20");
                Result<String> clueRes = carClueClient.commitClue(hxClueCommitDTO);
                return JSON.toJSONString(clueRes);
            }
        } else if ("zj".equals(carType)) {
            if ("city".equals(interfaceType)) {
                Result<JSONArray> zjCity = carClueClient.getZjCity();
                return JSON.toJSONString(zjCity);
            } else if ("car".equals(interfaceType)) {
                Result<JSONArray> zjCar = carClueClient.getZjCar();
                return JSON.toJSONString(zjCar);
            }else if ("commit".equals(interfaceType)) {
                HxClueCommitDTO hxClueCommitDTO = new HxClueCommitDTO();
                hxClueCommitDTO.setChannelId("IxFRGOyohB1vuQDk");
                hxClueCommitDTO.setPhone("13000000000");
                hxClueCommitDTO.setMember("马");
                hxClueCommitDTO.setProvince("天津");
//                hxClueCommitDTO.setCityId(19);
                hxClueCommitDTO.setCity("天津");
                hxClueCommitDTO.setBrand("WEY");
                hxClueCommitDTO.setSeries("坦克300");
                hxClueCommitDTO.setSeriesId(1019);
                hxClueCommitDTO.setPushTask("xsc");
                hxClueCommitDTO.setSoundUrl("http://www.baidu.com");
                hxClueCommitDTO.setBuyTime("2025-04-20");
                Result<String> clueRes = carClueClient.commitClue(hxClueCommitDTO);
                return JSON.toJSONString(clueRes);
            }
        }
        return "noMatch";
    }

    /**
     * 外采映射数据生成SQL
     * @return
     */
    @GetMapping("/getCarClueInit")
    public StringBuilder getCarClueInit(){
        String excelFilePath = "C:\\Users\\bingxu.kong\\Desktop\\车线索\\外采需求明细0120.xlsx";
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                StringBuilder insertStatement = new StringBuilder("INSERT INTO b_car_clue_init_mapping (");

                // 处理表头
                Row headerRow = sheet.getRow(0);
                for (Cell cell : headerRow) {
                    insertStatement.append(cell.getStringCellValue()).append(", ");
                }
                insertStatement.setLength(insertStatement.length() - 2); // 移除最后的逗号和空格
                insertStatement.append(") VALUES ");

                // 处理内容
                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    insertStatement.append("(");
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.BLANK) {
                            insertStatement.append("null, ");
                        } else {
                            switch (cell.getCellType()) {
                                case STRING:
                                    String cellValue = cell.toString().replace("\n", ",");
                                    insertStatement.append("'").append(cellValue).append("', ");
                                    break;
                                case NUMERIC:
                                    insertStatement.append((int) cell.getNumericCellValue()).append(", ");
                                    break;
                            }

                        }
                    }
                    insertStatement.setLength(insertStatement.length() - 2); // 移除最后的逗号和空格
                    insertStatement.append("), ");
                }
                insertStatement.setLength(insertStatement.length() - 2); // 移除最后的逗号和空格
                insertStatement.append(";");
                stringBuilder.append(insertStatement);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }


    @Resource
    AntaiosResourceClient antaiosResourceClient;

    @GetMapping("/getTagLibrary")
    public List<String> getTagLibrary(){
        AntaiosResourceDTO antaiosResourceDTO = new AntaiosResourceDTO();

        // 构建 JSON 请求数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "tagList");
        jsonObject.put("tagGroupName", "营销中台标签");

        antaiosResourceDTO.setApiCode("test123");
        antaiosResourceDTO.setJsonData(jsonObject);

        // 调用客户端获取标签库
        AntaiosResourceVo tagLibrary = antaiosResourceClient.getTagLibrary(antaiosResourceDTO);

        // 检查返回结果的状态码
        if (!"00000".equals(tagLibrary.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "同步标签库失败！" + JSONObject.toJSONString(tagLibrary)));
            return null;
        }

        // 获取数据列表
        String data = tagLibrary.getData();
        if (data == null || data.isEmpty()) {
            log.warn("返回的数据列表为空！");
            return null;
        }
        return Arrays.asList(data.split(","));
    }

}
