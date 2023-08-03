package com.br.marketing.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.zhongyou.ZhongYouClient;
import com.br.marketing.client.zhongyou.ZhongYouClientData;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ZhongyouFile;
import com.br.marketing.mapper.ZhongyouFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
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


    @Resource
    private ZhongyouFileMapper zhongyouFileMapper;

//    @RetryMethod
    public Result<List<Long>> saveFileNameList(LocalDate date){
        // 拉取数据
        ZhongYouClientData zhongYouClientData = new ZhongYouClientData(date);

        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(zhongYouClientData.getData(), zhongYouClientData.getUrl(), false, false);

        if (!"200".equals(stringStringHashMap.get("httpcode"))) {
            log.error("中邮文件列表接口httpcode非200异常，重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        List<Long> ids = saveFile(stringStringHashMap.get("content"));
        return new Result<List<Long>>().setCode(ResultCode.SUCCESS.getValue()).setDate(ids);
    }

    public void saveFileData(Long id){
        ZhongyouFile zhongyouFile = zhongyouFileMapper.selectByPrimaryKey(id);
        ZhongYouClientData zhongYouClientData = new ZhongYouClientData(zhongyouFile.getFileName());
        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(zhongYouClientData.getData(), zhongYouClientData.getUrl(), false, true);
        if (!"200".equals(stringStringHashMap.get("httpcode")) || StringUtils.isBlank(stringStringHashMap.get("content"))) {
            log.error("中邮文件内容接口httpcode非200异常");
        }
    }

    private  List<Long> saveFile(String content){
        JSONObject jsonData = JSONObject.parseObject(content);
        JSONArray fileNames = jsonData.getJSONArray("fileNames");
        if(fileNames.isEmpty()){
            log.error("中邮文件查询列表为空");
        }
        List<Long> ids = new ArrayList<>();
        for(int i=0;i<fileNames.size();i++){
            String fileName = JSONObject.parseObject(fileNames.getString(i)).getString("fileName");
            ZhongyouFile zhongyouFile  = new ZhongyouFile();
            zhongyouFile.setFileName(fileName);
            zhongyouFile.setDataMessage(content);
            zhongyouFile.setType("OUTMARKETING");
            if(zhongyouFileMapper.insertSelective(zhongyouFile)>0){
                ids.add(zhongyouFile.getId());
            }
        }
        return ids;
    }


}
