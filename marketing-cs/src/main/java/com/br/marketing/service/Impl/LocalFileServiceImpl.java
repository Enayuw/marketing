package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.LocalFileService;
import com.br.marketing.vo.LocalFileVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件接口实现
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.service.Impl
 * @Description: 文件接口实现
 * @CreateTime: 2022-09-15 15 :39
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Service
@Slf4j
public class LocalFileServiceImpl implements LocalFileService {

    @Resource
    LocalFileMapper localFileMapper;

    public void sortList(List<String> list,String target){
        //二叉树查找


    }



    @Override
    public PageResultReturn list(int current, int pageSize, String search, String apiCode, String uploadStartTime, String uploadEndTime, String fileType) {
        if (StringUtils.isNotEmpty(uploadStartTime)) {
            uploadStartTime = DateUtils.format(addDay(uploadStartTime), "yyyy-MM-dd HH:mm:ss");
        }
        if (StringUtils.isNotEmpty(uploadEndTime)) {
            uploadEndTime = DateUtils.format(addDay(uploadEndTime), "yyyy-MM-dd HH:mm:ss");
        }
        if(StringUtils.isNotBlank(fileType)){
            fileType = "'"+fileType.replace(",", "','")+"'";
        }
        PageHelper.startPage(current, pageSize);
        List<LocalFileVo> localFileList = localFileMapper.selectList(search, apiCode,uploadStartTime,uploadEndTime,fileType);

        return PageResultReturn.setPageResult(localFileList, current, pageSize);
    }
    @Override
    public Long allCount(String search, String apiCode, String uploadStartTime, String uploadEndTime, String fileType) {
        if(StringUtils.isNotBlank(fileType)){
            fileType = "'"+fileType.replace(",", "','")+"'";
        }
        return localFileMapper.allCount(search,apiCode,uploadStartTime,uploadEndTime,fileType);
    }
    private Date addDay(String date) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, "yyyy-MM-dd HH:mm:ss");
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, 0);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public void refreshPushNumber(List<Map<String, Object>> quantityList, Date pushStartTime, Date pushEndTime) {
        if (quantityList == null || quantityList.size() < 1) {
            return;
        }
        for (Map<String, Object> map : quantityList) {
            Long localId = Long.parseLong(String.valueOf(map.get("localId")));
            Integer quantity = Integer.parseInt(String.valueOf(map.get("quantity")));
            if (quantity == null || quantity < 0) {
                continue;
            }
            LocalFile localFile = new LocalFile();
            localFile.setId(localId);
            localFile.setPushNumber(quantity);
            localFile.setPushStartTime(pushStartTime);
            localFile.setPushEndTime(pushEndTime);
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.warn("更新推送量级，localId: {}, quantity: {}", localId,  quantity);
        }
    }

    @Override
    public List<LocalFile> getLastDataByApiCode(String apiCode, LocalDateTime dayStartTime,LocalDateTime dateEndTime) {
        return localFileMapper.getLastDataByApiCode(apiCode,dayStartTime,dateEndTime);
    }
}
