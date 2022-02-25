package com.br.marketing.service.Impl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import IceInternal.Ex;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.vo.TransferUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 萨摩耶 转化数据处理服务
 */
@Service
public class TransferToFileBySamoyeServiveImpl implements ITransferToFileService {

    @Autowired
    TransferFileTaskMapper transferFileTaskMapper;

    @Autowired
    MarketingCustomerMapper customerMapper;

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    RuleRedisServiceImpl ruleRedisService;

    private static final Logger log = LoggerFactory.getLogger(TransferToFileBySamoyeServiveImpl.class);

    final DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

    final DateTimeFormatter ymdDfBy_ = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    final static String samoyeDDprefix = "samoye_duandian_";


    final static String samoyeHYprefix = "samoye_alive_";

    final static String samoyeZHprefix = "samoye_zhuanhua_";

    final static String TRANSFER_TIME = " 18:00:00";

    @Value("${otherConfig.warning.path:00}")
    private String path;

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode) {

        List<TransferFileTask> resultList = new ArrayList<>();
        String yyyyMMdd = LocalDate.now().format(yyyyMMddDF);
        String bT = LocalDate.now().minusDays(1L).format(ymdDfBy_);
        String eT = LocalDate.now().format(ymdDfBy_);

        TransferFileTaskExample taskExample = new TransferFileTaskExample();
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd);
        List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
        Map<Integer, List<TransferFileTask>> collect = transferFileTasks.stream()
                .filter(t -> apiCode.equals(t.getApiCode()))
                .collect(Collectors.groupingBy(TransferFileTask::getFileType));
        if (collect.get(1) == null || collect.get(1).size() <= 0) {
            Integer s01 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S01", Arrays.asList("2", "3"));
            Integer s02 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S02", Arrays.asList("2", "3"));
            Integer s08 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S08", Arrays.asList("2"));
            Integer s0202 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S0202", Arrays.asList("2", "3"));
            int num = s01 + s02 + s08 + s0202;
            if (num > 0) {
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setTaskNumber(num);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }

        if (collect.get(2) == null || collect.get(2).size() <= 0) {
            Integer s01 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S01", Arrays.asList("4"));
            Integer s02 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S02", Arrays.asList("4"));
            Integer s0202 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S0202", Arrays.asList("4"));
            int num = s01 + s02 + s0202;
            if (num > 0) {
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(2);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setTaskNumber(num);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }
        //转化类型文件
        if (collect.get(3) == null || collect.get(3).size() <= 0) {
            Date now = new Date();
            Date transferDate = DateHelper.getDatePlusHourMinuteSecond(now, TRANSFER_TIME);
            bT = LocalDate.now().minusDays(1L).format(ymdDfBy_).concat(TRANSFER_TIME);
            eT = LocalDate.now().format(ymdDfBy_).concat(TRANSFER_TIME);
            //每天18:00:00之后执行
            if (now.after(transferDate)) {
                Integer s01 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S01", Arrays.asList("1"));
                Integer s02 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S02", Arrays.asList("1"));
                Integer s0202 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S0202", Arrays.asList("1"));
                Integer s04 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S04", Arrays.asList("1"));
                Integer s06 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S06", Arrays.asList("1"));
                Integer s08 = marketingSyncInfoMapper.countTransferFile(apiCode, bT, eT, "S08", Arrays.asList("1"));

                int num = s01 + s02 + s0202 + s04 + s06 + s08;
                if (num > 0) {
                    Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                    String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                    TransferFileTask transferFileTask = new TransferFileTask();
                    transferFileTask.setApiCode(apiCode);
                    transferFileTask.setFileType(3);
                    transferFileTask.setBatchNumber(batchNumber);
                    transferFileTask.setFileName("");
                    transferFileTask.setTaskNumber(num);
                    transferFileTask.setStartDate(yyyyMMdd);
                    transferFileTask.setContextId(transferFileContextId);
                    transferFileTask.setCreateTime(new Date());
                    transferFileTask.setUpdateTime(new Date());
                    transferFileTaskMapper.insertSelective(transferFileTask);
                    resultList.add(transferFileTask);
                }
            }
        }
        log.info("萨摩耶转化文件详情transferFileTasks = {}", JSONObject.toJSONString(resultList));
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask) {
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String startDate = LocalDate.parse(recordDate, yyyyMMddDF).minusDays(1L).format(ymdDfBy_);
        String endDate = LocalDate.parse(recordDate, yyyyMMddDF).format(ymdDfBy_);
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        List<String> groupTyps = new ArrayList<>();
        if (transferFileTask.getFileType().equals(1)) {
            fileName.append(samoyeDDprefix);
            groupTyps = Arrays.asList("S01", "S02", "S08", "S0202");
        } else if (transferFileTask.getFileType().equals(2)) {
            fileName.append(samoyeHYprefix);
            groupTyps = Arrays.asList("S01", "S02", "S0202");
        } else {
            fileName.append(samoyeZHprefix);
            groupTyps = Arrays.asList("S01", "S02", "S0202", "S04", "S06", "S08");
        }
        fileName.append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("taskid,案件编号,场景,场景标识,手机号,上传时间");
            fw.append("\r\n");
            writeDD(fw, apiCode, startDate, endDate, groupTyps, transferFileTask);
        } catch (Exception ex) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    void writeDD(Writer fw, String apiCode, String startDate
            , String endDate, List<String> groupTyps
            , TransferFileTask transferFileTask) throws IOException {
        //转化类型修改时间为T日18:00:00
        if (transferFileTask.getFileType().equals(3)) {
            startDate = startDate.concat(TRANSFER_TIME);
            endDate = endDate.concat(TRANSFER_TIME);
        }
        for (String groupType : groupTyps) {
            List<String> fileTypes = new ArrayList<>();
            if (groupType.equals("S01")) {
                if (transferFileTask.getFileType().equals(1)) {
                    fileTypes = Arrays.asList("2", "3");
                } else if (transferFileTask.getFileType().equals(2)) {
                    fileTypes = Arrays.asList("4");
                } else {
                    fileTypes = Arrays.asList("1");
                }
            } else if (groupType.equals("S02")) {
                if (transferFileTask.getFileType().equals(1)) {
                    fileTypes = Arrays.asList("2", "3");
                } else if (transferFileTask.getFileType().equals(2)) {
                    fileTypes = Arrays.asList("4");
                } else {
                    fileTypes = Arrays.asList("1");
                }
            } else if (groupType.equals("S0202")) {
                if (transferFileTask.getFileType().equals(1)) {
                    fileTypes = Arrays.asList("2", "3");
                } else if (transferFileTask.getFileType().equals(2)) {
                    fileTypes = Arrays.asList("4");
                } else {
                    fileTypes = Arrays.asList("1");
                }
            } else if (groupType.equals("S04")) {
                fileTypes = Arrays.asList("1");
            } else if (groupType.equals("S06")) {
                fileTypes = Arrays.asList("1");
            } else if (groupType.equals("S08")) {
                fileTypes = transferFileTask.getFileType().equals(3) ? Arrays.asList("1") : Arrays.asList("2");
            }
            Long minId = null;
            Boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                List<TransferUserVO> transferFileUser = marketingSyncInfoMapper
                        .getTransferFileUser(apiCode, startDate, endDate, minId, groupType, fileTypes);
                if (transferFileUser.size() <= 0) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = transferFileUser.get(transferFileUser.size() - 1).getId() + 1;

                List<String> taskIds = transferFileUser.stream().map(t -> t.getTaskId()).collect(Collectors.toList());
                List<String> custNums = transferFileUser.stream().map(t -> t.getCustNum()).collect(Collectors.toList());
                List<MarketingSyncUser> users = marketingSyncInfoMapper.getSyncUserByTaskAndCust(apiCode, taskIds, custNums);
                HashMap<String, String> hsCell = new HashMap<>();
                users.forEach(t -> {
                    String key = t.getCusBatch().concat("_").concat(t.getCustNum());
                    if (!hsCell.containsKey(key)) {
                        String decode = BrCipherMaker.getInstance().decode(t.getCell());
                        String s = StringUtils.isBlank(decode) ? t.getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                        hsCell.put(key, s);
                    }
                });

                for (TransferUserVO transferUserVO : transferFileUser) {
                    String nowKey = transferUserVO.getTaskId().concat("_").concat(transferUserVO.getCustNum());
                    String cell = StringUtils.isNotBlank(hsCell.get(nowKey)) ? hsCell.get(nowKey) : "";
                    StringBuilder sb = new StringBuilder();
                    sb.append(transferUserVO.getTaskId().concat(","));
                    sb.append(transferUserVO.getCustNum().concat(","));
                    sb.append(transferUserVO.getGroupType().concat(","));
                    sb.append(transferUserVO.getReserveField1().concat(","));
                    sb.append(cell.concat(","));
                    sb.append(DateHelper.strToDateLong(transferUserVO.getCreateTime()));
                    sb.append("\r\n");
                    fw.append(sb.toString());
                }
            }
        }

        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
    }

}
