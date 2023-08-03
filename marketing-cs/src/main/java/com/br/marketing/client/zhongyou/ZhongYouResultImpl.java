package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadPoolExecutor;
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

    private static final String RETURNCODE = "0000";

    @Override
    public String applyStream(InputStream inputStream) {
        ThreadPoolExecutor poolExecutor = BrExecutors.getThreadPool(5,5);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String tempString;
        int line = 1;
        String lineData = "";
        try {
            while (true) {
                if (((tempString = reader.readLine()) == null)) {
                    break;
                }
                lineData = tempString.trim();
                if (line == 1) {
                    // 如果第一行返回是一个json 格式则说明接口请求异常
                    if (isJson(lineData)) {
                        // 异常数据停止循环
                        break;
                    }
                    if (isNumeric(lineData)) {
                        // 设置第一行数据标记
                    }
                }
//                poolExecutor.execute();
                // 存储数据
                log.warn("--- 第 {} 行 ---",line);
                log.warn(lineData);
                line++;

            }
        } catch (IOException e) {
            log.error("数据流处理异常 line:{}",line);
            return "数据流处理异常 line:"+line;
        }
        if (line > 1) {
            return "success";
        }
        return lineData;
    }

    @Override
    public String applyEntity(HttpEntity httpEntity) {
        String resultString = "";
        try {
            String result = EntityUtils.toString(httpEntity, CHARSET_UTF8);
            JSONObject resultJson = JSONObject.parseObject(result);
            String responseCode = resultJson.getString("responseCode");
            if(RETURNCODE.equals(responseCode)){
                // 解析数据
                String sysSign = resultJson.getString("sysSign");
                String responseData = resultJson.getString("responseData");
                resultString = ZhongYouClientData.decryptData(responseData, sysSign);
            }
        } catch (Exception e) {
            resultString = "解析中邮Entity数据异常";
           log.error("解析中邮Entity数据异常");
        }
        return resultString;
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
