package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.br.common.util.BrCipherMaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * json加解密
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/4 14:20
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/4 14:20
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class BrCipherJsonUtils {
    public BrCipherJsonUtils() {
    }

    /**
     * 加密
     *
     * @param
     * @return
     */
    public static String cipherEncodeJsonData(String jsonData, String path) {
        return cipherJsonData(jsonData, path, Boolean.TRUE);
    }

    /**
     * 加密
     *
     * @param jsonData
     * @param path
     * @param keyArr
     * @return
     */
    public static String cipherEncodeJsonDataArr(String jsonData, String path, String keyArr) {
        return cipherJsonDataArr(jsonData, path, Boolean.TRUE, keyArr);
    }

    /**
     * 解密
     *
     * @param
     * @return
     */
    public static String cipherDecodeJsonData(String jsonData, String path) {
        return cipherJsonData(jsonData, path, Boolean.FALSE);
    }

    /**
     * 加解密
     *
     * @param jsonData
     * @param path
     * @param flag     true加密、false解密
     * @param keyArr
     * @return
     */
    private static String cipherJsonDataArr(String jsonData, String path, Boolean flag, String keyArr) {
        if (StringUtils.isNotBlank(path) && StringUtils.startsWith(jsonData, "{")) {
            if (StringUtils.isNotBlank(jsonData)) {
                try {
                    JSONObject parseObject = JSON.parseObject(jsonData);
                    JSONArray jsonArray = parseObject.getJSONArray(keyArr);
                    if (jsonArray != null && !jsonArray.isEmpty()) {
                        for (Object obj : jsonArray) {
                            JSONObject jsonDataObj = (JSONObject) obj;
                            String[] tagPaths = path.split(",");
                            for (int i = 0; i < tagPaths.length; i++) {
                                String tagPath = tagPaths[i];
                                Object eval = JSONPath.eval(jsonDataObj, tagPath);
                                if (eval != null) {
                                    if (eval instanceof String) {
                                        StringBuffer sb = new StringBuffer();
                                        String[] values = eval.toString().split(",");
                                        for (String key : values) {
                                            if (flag) {
                                                sb.append(BrCipherMaker.getInstance().encode(key)).append(",");
                                            } else {
                                                sb.append(BrCipherMaker.getInstance().decode(key)).append(",");
                                            }
                                        }
                                        if (sb.length() > 0) {
                                            JSONPath.set(jsonDataObj, tagPath, sb.substring(0, sb.length() - 1));
                                        }

                                    } else if (eval instanceof JSONArray) {
                                        JSONArray valueArr = new JSONArray();
                                        JSONArray values = (JSONArray) eval;
                                        for (Object key : values) {
                                            if (flag) {
                                                valueArr.add(BrCipherMaker.getInstance().encode(key.toString()));
                                            } else {
                                                valueArr.add(BrCipherMaker.getInstance().decode(key.toString()));
                                            }
                                        }
                                        if (!valueArr.isEmpty()) {
                                            JSONPath.set(jsonDataObj, tagPath, valueArr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return parseObject.toJSONString();
                } catch (Exception e) {
                    log.error("特殊加密jsonData异常", e);
                }
            }
            return null;
        } else {
            return jsonData;
        }
    }

    /**
     * 加解密
     *
     * @param jsonData
     * @param path
     * @param flag     true加密、false解密
     * @return
     */
    private static String cipherJsonData(String jsonData, String path, Boolean flag) {
        if (StringUtils.isNotBlank(path) && StringUtils.startsWith(jsonData, "{")) {
            if (StringUtils.isNotBlank(jsonData)) {
                try {
                    JSONObject jsonDataObj = JSON.parseObject(jsonData);
                    String[] tagPaths = path.split(",");
                    for (int i = 0; i < tagPaths.length; i++) {
                        String tagPath = tagPaths[i];
                        Object eval = JSONPath.eval(jsonDataObj, tagPath);
                        if (eval != null) {
                            if (eval instanceof String) {
                                StringBuffer sb = new StringBuffer();
                                String[] values = eval.toString().split(",");
                                for (String key : values) {
                                    if (flag) {
                                        sb.append(BrCipherMaker.getInstance().encode(key)).append(",");
                                    } else {
                                        sb.append(BrCipherMaker.getInstance().decode(key)).append(",");
                                    }
                                }
                                if (sb.length() > 0) {
                                    JSONPath.set(jsonDataObj, tagPath, sb.substring(0, sb.length() - 1));
                                }

                            } else if (eval instanceof JSONArray) {
                                JSONArray valueArr = new JSONArray();
                                JSONArray values = (JSONArray) eval;
                                for (Object key : values) {
                                    if (flag) {
                                        valueArr.add(BrCipherMaker.getInstance().encode(key.toString()));
                                    } else {
                                        valueArr.add(BrCipherMaker.getInstance().decode(key.toString()));
                                    }
                                }
                                if (!valueArr.isEmpty()) {
                                    JSONPath.set(jsonDataObj, tagPath, valueArr);
                                }
                            }
                        }
                    }
                    return jsonDataObj.toJSONString();
                } catch (Exception e) {
                    log.error("特殊加密jsonData异常", e);
                }
            }
            return null;
        } else {
            return jsonData;
        }
    }
}
