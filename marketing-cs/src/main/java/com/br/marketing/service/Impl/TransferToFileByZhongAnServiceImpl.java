package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.entity.ZhonganMarketingBan;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author songjuanjnuan
 * @Date 2022/12/08 16:31
 * @Description:众安转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByZhongAnServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private ZhonganMarketingBanMapper zhonganMarketingBanMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    final static String EXECUTE_TIME = " 10:00:00";

    final static String ZHONGAN_FILE = "_zhonganzhuanhua_";

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    @Override
    public String isMyParam(String apiCode,String jobParameter) {
//        if(StringUtils.isNotEmpty(jobParameter)){
//            String[] split = jobParameter.split(";");
//            for(String s : split){
//                String paramApiCode = s.split("#")[0];
//                if(apiCode.equals(paramApiCode)  && marketingCommonConfig.getJiuFuTransferApiCodes().contains(paramApiCode)){
//                    return s.split("#")[1];
//                }
//            }
//        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getZhongAnTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getZhongAnTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("众安异业撞库数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        log.warn("众安异业撞库数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(apiCode).append(ZHONGAN_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,zk_date");
            fw.append("\r\n");
            writeZhongAnTransferToFile(fw, apiCode,transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeZhongAnTransferToFile(Writer fw, String apiCode,TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String recordDate = transferFileTask.getStartDate();
        LocalDate localDate = LocalDate.parse(recordDate, YYYYMMDDSHORTDF);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            Result<List<ZhonganMarketingBan>> transferData = getOrderTransferData(apiCode,localDate.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<ZhonganMarketingBan> data = transferData.getData();
            //cell,applet_date
            for (ZhonganMarketingBan transferFilterData : data) {
                String cell = transferFilterData.getCell();
                String md5 = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(cell).getBytes());
                StringBuilder sb = new StringBuilder();
                sb.append(md5.concat(","));
                sb.append(transferFilterData.getZkDate());
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + data.size();
            data.clear();
        }

        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("众安异业撞库数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 获取转化数据
     * 按照inserttime排序
     *
     * @param date
     * @param pageIndex
     * @return
     */
    private Result<List<ZhonganMarketingBan>> getOrderTransferData(String apiCode,String date,Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<ZhonganMarketingBan> zhonganMarketingBans = zhonganMarketingBanMapper.getByZKData(apiCode,date,limitStart);
        if (zhonganMarketingBans.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(zhonganMarketingBans);
    }

}
