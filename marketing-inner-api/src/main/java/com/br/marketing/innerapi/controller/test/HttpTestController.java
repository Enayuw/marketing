package com.br.marketing.innerapi.controller.test;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.zbank.ZBankClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * http测试
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-13 11:18
 */
@RestController
@RequestMapping("test")
@Api(value = "http测试接口", tags = "http测试", produces = "application/json", consumes = "application/json", protocols = "http")
@Slf4j
public class HttpTestController {

    @Resource
    private ZBankClient zBankClient;

    /**
     * 测试众邦代理
     */
    @ApiOperation(value = "代理", notes = "代理")
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
}
