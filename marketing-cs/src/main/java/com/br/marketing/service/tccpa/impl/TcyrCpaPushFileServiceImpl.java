package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
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
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TcyrCpaPushFileServiceImpl implements TcyrCpaPushFileService {

    private final static String TITLE_GEN = "【同程易融CPA-推送文件数据生成】";

    private final static String TITLE_SYNC = "【同程易融CPA-推送文件数据同步】";

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
        String localPath = syncConfigService.getPath()
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
        //4.文件写入
        try {
            write(apiCode, localPath, yyyyMMdd);
        } catch (Exception e) {

        }
    }

    /**
     * @description 文件写入
     * @param apiCode
     * @param localPath 服务器路径
     * @param yyyyMMdd 日期
     * @return Boolean 是否成功
     * @author hedongshuo
     * @date 2025/8/26 16:19
     **/
    private Boolean write(String apiCode, String localPath, String yyyyMMdd) throws FileNotFoundException {
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
        JSONArray info = new JSONArray();
        //2.查询量级
        Integer queryCountTotal = 0;
        for (MarketingTcyrCpaPushFileScript script : scripts) {
            JSONObject json = new JSONObject();
            String extraCountSql = "select count(0) from ("
                    .concat(script.getExtractScript())
                    .concat(") as a");
            Integer count = 0;
            if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_TIDB.getValue()) {
                count = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDataCounttikv_(extraCountSql);
            } else if (script.getDataSource() == TcCpaPushFileScriptPriorityEnum.PRIORITY_DORIS.getValue()) {
                count = tcyrCpaPushFileScriptMapper.getTcyrCpaPushFileDataCountdoris_(extraCountSql);
            }
            json.put("priority", script.getPriority());
            json.put("queryCount", count);
            info.add(json);
            queryCountTotal += count;
        }
        if (queryCountTotal == 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "脚本查询量级为0！", TITLE_GEN));
            return false;
        }
        //3.所需配置
        Integer extraNumTotal = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("extraNumTotal");
        Integer extraNumSingle = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("extraNumSingle");
        Integer pageSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("pageSize");
        Integer threadPoolSize = marketingCommonConfig.getTcyrCpaPushFileConfig().getInteger("threadPoolSize");
        //4.创建目录
        File writeDic = new File(localPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "目录创建失败！", TITLE_GEN));
                return false;
            }
        }
        //5.文件写入
        //数据提取量级
        Integer extraDataNum = 0;
        //脚本提取量级
        Integer extraCsvNum = 0;
        StringBuilder fileNames = new StringBuilder();
        Map<String, Writer> fwMap = new HashMap();
        for (int i = 1; i <= (extraNumTotal + extraNumSingle - 1) / extraNumSingle; i++) {
            fwMap.put(String.valueOf(i), genWriter(localPath, yyyyMMdd, "_" + i, fileNames));
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
                        .concat(StringUtils.isEmpty(minCusNum) ? " " : " and a.cus_num > '" + minCusNum + "' ")
                        .concat("order by user_key limit ")
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
                    futures.add(CompletableFuture.runAsync(() -> writeData(fwMap.get(finalCsvIndex.toString()), resultThisCsv), actionPool));
                    csvIndex++;
                    List<String> resultNextCsv = result.subList(extraNumSingle - extraDataNum, result.size());
                    Integer finalCsvIndexPlus = csvIndex;
                    futures.add(CompletableFuture.runAsync(() -> writeData(fwMap.get(finalCsvIndexPlus.toString()), resultNextCsv), actionPool));
                    extraCsvNum = resultNextCsv.size();
                } else {
                    List<String> finalResult = result;
                    futures.add(CompletableFuture.runAsync(() -> writeData(fwMap.get(finalCsvIndex.toString()), finalResult), actionPool));
                    if (extraCsvNum + result.size() == extraNumSingle) {
                        extraCsvNum = 0;
                        csvIndex++;
                    }
                }
                if (extraDataNum == extraNumTotal) {
                    break outerLoop;
                }
            }
        }
        return true;
    }

    private void writeData(Writer fw, List<String> resultThisCsv) {
    }

    private Writer genWriter(String localPath, String yyyyMMdd, String suffix, StringBuilder fileNames) throws FileNotFoundException {
        String fileName = yyyyMMdd.concat(suffix).concat(".csv");
        fileNames.append(fileName).append(";");
        File file = new File(localPath.concat(fileName));
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
    }
}
