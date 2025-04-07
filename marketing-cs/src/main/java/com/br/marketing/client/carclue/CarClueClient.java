package com.br.marketing.client.carclue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.carclue.dto.HxClueCommitDTO;
import com.br.marketing.client.carclue.vo.ClueDetailVO;
import com.br.marketing.client.carclue.vo.ClueResVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.ApiRecordLogDTO;
import com.br.marketing.enums.ApiNmEnum;
import com.br.marketing.service.IInterfaceLogService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class CarClueClient {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Value(value = "${api.hxCar.cityList:'http://haoyunlailai.cn:6001/open_api/v1/city_list'}")
    private String cityList;

    @Value(value = "${api.hxCar.carList:'http://haoyunlailai.cn:6001/open_api/v1/car_list'}")
    private String carList;

    @Value(value = "${api.hxCar.commitClue:'http://haoyunlailai.cn:6001/open_api/v1/test_commit_clue'}")
    private String commitClue;

    @Value("${api.hxCar.isProxy:true}")
    private Boolean isProxy;

    @Resource
    IInterfaceLogService iInterfaceLogService;

    @Resource
    private HttpProxyClient httpProxyClient;

    /**
     * 线索上报接口
     *
     * @param dto
     * @return
     */
    public Result<String> commitClue(HxClueCommitDTO dto) {

        //获取挡板开关
        Map<String, Object> mock = marketingCommonConfig.getCommitClueMock();
        if (mock.get("switch") == Boolean.TRUE) {
            log.warn("线索上报进入挡板,入参：{}",JSONObject.toJSONString(dto));
            long start = System.currentTimeMillis();
            Result<String> stringResult = commitClueMock(mock);
            long end = System.currentTimeMillis();
            log.warn("线索上报结束挡板, result:{}, 耗时:{}", stringResult, end - start);
            return stringResult;
        }

        try {
            JSONObject jo = marketingCommonConfig.getHxClientConfig();
            String channelId = jo.getString("channelId");
            String channelKey = jo.getString("channelKey");
            dto.setChannelId(channelId);

            BeanMap beanMap = BeanMap.create(dto);
            String sign = generateSign(beanMap, channelKey);
            dto.setSign(sign);
            ApiRecordLogDTO record = iInterfaceLogService.isRecord(ApiNmEnum.CARCLUECOMMIT);
            HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(dto
                    , commitClue
                    , isProxy
                    , MediaType.APPLICATION_JSON_UTF8_VALUE
                    , ""
                    , record.getIsDbLog()
                    , record.getIsFileLog());
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HX_CAR_CLUE_INTERFACE.getCode(),
                        String.format("请求参数:%s,返回:%s", JSON.toJSONString(dto), JSON.toJSONString(resMap)), "调用海星车线索【线索提交】接口异常"));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
            String content = resMap.get("content");
            ClueResVO<ClueDetailVO> resVO = JSON.parseObject(content, new TypeReference<ClueResVO<ClueDetailVO>>() {
            }.getType());
            if (Integer.valueOf("20000").equals(resVO.getCode())) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resVO.getData().getClueId());
            }

            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(JSON.toJSONString(resMap));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 挡板
     * @return
     */
    private Result<String> commitClueMock(Map<String, Object> mock) {
        Result<String> result = new Result<>();
        Integer code = (Integer) mock.get("code");
        if(ResultCode.SUCCESS.getValue().equals(code)){
            Random random = new Random();
            int number = 10000 + random.nextInt(10000); // 生成一个5位数
            result.setDate(String.valueOf(number));
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("请求成功");
            return result;
        }
        result.setCode(ResultCode.FAIL.getValue());
        result.setMessage("请求失败");
        return result;
    }

    /**
     * 获取之家城市
     *
     * @return {
     * "code": 1,
     * "data": [
     * {
     * "nodes": [
     * {
     * "name": "石家庄",
     * "id": 37
     * },
     * {
     * "name": "廊坊",
     * "id": 61
     * },
     * {
     * "name": "衡水",
     * "id": 72
     * },
     * {
     * "name": "唐山",
     * "id": 84
     * },
     * {
     * "name": "秦皇岛",
     * "id": 99
     * },
     * {
     * "name": "邯郸",
     * "id": 107
     * },
     * {
     * "name": "邢台",
     * "id": 127
     * },
     * {
     * "name": "保定",
     * "id": 147
     * },
     * {
     * "name": "张家口",
     * "id": 173
     * },
     * {
     * "name": "承德",
     * "id": 191
     * },
     * {
     * "name": "沧州",
     * "id": 203
     * }
     * ],
     * "name": "河北",
     * "id": 36
     * }
     * ]
     * }
     */
    public Result<JSONArray> getZjCity() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String task = jo.getString("zjTask");
        return getCity( task);
    }

    /**
     * @return 格式
     * {
     * "code": 1,
     * "data": [
     * {
     * "cityName": "福州",
     * "provinceName": "福建省",
     * "cityId": 301,
     * "provinceId": 350000
     * }]
     * }
     */
    public Result<JSONArray> getYcCity(String task) {
        return getCity(task);
    }

    private Result<JSONArray> getCity( String task) {

        try {
            JSONObject jo = marketingCommonConfig.getHxClientConfig();
            String channelId = jo.getString("channelId");
            String channelKey = jo.getString("channelKey");
            Map<String, Object> data = new HashMap<>();
            data.put("channel_id", channelId);
            data.put("task", task);

            String sign = generateSign(data, channelKey);
            data.put("sign", sign);
            String param = param(data);
            String reqUrl = String.format("%s?%s", cityList, param);
            HashMap<String, String> resMap = httpProxyClient.get(reqUrl, isProxy, null);
            // 请求异常
            if (!"200".equals(resMap.get("httpcode"))
                    || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HX_CAR_CLUE_INTERFACE.getCode(),
                        String.format("请求参数:%s,返回:%s", JSON.toJSONString(data), JSON.toJSONString(resMap))
                        , "调用海星车线索【城市配置】接口异常"));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }

            String content = resMap.get("content");
            JSONObject resJo = JSON.parseObject(content);
            if (Integer.valueOf("20000").equals(resJo.getInteger("code"))) {
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(resJo.getJSONArray("data"));
            }
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(JSON.toJSONString(resMap));
        } catch (Exception ex) {
            return new Result<>()
                    .setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 获取之家车型
     *
     * @return {
     * "code": 1,
     * "data": [
     * {
     * "son_brand_id": 779,
     * "series_name": "奥迪Q7",
     * "brand_name": "奥迪",
     * "son_brand_name": "奥迪进口",
     * "brand_id": 565,
     * "series_id": 2542
     * }
     * ]
     * }
     */
    public Result<JSONArray> getZjCar() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String zjTask = jo.getString("zjTask");
        return getCar(zjTask);
    }

    /**
     * 获取易车车型
     *
     * @return {
     * "code": 1,
     * "data": [
     * {
     * "brandName": "辅恒汽车",
     * "seriesName": "景飞牌雅典纳",
     * "brandId": 767,
     * "seriesId": 10513
     * }
     * ]
     * }
     */
    public Result<JSONArray> getYcCar(String task) {
        return getCar(task);
    }

    private Result<JSONArray> getCar(String task) {

        try {
            JSONObject jo = marketingCommonConfig.getHxClientConfig();
            String channelId = jo.getString("channelId");
            String channelKey = jo.getString("channelKey");
            Map<String, Object> data = new HashMap<>();
            data.put("channel_id", channelId);
            data.put("task", task);
            data.put("page", "1");
            data.put("limit", "10000");

            String sign = generateSign(data, channelKey);
            data.put("sign", sign);
            String param = param(data);
            String reqUrl = String.format("%s?%s", carList, param);
            HashMap<String, String> resMap = httpProxyClient.get(reqUrl, isProxy, null);
            // 请求异常
            if (!"200".equals(resMap.get("httpcode"))
                    || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HX_CAR_CLUE_INTERFACE.getCode(),
                        String.format("请求参数:%s,返回:%s", JSON.toJSONString(data), JSON.toJSONString(resMap))
                        , "调用海星车线索【车系配置】接口异常"));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }

            String content = resMap.get("content");
            JSONObject resJo = JSON.parseObject(content);
            if (Integer.valueOf("20000").equals(resJo.getInteger("code"))) {
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(resJo.getJSONArray("data"));
            }
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(JSON.toJSONString(resMap));
        } catch (Exception ex) {
            return new Result<>()
                    .setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }


    private static String param(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key)
                    .append("=")
                    .append("task".equals(key)
                            ? URLEncoder.encode(String.valueOf(data.get(key)))
                            : data.get(key));
        }
        return sb.toString();
    }


    public static String generateSign(Map<String, Object> data, String channelKey) {
        // 1. 过滤掉空值和 sign 字段
        Map<String, Object> filteredData = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null && !ObjectUtils.isEmpty(entry.getValue()) && !"sign".equalsIgnoreCase(entry.getKey())) {
                filteredData.put(entry.getKey().toUpperCase(), entry.getValue()); // 转大写
            }
        }

        // 2. 按 ASCII 从小到大排序（字典序）
        List<String> keys = new ArrayList<>(filteredData.keySet());
        Collections.sort(keys);

        // 3. 使用 URL 键值对格式拼接成字符串 A
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(filteredData.get(key));
        }

        // 4. 拼接 channelKey
        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append("key=").append(channelKey);
        String signTemp = sb.toString();

        // 5. 对 signTemp 进行 MD5 运算并转换为大写
        return DigestUtils.md5DigestAsHex(signTemp.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

}
