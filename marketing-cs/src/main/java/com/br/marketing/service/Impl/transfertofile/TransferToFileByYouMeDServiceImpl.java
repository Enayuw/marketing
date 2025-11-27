package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.TransferOfCnIdVO;
import com.br.marketing.vo.TransferOfRdRFVO;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 你我贷转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByYouMeDServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    TableCreateServiceImpl tableCreateService;


    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    final static String EXECUTE_TIME = "10:00:00";

    final static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Autowired
    TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;


    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = StringUtils.isBlank(marketingCommonConfig.getYouMeDFileExecTime())?EXECUTE_TIME:marketingCommonConfig.getYouMeDFileExecTime();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, " "+execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            String fileName = String.format("niwodai_transform_%s.txt", yyyyMMdd);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("你我贷转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(fileName);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("你我贷转化数据落库-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        Integer num = 0;
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,userType,cell,A,loginTime,B,applyDt,C,applyDtApp,D,applyTime,E,F,lentTime,G,requestDate");
            fw.append("\r\n");
            writeYMDTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYMDTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException, ParseException {
        Long start = System.currentTimeMillis();
        //endDate-上传数据的有效结束时间，beginDate-上传数据的有效开始时间
        LocalDate yDate = LocalDate.now().minusDays(1L);
        String requestDataMinusOne = yDate.format(YYYYMMDDLINEDF);
        MarketingDataValidConfig configList = marketingDataValidConfigMapper
                .queryStartDateEndDatetikv_(apiCode, requestDataMinusOne, null);
        LocalDate startDate = LocalDate.now();
        String transferBeginDateStr =  configList.getValidStartDate();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(transferBeginDateStr);
        String tcId = tableCreateService.getTcId(apiCode);

        Boolean dateMark = Boolean.TRUE;
        AtomicInteger totalSize = new AtomicInteger();
        HashSet custNumSet = new HashSet<>();
        Integer datePage = 0;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(20), new ThreadFactoryBuilder().setNameFormat("YMDfile-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        while (dateMark) {
            Date nowDate = Date.from(startDate.minusDays(datePage).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(nowDate);
            if(nowDate.compareTo(date)<0){
                dateMark = Boolean.FALSE;
                continue;
            }
            Boolean dayMark = Boolean.TRUE;
            Long minId = null;
            while (dayMark){

                //region 任务执行控制
                Integer threadNum = (marketingCommonConfig.getYouMeDDataPull() == null
                        || StringUtils.isBlank(marketingCommonConfig.getYouMeDDataPull().get("threadNum")))
                        ? 5
                        : Integer.valueOf(marketingCommonConfig.getYouMeDDataPull().get("threadNum"));
                String isContinue =(marketingCommonConfig.getYouMeDDataPull() == null
                        || StringUtils.isBlank(marketingCommonConfig.getYouMeDDataPull().get("isContinue")))
                        ? "1"
                        : marketingCommonConfig.getYouMeDDataPull().get("isContinue");
                if (!"1".equals(isContinue)) {
                    dateMark = Boolean.FALSE;
                    dayMark = Boolean.FALSE;
                    log.warn("你我贷接收到中断指令");
                    continue;
                }
                if (threadNum.intValue() != threadPoolExecutor.getCorePoolSize()) {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPoolExecutor, threadNum);
                    log.warn(String.format("你我贷线程池线程数变更：核心线程数：%d，最大线程数：%d，活动线程数：%d", threadPoolExecutor.getCorePoolSize(), threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getActiveCount()));
                }
                //endregion

                List<TransferOfCnIdVO> transferSyncUsers = transferSyncUserMapper.getTransferReqDateAndIdByPage(tcId, apiCode, nowDateStr,minId);
                if (transferSyncUsers.size() <= 0) {
                    dayMark = false;
                    continue;
                }
                minId = transferSyncUsers.get(transferSyncUsers.size()-1).getId();
                List<String> custNums = transferSyncUsers.stream().filter(t -> custNumSet.add(t.getCustNum())).map(t -> t.getCustNum()).collect(Collectors.toList());
                if(custNums.size()<=0){
                    continue;
                }
                threadPoolExecutor.submit(() -> {
                    try {
                        fieldAction(transferSyncUsers, custNums, totalSize, transferBeginDateStr, apiCode, tcId, fw);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });
            }
            datePage++;
        }

        threadPoolExecutor.shutdown();
        while (true) {
            if (threadPoolExecutor.isTerminated()) {
                log.info("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                log.error("等待所有任务都执行完成", e);
            }
        }
        fw.close();


        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize.get());
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("你我贷转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    void fieldAction(List<TransferOfCnIdVO> transferSyncUsers, List<String> custNums,
                     AtomicInteger totalSize, String transferBegin,String apiCode, String tcid, Writer fw) {
        Integer num = 0;
        LocalDate yDate = LocalDate.now().minusDays(1L);
        String requestDataMinusOne = yDate.format(YYYYMMDDLINEDF);
        Set<String> custNumSet = transferSyncUsers.stream().map(TransferOfCnIdVO::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = transferDataValidityPeriodService
                .getValidityPeriodsByCustNum(custNumSet, apiCode, requestDataMinusOne);
        List<TransferOfRdRFVO> transferOfRdRFs = transferSyncUserMapper.getTransferOfRdRFs(transferBegin, custNums, tcid, apiCode);
        Map<String, List<TransferOfRdRFVO>> collect = transferOfRdRFs.stream()
                .sorted(Comparator.comparing(TransferOfRdRFVO::getRequestTime))
                .collect(Collectors.groupingBy(TransferOfRdRFVO::getCustNum));
        for (String custNum : collect.keySet()) {
            SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
            if (boMap == null) {
                log.warn("apiCode[{}]custNum[{}]不满足你我贷案件编号“有效期内”条件", apiCode, custNum);
                continue;
            }
            MarketingSyncUser syncUser = boMap.getSyncUsers().get(0);
            List<TransferOfRdRFVO> transferOfRdRFVOS = collect.get(custNum);
            if (transferOfRdRFVOS.size() <= 0) {
                continue;
            }
            if (syncUser == null) {
                continue;
            }
            String _Ahave = "";
            String _Bhave = "";
            String _Chave = "";
            String _Dhave = "";
            String _Fhave = "";

            String _Atime = "";
            String _Btime = "";
            String _Ctime = "";
            String _Dtime = "";
            String _Ftime = "";
            String _E = "";
            String _G = "";
            String tDate = "";
            for (int i = 0; i < transferOfRdRFVOS.size(); i++) {
                TransferOfRdRFVO transferOfRdRFVO = transferOfRdRFVOS.get(i);
                try {
                    JSONObject jb = JSON.parseObject(transferOfRdRFVO.getReserveField1());
                    if (jb == null) {
                        continue;
                    }
                    String a = jb.getString("A");
                    String b = jb.getString("B");
                    String c = jb.getString("C");
                    String d = jb.getString("D");
                    String e = jb.getString("E");
                    String f = jb.getString("F");
                    String g = jb.getString("G");

                    //region a,b,c,d,e,f,g 只取最新一条转化记录的值
                    if (i == transferOfRdRFVOS.size() - 1) {
                        tDate = transferOfRdRFVO.getRequestData();
                        if (StringUtils.isNotBlank(a)) {
                            _Ahave = a;
                        }
                        if (StringUtils.isNotBlank(b)) {
                            _Bhave = b;
                        }
                        if (StringUtils.isNotBlank(c)) {
                            _Chave = c;
                        }
                        if (StringUtils.isNotBlank(d)) {
                            _Dhave = d;
                        }
                        if (StringUtils.isNotBlank(e)) {
                            _E = e;
                        }
                        if (StringUtils.isNotBlank(f)) {
                            _Fhave = f;
                        }
                        if (StringUtils.isNotBlank(g)) {
                            _G = g;
                        }
                        if (StringUtils.isNotBlank(a)) {
                            _Ahave = a;
                        }
                    }
                    //endregion

                    //region 时间字段赋值，登录时间取最新的a=1的日期-1,其它日期取最早一条的key=1的日期-1
                    if ("1".equals(a)) {
                        _Atime = LocalDate.parse(transferOfRdRFVO.getRequestData(), df).minusDays(1L).format(df);
                    }
                    if (StringUtils.isBlank(_Btime) && "1".equals(b)) {
                        _Btime = LocalDate.parse(transferOfRdRFVO.getRequestData(), df).minusDays(1L).format(df);
                    }
                    if (StringUtils.isBlank(_Ctime) && "1".equals(c)) {
                        _Ctime = LocalDate.parse(transferOfRdRFVO.getRequestData(), df).minusDays(1L).format(df);
                    }
                    if (StringUtils.isBlank(_Dtime) && "1".equals(d)) {
                        _Dtime = LocalDate.parse(transferOfRdRFVO.getRequestData(), df).minusDays(1L).format(df);
                    }
                    if (StringUtils.isBlank(_Ftime) && "1".equals(f)) {
                        _Ftime = LocalDate.parse(transferOfRdRFVO.getRequestData(), df).minusDays(1L).format(df);
                    }
                    //endregion

                } catch (Exception e) {
                    continue;
                }
            }
            StringBuilder content = new StringBuilder();
            content.append(custNum).append(",")
                    .append(syncUser.getUserType()).append(",")
                    .append(Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(syncUser.getCell()))).append(",")
                    .append(_Ahave).append(",")
                    .append(_Atime).append(",")
                    .append(_Bhave).append(",")
                    .append(_Btime).append(",")
                    .append(_Chave).append(",")
                    .append(_Ctime).append(",")
                    .append(_Dhave).append(",")
                    .append(_Dtime).append(",")
                    .append(_E).append(",")
                    .append(_Fhave).append(",")
                    .append(_Ftime).append(",")
                    .append(_G).append(",")
                    .append(tDate).append("\r\n");
            try {
                fw.append(content.toString());
                num++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        totalSize.getAndAdd(num);
    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

}
