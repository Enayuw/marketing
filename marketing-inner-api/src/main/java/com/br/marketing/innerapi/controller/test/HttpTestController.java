package com.br.marketing.innerapi.controller.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http测试
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-13 11:18
 */
@RestController
@RequestMapping("test")
@Tag(name = "http测试接口", description = "http测试")
@Slf4j
public class HttpTestController {

    @Resource
    private ZbankClient zBankClient;



    /**
     * 测试众邦代理
     */
    @Operation(summary = "代理", description = "代理")
    @GetMapping(path = {"zbankProxy"})
    public JSONObject testZbank() throws Exception {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map4 = new HashMap<>();
        // 交易流水号
        map1.put("TxnSrlNo", "" + System.nanoTime());
        // 交易日期
        map1.put("TxnDt", "20220628");
        // 交易时间戳
        map1.put("TxnTs", "130322001");
        // 任务id
        map1.put("TskId", Md5Utils.cell32("15301786322"));
        // 主键
        map1.put("PrimKey", "" + System.nanoTime());
        ArrayList<Map<String, Object>> maps = new ArrayList<>();
        // 客户信息数组
        map1.put("CstInfoArray", maps);
        Map<String, Object> map2 = new HashMap<>();
        // 客户号
        map2.put("CstNo", "12");
        // 标签评级
        map2.put("TagGrd", "12");
        // 备注
        map2.put("Rmk", "12");
        maps.add(map2);
        map4.put("request", map1);
        String s = zBankClient.labelRatingRe(map4);
        log.warn("zBank##################################:" + s);
        return JSONObject.parseObject(s);
    }

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;

    /**
     * 测试参数
     * %7B"listValue"%3A%7B"script"%3A%7B"lang"%3A"painless"%2C"source"%3A"for%20(item%20in%20params%5B%27_source%27%5D%5B%27condition%27%5D)%20%7B%20if%20((item%5B%27field_key%27%5D%20%3D%3D%20%27scorencashonxcysxsxtg%27%20%26%26%20item%5B%27d_value%27%5D%20!%3Dnull%20%26%26%20item%5B%27d_value%27%5D%20>%3D%2075%20%26%26%20item%5B%27d_value%27%5D%20<%2080)%7C%7C(item%5B%27field_key%27%5D%20%3D%3D%20%27scorencashonxcysxsxtg%27%20%26%26%20item%5B%27d_value%27%5D%20!%3Dnull%20%20%26%26%20item%5B%27d_value%27%5D%20>%3D%2080%20%26%26%20item%5B%27d_value%27%5D%20<%2085))%20%7B%20return%20%27中价值%27%3B%20%7D%20%7D%20return%20%27%27%3B"%7D%7D%2C"valueType"%3A%7B"script"%3A%7B"lang"%3A"painless"%2C"source"%3A"for%20(item%20in%20params%5B%27_source%27%5D%5B%27condition%27%5D)%20%7B%20if%20((item%5B%27field_key%27%5D%20%3D%3D%20%27scorencashonxcysxsxtg%27%20%26%26%20item%5B%27d_value%27%5D%20!%3Dnull%20%20%26%26%20item%5B%27d_value%27%5D%20>%3D%2075%20%26%26%20item%5B%27d_value%27%5D%20<%2080)%7C%7C(item%5B%27field_key%27%5D%20%3D%3D%20%27scorencashonxcysxsxtg%27%20%26%26%20item%5B%27d_value%27%5D%20!%3Dnull%20%20%26%26%20item%5B%27d_value%27%5D%20>%3D%2080%20%26%26%20item%5B%27d_value%27%5D%20<%2085))%20%7B%20return%20%27type2%27%3B%20%7D%20%7D%20return%20%27%27%3B"%7D%7D%7D
     * @param scriptFields
     * @return
     */
    @GetMapping(path = {"testEs"})
    public String testEs(String scriptFields){
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode("7410950");
        queryBaseBean.setBatchNumbers("7410950_20241017000000_2626");
        queryBaseBean.setFileIds("3000069");
        queryBaseBean.setJsonData("{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\">=\",\"value\":\"10\"}]}");
        queryBaseBean.setScriptFields(scriptFields);
        List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
        return "";
    }


    public static void main(String[] args) {
//        String url = "https://marketing.100credit.com/api/marketing-inner-api/account/getLineAccountLogs?configIdStr=5231758614369179&current=1&size=10&t=1758619941.429";
//        RestTemplate restTemplate = new RestTemplate();
//        // 设置请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("SessionId", "7D193D9AF33F5CE772C2AF4DA30AF01D");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//        );
//        System.out.println(response.getBody());
        readExcelXls();
    }


    private static void readExcelXls() {
        String filePath = "E:\\sms_line_price\\sms_line_price_20250923_online_119.xlsx";
        File excelFile = new File(filePath);

        if (!excelFile.exists()) {
            System.out.println("文件不存在");
            return;
        }
        try {
            List<Map<String, String>> excelData = ExcelMergeCellReader.readExcelWithMergeCells(filePath);

            // 逐行打印结果
            for (Map<String, String> row : excelData) {
                StringBuilder line = new StringBuilder();
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    if (!"rowNumber".equals(entry.getKey())) {
                        line.append(entry.getValue()).append(",");
                    }
                }
                //System.out.println("行" + row.get("rowNumber") + ": " + line.toString().trim());
                //System.out.println(line.toString().trim());
                String[] columnsArray = line.toString().split(",");

                //{
                //  "priceDates": [
                //    {
                //      "price": 1,
                //      "effectStartDate": "2025-09-23"
                //    }
                //  ],
                //  "lineSupplier": "XY",
                //  "lines": [
                //    {
                //      "gatewayId": 690022,
                //      "callerFullname": "XY-06654094252"
                //    }
                //  ]
                //}
                JSONObject jsonObject = new JSONObject();
                //priceDates
                JSONArray priceDatesArray = new JSONArray();
                JSONObject jsonArrayItemObj = new JSONObject();
                jsonArrayItemObj.put("price",columnsArray[4]);
                jsonArrayItemObj.put("effectStartDate","2025-09-23");
                priceDatesArray.add(jsonArrayItemObj);
                jsonObject.put("priceDates",priceDatesArray);


                //lineSupplier
                jsonObject.put("lineSupplier",columnsArray[1]);

                //lines
                JSONArray linesArray = new JSONArray();
                JSONObject linesArrayItem = new JSONObject();
                linesArrayItem.put("gatewayId",columnsArray[3]);
                linesArrayItem.put("callerFullname",columnsArray[2]);
                linesArray.add(linesArrayItem);
                jsonObject.put("lines",linesArray);

                System.out.println(jsonObject.toString().trim());
                //postRequestTest(jsonObject);

                postRequestOnline(jsonObject);
            }

        } catch (Exception e) {
            System.out.println("读取Excel文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void postRequestOnline(JSONObject obj) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl ="https://marketing.100credit.com/api/marketing-inner-api/account/addLineAccount";
        try {
            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("SessionId", "216391C314D2BB82F13B2EF08A98EBE0");
            headers.set("Content-Type", "application/json");

            // 3. 创建HttpEntity
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(obj, headers);
            // 4. 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            // 5. 打印结果
            System.out.println("响应状态码: " + response.getStatusCode());
            System.out.println("响应头: " + response.getHeaders());
            System.out.println("响应体: " + response.getBody());

        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
