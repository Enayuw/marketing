package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tccpa.FilePushTaskFileDTO;
import com.br.marketing.dto.tccpa.FilePushTaskInfo;
import com.br.marketing.dto.tccpa.FilePushTaskScriptNumDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.MarketingTcyrCpaPushFileScriptMapper;
import com.br.marketing.mapper.MarketingTcyrCpaPushFileTaskMapper;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.tccpa.TcyrCpaPushFileGenService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
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
public class TcyrCpaPushFileGenServiceImpl implements TcyrCpaPushFileGenService {

    @Value("${innerSftp.uploadpath:00}")
    private String upLoadPath;

    private final static String TITLE = "【同程易融新场景-推送文件数据生成】";

    /** apiCode 与 scene 目录之间的固定片段 */
    private static final String FILE_PATH = "/tcxcj/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrCpaPushFileTaskMapper tcyrCpaPushFileTaskMapper;

    @Resource
    private MarketingTcyrCpaPushFileScriptMapper tcyrCpaPushFileScriptMapper;

    @Resource
    private SftpInnerServiceImpl sftpInnerService;

    @Resource
    private SyncConfigService syncConfigService;

    @Override
    public void fileGen() {
        String apiCode = marketingCommonConfig.getTcyrApiCode();
        // 仅读取当天 push_date 的脚本，避免 Job 次日未关时重复扫到昨日配置
        java.sql.Date todayPushDate = java.sql.Date.valueOf(LocalDate.now());
        MarketingTcyrCpaPushFileScriptExample scriptExample = new MarketingTcyrCpaPushFileScriptExample();
        scriptExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andPushDateEqualTo(todayPushDate)
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        scriptExample.setOrderByClause("priority asc");
        List<MarketingTcyrCpaPushFileScript> allScripts = tcyrCpaPushFileScriptMapper.selectByExample(scriptExample);
        if (CollectionUtils.isEmpty(allScripts)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "未配置提取脚本，请检查！", TITLE));
            return;
        }
        Map<String, Long> sceneCount = allScripts.stream()
                .collect(Collectors.groupingBy(this::sceneKey, Collectors.counting()));
        Set<String> duplicateSceneKeys = sceneCount.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (!duplicateSceneKeys.isEmpty()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "b_tcyr_cpa_push_file_script 存在重复 scene，已剔除这些 scene 下全部脚本，重复 sceneKey=" + duplicateSceneKeys, TITLE));
        }
        List<MarketingTcyrCpaPushFileScript> validScripts = allScripts.stream()
                .filter(s -> sceneCount.getOrDefault(sceneKey(s), 0L) == 1L)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(validScripts)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "无可用脚本（全部为重复 scene 或未配置），已跳过生成", TITLE));
            return;
        }
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        for (MarketingTcyrCpaPushFileScript script : validScripts) {
            processOneSceneTask(apiCode, script, yyyyMMdd);
        }
    }

    private String sceneKey(MarketingTcyrCpaPushFileScript s) {
        if (s == null || s.getScene() == null) {
            return "";
        }
        return s.getScene().trim();
    }

    /**
     * 路径片段：空 scene 用 default，并替换路径分隔符
     */
    private String scenePathSegment(String scene) {
        String s = scene == null ? "" : scene.trim();
        if (s.isEmpty()) {
            return "default";
        }
        return s.replace('/', '_').replace('\\', '_');
    }

    /**
     * 每个 scene 一条任务：{getPath()}/{apiCode}/tcxcj/{scene}/yyyyMMdd/
     */
    private void processOneSceneTask(String apiCode, MarketingTcyrCpaPushFileScript script, String yyyyMMdd) {
        MarketingTcyrCpaPushFileTaskExample taskExample = new MarketingTcyrCpaPushFileTaskExample();
        MarketingTcyrCpaPushFileTaskExample.Criteria tc = taskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andPushDateEqualTo(new Date())
                .andStatusNotEqualTo(TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue())
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        if (StringUtils.isEmpty(script.getScene())) {
            tc.andSceneIsNull();
        } else {
            tc.andSceneEqualTo(script.getScene());
        }
        if (tcyrCpaPushFileTaskMapper.countByExample(taskExample) > 0) {
            return;
        }
        String segment = scenePathSegment(script.getScene());
        String localPath = syncConfigService.getPath()
                .concat(apiCode)
                .concat(FILE_PATH)
                .concat(segment)
                .concat("/")
                .concat(yyyyMMdd)
                .concat("/");
        MarketingTcyrCpaPushFileTask task = new MarketingTcyrCpaPushFileTask();
        task.setApiCode(apiCode);
        task.setScene(script.getScene());
        task.setLocalPath(localPath);
        task.setPushDate(new Date());
        task.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_GENINAG.getValue());
        task.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        tcyrCpaPushFileTaskMapper.insertSelective(task);
        FilePushTaskInfo info = new FilePushTaskInfo();
        String infoString = null;
        try {
            Boolean isCompleted = write(localPath, yyyyMMdd, info, Collections.singletonList(script));
            if (Boolean.TRUE.equals(isCompleted)) {
                checkInfo(info);
            }
        } catch (Exception e) {
            info.setMessage(e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        try {
            infoString = objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        MarketingTcyrCpaPushFileTask updateTaskGen = new MarketingTcyrCpaPushFileTask();
        updateTaskGen.setId(task.getId());
        updateTaskGen.setTotal(info.getExtraNumAct());
        updateTaskGen.setInfo(infoString);
        if (StringUtils.isEmpty(info.getMessage())) {
            updateTaskGen.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_SUCCESS.getValue());
        } else {
            updateTaskGen.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "文件生成异常，请检查！", TITLE));
        }
        tcyrCpaPushFileTaskMapper.updateByPrimaryKeySelective(updateTaskGen);
        if (updateTaskGen.getStatus() == TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue()) {
            return;
        }
        List<String> fileNames = info.getFiles() == null ? Collections.emptyList() : info.getFiles().stream()
                .map(FilePushTaskFileDTO::getFileName)
                .collect(Collectors.toList());
        String uploadPath = upLoadPath.concat(apiCode).concat(FILE_PATH).concat(segment).concat("/").concat(yyyyMMdd);
        sftpInnerService.pushInnerSftp(localPath, uploadPath, fileNames);
        MarketingTcyrCpaPushFileTask updateTaskPutInnerSftp = new MarketingTcyrCpaPushFileTask();
        updateTaskPutInnerSftp.setId(task.getId());
        updateTaskPutInnerSftp.setInnerSftpPath(uploadPath);
        updateTaskPutInnerSftp.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_INNER_SFTP.getValue());
        tcyrCpaPushFileTaskMapper.updateByPrimaryKeySelective(updateTaskPutInnerSftp);
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
        if (info.getOnlyOk()) {
            return;
        }
        //2.核对量级
        Integer extraNumAct = info.getFiles().stream()
                .filter(dto -> !dto.getCsvIndex().equals("ok"))
                .filter(Objects::nonNull) // 过滤空对象
                .map(FilePushTaskFileDTO::getTotal) // 获取AtomicInteger对象
                .filter(Objects::nonNull)// 过滤空的AtomicInteger
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
     * @param localPath 服务器路径
     * @param yyyyMMdd  日期
     * @param info 执行情况
     * @return Boolean 是否成功
     * @description 文件写入
     * @author hedongshuo
     * @date 2025/8/26 16:19
     **/
    private Boolean write(String localPath, String yyyyMMdd, FilePushTaskInfo info,
                          List<MarketingTcyrCpaPushFileScript> scriptsForScene) {
        Map<String, ImmutablePair<BufferedWriter, FilePushTaskFileDTO>> fwMap = new HashMap<>();
        try {
            //1.创建目录
            File writeDic = new File(localPath);
            if (!writeDic.exists()) {
                boolean mkdirs = writeDic.mkdirs();
                if (!mkdirs) {
                    logWarnAndinfoRecord("目录创建失败！", info);
                    return false;
                }
            }
            //判断是否生成数据文件
            Boolean onlyOk = marketingCommonConfig.getTcyrCpaPushFileConfig().getBoolean("onlyOk");
            info.setOnlyOk(onlyOk);
            if(!onlyOk){
                //2.生成数据文件
                Boolean writeSuccess = writeFile(localPath, yyyyMMdd, info, fwMap, scriptsForScene);
                if (!writeSuccess) return false;
            }
            //3.生成标识文件
            List<String> countResult = new ArrayList<>();
            countResult.add(String.valueOf(fwMap.size()));
            fwMap.put("ok", genWriter(localPath, yyyyMMdd, null));
            writeData(fwMap.get("ok"), countResult);
        } catch (Exception e) {
            info.setMessage(e.getMessage());
        } finally {
            //4.关闭writer
            for (ImmutablePair<BufferedWriter, FilePushTaskFileDTO> pair : fwMap.values()) {
                if (pair == null) {
                    continue;
                }
                BufferedWriter writer = pair.getLeft();
                if (writer == null) {
                    continue;
                }
                try {
                    writer.close();
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "close writer error", TITLE));
                }
            }
        }
        //4.补充info
        List<FilePushTaskFileDTO> files = fwMap.values().stream()
                .filter(Objects::nonNull)
                .map(ImmutablePair::getRight)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        info.setFiles(files);
        return true;
    }

    private Boolean writeFile(String localPath, String yyyyMMdd, FilePushTaskInfo info,
                              Map<String, ImmutablePair<BufferedWriter, FilePushTaskFileDTO>> fwMap,
                              List<MarketingTcyrCpaPushFileScript> scripts) throws Exception {
        if (CollectionUtils.isEmpty(scripts)) {
            logWarnAndinfoRecord("未配置提取脚本，请检查！", info);
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
                count = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDataCountbI_(extraCountSql);
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
        Integer threadPoolSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("threadPoolSize");
        info.setExtraNumTotal(extraNumTotal);
        info.setExtraNumSingle(extraNumSingle);
        //期望提取量级
        Integer extraNumExp = Math.min(extraNumTotal, scriptNum);
        info.setExtraNumExp(extraNumExp);
        //数据提取量级
        Integer extraDataNum = 0;
        //文件提取量级
        Integer extraCsvNum = 0;
        //4.创建writer池
        for (int i = 1; i <= (extraNumExp  + extraNumSingle - 1) / extraNumSingle; i++) {
            fwMap.put(String.valueOf(i), genWriter(localPath, yyyyMMdd, String.valueOf(i)));
        }
        //csv索引
        Integer csvIndex = 1;
        //5.创建线程池
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_PUSH_FILE_GEN.getName(), threadPoolSize, threadPoolSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        outerLoop:
        for (MarketingTcyrCpaPushFileScript script : scripts) {
            //用cus_num卡
            String minCusNum = "";
            for (; ; ) {
                Integer pageSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("pageSize");
                String extraSql = script.getExtractScript()
                        .concat(StringUtils.isEmpty(minCusNum) ? " " : " and " + script.getOutputField() + " > " + "'" + minCusNum + "' ")
                        .concat("order by " + script.getOutputField() + " limit ")
                        .concat(pageSize.toString());
                List<String> result = null;
                if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_TIDB.getValue()) {
                    result = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDatatikv_(extraSql);
                } else if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_DORIS.getValue()) {
                    result = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDatabI_(extraSql);
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
                while (extraCsvNum + result.size() > extraNumSingle) {
                    List<String> resultThisCsv = result.subList(0, extraNumSingle - extraCsvNum);
                    Integer subCsvIndex = csvIndex;
                    futures.add(CompletableFuture.runAsync(()
                            -> writeData(fwMap.get(subCsvIndex.toString()), resultThisCsv), actionPool));
                    result = result.subList(extraNumSingle - extraCsvNum, result.size());
                    csvIndex++;
                    extraCsvNum = 0;
                }
                List<String> finalResult = result;
                Integer finalCsvIndex = csvIndex;
                futures.add(CompletableFuture.runAsync(()
                        -> writeData(fwMap.get(finalCsvIndex.toString()), finalResult), actionPool));
                if (extraCsvNum + result.size() == extraNumSingle) {
                    csvIndex++;
                    extraCsvNum = 0;
                } else {
                    extraCsvNum = extraCsvNum + result.size();
                }
                if (extraDataNum.intValue() == extraNumTotal.intValue()) {
                    break outerLoop;
                }
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        actionPool.shutdownAndAwaitTermination();
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
                message, TITLE));
    }

    /**
     * 文件写入
     * @param pair
     * @param cusNums
     */
    private void writeData(ImmutablePair<BufferedWriter, FilePushTaskFileDTO> pair, List<String> cusNums) {
        try {
            Writer writer = pair.getLeft();
            FilePushTaskFileDTO taskFileDTO = pair.getRight();
            for (String cusNum : cusNums) {
                //csv行
                StringBuilder line = new StringBuilder();
                line.append(cusNum).append("\r\n");
                writer.write(line.toString());
                taskFileDTO.getTotal().incrementAndGet();
            }
            writer.flush();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "文件写入异常", TITLE));
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
    private ImmutablePair<BufferedWriter, FilePushTaskFileDTO> genWriter(String localPath, String yyyyMMdd, String suffix) throws Exception {
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
