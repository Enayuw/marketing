package com.br.marketing.bridge.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bridge.service.YiXInMailStatisticsInfoService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.BFileBiConfig;
import com.br.marketing.entity.BFileBiConfigExample;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BFileBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisBIMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    private static final String API_CODE = "3710012";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

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
    private TransferFileExtractToDorisMapper transferFileExtractToDorisMapper;

    @Override
    public void transMailToMarketingBiProcess(String jobParam) {
        JSONObject param = JSONObject.parseObject(jobParam);

        LocalDate startDate = Optional.ofNullable(param.getString("startDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);
        LocalDate endDate = Optional.ofNullable(param.getString("endDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);

        // 使用Stream生成日期序列并构造邮件标题列表
        List<String> dates = Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1L)
                .map(date -> date.format(FORMATTER))
                .collect(Collectors.toList());
        String mailPrefix = marketingCommonConfig.getMailApiCodeSubjectMap().get(API_CODE);
        List<String> fileNames = dates.stream().map(mailPrefix::concat).collect(Collectors.toList());
        NfsFileTOBiRecordExample nfsFileTOBiRecordExample = new NfsFileTOBiRecordExample();
        nfsFileTOBiRecordExample.createCriteria().andFileNameIn(fileNames).andBusTypeEqualTo("9");
        // 按照邮件主题分组，获取邮件发送时间
        Map<String, List<String>> mailSendTimesMap = nfsFileTOBiRecordMapper.selectByExample(nfsFileTOBiRecordExample)
                .stream().collect(Collectors.groupingBy(NfsFileTOBiRecord::getFileName,
                        Collectors.mapping(NfsFileTOBiRecord::getSendTime, Collectors.toList())));

        Map<String, String> YiXinMailConfig = marketingCommonConfig.getYiXinMailConfigMap();
        String userName = YiXinMailConfig.get("userName");
        String password = YiXinMailConfig.get("password");

        BFileBiConfig bFileBiConfig = getBFileBiConfig().get(0);
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
                SearchTerm term = new SubjectTerm(mailPrefix.concat(mailDate));
                try {
                    Message[] messages = inbox.search(term);
                    if (ArrayUtils.isEmpty(messages)) {
                        log.warn("未找到邮件:{}", mailPrefix.concat(date));
                        continue;
                    }
                    for (Message message : messages) {
                        try {
                            String sendTime = SIMPLE_DATE_FORMAT.format(message.getSentDate());
                            if (MapUtils.isEmpty(mailSendTimesMap) ||
                                    !mailSendTimesMap.getOrDefault(mailPrefix.concat(date), Lists.newArrayList()).contains(sendTime)) {
                                dealDailyMail(date, message, bFileBiConfig, columns, mailPrefix, sendTime);
                            }
                        } catch (Exception e) {
                            String errMsg = "宜信邮件统计数据抓取任务异常: " + date + " Exception: " + e.getMessage();
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
                        }
                    }
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

    private void dealDailyMail(String date, Message message, BFileBiConfig bFileBiConfig,
                               List<String> columns, String mailPrefix, String sendTime) throws Exception {
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
            transferFileExtractToDorisMapper.insertDataToMarketingBiTable(insertSql.toString());

            NfsFileTOBiRecord record = new NfsFileTOBiRecord();
            record.setApiCode(API_CODE);
            record.setFilePath(mailPrefix.concat(date));
            record.setFileName(mailPrefix.concat(date));
            record.setExecuteDate(date);
            record.setSendTime(sendTime);
            record.setBusType("9");
            nfsFileTOBiRecordMapper.insertSelective(record);
        }
    }

    private List<BFileBiConfig> getBFileBiConfig() {
        BFileBiConfigExample example = new BFileBiConfigExample();
        example.createCriteria().andApiCodeEqualTo(API_CODE).andBusTypeEqualTo("9");
        List<BFileBiConfig> bFileBiConfigs = bFileBiConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bFileBiConfigs)) {
            String errMsg = "apiCode: " + API_CODE + " nfs转化提取文件落库到marketingBI没有找到对应的配置信息";
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
