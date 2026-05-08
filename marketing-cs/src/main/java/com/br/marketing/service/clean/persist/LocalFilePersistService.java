package com.br.marketing.service.clean.persist;

import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanHeaderTableMapping;
import com.br.marketing.entity.MarketingCleanPersistTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.enums.clean.CleanPersistTaskStatusEnum;
import com.br.marketing.mapper.LocalFilePersistMapper;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanHeaderTableMappingMapper;
import com.br.marketing.mapper.MarketingCleanPersistTaskMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.util.DataCleanDelimiterUtils;
import com.br.marketing.utils.HeaderToColumnUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 本地文件落库：根据 persist_task 读文件并写入动态表
 */
@Service
@Slf4j
public class LocalFilePersistService {

    @Resource
    private MarketingCleanPersistTaskMapper marketingCleanPersistTaskMapper;
    @Resource
    private MarketingCleanHeaderTableMappingMapper marketingCleanHeaderTableMappingMapper;
    @Resource
    private SyncConfigMapper syncConfigMapper;
    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;
    @Resource
    private LocalFilePersistMapper localFilePersistMapper;

    private static final String TABLE_PREFIX = "b_local_file_";
    private static final int BATCH_INSERT_SIZE = 500;

    private static final Pattern QUOTE_PATTERN = Pattern.compile("^\"|\"$");

    /**
     * 处理单条持久化任务：更新为执行中 → 解析/匹配 mapping → 建表(若无) → 读文件写表 → 更新任务结果
     *
     * @param task     持久化任务（status=0）
     * @param fullPath 文件绝对路径（localPath + fileName 拼好）
     */
    public void processTask(MarketingCleanPersistTask task, String fullPath) {
        Long taskId = task.getId();
        try {
            // 1. 解析 file_header（方案 A：task → clean_data_file → 读文件首行）
            String fileHeader = resolveFileHeader(task, fullPath);
            if (!StringUtils.hasText(fileHeader)) {
                log.warn("本地文件落库-无法解析表头，taskId={}, path={}", taskId, fullPath);
                updateTaskFail(taskId, 0);
                return;
            }
            // 表头分列与数据行一致用 sftpFileSeparator；Excel 首行为 POI 按列拼成逗号串，仍按逗号归一化
            String fieldSepForHeader = isExcelFile(task.getFileName()) ? null : task.getSftpFileSeparator();
            String normalizedHeader = HeaderToColumnUtil.normalizeHeaderSchema(fileHeader, fieldSepForHeader);
            if (normalizedHeader.isEmpty()) {
                log.warn("本地文件落库-归一化表头为空，taskId={}", taskId);
                updateTaskFail(taskId, 0);
                return;
            }

            // 2. 更新为执行中
            updateTaskStatus(taskId, CleanPersistTaskStatusEnum.RUNNING.getCode());

            // 3. 精确匹配 mapping：sync_config_id + header_schema
            MarketingCleanHeaderTableMapping mapping = findMapping(task.getSyncConfigId(), normalizedHeader);
            if (mapping == null) {
                mapping = createMappingAndTable(task, normalizedHeader);
                if (mapping == null) {
                    updateTaskFail(taskId, 0);
                    return;
                }
            }

            // 4. 读文件并写入动态表
            int rowCount = readFileAndInsert(fullPath, task.getFileName(), task.getCleanDataFileRecordId(), taskId, mapping,
                    task.getSftpFileSeparator());
            if (rowCount < 0) {
                updateTaskFail(taskId, 0);
                return;
            }

            // 5. 更新任务成功
            MarketingCleanPersistTask update = new MarketingCleanPersistTask();
            update.setId(taskId);
            update.setStatus(CleanPersistTaskStatusEnum.SUCCESS.getCode());
            update.setTotalRowCount(rowCount);
            update.setHeaderMappingId(mapping.getId());
            update.setUpdateTime(new Date());
            marketingCleanPersistTaskMapper.updateByPrimaryKeySelective(update);
            log.warn("本地文件落库成功，taskId={}, table={}, rows={}", taskId, mapping.getTableName(), rowCount);
        } catch (Exception e) {
            log.error("本地文件落库异常，taskId={}, path={}", taskId, fullPath, e);
            updateTaskFail(taskId, 0);
        }
    }

    private String resolveFileHeader(MarketingCleanPersistTask task, String fullPath) {
        if (StringUtils.hasText(task.getFileHeader())) {
            return task.getFileHeader();
        }
        MarketingCleanDataFile dataFile = marketingCleanDataFileMapper.selectByPrimaryKey(task.getCleanDataFileRecordId());
        if (dataFile != null && StringUtils.hasText(dataFile.getFileHeader())) {
            return dataFile.getFileHeader();
        }
        return readFirstLineAsHeader(fullPath, task.getFileName());
    }

