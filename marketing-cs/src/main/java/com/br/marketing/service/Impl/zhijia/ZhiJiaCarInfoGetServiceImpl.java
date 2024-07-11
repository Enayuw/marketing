package com.br.marketing.service.Impl.zhijia;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ZhiJiaCarBrandInfoMapper;
import com.br.marketing.mapper.ZhiJiaCarSeriesInfoMapper;
import io.etcd.jetcd.shaded.javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 宜信基础数据实现类
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:39
 */
@Service
@Slf4j
public class ZhiJiaCarInfoGetServiceImpl implements ZhiJiaCarInfoGetService {

    @Resource
    ZhiJiaCarBrandInfoMapper zhiJiaCarBrandInfoMapper;

    @Resource
    ZhiJiaCarSeriesInfoMapper zhiJiaCarSeriesInfoMapper;


    @Override
    public Result<ZhiJiaCarSeriesInfo> getZhiJiaCarInfo(ZhiJiaClueBackData zhiJiaClueBackInfo) {
        ZhiJiaCarSeriesInfo data = new ZhiJiaCarSeriesInfo();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        String brandName = zhiJiaClueBackInfo.getBrandName();
        String seriesName = zhiJiaClueBackInfo.getSeriesName();
        // 查询品牌
        ZhiJiaCarBrandInfoExample zhiJiaCarBrandInfoExample = new ZhiJiaCarBrandInfoExample();
        zhiJiaCarBrandInfoExample.createCriteria().andAppletDateGreaterThanOrEqualTo(LocalDate.now().toString());
        List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos = zhiJiaCarBrandInfoMapper.selectByExample(zhiJiaCarBrandInfoExample);
        if (zhiJiaCarBrandInfos.isEmpty()) {
            // 今日配置表为空,报警，并启用原有配置表！
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(),"今日车品牌配置表为空!"));
            ZhiJiaCarBrandInfoExample zhiJiaCarBrandInfoExample1 = new ZhiJiaCarBrandInfoExample();
            zhiJiaCarBrandInfoExample.createCriteria().andAppletDateLessThanOrEqualTo(LocalDate.now().toString());
            List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos1 = zhiJiaCarBrandInfoMapper.selectByExample(zhiJiaCarBrandInfoExample1);
            zhiJiaCarBrandInfos.addAll(zhiJiaCarBrandInfos1);

        }
        Set<String> brandNameSet = zhiJiaCarBrandInfos.stream().map(ZhiJiaCarBrandInfo::getBrandName).collect(Collectors.toSet());

        if (brandNameSet.contains(brandName)) {
            for (ZhiJiaCarBrandInfo brandInfo : zhiJiaCarBrandInfos) {
                threadPool.submit(() -> {
                    Integer brandId = brandInfo.getBrandId();
                    String newBrandName = brandInfo.getNewBrandName();

                    // 精确匹配
                    if (preciseMatch(brandName, newBrandName) || preciseMatch(newBrandName, brandName)) {
                        Integer seriesId = getSeriesId(seriesName, brandId);
                        if (ObjectUtil.isEmpty(seriesId)) {
                            String msg = "品牌编号：" + brandId + ", 被匹配车系名：" + seriesName + " 匹配车辆品牌失败！";
                            return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
                        }
                        data.setBrandId(brandId);
                        data.setSeriesId(seriesId);
                        return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
                    }

                    // 模糊匹配
                    if (complexFuzzyMatch(brandName, newBrandName) || complexFuzzyMatch(newBrandName, brandName)) {
                        Integer seriesId = getSeriesId(seriesName, brandId);
                        if (ObjectUtil.isEmpty(seriesId)) {
                            String msg = "品牌编号：" + brandId + ", 被匹配车系名：" + seriesName + " 匹配车辆品牌失败！";
                            return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
                        }
                        data.setBrandId(brandId);
                        data.setSeriesId(seriesId);
                        return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
                    }

                    // 补充配置匹配
                    String brandExtend = brandInfo.getBrandExtend();
                    List<String> brandList = Arrays.asList(brandExtend.split(","));
                    if (brandList.contains(brandName)) {
                        Integer seriesId = getSeriesId(seriesName, brandId);
                        if (ObjectUtil.isEmpty(seriesId)) {
                            String msg = "品牌编号：" + brandId + ", 被匹配车系名：" + seriesName + " 匹配车辆品牌失败！";
                            return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
                        }
                        data.setBrandId(brandId);
                        data.setSeriesId(seriesId);
                        return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
                    }
                    // 未匹配成功
                    String msg = "匹配车辆品牌失败！配置表中无这个车辆品牌，品牌名称：" + brandName;
                    return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
                });
            }
            threadPool.shutdown();
        }
        String msg = "匹配车辆品牌失败！配置表中无这个车辆品牌，品牌名称：" + brandName;
        return new Result<ZhiJiaCarSeriesInfo>().setCode(ResultCode.FAIL.getValue()).setMessage(msg);
    }


    /**
     * 获取车系id
     * @author guangxiu.li
     * @date 2024/7/10 19:57
     * @param seriesName
     * @param brandId
     * @return int
     */
    public Integer getSeriesId(String seriesName, Integer brandId) {
        Integer id = null;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50, 1);
        ZhiJiaCarSeriesInfoExample zhiJiaCarSeriesInfoExample = new ZhiJiaCarSeriesInfoExample();
        zhiJiaCarSeriesInfoExample.createCriteria()
                .andAppletDateGreaterThanOrEqualTo(LocalDate.now().toString())
                .andBrandIdEqualTo(brandId);
        List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos = zhiJiaCarSeriesInfoMapper.selectByExample(zhiJiaCarSeriesInfoExample);
        if (zhiJiaCarSeriesInfos.isEmpty()){
            // 今日车系配置表为空，报警，并启用原有配置表！
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode(),"今日车系配置表为空!"));
            ZhiJiaCarSeriesInfoExample zhiJiaCarSeriesInfoExample1 = new ZhiJiaCarSeriesInfoExample();
            zhiJiaCarSeriesInfoExample1.createCriteria()
                    .andBrandIdEqualTo(brandId);
            List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos1 = zhiJiaCarSeriesInfoMapper.selectByExample(zhiJiaCarSeriesInfoExample1);
            zhiJiaCarSeriesInfos.addAll(zhiJiaCarSeriesInfos1);
        }

        for (ZhiJiaCarSeriesInfo zhiJiaCarSeriesInfo : zhiJiaCarSeriesInfos) {
            threadPool.submit(() -> {
                String newSeriesName = zhiJiaCarSeriesInfo.getNewSeriesName();

                // 精确匹配
                if (preciseMatch(seriesName, newSeriesName) || preciseMatch(newSeriesName, seriesName)) {
                    return zhiJiaCarSeriesInfo.getSeriesId();
                }

                // 模糊匹配
                if (complexCarSeriesFuzzyMatch(seriesName, newSeriesName) || complexCarSeriesFuzzyMatch(newSeriesName, seriesName)) {
                    return zhiJiaCarSeriesInfo.getSeriesId();
                }

                // 扩展车系匹配
                String seriesExtend = zhiJiaCarSeriesInfo.getSeriesExtend();
                List<String> seriesList = Arrays.asList(seriesExtend.split(","));
                if (seriesList.contains(seriesName)) {
                    return zhiJiaCarSeriesInfo.getSeriesId();
                }

                return null; // 未匹配成功
            });
        }
        threadPool.shutdown();
        return id;
    }
    /**
     * 精准匹配两个字符串，返回匹配结果
     * @author guangxiu.li
     * @date 2024/7/9 17:46
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等则返回true，否则返回false
     */
    public static boolean preciseMatch(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 车品牌模糊匹配，返回布尔结果
     *
     * @param brand 车品牌名称
     * @param pattern 匹配模式
     * @return 如果brand匹配pattern则返回true，否则返回false
     */
    public static boolean complexFuzzyMatch(String brand, String pattern) {
        if (brand == null || pattern == null) {
            return false;
        }

        // 忽略大小写的匹配
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(brand);

        return matcher.find();
    }

    /**
     * 车系模糊匹配，返回布尔结果
     *
     * @param carSeries 车系名称
     * @param pattern 匹配模式
     * @return 如果carSeries匹配pattern则返回true，否则返回false
     */
    public static boolean complexCarSeriesFuzzyMatch(String carSeries, String pattern) {
        if (carSeries == null || pattern == null) {
            return false;
        }

        // 忽略大小写并去除非字母数字字符进行匹配
        String normalizedCarSeries = carSeries.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String normalizedPattern = pattern.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // 编译正则表达式
        Pattern compiledPattern = Pattern.compile(normalizedPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(normalizedCarSeries);

        return matcher.find();
    }
}
