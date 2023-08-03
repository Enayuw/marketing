package com.br.marketing.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.zhongyou.ZhongYouClient;
import com.br.marketing.client.zhongyou.ZhongYouClientData;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * 描述：： 中邮数据处理接口
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouDataService
 * @author: it-yml
 * @create: 2023-08-03 15:10
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ZhongYouDataService {
    @Resource
    private ZhongYouClient zhongYouClient;


    @RetryMethod
    public Result saveFileNameList(){
        // 拉取数据
        ZhongYouClientData zhongYouClientData = new ZhongYouClientData(LocalDate.now());

        HashMap<String, String> stringStringHashMap = zhongYouClient.sendByCodeWithLog(zhongYouClientData.getData(), zhongYouClientData.getUrl(), false, false);
        if (!"200".equals(stringStringHashMap.get("httpcode")) || StringUtils.isBlank(stringStringHashMap.get("content"))) {
            log.error("携程短信撞库接口httpcode非200异常，重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        saveFile(stringStringHashMap.get("content"));
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void saveFile(String content){
        JSONObject jsonData = JSONObject.parseObject(content);
        JSONArray fileNames = jsonData.getJSONArray("fileNames");
        // 保存
    }


}
