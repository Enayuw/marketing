package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.ZhongyouFileData;
import com.br.marketing.mapper.ZhongyouFileDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
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
        ThreadPoolExecutor zhongyouThread = BrExecutors.getThreadPool(marketingCommonConfig.getZhongYouFileDataThreadNum(), marketingCommonConfig.getZhongYouFileDataThreadNum());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Map<String, String> resultMap = new HashMap<>();
        try {
            List<ZhongyouFileData> zhongyouFileDataList = new ArrayList<>();
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(zhongyouThread, marketingCommonConfig.getZhongYouFileDataThreadNum());
            while (dealStream(fileId, reader, zhongyouFileDataList, resultMap, zhongyouThread)) {
                // do nothing;
            }
            shutdownThread(zhongyouThread);
        } catch (Exception e) {
            log.error("数据流处理异常：{}", e);
            resultMap.put("result", "数据流处理异常");
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
            List<ZhongyouFileData> saveZhongyouFileDataList = new ArrayList<>(zhongyouFileDataList);
            zhongyouThread.submit(() -> {
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
    private void zhongyouDataListBuild(Long fileId, List<ZhongyouFileData> zhongyouFileDataList,
                                       Map<String, String> resultMap, String lineData) {
        try {
            ZhongyouFileData zhongyouFileData = new ZhongyouFileData();
            zhongyouFileData.setFileId(fileId);
            zhongyouFileData.setStatus(1);
            zhongyouFileData.setType("2");
            zhongyouFileData.setApiCode(marketingCommonConfig.getZhongyouApiCode());

            // 如果第一行返回是一个json 格式则说明接口请求异常
            if (lineData.contains("||")) {
                int count = getCount(lineData, "||");
                if (count == marketingCommonConfig.getZhongyouColumnsSize()) {
                    String strategyId = lineData.split("\\|\\|")[0];
                    zhongyouFileData.setStrategyId(strategyId);
                } else {
                    zhongyouFileData.setStatus(2);
                    zhongyouFileData.setDataMessage("字段数不匹配");
                }
            } else if (isNumeric(lineData)) {
                // 设置第一行数据标记
                zhongyouFileData.setType("1");
            } else if (isJson(lineData)) {
                resultMap.put("result", lineData);
                log.error("中邮文件内行数据异常 lineData:{}", lineData);
                return;
            } else {
                zhongyouFileData.setStatus(2);
                zhongyouFileData.setDataMessage("数据格式异常");
            }
            zhongyouFileData.setFileData(lineData);
            String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            zhongyouFileData.setCreateDate(Integer.parseInt(format));
            zhongyouFileData.setCreateTime(new Date());
            zhongyouFileData.setUpdateTime(new Date());
            // 存储数据
            zhongyouFileDataList.add(zhongyouFileData);
        }catch (Exception e){
            log.error("中邮行数据读取解析异常：{}",e);
        }

    }

    public int getCount(String str, String key) {
        if (str == null || key == null || str.trim().isEmpty() || key.trim().isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(key, index)) != -1 && count <= marketingCommonConfig.getZhongyouColumnsSize() + 1) {
            index = index + key.length();
            count++;
        }
        return count;
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
        return tempString;
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
            } else {
                log.error("中邮文件接口返回code 码异常：{}", resultJson.toJSONString());
            }
        } catch (Exception e) {
            log.error("解析中邮Entity数据异常：{}",e);
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
