package com.br.marketing.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 按文件后缀区分 CSV / Excel，读取表头与样例数据行，统一为逗号分隔的一行字符串格式，
 * 供 fileDataAssemble 等下游使用。
 */
@Slf4j
public final class CleanDataFileReader {

    private static final int DEFAULT_MAX_DATA_ROWS = 10;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * 表头行（逗号分隔） + 数据行列表（每行逗号分隔）
     */
    @Getter
    @AllArgsConstructor
    public static class HeaderAndLines {
        private final String headerLine;
        private final List<String> dataLines;
    }

    /**
     * 根据 fileName 后缀选择 CSV 或 Excel 读取，返回表头与最多 maxDataRows 行数据（逗号分隔）。
     *
     * @param file         本地文件
     * @param fileName     文件名，用于判断后缀
     * @param maxDataRows  最多读取的数据行数（不含表头）
     * @return 表头一行 + 数据行列表；读失败或无表头时 headerLine 可能为空、dataLines 可能为空列表
     */
    public static HeaderAndLines read(File file, String fileName, int maxDataRows) {
        if (file == null || !file.exists() || !file.isFile()) {
            log.warn("文件不存在或不是文件: {}", file);
            return new HeaderAndLines("", new ArrayList<>());
        }
        String suffix = getSuffix(fileName);
        if (".xlsx".equals(suffix) || ".xls".equals(suffix)) {
            return readExcel(file, maxDataRows);
        } else {
            return readCsv(file, maxDataRows);
        }
    }

    public static HeaderAndLines read(File file, String fileName) {
        return read(file, fileName, DEFAULT_MAX_DATA_ROWS);
    }

    private static String getSuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        int i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i).toLowerCase() : "";
    }

    /**
     * CSV：按文本行读，第一非空行作为表头，后续非空行作为数据（最多 maxDataRows 行）。
     */
    static HeaderAndLines readCsv(File file, int maxDataRows) {
        String headerLine = "";
        List<String> dataLines = new ArrayList<>();
        int dataRowCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String row;
            while (dataRowCount <= maxDataRows && (row = br.readLine()) != null) {
                String trimmed = row.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (headerLine.isEmpty()) {
                    headerLine = trimmed;
                } else {
                    dataLines.add(trimmed);
                    dataRowCount++;
                }
            }
        } catch (Exception e) {
            log.error("读取 CSV 文件失败: {}", file.getAbsolutePath(), e);
        }
        return new HeaderAndLines(headerLine, dataLines);
    }

    /**
     * Excel：第一个 sheet，第一行为表头，后续行作为数据（最多 maxDataRows 行），单元格用逗号拼接。
     */
    static HeaderAndLines readExcel(File file, int maxDataRows) {
        String headerLine = "";
        List<String> dataLines = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new HeaderAndLines("", new ArrayList<>());
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                headerLine = rowToCsvLine(headerRow);
            }
            int endRow = Math.min(sheet.getLastRowNum(), maxDataRows);
            for (int r = 1; r <= endRow; r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    dataLines.add(rowToCsvLine(row));
                }
            }
        } catch (Exception e) {
            log.error("读取 Excel 文件失败: {}", file.getAbsolutePath(), e);
        }
        return new HeaderAndLines(headerLine, dataLines);
    }

    private static String rowToCsvLine(Row row) {
        List<String> cells = new ArrayList<>();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            cells.add(cellToString(cell));
        }
        return String.join(",", cells);
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell);
    }
}
