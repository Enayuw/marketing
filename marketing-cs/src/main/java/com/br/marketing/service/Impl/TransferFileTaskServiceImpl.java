package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.PageResultReturn;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
