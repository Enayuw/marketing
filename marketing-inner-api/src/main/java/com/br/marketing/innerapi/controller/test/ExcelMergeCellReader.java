package com.br.marketing.innerapi.controller.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelMergeCellReader {

    /**
     * 读取Excel文件，处理合并单元格
     */
    public static List<Map<String, String>> readExcelWithMergeCells(String filePath) {
        List<Map<String, String>> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 读取第一个sheet

            // 获取所有合并区域
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

            // 创建合并单元格值的映射表
            Map<String, String> mergeValueMap = buildMergeValueMap(sheet, mergedRegions);

            // 逐行读取
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                Map<String, String> rowData = processRowData(row, rowIndex, mergeValueMap, mergedRegions);
                result.add(rowData);
            }

        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 构建合并单元格值的映射表
     */
    private static Map<String, String> buildMergeValueMap(Sheet sheet, List<CellRangeAddress> mergedRegions) {
        Map<String, String> mergeValueMap = new HashMap<>();

        for (CellRangeAddress mergedRegion : mergedRegions) {
            int firstRow = mergedRegion.getFirstRow();
            int lastRow = mergedRegion.getLastRow();
            int firstCol = mergedRegion.getFirstColumn();
            int lastCol = mergedRegion.getLastColumn();

            // 获取合并区域第一个单元格的值
            Row firstRowObj = sheet.getRow(firstRow);
            if (firstRowObj != null) {
                Cell firstCell = firstRowObj.getCell(firstCol);
                String mergeValue = getCellValueAsString(firstCell);

                // 为合并区域内的每个单元格创建映射
                for (int r = firstRow; r <= lastRow; r++) {
                    for (int c = firstCol; c <= lastCol; c++) {
                        String key = r + "_" + c;
                        // 只有第一个单元格有实际值，其他单元格值为空但属于合并区域
                        if (r == firstRow && c == firstCol) {
                            mergeValueMap.put(key, mergeValue);
                        } else {
                            // 标记这个位置属于合并单元格，并记录主单元格位置
                            mergeValueMap.put(key, "MERGE:" + firstRow + "_" + firstCol + ":" + mergeValue);
                        }
                    }
                }
            }
        }

        return mergeValueMap;
    }

    /**
     * 处理单行数据
     */
    private static Map<String, String> processRowData(Row row, int rowIndex,
                                                      Map<String, String> mergeValueMap,
                                                      List<CellRangeAddress> mergedRegions) {
        Map<String, String> rowData = new LinkedHashMap<>();

        // 添加行号
        rowData.put("rowNumber", String.valueOf(rowIndex + 1));

        int maxColIndex = getMaxColumnIndex(row);

        for (int colIndex = 0; colIndex <= maxColIndex; colIndex++) {
            String key = "col_" + (colIndex + 1);
            String value = getCellValue(row, colIndex, rowIndex, mergeValueMap);
            rowData.put(key, value);
        }

        return rowData;
    }

    /**
     * 获取单元格值（处理合并单元格）
     */
    private static String getCellValue(Row row, int colIndex, int rowIndex, Map<String, String> mergeValueMap) {
        String mapKey = rowIndex + "_" + colIndex;

        // 检查是否是合并单元格
        if (mergeValueMap.containsKey(mapKey)) {
            String mergeInfo = mergeValueMap.get(mapKey);

            if (mergeInfo.startsWith("MERGE:")) {
                // 这是合并单元格的从属单元格
                String[] parts = mergeInfo.split(":");
                String mainCellValue = parts[2];
                String[] mainCellPos = parts[1].split("_");
                int mainRow = Integer.parseInt(mainCellPos[0]);
                int mainCol = Integer.parseInt(mainCellPos[1]);

                // 计算相对行号（从1开始）
                int relativeRow = rowIndex - mainRow + 1;

                // 按照要求格式返回：合并字段,Y1
                //return mainCellValue + ",Y" + relativeRow;
                return mainCellValue;
            } else {
                // 这是合并单元格的主单元格
                //return mergeInfo + ",Y1";
                return mergeInfo;
            }
        }

        // 普通单元格
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell);
    }

    /**
     * 获取单元格的字符串值
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 获取行的最大列索引
     */
    private static int getMaxColumnIndex(Row row) {
        int maxColIndex = -1;
        for (int i = row.getLastCellNum() - 1; i >= 0; i--) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String value = getCellValueAsString(cell);
                if (!value.isEmpty()) {
                    maxColIndex = i;
                    break;
                }
            }
        }
        return Math.max(maxColIndex, row.getLastCellNum() - 1);
    }

    /**
     * 打印读取结果
     */
    public static void printExcelData(String filePath) {
        List<Map<String, String>> data = readExcelWithMergeCells(filePath);

        for (Map<String, String> row : data) {
            StringBuilder line = new StringBuilder();
            line.append("第").append(row.get("rowNumber")).append("行: ");

            for (int i = 1; i <= row.size() - 1; i++) {
                String key = "col_" + i;
                if (row.containsKey(key)) {
                    line.append(row.get(key)).append(" | ");
                }
            }

            System.out.println(line.toString());
        }
    }
}