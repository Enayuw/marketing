package com.br.marketing.bridge.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.bridge.service.MailStatisticsInfoService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.BMailBiConfig;
import com.br.marketing.entity.BMailBiConfigExample;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BMailBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisBIMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description MailStatisticsInfoServiceImpl
 * @Author xiong.luo
 * @CreateTime 2025/07/08
 */
@Service
@Slf4j
public class MailStatisticsInfoServiceImpl implements MailStatisticsInfoService {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String START_DATE = "startDate";

    public static final String END_DATE = "endDate";

    @Resource
    private JavaMailSenderImpl mailSender;

    @Resource
    private NfsFileTOBiRecordMapper nfsFileTOBiRecordMapper;

    @Resource
    private BMailBiConfigMapper bMailBiConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileExtractToDorisBIMapper transferFileExtractToDorisBIMapper;

    @Override
    public void transMailToMarketingBiProcess(String jobParam) {
        JSONObject param = JSONObject.parseObject(jobParam);
        Map<String, String> mailReadConfigMap = marketingCommonConfig.getMailReadConfigMap();
        String userName = mailReadConfigMap.get("userName");
        String password = mailReadConfigMap.get("password");

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

            List<BMailBiConfig> configs = bMailBiConfigMapper.selectByExample(new BMailBiConfigExample());
            for (BMailBiConfig bMailBiConfig : configs) {
                String apiCode = bMailBiConfig.getApiCode();

                String mailReadStartDate = bMailBiConfig.getStartDate().replaceAll("[^0-9\\-]", "");
                String mailReadEndDate = bMailBiConfig.getEndDate().replaceAll("[^0-9\\-]", "");

                DateTimeFormatter mailSubjectFormatter = StringUtils.isBlank(bMailBiConfig.getDateFormat()) ?
                        null : DateTimeFormatter.ofPattern(bMailBiConfig.getDateFormat());
                LocalDate startDate = Optional.ofNullable(param.getString(START_DATE))
                        .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                        .orElseGet(() -> LocalDate.now().plusDays(Long.parseLong(mailReadStartDate)));
                LocalDate endDate = Optional.ofNullable(param.getString(END_DATE))
                        .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                        .orElseGet(() -> LocalDate.now().plusDays(Long.parseLong(mailReadEndDate)));

                // 使用Stream生成日期序列并构造邮件标题列表
                List<String> dates = Stream.iterate(startDate, date -> date.plusDays(1))
                        .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1L)
                        .map(date -> date.format(FORMATTER))
                        .collect(Collectors.toList());
                String mailPrefix = bMailBiConfig.getSubject();

                for (String date : dates) {
                    log.warn("邮件统计数据抓取任务，apiCode:{}, 处理日期:{}", apiCode, date);
                    String mailDate = Objects.isNull(mailSubjectFormatter) ? null : LocalDate.parse(date, FORMATTER).format(mailSubjectFormatter);
                    String fileName = mailPrefix.concat(date);
                    NfsFileTOBiRecordExample nfsFileTOBiRecordExample = new NfsFileTOBiRecordExample();
                    nfsFileTOBiRecordExample.createCriteria().andApiCodeEqualTo(apiCode).andFileNameEqualTo(fileName)
                            .andBusTypeEqualTo("9");
                    // 按照邮件主题分组，获取邮件发送时间
                    List<String> mailSendTimeList = nfsFileTOBiRecordMapper.selectByExample(nfsFileTOBiRecordExample)
                            .stream().map(NfsFileTOBiRecord::getSendTime).collect(Collectors.toList());

                    String fullSubject = Objects.isNull(mailDate) ? mailPrefix : mailPrefix.concat(mailDate);
                    SearchTerm term = new SubjectTerm(fullSubject);
                    try {
                        Message[] messages = inbox.search(term);
                        if (ArrayUtils.isEmpty(messages)) {
                            log.warn("未找到邮件:{}", fullSubject);
                            continue;
                        }
                        for (Message message : messages) {
                            try {
                                String sendTime = SIMPLE_DATE_FORMAT.format(message.getSentDate());
                                // 对于邮件标题中没有日期的数据，判断发件日期是否等于当前要处理的数据日期
                                if(Objects.isNull(mailDate) && !StringUtils.equals(date, sendTime.substring(0, 8))) {
                                    continue;
                                }
                                if (CollectionUtils.isEmpty(mailSendTimeList) || !mailSendTimeList.contains(sendTime)) {
                                    dealDailyMail(date, message, bMailBiConfig, mailPrefix, sendTime, apiCode, mailReadEndDate);
                                }
                            } catch (Exception e) {
                                String errMsg = "邮件统计数据抓取任务异常, apiCode:" + apiCode + " , fileName:" + fileName + " Exception: " + e.getMessage();
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
                            }
                        }
                    } catch (Exception e) {
                        String errMsg = "邮件统计数据抓取任务异常, apiCode:" + apiCode + " , fileName:" + fileName + " Exception: " + e.getMessage();
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
                    }
                }
            }
        } catch (MessagingException e) {
            String errMsg = "邮件统计数据抓取任务连接邮件服务器异常, Exception: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    log.warn("邮件统计数据抓取任务关闭收件箱异常: {}", e.getMessage());
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    log.warn("邮件统计数据抓取任务关闭邮件服务器异常: {}", e.getMessage());
                }
            }
        }
    }

    private void dealDailyMail(String date, Message message, BMailBiConfig bFileBiConfig,
                               String mailPrefix, String sendTime, String apiCode, String mailReadEndDate) throws Exception {
        List<BodyPart> attachments = getExcelAttachments(message);
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        BodyPart attachment = attachments.get(0);
        try (InputStream is = attachment.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(bFileBiConfig.getSheetName());
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

            Map<String, String> colFieldMap = JSON.parseObject(bFileBiConfig.getDbColFieldsMap(),
                    new TypeReference<Map<String, String>>() {
                    });
            Row header = sheet.getRow(0);
            List<String> fileHeaders = Lists.newArrayList(header.cellIterator())
                    .stream().map(Cell::getStringCellValue).collect(Collectors.toList());
            Map<String, Integer> indexFieldMap = colFieldMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, entry -> fileHeaders.indexOf(entry.getKey())));

            StringBuilder insertSql = new StringBuilder("INSERT INTO ")
                    .append(bFileBiConfig.getDbName()).append(" (")
                    .append(indexFieldMap.keySet().stream().map(String::trim).collect(Collectors.joining(", ")))
                    .append(") VALUES ");
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                parseRow(sheet, mergedRegions, rowIndex, insertSql, date, indexFieldMap, sendTime, mailReadEndDate);
            }
            if (insertSql.charAt(insertSql.length() - 1) == ',') {
                insertSql.setLength(insertSql.length() - 1);
            }
            transferFileExtractToDorisBIMapper.insertDataToMarketingBiTablebI_(insertSql.toString());

            NfsFileTOBiRecord record = new NfsFileTOBiRecord();
            record.setApiCode(apiCode);
            record.setFilePath(mailPrefix.concat(date));
            record.setFileName(mailPrefix.concat(date));
            record.setExecuteDate(date);
            record.setSendTime(sendTime);
            record.setBusType("9");
            nfsFileTOBiRecordMapper.insertSelective(record);
        }
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
    private void parseRow(Sheet sheet, List<CellRangeAddress> mergedRegions, int rowIndex,
                          StringBuilder insertSql, String date, Map<String, Integer> colFieldMap, String sendTime, String mailReadEndDate) {
        insertSql.append("\n(");
        colFieldMap.forEach((field, index) -> {
            String rawValue = getCellStringValue(sheet, mergedRegions, rowIndex, index, field, date, sendTime, mailReadEndDate);
            if (StringUtils.isEmpty(rawValue)) {
                insertSql.append("NULL, ");
            } else {
                insertSql.append("'").append(rawValue).append("', ");
            }
        });
        insertSql.setLength(insertSql.length() - 2);
        insertSql.append("),");
    }

    // 合并单元格特殊处理
    private String getCellStringValue(Sheet sheet, List<CellRangeAddress> regions, int row, int col, String colName,
                                      String date, String sendTime, String mailReadEndDate) {
        if (StringUtils.equals(colName, "data_date") || StringUtils.equals(colName, "date_data")) {
            return LocalDate.parse(date, FORMATTER).minusDays(Long.parseLong(mailReadEndDate) + 1).format(FORMATTER);
        } else if(StringUtils.equals(colName, "send_time")) {
            return sendTime;
        }
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
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(6, RoundingMode.HALF_UP).toPlainString();
                }
            default:
                return cell.toString();
        }
    }
}