    private String readFirstLineAsHeader(String fullPath, String fileName) {
        File file = new File(fullPath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        if (isExcelFile(fileName)) {
            try (FileInputStream fis = new FileInputStream(file); Workbook wb = WorkbookFactory.create(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                Row row = sheet.getRow(0);
                if (row == null) {
                    return null;
                }
                DataFormatter formatter = new DataFormatter();
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    if (c > 0) {
                        sb.append(',');
                    }
                    sb.append(formatter.formatCellValue(row.getCell(c)));
                }
                return sb.toString();
            } catch (Exception e) {
                log.warn("读取 Excel 首行失败: {}", fullPath, e);
                return null;
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String t = line.trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
        } catch (IOException e) {
            log.warn("读取文件首行失败: {}", fullPath, e);
        }
        return null;
    }

    private MarketingCleanHeaderTableMapping findMapping(Long syncConfigId, String normalizedHeaderSchema) {
        com.br.marketing.entity.MarketingCleanHeaderTableMappingExample ex =
                new com.br.marketing.entity.MarketingCleanHeaderTableMappingExample();
        ex.createCriteria().andSyncConfigIdEqualTo(syncConfigId).andHeaderSchemaEqualTo(normalizedHeaderSchema);
        List<MarketingCleanHeaderTableMapping> list = marketingCleanHeaderTableMappingMapper.selectByExample(ex);
        return list.isEmpty() ? null : list.get(0);
    }

    private MarketingCleanHeaderTableMapping createMappingAndTable(MarketingCleanPersistTask task, String normalizedHeader) {
        SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(task.getSyncConfigId());
        if (syncConfig == null || !StringUtils.hasText(syncConfig.getApiCode())) {
            log.warn("本地文件落库-未找到 sync_config 或 api_code 为空，syncConfigId={}", task.getSyncConfigId());
            return null;
        }
        String apiCode = syncConfig.getApiCode();
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        String headerSign = HeaderToColumnUtil.headerSignMd5(normalizedHeader);
        String tableName = TABLE_PREFIX + apiCode + "_" +
                task.getSyncConfigId() + "_" +
                yyMMdd + "_" +
                (headerSign.length() > 12 ? headerSign.substring(0, 12) : headerSign);
        String columnSchemaEn = HeaderToColumnUtil.headerSchemaToColumnSchemaEn(normalizedHeader);

        MarketingCleanHeaderTableMapping mapping = new MarketingCleanHeaderTableMapping();
        mapping.setSyncConfigId(task.getSyncConfigId());
        mapping.setHeaderSchema(normalizedHeader);
        mapping.setHeaderSign(headerSign);
        mapping.setColumnSchemaEn(columnSchemaEn);
        mapping.setTableName(tableName);
        Date now = new Date();
        mapping.setCreateTime(now);
        mapping.setUpdateTime(now);
        marketingCleanHeaderTableMappingMapper.insertSelective(mapping);

        createTable(tableName, columnSchemaEn);
        return mapping;
    }

    private void createTable(String tableName, String columnSchemaEn) {
        String[] cols = columnSchemaEn.split(",");
        StringBuilder ddl = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (")
                .append("`id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,")
                .append("`clean_data_file_record_id` bigint DEFAULT NULL,")
                .append("`persist_task_record_id` bigint DEFAULT NULL,")
                .append("`row_index` int DEFAULT NULL COMMENT '数据在文件中的行号（从1起，表头为第1行）',");
        for (String c : cols) {
            ddl.append("`").append(c.trim()).append("` varchar(512) DEFAULT NULL,");
        }
        ddl.setLength(ddl.length() - 1);
        ddl.append(")");
        localFilePersistMapper.executeDdl(ddl.toString());
    }

    /**
     * 分批读文件并插入：每次最多读 BATCH_INSERT_SIZE 行，插入后继续下一批，避免整文件进内存。
     */
    /**
     * @param sftpFileSeparator 非 Excel 时按此分隔符切分列（与清洗配置 fieldDelimiter 一致）；null 时按逗号
     */
    private int readFileAndInsert(String fullPath, String fileName, Long cleanDataFileRecordId,
                                  Long persistTaskId, MarketingCleanHeaderTableMapping mapping,
                                  String sftpFileSeparator) {
        File file = new File(fullPath);
        if (!file.exists() || !file.isFile()) {
            log.warn("文件不存在: {}", fullPath);
            return -1;
        }
        String[] columns = mapping.getColumnSchemaEn().split(",");
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        if (isExcelFile(fileName)) {
            return readExcelBatchAndInsert(fullPath, columns, cleanDataFileRecordId, persistTaskId, mapping);
        } else {
            return readCsvBatchAndInsert(fullPath, columns, cleanDataFileRecordId, persistTaskId, mapping, sftpFileSeparator);
        }
    }

    /**
     * CSV/文本：流式按行读，攒满一批插入一批，不整文件进内存。按 {@code fieldDelimiter} 字面量分列（非正则）。
     */
    private int readCsvBatchAndInsert(String fullPath, String[] columns, Long cleanDataFileRecordId,
                                      Long persistTaskId, MarketingCleanHeaderTableMapping mapping,
                                      String fieldDelimiter) {
        int total = 0;
        int rowIndex = 2;
        List<List<String>> batch = new ArrayList<>(BATCH_INSERT_SIZE);
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false;
                    continue;
                }
                List<String> cells = new ArrayList<>();
                for (String s : DataCleanDelimiterUtils.splitLine(t, fieldDelimiter)) {
                    cells.add(QUOTE_PATTERN.matcher(s.trim()).replaceAll(""));
                }
                batch.add(cells);
                if (batch.size() >= BATCH_INSERT_SIZE) {
                    int n = insertBatch(mapping.getTableName(), columns, cleanDataFileRecordId, persistTaskId, rowIndex, batch);
                    if (n < 0) {
                        return -1;
                    }
                    total += n;
                    rowIndex += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                int n = insertBatch(mapping.getTableName(), columns, cleanDataFileRecordId, persistTaskId, rowIndex, batch);
                if (n < 0) {
                    return -1;
                }
                total += n;
            }
        } catch (IOException e) {
            log.error("读取文件失败: {}", fullPath, e);
            return -1;
        }
        return total;
    }

    /**
     * Excel：按行遍历，攒满一批插入一批，仅保留当前批在内存（Workbook 仍会加载整个文件，由 POI 限制）。
     */
    private int readExcelBatchAndInsert(String fullPath, String[] columns, Long cleanDataFileRecordId,
                                        Long persistTaskId, MarketingCleanHeaderTableMapping mapping) {
        int total = 0;
        int rowIndex = 2;
        List<List<String>> batch = new ArrayList<>(BATCH_INSERT_SIZE);
        try (FileInputStream fis = new FileInputStream(fullPath); Workbook wb = WorkbookFactory.create(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                return 0;
            }

            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    cells.add(formatter.formatCellValue(row.getCell(c)));
                }
                if (cells.isEmpty()) {
                    continue;
                }
                batch.add(cells);
                if (batch.size() >= BATCH_INSERT_SIZE) {
                    int n = insertBatch(mapping.getTableName(), columns, cleanDataFileRecordId, persistTaskId, rowIndex, batch);
                    if (n < 0) {
                        return -1;
                    }
                    total += n;
                    rowIndex += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                int n = insertBatch(mapping.getTableName(), columns, cleanDataFileRecordId, persistTaskId, rowIndex, batch);
                if (n < 0) {
                    return -1;
                }
                total += n;
            }
        } catch (Exception e) {
            log.error("读取 Excel 失败: {}", fullPath, e);
            return -1;
        }
        return total;
    }

