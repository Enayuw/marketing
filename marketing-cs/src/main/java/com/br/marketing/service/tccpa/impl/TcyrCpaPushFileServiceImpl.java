package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tccpa.FilePushTaskFileDTO;
import com.br.marketing.dto.tccpa.FilePushTaskInfo;
import com.br.marketing.dto.tccpa.FilePushTaskScriptNumDTO;
import com.br.marketing.entity.MarketingTcyrCpaPushFileScript;
import com.br.marketing.entity.MarketingTcyrCpaPushFileScriptExample;
import com.br.marketing.entity.MarketingTcyrCpaPushFileTask;
import com.br.marketing.entity.MarketingTcyrCpaPushFileTaskExample;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaPushFileScriptPriorityEnum;
import com.br.marketing.enums.TcCpaPushFileTaskStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaPushFileScriptMapper;
import com.br.marketing.mapper.MarketingTcyrCpaPushFileTaskMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.tccpa.TcyrCpaPushFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcyrCpaPushFileServiceImpl implements TcyrCpaPushFileService {

    private final static String TITLE_GEN = "【同程易融CPA-推送文件数据生成】";

    private final static String TITLE_SYNC = "【同程易融CPA-推送文件数据同步】";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrCpaPushFileTaskMapper tcyrCpaPushFileTaskMapper;

    @Resource
    private MarketingTcyrCpaPushFileScriptMapper tcyrCpaPushFileScriptMapper;

    @Resource
    SyncConfigService syncConfigService;

    @Override
    public void fileGen() {
        String apiCode = marketingCommonConfig.getTcyrCpaApiCode();
        //1.查询今天是否已有推送文件任务
        MarketingTcyrCpaPushFileTaskExample taskExample = new MarketingTcyrCpaPushFileTaskExample();
        taskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andPushDateEqualTo(new Date())
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        List<MarketingTcyrCpaPushFileTask> tasks = tcyrCpaPushFileTaskMapper.selectByExample(taskExample);
        if (tasks.size() > 0) {
            return;
        }
        //2.服务器路径
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
//        String localPath = syncConfigService.getPath()
//                .concat("tongcheng_cpa_push_file/")
//                .concat(yyyyMMdd)
//                .concat("/");
        String localPath = "D:/"
                .concat("tongcheng_cpa_push_file/")
                .concat(yyyyMMdd)
                .concat("/");
        //3.新增一条推送文件任务
        MarketingTcyrCpaPushFileTask task = new MarketingTcyrCpaPushFileTask();
        task.setApiCode(apiCode);
        task.setLocalPath(localPath);
        task.setPushDate(new Date());
        task.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_GENINAG.getValue());
        task.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        tcyrCpaPushFileTaskMapper.insertSelective(task);
        FilePushTaskInfo info = new FilePushTaskInfo();
        String infoString = null;
        try {
            //4.文件写入
            Boolean isCompleted = write(apiCode, localPath, yyyyMMdd, info);
            //5.整理并核验info
            if (isCompleted) {
                checkInfo(info);
            }
        } catch (Exception e) {
            info.setMessage(e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE_GEN), e);
        }
        try {
            infoString = objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE_GEN), e);
        }
        //6.更新推送文件任务
        MarketingTcyrCpaPushFileTask updateTask = new MarketingTcyrCpaPushFileTask();
        updateTask.setId(task.getId());
        updateTask.setTotal(info.getExtraNumAct());
        updateTask.setInfo(infoString);
        if (StringUtils.isEmpty(info.getMessage())) {
            updateTask.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_SUCCESS.getValue());
        } else {
            updateTask.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue());
        }
        tcyrCpaPushFileTaskMapper.updateByPrimaryKeySelective(updateTask);
    }

    /**
     * 整理并核验info
     * @param info
     */
    private void checkInfo(FilePushTaskInfo info) {
        //1.校验标识文件是否生成
        boolean isOk = info.getFiles().stream()
                .filter(Objects::nonNull)
                .anyMatch(file -> "ok".equals(file.getCsvIndex()));
        if (!isOk) {
            logWarnAndinfoRecord("未生成标识文件！", info);
        }
        //2.核对量级
        Integer extraNumAct = info.getFiles().stream()
                .filter(Objects::nonNull) // 过滤空对象
                .map(FilePushTaskFileDTO::getTotal) // 获取AtomicInteger对象
                .filter(Objects::nonNull) // 过滤空的AtomicInteger
                .mapToInt(AtomicInteger::get) // 转换为int值
                .sum();
        info.setExtraNumAct(extraNumAct);
        if (extraNumAct.intValue() != (info.getExtraNumExp().intValue())) {
            logWarnAndinfoRecord(
                    "期望提取量级：" + info.getExtraNumExp() + ",实际提取量级：" + extraNumAct + "，请核对！", info);
        }
    }

    /**
     * 写入主流程
     * @param apiCode
     * @param localPath 服务器路径
     * @param yyyyMMdd  日期
     * @param info 执行情况
     * @return Boolean 是否成功
     * @description 文件写入
     * @author hedongshuo
     * @date 2025/8/26 16:19
     **/
    private Boolean write(String apiCode, String localPath, String yyyyMMdd, FilePushTaskInfo info) throws FileNotFoundException {
        //1.查询提取脚本
        MarketingTcyrCpaPushFileScriptExample scriptExample = new MarketingTcyrCpaPushFileScriptExample();
        scriptExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        scriptExample.setOrderByClause("priority asc");
        List<MarketingTcyrCpaPushFileScript> scripts = tcyrCpaPushFileScriptMapper.selectByExample(scriptExample);
        if (CollectionUtils.isEmpty(scripts)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "未配置提取脚本，请检查！", TITLE_GEN));
            return false;
        }
        //2.查询量级
        List<FilePushTaskScriptNumDTO> scriptNumDTOS = new ArrayList<>();
        Integer scriptNum = 0;
        for (MarketingTcyrCpaPushFileScript script : scripts) {
            String extraCountSql = "select count(0) from ("
                    .concat(script.getExtractScript())
                    .concat(") as countTable");
            Integer count = 0;
            if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_TIDB.getValue()) {
                count = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDataCounttikv_(extraCountSql);
            } else if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_DORIS.getValue()) {
                count = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDataCountdoris_(extraCountSql);
            }
            FilePushTaskScriptNumDTO scriptNumDTO = new FilePushTaskScriptNumDTO();
            scriptNumDTO.setPriority(script.getPriority());
            scriptNumDTO.setCount(count);
            scriptNumDTOS.add(scriptNumDTO);
            scriptNum += count;
        }
        info.setScriptNumDTOS(scriptNumDTOS);
        info.setScriptNum(scriptNum);
        if (scriptNum.intValue() == 0) {
            logWarnAndinfoRecord("脚本查询量级为0！", info);
            return false;
        }
        //3.所需配置
        Integer extraNumTotal = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("extraNumTotal");
        Integer extraNumSingle = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("extraNumSingle");
        Integer pageSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("pageSize");
        Integer threadPoolSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("threadPoolSize");
        info.setExtraNumTotal(extraNumTotal);
        info.setExtraNumSingle(extraNumSingle);
        //4.创建目录
        File writeDic = new File(localPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                logWarnAndinfoRecord("目录创建失败！", info);
                return false;
            }
        }
        //期望提取量级
        Integer extraNumExp = Math.min(extraNumTotal, scriptNum);
        info.setExtraNumExp(extraNumExp);
        //数据提取量级
        Integer extraDataNum = 0;
        //脚本提取量级
        Integer extraCsvNum = 0;
        //5.创建writer池
        Map<String, ImmutablePair<BufferedWriter, FilePushTaskFileDTO>> fwMap = new HashMap();
        for (int i = 1; i <= (extraNumExp  + extraNumSingle - 1) / extraNumSingle; i++) {
            fwMap.put(String.valueOf(i), genWriter(localPath, yyyyMMdd, String.valueOf(i)));
        }
        //csv索引
        Integer csvIndex = 1;
        //6.创建线程池
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_PUSH_FILE_GEN.getName(), threadPoolSize, threadPoolSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        outerLoop:
        for (MarketingTcyrCpaPushFileScript script : scripts) {
            //用cus_num卡
            String minCusNum = "";
            for (; ; ) {
                String extraSql = script.getExtractScript()
                        .concat(StringUtils.isEmpty(minCusNum) ? " " : " and " + script.getOutputField() + " > " + "'" + minCusNum + "' ")
                        .concat("order by " + script.getOutputField() + " limit ")
                        .concat(pageSize.toString());
                List<String> result = null;
                if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_TIDB.getValue()) {
                    result = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDatatikv_(extraSql);
                } else if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_DORIS.getValue()) {
                    result = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDatadoris_(extraSql);
                }
                if(CollectionUtils.isEmpty(result)){
                    //未查到数据，执行下一个脚本
                    continue outerLoop;
                }
                //判断总量级
                if(extraDataNum + result.size() > extraNumTotal){
                    //确定可提取的数据
                    result = result.subList(0, extraNumTotal - extraDataNum);
                }
                minCusNum = result.get(result.size() - 1);
                extraDataNum = extraDataNum + result.size();
                //判断可写入文件的量级
                Integer finalCsvIndex = csvIndex;
                if (extraCsvNum + result.size() > extraNumSingle) {
                    List<String> resultThisCsv = result.subList(0, extraNumSingle - extraCsvNum);
                    futures.add(CompletableFuture.runAsync(()
                            -> writeData(fwMap.get(finalCsvIndex.toString()), resultThisCsv), actionPool));
                    csvIndex++;
                    List<String> resultNextCsv = result.subList(extraNumSingle - extraDataNum, result.size());
                    Integer finalCsvIndexPlus = csvIndex;
                    futures.add(CompletableFuture.runAsync(()
                            -> writeData(fwMap.get(finalCsvIndexPlus.toString()), resultNextCsv), actionPool));
                    extraCsvNum = resultNextCsv.size();
                } else {
                    List<String> finalResult = result;
                    futures.add(CompletableFuture.runAsync(()
                            -> writeData(fwMap.get(finalCsvIndex.toString()), finalResult), actionPool));
                    if (extraCsvNum + result.size() == extraNumSingle) {
                        csvIndex++;
                        extraCsvNum = 0;
                    }
                }
                if (extraDataNum.intValue() == extraNumTotal.intValue()) {
                    break outerLoop;
                }
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        actionPool.shutdownAndAwaitTermination();
        //7.补充标识文件
        fwMap.put("ok", genWriter(localPath, yyyyMMdd, null));
        //8.补充info
        List<FilePushTaskFileDTO> files = fwMap.values().stream()
                .filter(Objects::nonNull)
                .map(ImmutablePair::getRight)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        info.setFiles(files);
        return true;
    }

    /**
     * 更新message并告警
     * @param message
     * @param info
     */
    private void logWarnAndinfoRecord(String message, FilePushTaskInfo info) {
        info.setMessage(message);
        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                message, TITLE_GEN));
    }

    /**
     * 文件写入
     * @param pair
     * @param cusNums
     */
    private void writeData(ImmutablePair<BufferedWriter, FilePushTaskFileDTO> pair, List<String> cusNums) {
        Writer writer = pair.getLeft();
        FilePushTaskFileDTO taskFileDTO = pair.getRight();
        for (String cusNum : cusNums) {
            //csv行
            StringBuilder line = new StringBuilder();
            line.append(cusNum).append("\r\n");
            try {
                writer.write(line.toString());
                taskFileDTO.getTotal().incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 生成writer及文件信息
     * @param localPath
     * @param yyyyMMdd
     * @param suffix
     * @return
     * @throws FileNotFoundException
     */
    private ImmutablePair<BufferedWriter, FilePushTaskFileDTO> genWriter(String localPath, String yyyyMMdd, String suffix) throws FileNotFoundException {
        String fileName;
        FilePushTaskFileDTO taskFileDTO = new FilePushTaskFileDTO();
        if (StringUtils.isEmpty(suffix)) {
            suffix = "ok";
            fileName = yyyyMMdd.concat(".").concat(suffix);
        } else {
            fileName = yyyyMMdd.concat("_").concat(suffix).concat(".csv");
        }
        taskFileDTO.setCsvIndex(suffix);
        taskFileDTO.setFileName(fileName);
        AtomicInteger total = new AtomicInteger(0);
        taskFileDTO.setTotal(total);
        File file = new File(localPath.concat(fileName));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        return ImmutablePair.of(writer, taskFileDTO);
    }
}
