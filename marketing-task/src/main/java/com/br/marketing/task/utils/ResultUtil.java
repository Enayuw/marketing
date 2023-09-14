package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.RegexConstants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Bairong on 2019/8/21.
 * <p>
 * 结果处理工具类，生成结果文件
 */
@Slf4j
public class ResultUtil {


    private MarketingTaskService marketingTaskService;


    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw, String sep, Map<String, String> proFieldMap,
                                    MarketingUser user, JSONObject meal, String cusBatchNumber, String fileId, String pushCustomer,
                                    String baseHeadInfo, StrategyProductDetailVO fieldInfo) throws IOException {
        log.info("cus_num：{} 画像流水:{}", user.getCusNum(), resultJson);
        JSONObject esResult = new JSONObject();
        StringBuilder sb = new StringBuilder();
        MarketingHistory mh = new MarketingHistory();
        /**
         * 添加基本信息
         */Date requestTime = new Date();
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(requestTime)).append(sep)
                .append(user.getBatchNumber()).append(sep)
                .append(user.getCusNum()).append(sep)
                .append(strategyId).append(sep).append(sep);
        mh.setRequestTime(requestTime);
        mh.setBatchNumber(user.getBatchNumber());
        mh.setCusNum(user.getCusNum());
        mh.setApiCode(user.getApiCode());
        mh.setStrategyId(strategyId);
        mh.setVersion("");
        if (StringUtils.isNotBlank(baseHeadInfo)) {
            JSONObject jsonObject = null;
            if (StringUtils.isNotBlank(user.getExtendJson())) {
                try {
                    jsonObject = JSONObject.parseObject(user.getExtendJson());
                } catch (Exception ex) {
                    log.error("跑分扩展信息解析有误 apiCode:{},id:{}", user.getApiCode(), user.getId());
                }
            }
            BaseHeadConfigVO o = JSON.parseObject(baseHeadInfo, new TypeReference<BaseHeadConfigVO>() {
            }.getType());
//                Map<String, Integer> headMap = o.getBaseHead().stream().collect(Collectors.toMap(BaseHead::getName, BaseHead::getType));
            for (String s : o.getShowBaseHead()) {
                if (jsonObject != null) {
                    String ss = jsonObject.getString(s);
                    if (StringUtils.isNotBlank(ss)) {
                        sb.append(ss);
                    }
                    String title = s.toLowerCase();
                    if ("taskid".equals(title)) {
                        user.setTaskId(StringUtils.isBlank(ss) ? "" : ss);
                    } else if ("usertype".equals(title)) {
                        user.setUserType(StringUtils.isBlank(ss) ? "" : ss);
                    } else if ("custnum".equals(title)) {
                        user.setCusNum(StringUtils.isBlank(ss) ? "" : ss);
                    } else if ("idcard".equals(title)) {
                        user.setIdCard(StringUtils.isBlank(ss) ? "" : ss);
                    } else if ("name".equals(title)) {
                        user.setName(StringUtils.isBlank(ss) ? "" : ss);
                    } else if ("cell".equals(title)) {

                    } else {
                        esResult.put(s, StringUtils.isBlank(ss) ? "" : ss);
                    }
                }
                sb.append(sep);
            }
        }
        Set<String> products = new HashSet<String>();
        for (String pro : proFieldMap.keySet()) {
            products.add(pro.toLowerCase());
        }
        log.info("batch_number:{} products:{}", user.getBatchNumber(), products);

        buildResult(resultJson, sb, sep, esResult, fieldInfo);

        if (log.isInfoEnabled()) {
            log.info("sb信息--" + sb.toString());
        }
        fw.append(sb + "\r\n");
        if ("1".equals(pushCustomer)) {
            mh.setIdCard(user.getIdCard());
            mh.setName(user.getName());
            mh.setCell(user.getCell());
            mh.setCusBatchNumber(cusBatchNumber);
            mh.setBatchNumber(user.getBatchNumber());
            mh.setFileId(fileId);
            mh.setTaskId(user.getTaskId());
            mh.setUserType(user.getUserType());
            mh.setHxSwiftNumber(StringUtils.isNotBlank(resultJson.getString("swift_number")) ? resultJson.getString("swift_number") : "");
            //region 写入condition
            HashMap<String, MarketingCondition> conditions = new HashMap<>();
            List<MarketingCondition> conditionList = new ArrayList<>();
            for (String product : meal.keySet()) {
                MarketingCondition marketingCondition = new MarketingCondition();
                marketingCondition.setCode(product);
                marketingCondition.setVersion(meal.getJSONObject(product).getString("version"));
                conditions.put(product.toLowerCase(), marketingCondition);
            }
            for (String s : esResult.keySet()) {
                MarketingCondition marketingCondition = conditions.get(s);
                if (marketingCondition != null) {
                    marketingCondition.setFlag(resultJson.get("flag_score") == null ? "" : resultJson.getString("flag_score"));
                    marketingCondition.setFieldKey(s);
                    marketingCondition.setDValue(StringUtils.isBlank(esResult.getString(s)) ? 0 : Double.valueOf(esResult.getString(s)));
                    marketingCondition.setStrValue("");
                    conditionList.add(marketingCondition);
                } else {
                    MarketingCondition marketingConditionStr = new MarketingCondition();
                    marketingConditionStr.setFieldKey(s);
                    marketingConditionStr.setStrValue(esResult.getString(s));
                    conditionList.add(marketingConditionStr);
                }
            }
            mh.setCondition(conditionList);
            mh.setReserveField(esResult.toJSONString());
            //endregion
            String id = UuidUtils.getUuid();
            MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
            service.insert(mh, id);
        }
    }

    /**
     * 跑分 优化后的文件写入
     *
     * @param resultJson
     * @param strategyId
     * @param fw
     * @param sep
     * @param proFieldMap
     * @param user
     * @param meal
     * @param cusBatchNumber
     * @param fileId
     * @param pushCustomer
     * @param baseHeadInfo
     * @param fieldInfo
     * @param marketingTask
     * @throws IOException
     */
    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw, String sep, Map<String, String> proFieldMap,
                                    MarketingSyncUser user, JSONObject meal, String cusBatchNumber, String fileId, String pushCustomer,
                                    BaseHeadConfigVO baseHeadInfo, StrategyProductDetailVO fieldInfo, MarketingTask marketingTask
            , MarketingTaskService marketingTaskService,String part) throws IOException {
        log.info("cus_num：{} 画像流水:{}", user.getCustNum(), resultJson);
        JSONObject esResult = new JSONObject();
        StringBuilder sb = new StringBuilder();
        MarketingHistory mh = new MarketingHistory();
        //region 基本信息
        Date requestTime = new Date();
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(requestTime)).append(sep)
                .append(marketingTask.getBatchNumber()).append(sep)
                .append(user.getCustNum()).append(sep)
                .append(strategyId).append(sep).append(sep);
        mh.setRequestTime(requestTime);
        mh.setBatchNumber(marketingTask.getBatchNumber());
        mh.setCusNum(user.getCustNum());
        mh.setApiCode(user.getApiCode());
        mh.setStrategyId(strategyId);
        mh.setVersion("");
        mh.setPart(part);
        mh.setScoreTime(System.currentTimeMillis());
        //endregion

        //客户上传字段处理
        getCustomerHead(user, baseHeadInfo, sb, mh, esResult, sep);

        Set<String> products = new HashSet<String>();
        for (String pro : proFieldMap.keySet()) {
            products.add(pro.toLowerCase());
        }
        log.info("batch_number:{} products:{}", marketingTask.getBatchNumber(), products);
        if (resultJson != null) {
            buildResult(resultJson, sb, sep, esResult, fieldInfo);
            mh.setHxSwiftNumber(StringUtils.isNotBlank(resultJson.getString("swift_number")) ? resultJson.getString("swift_number") : "");
        }
        if (log.isInfoEnabled()) {
            log.info("sb信息--" + sb.toString());
        }
        fw.append(sb + "\r\n");
        boolean isVer = marketingTask.getMonitorType() == 2;
        boolean isOffLine = marketingTask.getIsOnline() == 2;
        if ("1".equals(pushCustomer) && !isVer && !isOffLine) {
            mh.setCusBatchNumber(cusBatchNumber);
            mh.setBatchNumber(marketingTask.getBatchNumber());
            mh.setFileId(fileId);
            //region 产品模型，扩展字段存入condition
            HashMap<String, MarketingCondition> conditions = new HashMap<>();
            List<MarketingCondition> conditionList = new ArrayList<>();
            for (String product : meal.keySet()) {
                MarketingCondition marketingCondition = new MarketingCondition();
                marketingCondition.setCode(product);
                marketingCondition.setVersion(meal.getJSONObject(product).getString("version"));
                conditions.put(product.toLowerCase(), marketingCondition);
            }
            for (String s : esResult.keySet()) {
                MarketingCondition marketingCondition = conditions.get(s);
                if (marketingCondition != null) {
                    marketingCondition.setFlag(resultJson.get("flag_score") == null ? "" : resultJson.getString("flag_score"));
                    marketingCondition.setFieldKey(s);
                    String strValue =  esResult.getString(s);
                    if (StringUtils.isNotBlank(strValue)) {
                        if(Pattern.compile(RegexConstants.Numeric).matcher(strValue).matches()){
                            marketingCondition.setDValue(Double.valueOf(esResult.getString(s)));
                        }
                        Long date = DateHelper.strToMill(strValue);
                        if(date !=null){
                            marketingCondition.setLValue(date);
                        }
                    }
//                    marketingCondition.setDValue(StringUtils.isBlank(esResult.getString(s)) ? 0 : Double.valueOf(esResult.getString(s)));
                    marketingCondition.setStrValue(strValue);
                    conditionList.add(marketingCondition);
                } else {
                    String strValue = esResult.getString(s);
                    MarketingCondition marketingConditionStr = new MarketingCondition();
                    marketingConditionStr.setFieldKey(s);
                    marketingConditionStr.setStrValue(esResult.getString(s));
                    if (StringUtils.isNotBlank(strValue)) {
                        if(Pattern.compile(RegexConstants.Numeric).matcher(strValue).matches()){
                            marketingConditionStr.setDValue(Double.valueOf(esResult.getString(s)));
                        }
                        Long date = DateHelper.strToMill(strValue);
                        if(date !=null){
                            marketingConditionStr.setLValue(date);
                        }

                    }
                    conditionList.add(marketingConditionStr);
                }
            }
            mh.setCondition(conditionList);
            mh.setReserveField(esResult.toJSONString());
            //endregion
            String id = UuidUtils.getUuid();
            MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
            service.insert(mh, id);
        }
        if (isVer) {
            MarketingTaskResultPreview preview = new MarketingTaskResultPreview();
            preview.setApiCode(marketingTask.getApiCode());
            preview.setTaskId(marketingTask.getId());
            preview.setFileId(Long.valueOf(fileId));
            preview.setBatchNumber(marketingTask.getBatchNumber());
            preview.setContent(sb.toString().substring(0, sb.toString().length() - 1));
            preview.setIsTitle(0);
            marketingTaskService.saveScoreResult(preview);
        }
    }


    private static Result buildResult(JSONObject hxJson, StringBuilder sb, String sep, JSONObject esResult, StrategyProductDetailVO fieldInfo) {
        StringBuilder result = new StringBuilder();
        if (fieldInfo == null) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        for (int i = 0; i < fieldInfo.getFields().size(); i++) {
            String field = fieldInfo.getFields().get(i);
            String fieldRes = hxJson.getString(field);
            result.append(StringUtils.isNotBlank(fieldRes) ? fieldRes : "").append(sep);
            if (esResult != null) {
                esResult.put(field, StringUtils.isNotBlank(fieldRes) ? fieldRes : "");
            }
        }
        sb.append(result);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private static String encrypt3k(Integer type, String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        String decode = BrCipherMaker.getInstance().decode(content);
        if (StringUtils.isBlank(decode)) {
            return content;
        }
        if (ScoreThreeKeyEncryptEnum.md5.getValue().equals(type)) {
            return DigestUtils.md5DigestAsHex(decode.getBytes());
        }

        if (ScoreThreeKeyEncryptEnum.sha256.getValue().equals(type)) {
            return Sha256Util.getSHA256Encrypt(decode);
        }
        return content;
    }

    private static void getCustomerHead(MarketingSyncUser syncUser
            , BaseHeadConfigVO baseHeadConfigVO, StringBuilder sb, MarketingHistory mh, JSONObject conditionObj, String sep) {

        Integer ia = 0, ib = 1, ic = 2;
        if (baseHeadConfigVO != null) {
            JSONObject icData = null;

            //region 客户上传字段信息解读

            if (StringUtils.isNotBlank(syncUser.getReserveField1())) {
                try {
                    icData = JSON.parseObject(syncUser.getReserveField1());
                } catch (Exception ex) {
                    log.error("用户上传数据非法的扩展信息：apiCode:{},id:{}"
                            , syncUser.getApiCode(), syncUser.getId());
                }
            }
            for (String s : baseHeadConfigVO.getShowBaseHead()) {
                BaseHead head = new BaseHead().setType(ia);
                head.setName(s);
                Optional<BaseHead> first = baseHeadConfigVO.getBaseHead().stream().filter(t -> t.getName().equals(s)).findFirst();
                if (first.isPresent()) {
                    head = first.get();
                }
                String title = s.toLowerCase();
                String str = "";
                String strCell = "";
                String strId = "";
                String strNm = "";

                //region 遍历配置
                if (ia.equals(head.getType())) {
                    str = "";
                } else if (ib.equals(head.getType())) {
                    switch (title) {
                        case "apicode":
                            str = syncUser.getApiCode();
                            break;
                        case "cusbatch":
                            str = syncUser.getCusBatch();
                            break;
                        case "taskid":
                            str = syncUser.getCusBatch();
                            break;
                        case "requestbatch":
                            str = syncUser.getRequestBatch();
                            break;
                        case "requestid":
                            str = syncUser.getRequestBatch();
                            break;
                        case "custnum":
                            str = syncUser.getCustNum();
                            break;
                        case "idcard":
                            str = encrypt3k(head.getThreekEncryptType(), syncUser.getIdCard());
                            strId = syncUser.getIdCard();
                            break;
                        case "id":
                            str = encrypt3k(head.getThreekEncryptType(), syncUser.getIdCard());
                            strId = syncUser.getIdCard();
                            break;
                        case "cell":
                            str = encrypt3k(head.getThreekEncryptType(), syncUser.getCell());
                            strCell = syncUser.getCell();
                            break;
                        case "name":
                            str = encrypt3k(head.getThreekEncryptType(), syncUser.getName());
                            strNm = syncUser.getName();
                            break;
                        case "grouptype":
                            str = syncUser.getGroupType();
                            break;
                        case "usertype":
                            str = syncUser.getUserType();
                            break;
                        case "registerdate":
                            str = syncUser.getRegisterDate();
                            break;
                        /* 2021-8-18 14:41:12
                         * 回传文件结果表头新增字段：
                         * createTime 基础字段
                         */
                        case "createtime": // 客户数据上传日期（精确到日）
                            str = syncUser.getAppletDate();
                            break;
                        default:
                            str = "";
                    }
                } else if (ic.equals(head.getType())) {
                    if (icData != null) {
                        str = icData.getString(head.getName());
                    }
                } else {
                    str = "";
                }
                //endregion

                //region 文件写入
                if (StringUtils.isNotBlank(str)) {
                    sb.append(str).append(sep);
                } else {
                    sb.append(sep);
                }
                //endregion

                //region es写入基本字段
                if (mh != null) {
                    if ("taskid".equals(title)) {
                        mh.setTaskId(StringUtils.isBlank(str) ? "" : str);
                    } else if ("usertype".equals(title)) {
                        mh.setUserType(StringUtils.isBlank(str) ? "" : str);
                    } else if ("custnum".equals(title)) {
                        mh.setCusNum(StringUtils.isBlank(str) ? "" : str);
                    } else if ("idcard".equals(title)) {
                        mh.setIdCard(StringUtils.isBlank(strId) ? "" : strId);
                    } else if ("id".equals(title)) {
                        mh.setIdCard(StringUtils.isBlank(strId) ? "" : strId);
                    } else if ("name".equals(title)) {
                        mh.setName(StringUtils.isBlank(strNm) ? "" : strNm);
                    } else if ("cell".equals(title)) {
                        mh.setCell(StringUtils.isBlank(strCell) ? "" : strCell);
                    } else {
                        conditionObj.put(head.getName(), StringUtils.isBlank(str) ? "" : str);
                    }
                }
                //endregion
            }

            //endregion
        }
    }
}
