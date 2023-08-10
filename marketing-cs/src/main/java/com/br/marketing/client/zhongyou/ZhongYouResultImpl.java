package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.ZhongyouFileData;
import com.br.marketing.mapper.ZhongyouFileDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static net.lingala.zip4j.util.InternalZipConstants.CHARSET_UTF8;

/**
 * 描述：： 中邮结果处理逻辑
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouResultImpl
 * @author: it-yml
 * @create: 2023-08-02 14:57
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ZhongYouResultImpl implements ZhongYouResultInterface {

    /**
     * 中邮返回code码
     */
    private static final String RETURN_CODE = "0000";

    /**
     * 存储文件内容条数
     */
    private static final Integer SAVE_PARTITION_SIZE = 2000;

    @Resource
    private ZhongyouFileDataMapper zhongyouFileDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ZhongYouClientData zhongYouClientData;

    @Override
    public Map<String, String> applyStream(InputStream inputStream, Long fileId) {
        ThreadPoolExecutor zhongyouThread = BrExecutors.getThreadPool(5, 5);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Map<String, String> resultMap = new HashMap<>();
        try {
            List<ZhongyouFileData> zhongyouFileDataList = new ArrayList<>();

            while (dealStream(fileId, reader, zhongyouFileDataList, resultMap, zhongyouThread)) {
                // do nothing;
            }
            shutdownThread(zhongyouThread);
        } catch (Exception e) {
            log.error("数据流处理异常：{}", e.toString());
            resultMap.put("result", "数据流处理异常");
            resultMap.put("responseData", "数据流处理异常");
        }
        return resultMap;
    }

    /**
     * 关闭线程池
     *
     * @param zhongyouThread 线程池
     */
    private static void shutdownThread(ThreadPoolExecutor zhongyouThread) {
        zhongyouThread.shutdown();
        try {
            while (!zhongyouThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 主流程处理逻辑
     *
     * @param fileId               文件id
     * @param reader               stream流
     * @param zhongyouFileDataList 数据集
     * @param resultMap            结果集
     * @param zhongyouThread       线程池
     * @return 是否继续while 循环
     * @throws IOException IO异常
     */
    private boolean dealStream(Long fileId, BufferedReader reader, List<ZhongyouFileData> zhongyouFileDataList,
                               Map<String, String> resultMap, ThreadPoolExecutor zhongyouThread) throws IOException {
        String lineData = getLineData(reader, zhongyouFileDataList);
        if (lineData == null) return false;
        zhongyouDataListBuild(fileId, zhongyouFileDataList, resultMap, lineData);
        zhongyouDataListSave(zhongyouFileDataList, zhongyouThread);
        return true;
    }

    /**
     * 集合数据存储
     *
     * @param zhongyouFileDataList 数据集
     * @param zhongyouThread       线程池
     */
    private void zhongyouDataListSave(List<ZhongyouFileData> zhongyouFileDataList, ThreadPoolExecutor zhongyouThread) {
        if (zhongyouFileDataList.size() == SAVE_PARTITION_SIZE) {
            // 线程池存储
            zhongyouThread.submit(() -> {
                List<ZhongyouFileData> saveZhongyouFileDataList = new ArrayList<>(zhongyouFileDataList);
                zhongyouFileDataMapper.saveBatch(saveZhongyouFileDataList);
            });
            // 清空集合
            zhongyouFileDataList.clear();
        }
    }

    /**
     * 存储集合数据构建
     *
     * @param fileId               文件id
     * @param zhongyouFileDataList 中邮待存储鞂
     * @param resultMap            返回结果集
     * @param lineData             行内容
     */
    private  void zhongyouDataListBuild(Long fileId, List<ZhongyouFileData> zhongyouFileDataList,
                                              Map<String, String> resultMap, String lineData) {
        ZhongyouFileData zhongyouFileData = new ZhongyouFileData();
        zhongyouFileData.setFileId(fileId);
        zhongyouFileData.setStatus(1);
        zhongyouFileData.setType("2");
        zhongyouFileData.setApiCode(marketingCommonConfig.getZhongyouApiCode());

        // 如果第一行返回是一个json 格式则说明接口请求异常
        if (isJson(lineData)) {
            zhongyouFileData.setDataMessage(lineData);
            zhongyouFileData.setStatus(2);
            resultMap.put("result", lineData);
            log.error("中邮文件内数据接口请求异常 lineData:{}", lineData);
        } else if (isNumeric(lineData)) {
            // 设置第一行数据标记
            zhongyouFileData.setType("1");
        } else if (lineData.contains("||")) {
            int length = lineData.split("\\|\\|").length;
            if (length != marketingCommonConfig.getZhongyouColumnsSize()) {
                zhongyouFileData.setStatus(2);
                zhongyouFileData.setDataMessage("字段数不匹配：" + length);
            }
        }
        zhongyouFileData.setFileData(lineData);
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        zhongyouFileData.setCreateDate(Integer.parseInt(format));
        zhongyouFileData.setCreateTime(new Date());
        zhongyouFileData.setUpdateTime(new Date());
        // 存储数据
        zhongyouFileDataList.add(zhongyouFileData);
    }

    /**
     * 获取数据流里的每一行数据
     *
     * @param reader               数据流
     * @param zhongyouFileDataList 中邮待存储集合
     * @return 行数据流
     * @throws IOException IO异常
     */
    private String getLineData(BufferedReader reader, List<ZhongyouFileData> zhongyouFileDataList) throws IOException {
        String tempString;
        if (((tempString = reader.readLine()) == null)) {
            // 最后一批不足2000的数据主线程直接存储
            if (!zhongyouFileDataList.isEmpty()) {
                zhongyouFileDataMapper.saveBatch(zhongyouFileDataList);
            }
            return null;
        }
        return tempString.trim();
    }

    @Override
    public Map<String, String> applyEntity(HttpEntity httpEntity) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            String result = EntityUtils.toString(httpEntity, CHARSET_UTF8);
            resultMap.put("result", result);
            JSONObject resultJson = JSONObject.parseObject(result);
            String responseCode = resultJson.getString("responseCode");
            if (RETURN_CODE.equals(responseCode)) {
                // 解析数据
                String sysSign = resultJson.getString("sysSign");
                String responseData = resultJson.getString("responseData");
                String resultString = zhongYouClientData.decryptData(responseData, sysSign);
                resultMap.put("responseData", resultString);
            }
        } catch (Exception e) {
            resultMap.put("responseData", "解析中邮Entity数据异常");
            log.error("解析中邮Entity数据异常");
        }

        return resultMap;
    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private static boolean isJson(String str) {
        try {
            JSONObject.parseObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
