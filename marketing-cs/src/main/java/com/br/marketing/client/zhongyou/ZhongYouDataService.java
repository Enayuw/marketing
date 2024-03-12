package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.ZhongyouDataCountDTO;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhongyouFileDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN;

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
    public static final String ZHONGYOUOUTMARKETING = "zhongyououtmarketing";
    @Resource
    private ZhongYouClient zhongYouClient;


    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ZhongyouFileDataMapper zhongyouFileDataMapper;

    @Resource
    private RabbitMqProducter producter;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    TableCreateServiceImpl tableCreateService;

    @Resource
    ZhongYouClientData zhongYouClientData;

    @RetryMethod(retryNowNum = 2)
    public Result<List<Long>> saveFileNameList(LocalDate date) {
        // 拉取数据
        String fileDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(
                        zhongYouClientData.fileNameListData(fileDate),
                        zhongYouClientData.getQueryUrl(),
                        zhongYouClientData.getIsProxy(),
                        false,
                        null);
        if (!"200".equals(stringStringHashMap.get("httpcode"))) {
            log.error("中邮文件列表接口httpcode非200异常，重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        if(StringUtils.isEmpty(stringStringHashMap.get("content"))){
            log.error("中邮获取文件列表responseData数据为空");
            return new Result<List<Long>>().setCode(ResultCode.FAIL.getValue());
        }
        List<Long> ids = saveFile(stringStringHashMap.get("content"));
        return new Result<List<Long>>().setCode(ResultCode.SUCCESS.getValue()).setDate(ids);
    }

    @RetryMethod(retryNowNum = 2)
    public Result saveFileData(Long fileId) {
        LocalFile zhongyouFile = localFileMapper.selectByPrimaryKey(fileId);
        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(
                        zhongYouClientData.fileDownLoadData(zhongyouFile.getFileName()),
                        zhongYouClientData.getDownloadUrl(),
                        zhongYouClientData.getIsProxy(),
                        true,
                        fileId);
        if (!"200".equals(stringStringHashMap.get("httpcode"))) {
            log.error("中邮文件内容接口httpcode非200异常");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        sendZhongyouDataMq(fileId);
        return new Result<List<Long>>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 发送mq
     *
     * @param fileId 文件id
     */
    private void sendZhongyouDataMq(Long fileId) {
        List<ZhongyouDataCountDTO> zhongyouDataCountDTOList = zhongyouFileDataMapper.selectZhongyouCount(fileId);
        if (zhongyouDataCountDTOList.size() == 2) {
            String fileData = zhongyouDataCountDTOList.get(0).getFileData();
            Integer num = zhongyouDataCountDTOList.get(1).getNum();
            if (!Integer.valueOf(fileData).equals(num)) {
                log.error("中邮文件数据量级不匹配：文件给定量级-> {},实际入库量级-> {}", fileData, num);
            }
            producter.send(ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN, String.valueOf(fileId));
        }else {
            log.error("中邮文件内容数据异常 fileId ：{}",fileId);
        }

    }

    private List<Long> saveFile(String content) {
        JSONObject jsonData = JSONObject.parseObject(content);
        JSONArray fileNames = jsonData.getJSONArray("fileNames");
        if (fileNames.isEmpty()) {
            log.error("中邮文件查询列表为空");
        }
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = JSONObject.parseObject(fileNames.getString(i)).getString("fileName");
            LocalFile zhongyouFile = zhongyouFileBuild(fileName);
            if (localFileMapper.insertSelective(zhongyouFile) > 0) {
                ids.add(zhongyouFile.getId());
            }
        }
        return ids;
    }

    /**
     * 中邮文件实体创建
     *
     * @param fileName 文件名称
     * @return ZhongYouFile 实体
     */
    private  LocalFile zhongyouFileBuild(String fileName) {
        LocalFile zhongyouFile = new LocalFile();
        zhongyouFile.setFileName(fileName);
        zhongyouFile.setCid(tableCreateService.getCId(marketingCommonConfig.getZhongyouApiCode()));
        zhongyouFile.setApiCode(marketingCommonConfig.getZhongyouApiCode());
        zhongyouFile.setFileType(ZHONGYOUOUTMARKETING);
        zhongyouFile.setStatus("1");
        zhongyouFile.setCreateTime(new Date());
        zhongyouFile.setUpdateTime(new Date());
        return zhongyouFile;
    }

}
