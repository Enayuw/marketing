package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.BFileBiConfig;
import com.br.marketing.entity.BFileBiConfigExample;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BFileBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisBIMapper;
import com.br.marketing.service.YiXInMailStatisticsInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description YiXinMailStatisticsInfoServiceImpl
 * @Author xiong.luo
 * @CreateTime 2025/07/08
 */
@Service
@Slf4j
public class YiXinMailStatisticsInfoServiceImpl implements YiXInMailStatisticsInfoService {

    private static final String MAIL_PREFIX = "三方营销效果监控-百融";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private JavaMailSenderImpl mailSender;

    @Resource
    private NfsFileTOBiRecordMapper nfsFileTOBiRecordMapper;

    @Resource
    private BFileBiConfigMapper bFileBiConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileExtractToDorisBIMapper transferFileExtractToDorisBIMapper;

    @Override
    public void transMailToMarketingBiProcess(String jobParam) {
        JSONObject param = JSONObject.parseObject(jobParam);

        LocalDate startDate = Optional.ofNullable(param.getString("startDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(() -> LocalDate.now().minusDays(2));
        LocalDate endDate = Optional.ofNullable(param.getString("endDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(() -> LocalDate.now().minusDays(2));
        boolean forceOverride = Optional.ofNullable(param.getBoolean("forceOverride")).orElse(false);

        // 使用Stream生成日期序列并构造结果列表
        List<String> dates = Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(startDate.until(endDate).getDays() + 1L)
                .map(date -> date.format(FORMATTER))
                .collect(Collectors.toList());
        List<String> fileNames = dates.stream().map(MAIL_PREFIX::concat).collect(Collectors.toList());
        NfsFileTOBiRecordExample nfsFileTOBiRecordExample = new NfsFileTOBiRecordExample();
        nfsFileTOBiRecordExample.createCriteria().andFileNameIn(fileNames);
        // 这里认为执行日期等于数据日期
        List<String> executedDates = nfsFileTOBiRecordMapper.selectByExample(nfsFileTOBiRecordExample)
                .stream().map(NfsFileTOBiRecord::getExecuteDate).collect(Collectors.toList());
        if (!forceOverride) {
            dates = dates.stream().filter(fileName -> !executedDates.contains(fileName)).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(dates)) {
            return;
        }

        Map<String, String> YiXinMailConfig = marketingCommonConfig.getYiXinMailConfigMap();
        String userName = YiXinMailConfig.get("userName");
        String password = YiXinMailConfig.get("password");

        String apiCode = marketingCommonConfig.getMailSubjectApiCodeMap().get(MAIL_PREFIX);
        BFileBiConfig bFileBiConfig = getBFileBiConfig(apiCode, "2").get(0);
        List<String> columns = Arrays.asList(bFileBiConfig.getDbFields().split(","));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "imap");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.auth", "true");

        // 连接邮箱服务器
        Session session = Session.getInstance(props);
        Store store = null;
        Folder inbox = null;
        try {
            store = session.getStore("imap");
            store.connect(mailSender.getHost(), userName, password);
            // 打开收件箱
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            for (String date : dates) {
                log.warn("宜信邮件统计数据抓取任务，开始处理日期:{}", date);
                String mailDate = LocalDate.parse(date, FORMATTER).format(OUTPUT_FORMATTER);
                SearchTerm term = new SubjectTerm(MAIL_PREFIX.concat(mailDate));
                try {
                    Message[] messages = inbox.search(term);
                    if (ArrayUtils.isEmpty(messages)) {
                        log.warn("未找到邮件:{}", MAIL_PREFIX.concat(date));
                    }
                    Message message = messages[messages.length - 1];
                    dealDailyMail(date, message, bFileBiConfig, columns);
                } catch (Exception e) {
                    String errMsg = "宜信邮件统计数据抓取任务异常: " + date + " Exception: " + e.getMessage();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
                }
            }
        } catch (MessagingException e) {
            String errMsg = "宜信邮件统计数据抓取任务连接邮件服务器异常, Exception: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    log.warn("宜信邮件统计数据抓取任务关闭收件箱异常: {}", e.getMessage());
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    log.warn("宜信邮件统计数据抓取任务关闭邮件服务器异常: {}", e.getMessage());
                }
            }
        }
    }

    private void dealDailyMail(String date, Message message, BFileBiConfig bFileBiConfig, List<String> columns) throws Exception {
        List<BodyPart> attachments = getExcelAttachments(message);
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        BodyPart attachment = attachments.get(0);
        try (InputStream is = attachment.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

            StringBuilder insertSql = new StringBuilder("INSERT INTO ")
                    .append(bFileBiConfig.getDbName()).append(" (")
                    .append(columns.stream().map(String::trim).collect(Collectors.joining(", ")))
                    .append(") VALUES ");
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                parseRow(row, sheet, mergedRegions, rowIndex, insertSql, date);
            }
            if (insertSql.charAt(insertSql.length() - 1) == ',') {
                insertSql.setLength(insertSql.length() - 1);
            }
            String delSql = "DELETE FROM " + bFileBiConfig.getDbName() + " WHERE execute_date = '" + date + "'";
            transferFileExtractToDorisBIMapper.deleteDataFromMarketingBiTablebI_(delSql);
            transferFileExtractToDorisBIMapper.insertDataToMarketingBiTablebI_(insertSql.toString());

            NfsFileTOBiRecordExample example = new NfsFileTOBiRecordExample();
            example.createCriteria().andFileNameEqualTo(MAIL_PREFIX.concat(date));
            nfsFileTOBiRecordMapper.deleteByExample(example);

            NfsFileTOBiRecord record = new NfsFileTOBiRecord();
            record.setApiCode(marketingCommonConfig.getMailSubjectApiCodeMap().get(MAIL_PREFIX));
            record.setFilePath(MAIL_PREFIX.concat(date));
            record.setFileName(MAIL_PREFIX.concat(date));
            record.setExecuteDate(date);
            nfsFileTOBiRecordMapper.insertSelective(record);
        }
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
        if (!part.isMimeType("multipart/*")) {
            return excelAttachments;
        }
        Multipart multipart = (Multipart) part.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                if (MimeUtility.decodeText(bodyPart.getFileName()).toLowerCase().endsWith(".xlsx")) {
                    excelAttachments.add(bodyPart);
                }
            } else if (bodyPart.isMimeType("multipart/*")) {
                excelAttachments.addAll(getExcelAttachments(bodyPart));
            }
        }
        return excelAttachments;
    }

    // 解析单行数据
    private void parseRow(Row row, Sheet sheet, List<CellRangeAddress> mergedRegions,
                          int rowIndex, StringBuilder insertSql, String date) {
        insertSql.append("\n('").append(getMergedCellValue(sheet, mergedRegions, rowIndex, 0))
                .append("', '").append(getMergedCellValue(sheet, mergedRegions, rowIndex, 1))
                .append("'");
        for (int i = 2; i < row.getLastCellNum(); i++) {
            String rawValue = getCellStringValue(row.getCell(i));
            if (StringUtils.isEmpty(rawValue)) {
                insertSql.append(", NULL");
            } else {
                insertSql.append(", '").append(rawValue).append("'");
            }
        }
        insertSql.append(", '").append(date).append("'),");
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
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double numValue = cell.getNumericCellValue();
                return new BigDecimal(numValue).setScale(6, RoundingMode.HALF_UP).toPlainString();
            default:
                return cell.toString();
        }
    }
}
