package com.br.marketing.bridge.job;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.BFileBiConfig;
import com.br.marketing.entity.BFileBiConfigExample;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BFileBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.service.TransFileToMarketingBiService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiong.luo
 * @description: 宜信邮件统计数据入库JOB
 * @date 2025/7/7 20:04
 */
@Component
@Slf4j
public class YiXinMailStatisticsInfoJob extends AbstractSimpleElasticJob {

    private static final String MAIL_PREFIX = "三方营销效果监控-百融";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private JavaMailSenderImpl mailSender;

    @Resource
    private NfsFileTOBiRecordMapper nfsFileTOBiRecordMapper;

    @Resource
    private BFileBiConfigMapper bFileBiConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long curTime = System.currentTimeMillis();
        log.warn("内部服务器转化文件提取到marketingBI任务开始");

        JSONObject param = JSONObject.parseObject(context.getJobParameter());

        LocalDate startDate = Optional.ofNullable(param.getString("startDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);
        LocalDate endDate = Optional.ofNullable(param.getString("endDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);
        boolean forceOverride = Optional.ofNullable(param.getBoolean("endDate")).orElse(false);

        // 使用Stream生成日期序列并构造结果列表
        List<String> dates = Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(endDate.until(startDate).getDays() + 1L)
                .map(date -> date.format(FORMATTER))
                .collect(Collectors.toList());
        List<String> fileNames = dates.stream().map(MAIL_PREFIX::concat).collect(Collectors.toList());

        NfsFileTOBiRecordExample nfsFileTOBiRecordExample = new NfsFileTOBiRecordExample();
        nfsFileTOBiRecordExample.createCriteria().andFileNameIn(fileNames);
        List<String> executedDates = nfsFileTOBiRecordMapper.selectByExample(nfsFileTOBiRecordExample)
                .stream().map(NfsFileTOBiRecord::getExecuteDate).collect(Collectors.toList());
        if (!forceOverride) {
            dates = dates.stream().filter(fileName -> !executedDates.contains(fileName)).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(dates)) {
            return;
        }

        BFileBiConfig bFileBiConfig = getBFileBiConfig("370012", "2").get(0);
        List<String> columns = Arrays.asList(bFileBiConfig.getDbFields().split(","));
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(bFileBiConfig.getDbName()).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i != columns.size() - 1) {
                insertSql.append(columns.get(i).trim()).append(", ");
            } else {
                insertSql.append(columns.get(i).trim()).append(") VALUES ");
            }
        }

        dates.forEach(date -> {
            log.warn("开始处理日期:{}", date);
            List<Message> messages = readMailBySubject(MAIL_PREFIX.concat(date));
            if (CollectionUtils.isEmpty(messages)) {
                log.warn("未找到邮件:{}", date);
                return;
            }
            Message message = messages.get(0);
            try {
                List<BodyPart> attachments = getExcelAttachments(message);

                for (BodyPart attachment : attachments) {
                    try (InputStream is = attachment.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
                        Sheet sheet = workbook.getSheetAt(0);
                        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                            Row row = sheet.getRow(rowIndex);
                            if (row == null) {
                                continue;
                            }
                            MarketingEffect obj = parseRow(row, sheet, mergedRegions, rowIndex);
                            results.add(obj);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("内部服务器转化文件提取到marketingBI任务异常:{}", e.getMessage());
            }


        });
        log.warn("内部服务器转化文件提取到marketingBI任务结束, 耗时:{}s", (System.currentTimeMillis() - curTime) / 1000);
    }

    private List<BFileBiConfig> getBFileBiConfig(String apiCode, String busType) {
        BFileBiConfigExample example = new BFileBiConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBusTypeEqualTo(busType);
        List<BFileBiConfig> bFileBiConfigs = bFileBiConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bFileBiConfigs)) {
            String errMsg = "apiCode: " + apiCode + " nfs转化提取文件落库到marketingBI没有找到对应的配置信息";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
            return new ArrayList<>();
        }
        return bFileBiConfigs;
    }

    private List<BodyPart> getExcelAttachments(Part part) throws Exception {
        List<BodyPart> excelAttachments = Lists.newArrayList();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                        bodyPart.getFileName().toLowerCase().endsWith(".xlsx")) {

                    if (bodyPart.getFileName().toLowerCase().endsWith(".xlsx")) {
                        excelAttachments.add(bodyPart);
                    }
                } else if (bodyPart.isMimeType("multipart/*")) {
                    excelAttachments.addAll(getExcelAttachments(bodyPart));
                }
            }
        }
        return excelAttachments;
    }

    // 解析单行数据
    private MarketingEffect parseRow(Row row, Sheet sheet, List<CellRangeAddress> mergedRegions, int rowIndex) {
        MarketingEffect obj = new MarketingEffect();

        // 处理合并单元格（第0列）
        obj.setPeriod(getMergedCellValue(sheet, mergedRegions, rowIndex, 0));

        // 处理其他列（跳过空值造成的列偏移）
        int colOffset = (row.getCell(2) == null) ? 1 : 0;

        obj.setNameListPackage(getCellStringValue(row.getCell(1)));
        obj.setPartner(getCellStringValue(row.getCell(2)));
        obj.setIntoRate(getNumericValue(row.getCell(3 - colOffset)));

        return obj;
    }

    // 合并单元格特殊处理
    private String getMergedCellValue(Sheet sheet, List<CellRangeAddress> regions, int row, int col) {
        for (CellRangeAddress region : regions) {
            if (region.isInRange(row, col)) {
                Row firstRow = sheet.getRow(region.getFirstRow());
                return getCellStringValue(firstRow.getCell(region.getFirstColumn()));
            }
        }
        return getCellStringValue(sheet.getRow(row).getCell(col));
    }

    // 通用单元格值获取
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: return cell.toString();
        }
    }

    private List<Message> readMailBySubject(String subject) {
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "imap");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.auth", "true");

        Session session = Session.getInstance(props);

        // 连接邮箱服务器
        try {
            Store store = session.getStore("imap");
            store.connect(mailSender.getHost(), mailSender.getUsername(), mailSender.getPassword());
            // 打开收件箱
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            SearchTerm term = new SubjectTerm(subject);
            Message[] messages = inbox.search(term);
            return Lists.newArrayList(messages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}