    private int insertBatch(String tableName, String[] columns, Long cleanDataFileRecordId,
                            Long persistTaskId, int startRowIndex, List<List<String>> batch) {
        try {
            String sql = buildBatchInsertSql(tableName, columns, cleanDataFileRecordId, persistTaskId, startRowIndex, batch);
            localFilePersistMapper.executeInsert(sql);
            return batch.size();
        } catch (Exception e) {
            log.error("批量插入失败，table={}", tableName, e);
            return -1;
        }
    }

    private String buildBatchInsertSql(String tableName, String[] columns, Long cleanDataFileRecordId,
                                       Long persistTaskId, int startRowIndex, List<List<String>> batch) {
        StringBuilder sql = new StringBuilder("INSERT INTO `")
                .append(tableName)
                .append("` (`clean_data_file_record_id`,`persist_task_record_id`,`row_index`");
        for (String col : columns) {
            sql.append(",`").append(col).append("`");
        }
        sql.append(") VALUES ");
        for (int i = 0; i < batch.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            List<String> row = batch.get(i);
            int rowIndex = startRowIndex + i;
            sql.append("(").append(cleanDataFileRecordId != null ? cleanDataFileRecordId : "NULL");
            sql.append(",").append(persistTaskId != null ? persistTaskId : "NULL");
            sql.append(",").append(rowIndex);
            for (int c = 0; c < columns.length; c++) {
                String val = c < row.size() ? row.get(c) : "";
                sql.append(",").append(escapeSqlValue(val));
            }
            sql.append(")");
        }
        return sql.toString();
    }

    private static String escapeSqlValue(String value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'";
    }

    private static boolean isExcelFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    private void updateTaskStatus(Long taskId, Integer status) {
        MarketingCleanPersistTask u = new MarketingCleanPersistTask();
        u.setId(taskId);
        u.setStatus(status);
        u.setUpdateTime(new Date());
        marketingCleanPersistTaskMapper.updateByPrimaryKeySelective(u);
    }

    private void updateTaskFail(Long taskId, int totalRowCount) {
        MarketingCleanPersistTask u = new MarketingCleanPersistTask();
        u.setId(taskId);
        u.setStatus(CleanPersistTaskStatusEnum.FAIL.getCode());
        u.setTotalRowCount(totalRowCount);
        u.setUpdateTime(new Date());
        marketingCleanPersistTaskMapper.updateByPrimaryKeySelective(u);
    }
}
