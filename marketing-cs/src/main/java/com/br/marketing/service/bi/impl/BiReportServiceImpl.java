package com.br.marketing.service.bi.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bi.BiReportConverterSelector;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.BiReportMapper;
import com.br.marketing.mapper.SourceStatisticDictMapper;
import com.br.marketing.service.bi.BiReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.bi.BiReportConfigDictVO;
import com.br.marketing.vo.bi.BiReportTimeRangeVO;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportConfigDictParam;
import com.br.marketing.vo.bi.param.BiReportConfigParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BI报表相关Service实现
 * 
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Service
@Slf4j
public class BiReportServiceImpl implements BiReportService {

    @Resource
    private BiReportConverterSelector selector;
    @Resource
    private FastDfsClient fastDfsClient;

    @Resource
    private SourceStatisticDictMapper statisticDictMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private BiReportMapper biReportMapper;

    /**
     * 获取BI报表
     * 
     * @param param 参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<BiReportVO> getBiReport(BiReportParam param) {
        BiReportTypeEnum reportType = BiReportTypeEnum.getEnumByTypeName(param.getReportTypeName());
        // 根据报告名称未匹配到对应报告类型
        if (reportType == null) {
            return null;
        }
        List<?> data = selector.fetchData(param, reportType);
        JSONObject extend = selector.buildExtend(param, reportType);
        return selector.process(data, extend, reportType);
    }

    /**
     * 下载报表
     * 
     * @param params 参数
     * @param request 请求
     * @param response 响应
     * @return {@link String }
     * @throws Exception 例外
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public String downloadReport(List<BiReportDownLoadParam> params, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (CollectionUtil.isEmpty(params)) {
            throw new Exception("下载错误，未获取到报表下载数据！！！");
        }
        String fastDfsUrl;
        BiReportTypeEnum reportType = BiReportTypeEnum.getEnumByTypeName(params.get(0).getReportTypeName());
        // 根据报告名称未匹配到对应报告类型
        if (reportType == null) {
            return null;
        }
        String encodeFileName;
        String reportTaskName = params.get(0).getReportTaskName();
        if(StringUtils.isEmpty(reportTaskName)){
            // 报告名称
            encodeFileName = URLEncoder.encode(params.get(0).getReportName() + ".xlsx", StandardCharsets.UTF_8.toString());
        }else {
            // 任务名称
            encodeFileName = URLEncoder.encode(params.get(0).getReportTaskName() + ".xlsx", StandardCharsets.UTF_8.toString());
        }
        // try-with-resource 的方式关闭流
        try (ExcelWriter excelWriter = ExcelUtil.getWriter(true); ServletOutputStream out = response.getOutputStream()) {
            selector.exportData(excelWriter, params, reportType);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodeFileName);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            excelWriter.flush(out, true);
            fastDfsUrl = syncToFastDfs(excelWriter, encodeFileName);
        }
        return fastDfsUrl;
    }

    private String syncToFastDfs(ExcelWriter writer, String encodeFileName) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.flush(out);
        int fileSize = out.toByteArray().length;
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        return fastDfsClient.uploadFile(inputStream, (long)fileSize, encodeFileName);
    }

    @Override
    public List<BiReportConfigDictVO> getBiReportConfigDict(BiReportConfigDictParam param) {
        List<SourceStatisticDict> sourceStatisticDicts = statisticDictMapper.selectListbI_(param);
        return sourceStatisticDicts.stream().map((SourceStatisticDict t) -> {
            BiReportConfigDictVO vo = new BiReportConfigDictVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ApiResult<Boolean> saveBiReportConfigDict(BiReportConfigDictParam param) {
        SourceStatisticDict sourceStatisticDict = new SourceStatisticDict();
        sourceStatisticDict.setDictKey(param.getDictKey());
        sourceStatisticDict.setDictDesc(param.getDictDesc());
        sourceStatisticDict.setDictValue(param.getDictValue());
        sourceStatisticDict.setApiCode(param.getApiCode());
        sourceStatisticDict.setDictDesc(param.getDictDesc());
        sourceStatisticDict.setCreateTime(new Date());
        sourceStatisticDict.setUpdateTime(new Date());
        sourceStatisticDict.setIsDel(param.getIsDel());

        statisticDictMapper.insertbI_(sourceStatisticDict);
        return new ApiResult<Boolean>().success();
    }

    /**
     * 获取报告组列表
     *
     * @param param 参数
     * @return java.util.List<java.lang.String>
     * @author hedongshuo
     * @date 2024/9/18 10:24
     */
    @Override
    public List<String> getReportGroupList(BiReportConfigParam param) {
        String apiCode = param.getApiCode();
        String reportTypeName = param.getReportTypeName();
        String userType = param.getUserType();
        Assert.notNull(userType, "缺少必输字段-场景userType");
        HashMap<String, JSONObject> biReportGroupConfig = marketingCommonConfig.getBiReportGroupConfig();
        JSONObject groupConfig = biReportGroupConfig.get(apiCode);
        JSONObject userTypeConfig = groupConfig.getJSONObject(userType);
        JSONArray groups = userTypeConfig.getJSONArray(reportTypeName);
        return groups.toJavaList(String.class);
    }

    /**
     * 获取报告时间范围
     *
     * @param param 参数
     * @return com.br.marketing.vo.bi.BiReportTimeRangeVO
     * @author hedongshuo
     * @date 2024/9/18 19:47
     */
    @Override
    public BiReportTimeRangeVO getReportTimeRange(BiReportConfigParam param) {
        String apiCode = param.getApiCode();
        String userType = param.getUserType();
        String statisticDate = param.getStatisticDate();
        Assert.notNull(userType, "缺少必输字段-统计日期");
        return biReportMapper.getReportTimeRange(apiCode, userType, statisticDate);
    }
}
