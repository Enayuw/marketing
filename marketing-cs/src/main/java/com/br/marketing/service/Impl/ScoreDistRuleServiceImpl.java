package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.entity.ReportIntervalConfig;
import com.br.marketing.entity.ReportIntervalConfigExample;
import com.br.marketing.entity.ReportIntervalModel;
import com.br.marketing.mapper.ReportIntervalConfigMapper;
import com.br.marketing.mapper.ReportIntervalModelMapper;
import com.br.marketing.service.ScoreDistRuleService;
import com.br.marketing.vo.ScoreDistRuleVo;
import com.br.marketing.vo.bi.AxisWrapVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScoreDistRuleServiceImpl implements ScoreDistRuleService {

    private static final Integer PAGE_SIZE = 10;

    private static final String  AXIS_TYPE_SINGLE = "1";

    private static final String  AXIS_TYPE_CROSS = "2";

    private static final Integer  STATUS_ENABLED = 1;

    private static final Integer  STATUS_FORBIDDEN = 2;
    
    @Resource
    ReportIntervalConfigMapper reportIntervalConfigMapper;

    @Resource
    ReportIntervalModelMapper reportIntervalModelMapper;

    @Override
    public Result<PageResultReturn<ScoreDistRuleVo>> getScoreDistRuleList(SearchConditionDTO dto) {
        if (dto.getSize() == null) {
            dto.setSize(PAGE_SIZE);
        }
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<ScoreDistRuleVo> list = reportIntervalConfigMapper.getScoreDistRuleList(dto);
        PageResultReturn pageResultReturn = PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pageResultReturn);
    }

    @Override
    public Result<List<ScoreDistRuleVo>> getScoreDistRuleByApiCode(String apiCode) {
        ReportIntervalConfigExample configExample = new ReportIntervalConfigExample();
        configExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andApiCodeEqualTo(apiCode);
        List<ReportIntervalConfig> configs = reportIntervalConfigMapper.selectByExample(configExample);
        if (configs == null || configs.isEmpty()) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Collections.emptyList());
        }
        List<ScoreDistRuleVo> scoreDistRuleVos = configs.stream()
                .map(config -> {
                    ScoreDistRuleVo vo = new ScoreDistRuleVo();
                    BeanUtils.copyProperties(config, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(scoreDistRuleVos);
    }

    @Override
    public List<AxisWrapVO> getScoreDistRuleDetail(Long configId) {
        //1.model原始数据查询
        List<ReportIntervalModel> models = reportIntervalModelMapper.getByConfigId(configId);
        //2.构建数据
        List<AxisWrapVO> axisWrapVOS = Lists.newArrayList();
        for (ReportIntervalModel model : models) {
            AxisWrapVO axisWrapVO = new AxisWrapVO();
            axisWrapVOS.add(axisWrapVO);
            axisWrapVO.setReportScoreType(Integer.parseInt(model.getAxisType()));
            axisWrapVO.setXAxisProduct(model.getxModelName());
            axisWrapVO.setYAxisProduct(model.getyModelName());
            axisWrapVO.setXAxis(parseIntervals(model.getxIntervalList()));
            List<WrapDataVO> yAxis = Lists.newArrayList();
            ArrayList<String> slashList = new ArrayList<>(Collections.nCopies(axisWrapVO.getXAxis().size(), "/"));
            axisWrapVO.setYAxis(yAxis);
            if (AXIS_TYPE_SINGLE.equals(model.getAxisType())) {
                List<String> keys = Splitter.on(",").splitToList(model.getxModelName().trim());
                for (String yName : keys) {
                    yAxis.add(new WrapDataVO(yName, slashList));
                }
            } else if (AXIS_TYPE_CROSS.equals(model.getAxisType())) {
                List<String> yStep = parseIntervals(model.getyIntervalList());
                for (String yName : yStep) {
                    yAxis.add(new WrapDataVO(yName, slashList));
                }
            }
        }
        return axisWrapVOS;
    }

    /**
     * 规则模板禁用
     * @param configId
     * @return
     */
    @Override
    public Result forbScoreDistRule(Long configId) {
        configStatusUpd(configId, STATUS_FORBIDDEN);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 规则模板启用
     * @param configId
     * @return
     */
    @Override
    public Result enableScoreDistRule(Long configId) {
        configStatusUpd(configId, STATUS_ENABLED);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 规则模板删除
     * @param configId
     * @return
     */
    @Override
    public Result deleteScoreDistRule(Long configId) {
        ReportIntervalConfigExample configExample = new ReportIntervalConfigExample();
        configExample.createCriteria().andIdEqualTo(configId);
        ReportIntervalConfig config = new ReportIntervalConfig();
        config.setIsDel(Constants.DATA_DEL);
        reportIntervalConfigMapper.updateByExampleSelective(config, configExample);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    private void configStatusUpd(Long configId, Integer status) {
        ReportIntervalConfigExample configExample = new ReportIntervalConfigExample();
        configExample.createCriteria().andIdEqualTo(configId);
        ReportIntervalConfig config = new ReportIntervalConfig();
        config.setStatus(status);
        reportIntervalConfigMapper.updateByExampleSelective(config, configExample);
    }

    /**
     * “[-1,0), [0,949), [950,1000), [1000,1050), [1050,1100)”，拆成List<String>
     * “云南, 海南, 内蒙古, 广东, 四川, 青海, 西藏, 新疆, 山西, 宁夏, 福建, 重庆, 江苏, 陕西, 安徽, 浙江, 辽宁, 广西, 湖北, 甘肃, 河南, 河北, 贵州, 北京, 上海, 山东, 天津, 湖南”，也要实现拆分
     * @param intervalString
     * @return
     */
    private List<String> parseIntervals(String intervalString) {
        if (intervalString == null || intervalString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (StringUtils.containsChinese(intervalString)) {
            return Splitter.on(",").splitToList(intervalString.trim());
        }
        return Pattern.compile("(?<=[\\]\\)])\\s*,\\s*")
                .splitAsStream(intervalString.trim())
                .map(String::trim)
                .collect(Collectors.toList());
        }
}
