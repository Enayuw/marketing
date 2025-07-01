package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.enums.FileTypeEnum;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.TransferFileTaskService;
import com.br.marketing.vo.TransferFileTaskVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 转化文件任务业务逻辑实现
 * @author songjuanjuan
 * @dateTime 2022/05/26 11:12
 */
@Service
@Slf4j
public class TransferFileTaskServiceImpl implements TransferFileTaskService {

    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;


    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    final static List<Integer> FILE_TYPE_LIST = Arrays.asList(FileTypeEnum.CAR_DATA.getValue(), FileTypeEnum.SHORK_LINK_DATA.getValue());

    @Override
    public PageResultReturn getTransferFileList(int current, int size, String serach, String startDateStart, String startDateEnd) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        if (StringUtils.isNotEmpty(startDateEnd)){
            startDateEnd = DateUtils.format(addDay(startDateEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        if (StringUtils.isNotEmpty(serach) && serach.contains("_")){
            serach = serach.replace("_", "\\_");
        }
        PageHelper.startPage(current, size);
        List<TransferFileTaskVO> list = transferFileTaskMapper.getTransferFileList(serach,startDateStart,startDateEnd);
        list.stream().map(transferFileTaskVO -> {
            if(transferFileTaskVO.getStatus()==4
                    && yyyyMMdd.equals(transferFileTaskVO.getStartDate())
                    && !FILE_TYPE_LIST.contains(transferFileTaskVO.getFileType())){
                transferFileTaskVO.setIsOperation(1);
            }else {
                transferFileTaskVO.setIsOperation(0);
            }
            LocalDate localDate = LocalDate.parse(transferFileTaskVO.getStartDate(), YYYYMMDDSHORTDF);
            transferFileTaskVO.setStartDate(localDate.toString());
            return transferFileTaskVO;
        }).collect(Collectors.toList());

        return PageResultReturn.setPageResult(list, current,size);
    }

    @Override
    public ApiResult reStartTransfer(Integer id) {

        TransferFileTask fileTask = transferFileTaskMapper.selectByPrimaryKey(id.longValue());
        if(Objects.isNull(fileTask)){
            return new ApiResult<>().fail("该条数据提取记录不存在");
        }
        transferFileTaskMapper.deleteByPrimaryKey(id.longValue());

        String extend = fileTask.getExtend();
        if(StringUtils.isEmpty(extend) || !extend.contains("mrpExtraTaskId")){
            return new ApiResult<>().success();
        }
        JSONObject jo = JSONObject.parseObject(extend);
        if(jo == null){
            return new ApiResult<>().success();
        }
        String mrpExtraTaskId = jo.getString("mrpExtraTaskId");
        if(StringUtils.isEmpty(mrpExtraTaskId)){
            return new ApiResult<>().success();
        }
        String startDate = fileTask.getStartDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate actionLocalDate = LocalDate.parse(startDate, formatter);
        String actionDate = actionLocalDate.toString();

        transferFileTaskMapper.deleteMrpExtraTaskAction(mrpExtraTaskId, actionDate);
        log.warn("删除MRP文件提取记录, mrpExtraTaskId: {}, actionDate: {}", mrpExtraTaskId, actionDate);

        /*//目前手动触发定时任务
        JobOperateAPI jobOperateAPI = JobAPIFactory.createJobOperateAPI(zkAddressList,nameSpace, Optional.absent());
        jobOperateAPI.trigger(Optional.of(TRANSFERFILEJOB),Optional.absent());
        //PutToSftpJob有判断1分钟的条件，后续运营使用考虑异步触发，目前手动执行
        jobOperateAPI.trigger(Optional.of(SYNCFILEJOB),Optional.absent());*/
        return new ApiResult<>().success();
    }

    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

}
