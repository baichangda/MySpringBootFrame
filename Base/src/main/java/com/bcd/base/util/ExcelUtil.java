package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelUtil {
    /**
     * 将值填充到单元格中
     *
     * @param cell
     * @param val
     */
    public static void inputValue(Cell cell, Object val) {
        if (val == null) {
            cell.setCellValue("");
            return;
        }
        Class clazz = val.getClass();
        if (String.class.isAssignableFrom(clazz)) {
            cell.setCellValue((String) val);
        } else if (Number.class.isAssignableFrom(clazz)) {
            //为了避免数字可能超过Double最大值,使用String表示数字
            cell.setCellValue(val.toString());
        } else if (Date.class.isAssignableFrom(clazz)) {
            //为了避免日期类型转换过去后显示为数字,需要设置单元格格式
            Workbook workbook = cell.getRow().getSheet().getWorkbook();
            DataFormat dataFormat = workbook.createDataFormat();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(dataFormat.getFormat("yyyy/m/d"));
            cell.setCellStyle(cellStyle);

            cell.setCellValue((Date) val);
        } else if (Boolean.class.isAssignableFrom(clazz)) {
            cell.setCellValue((Boolean) val);
        } else if (Calendar.class.isAssignableFrom(clazz)) {
            cell.setCellValue((Calendar) val);
        } else if (RichTextString.class.isAssignableFrom(clazz)) {
            cell.setCellValue((RichTextString) val);
        }
    }

    public static Object readCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case BLANK: {
                return null;
            }
            case BOOLEAN: {
                return cell.getBooleanCellValue();
            }
            case NUMERIC: {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            }
            case STRING: {
                return cell.getStringCellValue();
            }
            case FORMULA: {
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return cell.getNumericCellValue();
                }
            }
            default: {
                return cell.toString();
            }
        }
    }

    /**
     * 导出excel(.xlsx)
     *
     * @param dataList 数据集合
     * @return XSSFWorkbook(注意需要关闭)
     */
    public static XSSFWorkbook exportExcel_2007(List<List> dataList, BiConsumer<Cell, Object> cellBiConsumer) {
        XSSFWorkbook workBook = new XSSFWorkbook();
        Sheet sheet = workBook.createSheet();
        writeSheet(sheet, 1, 1, cellBiConsumer, dataList);
        return workBook;
    }

    /**
     * 导出excel(.xls)
     *
     * @param dataList 数据集合
     * @return HSSFWorkbook(注意需要关闭)
     */
    public static HSSFWorkbook exportExcel_2003(List<List> dataList, BiConsumer<Cell, Object> cellBiConsumer) {
        HSSFWorkbook workBook = new HSSFWorkbook();
        Sheet sheet = workBook.createSheet();
        writeSheet(sheet, 1, 1, cellBiConsumer, dataList);
        return workBook;
    }

    /**
     * 将源excel的数据经过加工写入到目标excel中
     *
     * @param sourcePath     源excel
     * @param targetPath     目标excel
     * @param sheetIndex     path的sheet编号,从1开始
     * @param beginRowIndex  sheet开始的行号,从1开始
     * @param beginColIndex  sheet开始的列号,从1开始
     * @param cellBiConsumer 读取cell值的方法
     * @param dataList       数据集合
     */
    public static void writeExcel(final Path sourcePath, final Path targetPath, final int sheetIndex, final int beginRowIndex, final int beginColIndex, BiConsumer<Cell, Object> cellBiConsumer, List<List> dataList) {
        try (InputStream is = Files.newInputStream(sourcePath);
             OutputStream os = Files.newOutputStream(targetPath);
             Workbook workbook = WorkbookFactory.create(is)) {
            if (sheetIndex > workbook.getNumberOfSheets()) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
            writeSheet(sheet, beginRowIndex, beginColIndex, cellBiConsumer, dataList);
            workbook.write(os);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 生成(.xlsx)
     *
     * @param path           文件路径
     * @param dataList       数据集合
     * @param cellBiConsumer 单元格插入方法
     */
    public static void writeExcel_2007(Path path, List<List> dataList, BiConsumer<Cell, Object> cellBiConsumer) {
        FileUtil.createFileIfNotExists(path);
        try (OutputStream os = Files.newOutputStream(path);
             XSSFWorkbook workbook = exportExcel_2007(dataList, cellBiConsumer)) {
            workbook.write(os);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 生成(.xls)
     *
     * @param path           文件路径
     * @param dataList       数据集合
     * @param cellBiConsumer 单元格插入方法
     */
    public static void writeExcel_2003(Path path, List<List> dataList, BiConsumer<Cell, Object> cellBiConsumer) {
        FileUtil.createFileIfNotExists(path);
        try (OutputStream os = Files.newOutputStream(path);
             HSSFWorkbook workbook = exportExcel_2003(dataList, cellBiConsumer)) {
            workbook.write(os);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }


    /**
     * 写入数据到sheet
     * 会进行覆盖操作
     *
     * @param sheet          操作的sheet
     * @param beginRowIndex  sheet开始的行号,从1开始
     * @param beginColIndex  sheet开始的列号,从1开始
     * @param cellBiConsumer 读取cell值的方法
     * @param dataList       数据集合
     */
    public static void writeSheet(Sheet sheet, final int beginRowIndex, final int beginColIndex, BiConsumer<Cell, Object> cellBiConsumer, List<List> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        for (int i = 0; i <= dataList.size() - 1; i++) {
            List data = dataList.get(i);
            int rowIndex = i + beginRowIndex - 1;
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            for (int j = 0; j <= data.size() - 1; j++) {
                int colIndex = j + beginColIndex - 1;
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                Object val = data.get(j);
                if (cellBiConsumer == null) {
                    inputValue(cell, val);
                } else {
                    cellBiConsumer.accept(cell, val);
                }
            }
        }
    }


    /**
     * 根绝开始行，开始列，结束列。获取excel中的数据
     * 数据格式为clazzArr指定
     * 结束行 为指定的列 为空
     *
     * @param sheet         操作的sheet
     * @param beginRowIndex sheet开始的行号,从1开始
     * @param beginColIndex sheet开始的列号,从1开始
     * @param endColIndex   sheet结束的列号,从1开始
     * @param rowFunction   判断结束行 方法
     * @param cellFunction  读取单元格数据 方法
     * @return
     */
    public static List<List> readSheet(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                       final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction) {
        List<List> returnList = new ArrayList<>();
        int startRow = beginRowIndex - 1;
        int endRow = sheet.getLastRowNum();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            boolean isContinue = rowFunction.apply(row);
            if (!isContinue) {
                break;
            }
            List<Object> objList = new ArrayList<>();
            for (int j = beginColIndex - 1; j <= endColIndex - 1; j++) {
                Cell cell = row.getCell(j);
                Object val;
                if (cellFunction == null) {
                    val = readCell(cell);
                } else {
                    val = cellFunction.apply(cell);
                }
                objList.add(val);
            }
            returnList.add(objList);
        }
        return returnList;
    }

    /**
     * 读取sheet,在积累到一定的数据量之后,暂停读取,先执行任务
     *
     * @param sheet         操作的sheet
     * @param beginRowIndex sheet开始的行号,从1开始
     * @param beginColIndex sheet开始的列号,从1开始
     * @param endColIndex   sheet结束的列号,从1开始
     * @param rowFunction   判断结束行 方法
     * @param cellFunction  读取单元格数据 方法
     * @param pauseNum      暂停的数量
     * @param workConsumer  执行任务 方法
     */
    public static void readSheetAndPauseWork(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                             final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction, int pauseNum, Consumer<List<List>> workConsumer) {
        List<List> tempList = new ArrayList<>();
        int startRow = beginRowIndex - 1;
        int endRow = sheet.getLastRowNum();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            boolean isContinue = rowFunction.apply(row);
            if (!isContinue) {
                break;
            }
            List<Object> objList = new ArrayList<>();
            for (int j = beginColIndex - 1; j <= endColIndex - 1; j++) {
                Cell cell = row.getCell(j);
                Object val;
                if (cellFunction == null) {
                    val = readCell(cell);
                } else {
                    val = cellFunction.apply(cell);
                }
                objList.add(val);
            }
            tempList.add(objList);
            if (tempList.size() % pauseNum == 0) {
                workConsumer.accept(tempList);
                tempList = new ArrayList<>();
            }
        }
        workConsumer.accept(tempList);
    }

    public static void readExcelAndPauseWork(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                             final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction, int pauseNum, Consumer<List<List>> workConsumer) {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (sheetIndex > workbook.getNumberOfSheets()) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
            readSheetAndPauseWork(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction, pauseNum, workConsumer);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }

    }

    public static List<List> readExcel(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                       final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction) {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (sheetIndex > workbook.getNumberOfSheets()) {
                return new ArrayList<>();
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
            return readSheet(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static List<Map<String, Object>> readSheet(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                                      final String[] fieldNameArr, final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction) {
        List<List> dataList = readSheet(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction);
        return parseToJsonArrayData(dataList, fieldNameArr);
    }

    public static List<Map<String, Object>> readExcel(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                                      final String[] fieldNameArr, final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction) {
        List<List> dataList = readExcel(is, sheetIndex, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction);
        return parseToJsonArrayData(dataList, fieldNameArr);
    }


    private static List<Map<String, Object>> parseToJsonArrayData(List<List> dataList, final String[] fieldNameArr) {
        return dataList.stream().map(data -> {
            Map<String, Object> jsonObject = new LinkedHashMap<>();
            for (int i = 0; i <= data.size() - 1; i++) {
                jsonObject.put(fieldNameArr[i], data.get(i));
            }
            return jsonObject;
        }).collect(Collectors.toList());
    }


    public static void main(String[] args) throws IOException {
        Path dir=Paths.get("/Users/baichangda/bcd/workspace/vcp-32960-gateway/NettyServer/src/main/java/com/incarcloud/nettyserver/tcp/data");
        Set<String> set=new HashSet<>();
        set.add("Packet.java");
        set.add("PacketData.java");
        Path output=Paths.get("/Users/baichangda/output.xlsx");
        Files.deleteIfExists(output);
        Workbook workbook=new XSSFWorkbook();
        Sheet sheet=workbook.createSheet();
        List<List> dataList=new ArrayList<>();
        Files.list(dir).filter(e->!set.contains(e.getFileName().toString())).forEach(p->{
            String fileName=p.getFileName().toString().split("\\.")[0];
            dataList.add(Arrays.asList("类型名称",fileName));
            dataList.add(Arrays.asList("字段","类型","描述"));
            try(BufferedReader br=Files.newBufferedReader(p)){
                String line;
                String comment="";
                while((line=br.readLine())!=null){
                    List data=new ArrayList();
                    line=line.trim();
                    if(!line.isEmpty()&&!line.contains("return ")&&!line.contains("this.")&&!line.contains("(")
                            &&!line.contains(")")&&!line.contains("@")&&!line.contains("import ")
                            &&!line.contains("package ")&&!line.contains("*")&&!line.contains("}")) {
                        if (line.contains(" class ")) {
                            int begin = line.indexOf(" class ") + " class ".length();
                            int end = line.indexOf(" ", begin);
                            String className = line.substring(begin, end);
                            data.add(className);
                        } else if (line.contains("//")) {
                            int begin = line.indexOf("//") + "//".length();
                            comment = line.substring(begin);
                        } else{
                            String[] fieldArr=line.split(" ");
                            String type=fieldArr[0].trim();
                            fieldArr[1]=fieldArr[1].trim();
                            String fieldName=fieldArr[1].substring(0,fieldArr[1].length()-1);
                            data.add(fieldName);
                            data.add(type);
                            if(fieldName.contains("Hex")){
                                data.add(comment+"16进制");
                            }else{
                                data.add(comment);
                            }
                            dataList.add(data);
                        }
                    }
                }
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
            dataList.add(Arrays.asList());
            dataList.add(Arrays.asList());
        });
        ExcelUtil.writeSheet(sheet,1,1,(cell,val)->ExcelUtil.inputValue(cell,val),dataList);
        try(OutputStream os=Files.newOutputStream(output)){
            workbook.write(os);
        }

    }
}
