package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.TransferFileTaskService;
import com.br.marketing.vo.TransferFileTaskVO;
import com.dangdang.ddframe.job.api.JobAPIFactory;
import com.dangdang.ddframe.job.api.JobOperateAPI;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 转化文件任务业务逻辑实现
 * @author songjuanjuan
 * @dateTime 2022/05/26 11:12
 */
@Service
@Slf4j
public class TransferFileTaskServiceImpl implements TransferFileTaskService {

    @Value("${SERVER_LISTS:00}")
    private String zkAddressList;

    @Value("${NAMESPACE:00}")
    private String nameSpace;

    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;

    private  static final String TRANSFERFILEJOB = "TransferFileTaskJob";
    private  static final String SYNCFILEJOB = "PutToSftpJob";


    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

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
            if(transferFileTaskVO.getStatus()==4 && yyyyMMdd.equals(transferFileTaskVO.getStartDate())){
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
        JobOperateAPI jobOperateAPI = JobAPIFactory.createJobOperateAPI(zkAddressList,nameSpace, Optional.absent());
        jobOperateAPI.trigger(Optional.of(TRANSFERFILEJOB),Optional.absent());
        jobOperateAPI.trigger(Optional.of(SYNCFILEJOB),Optional.absent());
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
