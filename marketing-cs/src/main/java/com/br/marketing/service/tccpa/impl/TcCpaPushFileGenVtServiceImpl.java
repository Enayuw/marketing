package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tccpa.FilePushTaskFileDTO;
import com.br.marketing.dto.tccpa.FilePushTaskInfo;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaPushFileTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaPushDataMapper;
import com.br.marketing.mapper.TcyrCpaPushFileTaskVtMapper;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.tccpa.TcCpaPushFileGenVtService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
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

@Slf4j
@Service
public class TcCpaPushFileGenVtServiceImpl implements TcCpaPushFileGenVtService {

    @Value("${innerSftp.uploadpath:00}")
    private String upLoadPath;

    private final static String TITLE = "【TCYR CPA自动化-推送文件数据生成】";

    private final static String FILE_PATH = "/tongcheng_cpa_push_file_vt/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private TcyrCpaPushFileTaskVtMapper tcyrCpaPushFileTaskVtMapper;

    @Resource
    private TcyrCpaPushDataMapper tcyrCpaPushDataMapper;

    @Resource
    private SftpInnerServiceImpl sftpInnerService;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Override
    public void process() {
        String apiCode = marketingCommonConfig.getTcyrCpaApiCode();
        //1.查询今天是否已有推送文件任务
        TcyrCpaPushFileTaskVtExample taskExample = new TcyrCpaPushFileTaskVtExample();
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andPushDateEqualTo(new Date())
                .andStatusNotEqualTo(TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue())
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        List<TcyrCpaPushFileTaskVt> tasks = tcyrCpaPushFileTaskVtMapper.selectByExample(taskExample);
        if (CollectionUtils.isNotEmpty(tasks)) {
            return;
        }

        // 判断撞库任务是否还有没筛选完成的
        TcyrCpaCollidingTaskExample example = new TcyrCpaCollidingTaskExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue())
                .andCollidingDateEqualTo(new Date())
                .andStatusLessThan(TcCpaCollidingTaskStatusEnum.STATUS_FILTER_COMPLETED.getValue());
        if (tcyrCpaCollidingTaskMapper.countByExample(example) > 0) {
            return;
        }
        // 查询已经完成筛选的任务
        example = new TcyrCpaCollidingTaskExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue())
                .andStatusEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_FILTER_COMPLETED.getValue());
        List<TcyrCpaCollidingTask> collidingTasks = tcyrCpaCollidingTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(collidingTasks)) {
            return;
        }
        // 更新撞库任务状态
        collidingTasks.forEach(collidingTask -> {
            collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_PUSHING.getValue());
            tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(collidingTask);
        });

        //2.服务器路径
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        String localPath = syncConfigService.getPath().concat(apiCode).concat(FILE_PATH).concat(yyyyMMdd).concat("/");
//        String localPath = "D:/".concat("tongcheng_cpa_push_file_vt/").concat(yyyyMMdd).concat("/");

        //3.新增推送文件任务
        TcyrCpaPushFileTaskVt pushTask = new TcyrCpaPushFileTaskVt();
        pushTask.setApiCode(apiCode);
        pushTask.setLocalPath(localPath);
        pushTask.setPushDate(new Date());
        pushTask.setPushTime(collidingTasks.stream().max(Comparator.comparing(TcyrCpaCollidingTask::getCollidingTime))
                .orElse(new TcyrCpaCollidingTask()).getCollidingTime());
        pushTask.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_GENINAG.getValue());
        pushTask.setIsDel(Constants.DATA_VALID);
        pushTask.setCollidingTaskIds(collidingTasks.stream().map(TcyrCpaCollidingTask::getId).map(String::valueOf)
                .collect(Collectors.joining(",")));
        tcyrCpaPushFileTaskVtMapper.insertSelective(pushTask);

        FilePushTaskInfo info = new FilePushTaskInfo();
        List<Long> taskIds = collidingTasks.stream().map(TcyrCpaCollidingTask::getId).collect(Collectors.toList());
        String infoString = null;
        try {
            //4.文件写入
            Boolean isCompleted = write(localPath, yyyyMMdd, info, taskIds);
            //5.整理并核验info
            if (isCompleted) {
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
        //6.更新推送文件任务
        TcyrCpaPushFileTaskVt updateTaskGen = new TcyrCpaPushFileTaskVt();
        updateTaskGen.setId(pushTask.getId());
        updateTaskGen.setTotal(info.getExtraNumAct());
        updateTaskGen.setInfo(infoString);
        if (StringUtils.isEmpty(info.getMessage())) {
            updateTaskGen.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_SUCCESS.getValue());
        } else {
            updateTaskGen.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "文件生成异常，请检查！", TITLE));
        }
        tcyrCpaPushFileTaskVtMapper.updateByPrimaryKeySelective(updateTaskGen);
        if (Objects.equals(updateTaskGen.getStatus(), TcCpaPushFileTaskStatusEnum.STATUS_FAIL.getValue())) {
            return;
        }
        //7.上传至内部sftp
        List<String> fileNames = info.getFiles().stream()
                .map(FilePushTaskFileDTO::getFileName)
                .collect(Collectors.toList());
        String uploadPath = upLoadPath.concat(apiCode).concat(FILE_PATH).concat(yyyyMMdd);
        sftpInnerService.pushInnerSftp(localPath, uploadPath, fileNames);
        TcyrCpaPushFileTaskVt updateTaskPutInnerSftp = new TcyrCpaPushFileTaskVt();
        updateTaskPutInnerSftp.setId(pushTask.getId());
        updateTaskPutInnerSftp.setInnerSftpPath(uploadPath);
        updateTaskPutInnerSftp.setStatus(TcCpaPushFileTaskStatusEnum.STATUS_INNER_SFTP.getValue());
        tcyrCpaPushFileTaskVtMapper.updateByPrimaryKeySelective(updateTaskPutInnerSftp);
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
        int extraNumAct = info.getFiles().stream()
                .filter(Objects::nonNull)  // 过滤空对象
                .filter(dto -> !dto.getCsvIndex().equals("ok"))
                .map(FilePushTaskFileDTO::getTotal) // 获取AtomicInteger对象
                .filter(Objects::nonNull)// 过滤空的AtomicInteger
                .mapToInt(AtomicInteger::get) // 转换为int值
                .sum();
        info.setExtraNumAct(extraNumAct);
        if (extraNumAct != (info.getExtraNumExp())) {
            info.setMessage("期望提取量级：" + info.getExtraNumExp() + ",实际提取量级：" + extraNumAct + " 不相等");
            logWarnAndinfoRecord("期望提取量级：" + info.getExtraNumExp() + ",实际提取量级：" + extraNumAct + "，请核对！", info);
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
    private Boolean write(String localPath, String yyyyMMdd, FilePushTaskInfo info, List<Long> taskIds) {
        Map<String, ImmutablePair<BufferedWriter, FilePushTaskFileDTO>> fwMap = new HashMap();
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
            //2.生成数据文件
            boolean writeSuccess = taskIds.stream().allMatch(taskId -> {
                try {
                    return writeFile(localPath, yyyyMMdd, info, fwMap, taskId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            if (!writeSuccess) {
                return false;
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
                            "close writer error", TITLE), e);
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
                              Map<String, ImmutablePair<BufferedWriter, FilePushTaskFileDTO>> fwMap, Long taskId) throws Exception {
        // 查询量级
        TcyrCpaPushDataExample example = new TcyrCpaPushDataExample();
        example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andTaskIdEqualTo(taskId.intValue());
        int pushDataNum = tcyrCpaPushDataMapper.countByExample(example);

        //所需配置
        Integer extraNumSingle = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("extraNumSingle");
        Integer threadPoolSize = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("threadPoolSize");
        info.setExtraNumSingle(extraNumSingle);

        //期望提取量级
        info.setExtraNumExp(pushDataNum);
        //文件提取量级
        int extraCsvNum = 0;
        //4.创建writer池
        for (int i = 1; i <= (pushDataNum + extraNumSingle - 1) / extraNumSingle; i++) {
            fwMap.put(String.valueOf(i), genWriter(localPath, yyyyMMdd, String.valueOf(i)));
        }
        //csv索引
        int csvIndex = 1;
        //5.创建线程池
        //
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_PUSH_FILE_GEN_VT.getName(), threadPoolSize, threadPoolSize);
        try {
            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            // 游标分页
            Long lastId = 0L;
            for (; ; ) {
                Integer pageSize = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("pageSize");
                List<TcyrCpaPushData> pushDataList = tcyrCpaPushDataMapper.selectWithPagination(lastId, pageSize, taskId);
                if (CollectionUtils.isEmpty(pushDataList)) {
                    break;
                }
                TcyrCpaPushData lastRecord = pushDataList.get(pushDataList.size() - 1);
                lastId = lastRecord.getId();

                List<String> userKeys = pushDataList.stream().map(TcyrCpaPushData::getUserKey).collect(Collectors.toList());
                int fromIndex = 0;
                while (fromIndex < userKeys.size()) {
                    int remainingCapacity = extraNumSingle - extraCsvNum;
                    int toIndex = Math.min(fromIndex + remainingCapacity, userKeys.size());
                    List<String> chunk = userKeys.subList(fromIndex, toIndex);
                    int currentCsvIndex = csvIndex;
                    futures.add(CompletableFuture.runAsync(
                            () -> writeData(fwMap.get(Integer.toString(currentCsvIndex)), chunk),
                            actionPool
                    ));
                    extraCsvNum += chunk.size();
                    if (extraCsvNum >= extraNumSingle) {
                        csvIndex++;
                        extraCsvNum = 0;
                    }
                    fromIndex = toIndex;
                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            logWarnAndinfoRecord(e.getMessage(), info);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
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
     * @param userKeys
     */
    private void writeData(ImmutablePair<BufferedWriter, FilePushTaskFileDTO> pair, List<String> userKeys) {
        try {
            Writer writer = pair.getLeft();
            FilePushTaskFileDTO taskFileDTO = pair.getRight();
            for (String userKey : userKeys) {
                writer.write(userKey + "\r\n");
                taskFileDTO.getTotal().incrementAndGet();
            }
            writer.flush();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "文件写入异常", TITLE), e);
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
