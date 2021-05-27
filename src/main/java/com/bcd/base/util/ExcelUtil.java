package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ExcelUtil {
    /**
     * 将值填充到单元格中
     *
     * @param cell
     * @param val
     */
    public static void writeToCell(Cell cell, Object val) {
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

    public static Object readFromCell(Cell cell) {
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
     * 导出excel
     *
     * @param workbook
     * @param dataList       数据集合
     * @param cellBiConsumer
     * @return SXSSFWorkbook(注意需要关闭)
     */
    public static void writeToWorkbook(Workbook workbook, List<List> dataList, BiConsumer<Cell, Object> cellBiConsumer) {
        Sheet sheet = workbook.createSheet();
        writeToSheet(sheet, 1, 1, cellBiConsumer, dataList);
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
    public static void writeToSheet(Sheet sheet, final int beginRowIndex, final int beginColIndex, BiConsumer<Cell, Object> cellBiConsumer, List<List> dataList) {
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
                    writeToCell(cell, val);
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
    public static List<List> readFromSheet(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
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
                    val = readFromCell(cell);
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
    public static void readFromSheetWithPause(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
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
                    val = readFromCell(cell);
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

    public static void readFromInputStreamWithPause(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                                    final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction, int pauseNum, Consumer<List<List>> workConsumer) {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (sheetIndex > workbook.getNumberOfSheets()) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
            readFromSheetWithPause(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction, pauseNum, workConsumer);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static List<List> readFromInputStream(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                                 final Function<Row, Boolean> rowFunction, final Function<Cell, Object> cellFunction) {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (sheetIndex > workbook.getNumberOfSheets()) {
                return new ArrayList<>();
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
            return readFromSheet(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction, cellFunction);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

}
