/*
import com.alibaba.fastjson.JSONObject;
import com.br.phone.check.constant.ParamConstant;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

*
 * 生成excel的工具类


@Slf4j
public class ExcelUtil {

*
     * 自动调整列宽
     * @param sheet sheet
     * @param headList headList


    private static void adjustColumnSize(Sheet sheet, String[] headList) {
        for (int i = 0; i < headList.length; i++) {
            sheet.autoSizeColumn((short)i);
            sheet.setColumnWidth(i, headList[i].getBytes().length*2*256);
        }
    }

*
     * 创建表头
     * @param sheet sheet
     * @param rowNo rowNo
     * @param columnName columnName


    public static void createColumnHeader(Sheet sheet, int rowNo, String[] columnName
            , CellStyle cellStyle){
        adjustColumnSize(sheet,columnName);
        Row titleRow = sheet.createRow(rowNo);
        for(int i=0;i<columnName.length;i++){
            //设置所有内容为文本格式
            sheet.setDefaultColumnStyle(i,defaultStyle(sheet));
            Cell cell = titleRow.createCell(i);
            if(cellStyle!=null) {
                cell.setCellStyle(cellStyle);
            }
            cell.setCellValue(columnName[i]);
        }
    }

    public static CellStyle defaultStyle(Sheet sheet){
        Workbook workbook = sheet.getWorkbook();
        //设置为文本格式
        DataFormat format = workbook.createDataFormat();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(format.getFormat("@"));
//        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 指定单元格垂直居中对齐
        cellStyle.setWrapText(true);
        return cellStyle;
    }

*
     * 添加数据
     * @param sheet sheet
     * @param rowNo rowNo
     * @param columns columns
     * @param data data


    public static void createColumnData(Sheet sheet, int rowNo, String[] columns
            , JSONObject data, CellStyle cellStyle){
        Row row = sheet.createRow(rowNo);
        for(int i=0;i<columns.length;i++){
            Cell cell = row.createCell(i);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            if(cellStyle==null){
                cell.setCellStyle(defaultStyle(sheet));
            }else{
                cell.setCellStyle(cellStyle);
            }
            String str = data.getString(columns[i]);
            if(null != str){
                if(i == 0){
                    //格式化时间
                    str = getFormatTime(str);
                }else if(i == 5){
                    //转换核查结果
                    str = ParamConstant.PARAM_MAP_CHECK_RESULT_TYPE.get(Integer.valueOf(str));
                }else if(i== 8){
                    //查询方式
                    str = ParamConstant.PARAM_MAP_QUERY_TYPE.get(Integer.valueOf(str));
                }else if(i == 9){
                    //核查状态
                    str = ParamConstant.PARAM_MAP_CHECK_STATUS.get(Integer.valueOf(str));
                }
                cell.setCellValue(str);
            }else{
                cell.setCellValue("");
            }

        }
    }


    private static String getFormatTime(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.valueOf(dateStr));
        return sdf.format(c.getTime());
    }


    private static void processExcelData(String sourcePath,String targetPath,String idsPath){

        List<String> files =  FileUtil.getAllFileNameInFold(sourcePath);
        Map<String,String> map = init(idsPath);
        for(String fileName:files){
            if(fileName.endsWith("id11.txt")){
                List<String> ids = FileUtil.readLogByList(fileName);
                for(String id:ids){
                    if(map.containsKey(id)){
                        System.out.println(map.get(id));
                    }else{
                        System.out.println(id);
                    }
                }
            }
        }
    }

    private static Map<String,String> init(String idsPath){
        Map<String,String> map = new HashMap<String,String>();
        List<String> ids = FileUtil.readLogByList(idsPath);
        for(String id:ids){
            String [] arr = id.split("\t");
            map.put(arr[0],arr[1]);
        }
        return map;
    }

    private static void copyExcel(String fileName, Map<String,String> map){
        File  sourceFile = new File(fileName);
        InputStream sis = null;
        try {
            sis = new FileInputStream(sourceFile);
            Workbook sWorkbook = new XSSFWorkbook(sis);
            long st = System.currentTimeMillis();
            for(int i =0 ;i<sWorkbook.getNumberOfSheets();i++){
                Sheet sheet = sWorkbook.getSheetAt(i);
                long sheetSt = System.currentTimeMillis();
                for(int j=0;j<sheet.getLastRowNum();j++){
                    Row row = sheet.getRow(j);
                    for(int k=0;k<row.getLastCellNum();k++){
                        System.out.println(row.getCell(k).getStringCellValue());
                    }
                }
                long sheetEt = System.currentTimeMillis();
                System.out.println("遍历sheet"+i+"耗时:"+(sheetEt-sheetSt));
            }
            long et = System.currentTimeMillis();
            System.out.println("遍历总时间:"+(et-st));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateExcelDemo(SXSSFWorkbook workbook,String fileName){
        int sheetCount = 20;
        int columns = 500;
        int rows = 10000;
        long st = System.currentTimeMillis();
        for(int i=0;i<sheetCount;i++){
            long st1 = System.currentTimeMillis();
            Sheet sheet = workbook.createSheet("列表"+i);

            for(int j=0;j<rows;j++){
                Row row = sheet.createRow(j);
                for(int k=0;k<columns;k++){
                    try{
                        Cell cell = row.createCell(k);
                        cell.setCellValue(fileName+" i:"+i+" j:"+j+" k:"+k);
                        cell = null;
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("sheet:"+i+" rows:"+j+" cells:"+k);
                    }
                }
                row = null;
            }
            sheet = null;
            long et = System.currentTimeMillis();
            System.out.println("生产sheet"+i+"完成 耗时:"+(et-st1)/1000);
        }
        long total = System.currentTimeMillis();
        System.out.println("生产总数据耗时:"+(total-st)/1000);
        try{
            File file = new File(fileName+".xlsx");
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.flush();
            out.close();
            long fileFinshedTime = System.currentTimeMillis();
            System.out.println("生产文件总数据耗时:"+(fileFinshedTime-total)/1000);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void mergeExcel(String sourceFilepath,String targetFilePath){
        try {
            File  sourceFile = new File(sourceFilepath);
            File  targetFile = new File(targetFilePath);
            InputStream sis = null;
            InputStream tis = null;
            sis = new FileInputStream(sourceFile);
            tis = new FileInputStream(targetFile);
            Workbook sWorkbook = new XSSFWorkbook(sis);
            Workbook tWorkbook = new XSSFWorkbook(tis);
            long mergeSt = System.currentTimeMillis();
            for(int i=0;i<tWorkbook.getNumberOfSheets();i++){
                Sheet tSheet = tWorkbook.getSheetAt(i);
                String tSheetName = tSheet.getSheetName();
                for(int j=0;j<sWorkbook.getNumberOfSheets();j++){
                    Sheet sSheet = sWorkbook.getSheetAt(j);
                    String sSheetName = sSheet.getSheetName();
                    if(tSheetName.equals(sSheetName)){
                        int lastRow = tSheet.getLastRowNum();
                        for(int row =0;row<sSheet.getLastRowNum();row++){
                            int tRowNum = lastRow+1+row;
                            Row sRow = sSheet.getRow(row);
                            Row tRow = tSheet.createRow(tRowNum);
                            for(int column = 0;column<sRow.getLastCellNum();column++){
                                Cell cell = tRow.createCell(column);
                                cell.setCellValue(sRow.getCell(column).getStringCellValue());
                                cell = null;
                            }
                            sRow = null;
                        }
                        sSheet = null;
                    }else{
                        continue;
                    }
                }
                tSheet = null;
            }
            long mergeEt = System.currentTimeMillis();
            FileOutputStream out = new FileOutputStream(targetFile);
            tWorkbook.write(out);
            out.flush();
            out.close();
            System.out.println("合并总耗时:"+(mergeEt-mergeSt)/1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static Map<String,LinkedHashMap<String,Integer>> header = new LinkedHashMap<String,LinkedHashMap<String,Integer>>();
    private static Map<String,LinkedHashMap<String,LinkedHashMap<Integer,String>>> fileMapper = new LinkedHashMap<String,LinkedHashMap<String,LinkedHashMap<Integer,String>>>();


    public static void main(String args[]){
        long headerSt = System.currentTimeMillis();
        getTitleInfo("D:\\xingyeExcel\\source");
        long headerEt = System.currentTimeMillis();
        copyXlsx("D:\\xingyeExcel\\source","D:\\xingyeExcel\\target\\merger.xlsx");
        long allEnd = System.currentTimeMillis();
        System.out.println("生成表头耗时:"+(headerEt-headerSt)+" 复制数据耗时:"+(allEnd-headerEt)+" 总耗时:"+(allEnd-headerSt));


//        String sheetName ="特殊名单验证";
//        String dir = "E:\\xingyeExcel\\fenhang";
//        Map<String,List<Integer>> map = getErrorData(sheetName,dir);
//        dealErrorData(map,sheetName,dir);


    }

    private static void dealErrorData(Map<String,List<Integer>>map,String sheetName,String dir){
        List<String> fileNames = FileUtil.getAllFileNameInFold(dir);
        for(String fileName:fileNames){
            File file = new File(fileName);
            FileInputStream in = null;
            try{
                in = new FileInputStream(file);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Workbook sourceWB = null;
            Sheet sourceSheet = null;
            try {
                sourceWB = new XSSFWorkbook(in);
                sourceSheet = sourceWB.getSheet(sheetName);
            } catch (IOException e) {
                e.printStackTrace();
            }catch (Exception e){
                //创建失败后说明本excel不存在该sheet页,处理下个文件
                System.out.println("文件:"+fileName+"创建sheet:"+sheetName+"失败");
                continue;
            }
            int rowId = 1;
            List<Integer> rowNums = map.get(fileName);
            if(null == rowNums || rowNums.isEmpty())
                continue;
            for (Row row : sourceSheet) {
                int index = 0;
                for(Cell cell:row) {
                    String value = cell.getStringCellValue();
                    if(index == 7 && rowNums.contains(rowId)){
                        Cell newCell = row.createCell(7);
                        newCell.setCellValue("");
                        System.out.println("处理文件:"+fileName+" 第"+rowId+"行数据:"+value);
                    }
                    index++;
                }
                rowId++;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                sourceWB.write(out);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*
     * 获取错误数据


    private static Map<String,List<Integer>> getErrorData(String sheetName,String dir){
        List<String> fileNames = FileUtil.getAllFileNameInFold(dir);
        Map<String,List<Integer>> map = new LinkedHashMap<String,List<Integer>>();
        int errorCount = 0;
        for(String fileName:fileNames){
            List<Integer> rowList = new ArrayList<>();
            int rowId = 1;
            FileInputStream in = null;
            File file = new File(fileName);
            try{
                in = new FileInputStream(file);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Workbook sourceWB = StreamingReader.builder()
                    .rowCacheSize(1000)  //缓存到内存中的行数，默认是10
                    .bufferSize(40960)  //读取资源时，缓存到内存的字节大小，默认是1024
                    .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件
            Sheet sourceSheet = null;
            try{
                sourceSheet = sourceWB.getSheet(sheetName);
            }catch (Exception e){
                //创建失败后说明本excel不存在该sheet页,处理下个文件
                System.out.println("文件:"+fileName+"创建sheet:"+sheetName+"失败");
                continue;
            }
            boolean headFlag = true;
            int headCount = 0;
            for (Row row : sourceSheet) {
                int index = 0;
                boolean flag = true;
                for(Cell cell:row){
                    //flag  8
                    String value = cell.getStringCellValue();
                    if((!headFlag)&&(index == 7)){
                        if(!value.equals("1")){
                            flag = false;
                            break;
                        }
                    }else if(index>7){
                        if((!headFlag)&&(!value.isEmpty())){
                            flag = false;
                            break;
                        }
                    }
                    index++;
                    if((!headFlag)&&(index == headCount) && flag){
                        errorCount++;
                        rowList.add(rowId);
                        System.out.println(fileName+" "+rowId);
                    }
                }
                if(headFlag){
                    headCount = index;
                    headFlag =false;
                }
                rowId++;
            }
            if(!rowList.isEmpty()){
                map.put(fileName,rowList);
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("错误总记录数:"+errorCount);
        return map;
    }
    private static void createHeader(Sheet sheet,int rowNum){
        Row row = sheet.createRow(rowNum);
        LinkedHashMap<String,Integer> columnMap = header.get(sheet.getSheetName());
        int columnIndex = 0;
        for(String key:columnMap.keySet()){
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(key);
            columnIndex++;
        }
    }
    private static void createData(Sheet sheet,String source){
        FileInputStream in = null;
        List<String> fileNames = FileUtil.getAllFileNameInFold(source);
        int dataRow = 1;//数据开始记录数
        for(String fileName:fileNames) {
            try{
                in = new FileInputStream(fileName);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Workbook sourceWB = StreamingReader.builder()
                    .rowCacheSize(1000)  //缓存到内存中的行数，默认是10
                    .bufferSize(40960)  //读取资源时，缓存到内存的字节大小，默认是1024
                    .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件
            Sheet sourceSheet = null;
            try{
                sourceSheet = sourceWB.getSheet(sheet.getSheetName());
            }catch (Exception e){
                //创建失败后说明本excel不存在该sheet页,处理下个文件
                System.out.println("文件:"+fileName+"创建sheet:"+sheet.getSheetName()+"失败");
                continue;
            }
            //对应文件下面对应sheet页每列对应的表头信息
            LinkedHashMap<Integer,String> sourceMap = fileMapper.get(fileName).get(sheet.getSheetName());
            LinkedHashMap<String,Integer> targetMap = header.get(sheet.getSheetName());
            boolean headFlag = true;
            for (Row row : sourceSheet) {
                //跳过表头
                if(headFlag){
                    headFlag = false;
                    continue;
                }
                Row tRow =sheet.createRow(dataRow);
                int columnIndex = 0;
                //遍历所有的列
                for (Cell cell : row) {
                    //跳过空的单元格
                    if(null == cell)
                        continue;
                    String value = cell.getStringCellValue();
                    String column = sourceMap.get(columnIndex);
                    //跳过无效列
                    if(null == column||column.isEmpty())
                        continue;
                    int targetCellIndex = targetMap.get(column);
                    Cell targetCell = tRow.createCell(targetCellIndex);
                    targetCell.setCellValue(value);
                    columnIndex++;
                    //本行循环完成后,重置列索引
                    if(row.getLastCellNum() == columnIndex){
                        columnIndex = 0;
                    }
                }
                if(dataRow%1000 == 0){
                    System.out.println("当前处理文件名:"+ fileName+"  当前处理sheet页为:"+sheet.getSheetName()+"  数据复制记录数:"+dataRow);
                }
                dataRow++;
            }
        }
        System.out.println(sheet.getSheetName()+"总记录数:"+dataRow+" 最后一行记录编号:"+sheet.getLastRowNum());
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void copyXlsx(String source,String targetFile){
        FileOutputStream out = null;
        try {
            SXSSFWorkbook targetWB = new SXSSFWorkbook(1000);
            for(String sheetName:header.keySet()){
                Sheet sheet = targetWB.createSheet(sheetName);
                //生成表头
                createHeader(sheet,0);
                long dataSt = System.currentTimeMillis();
                //写数据到sheet页
                createData(sheet,source);
                long dataEt = System.currentTimeMillis();
                System.out.println("复制"+sheet.getSheetName()+"合计耗时:"+(dataEt-dataSt));
            }
            out = new FileOutputStream(targetFile);
            targetWB.write(out);
            //数据写入硬盘
            targetWB.dispose();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void getTitleInfo(String dirPath){
        FileInputStream in = null;
        List<String> fileNames = FileUtil.getAllFileNameInFold(dirPath);
       for(String fileName:fileNames){
            LinkedHashMap<String,LinkedHashMap<Integer,String>> fileSheetMapper = new LinkedHashMap<String,LinkedHashMap<Integer,String>>();
            try {
                in = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Workbook wk = StreamingReader.builder()
                    .rowCacheSize(1000)  //缓存到内存中的行数，默认是10
                    .bufferSize(40960)  //读取资源时，缓存到内存的字节大小，默认是1024
                    .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件

            for(int i=0;i<wk.getNumberOfSheets();i++){
                LinkedHashMap<Integer,String> sheetColumnMapper = new LinkedHashMap<Integer,String>();
                Sheet sheet = wk.getSheetAt(i);
                String sheetName =sheet.getSheetName();
                //跳过数据字典处理
                if(sheetName.endsWith("字典"))
                    continue;
                LinkedHashMap<String,Integer> set = null;
                if(header.containsKey(sheetName)){
                    set = header.get(sheetName);
                }else{
                    set = new LinkedHashMap<String,Integer>();
                }
                //遍历所有的行
                for (Row row : sheet) {
                    int columnIndex = 0;
                    int setSize = set.size();
                    //遍历所有的列
                    for (Cell cell : row) {
                        String column = cell.getStringCellValue();
                        if((null != column)&&(!"".equals(column))&&(!set.containsKey(column))){
                            set.put(column,setSize);
                            setSize++;
                        }
                        sheetColumnMapper.put(columnIndex,column);
                        columnIndex++;

                    }
                    header.put(sheetName, set);
                    break;
                }
                fileSheetMapper.put(sheetName,sheetColumnMapper);
            }
            fileMapper.put(fileName,fileSheetMapper);
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/
