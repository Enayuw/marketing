package com.br.marketing.service.bi.impl;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bi.BiReportConverterSelector;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.SourceStatisticDictMapper;
import com.br.marketing.service.bi.BiReportService;
import com.br.marketing.vo.bi.BiReportConfigDictVO;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportConfigDIctParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    /**
     * 获取BI报表
     * 
     * @param param 参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public BiReportVO getBiReport(BiReportParam param) {
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
     * @param param 参数
     * @param request 请求
     * @param response 响应
     * @return {@link String }
     * @throws Exception 例外
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public String downloadReport(BiReportDownLoadParam param, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fastDfsUrl;
        BiReportTypeEnum reportType = BiReportTypeEnum.getEnumByTypeName(param.getReportTypeName());
        // 根据报告名称未匹配到对应报告类型
        if (reportType == null) {
            return null;
        }
        // 设置下载协议头，防止中文乱码做URLEncoder处理
        String encodeFileName = URLEncoder.encode(param.getReportName() + ".xlsx", StandardCharsets.UTF_8.toString());
        // try-with-resource 的方式关闭流
        try (ExcelWriter excelWriter = ExcelUtil.getWriter(true); ServletOutputStream out = response.getOutputStream()) {
            selector.exportData(excelWriter, param, reportType);
            // 自适应宽度
            excelWriter.autoSizeColumnAll();
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
    public List<BiReportConfigDictVO> getBiReportConfigDict(BiReportConfigDIctParam param) {
        List<SourceStatisticDict> sourceStatisticDicts = statisticDictMapper.selectListbI_(param);
        return sourceStatisticDicts.stream().map((SourceStatisticDict t) -> {
            BiReportConfigDictVO vo = new BiReportConfigDictVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ApiResult<Boolean> saveBiReportConfigDict(BiReportConfigDIctParam param) {
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
}
