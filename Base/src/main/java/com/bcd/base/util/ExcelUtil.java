package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExcelUtil {
    /**
     * 将值填充到单元格中
     * @param cell
     * @param val
     */
    public static void inputValue(Cell cell, Object val){
        if(val==null){
            cell.setCellValue("");
            return;
        }
        Class clazz= val.getClass();
        if(String.class.isAssignableFrom(clazz)){
            cell.setCellValue((String)val);
        }else if(Number.class.isAssignableFrom(clazz)){
            //为了避免数字可能超过Double最大值,使用String表示数字
            cell.setCellValue(val.toString());
        }else if(Date.class.isAssignableFrom(clazz)){
            //为了避免日期类型转换过去后显示为数字,需要设置单元格格式
            Workbook workbook= cell.getRow().getSheet().getWorkbook();
            DataFormat dataFormat= workbook.createDataFormat();
            CellStyle cellStyle=workbook.createCellStyle();
            cellStyle.setDataFormat(dataFormat.getFormat("yyyy/m/d"));
            cell.setCellStyle(cellStyle);

            cell.setCellValue((Date)val);
        }else if(Boolean.class.isAssignableFrom(clazz)){
            cell.setCellValue((Boolean)val);
        }else if(Calendar.class.isAssignableFrom(clazz)){
            cell.setCellValue((Calendar)val);
        }else if(RichTextString.class.isAssignableFrom(clazz)){
            cell.setCellValue((RichTextString)val);
        }
    }

    public static Object readCell(Cell cell){
        if(cell==null){
            return null;
        }
        CellType cellType=cell.getCellType();
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
     * @param dataList 数据集合
     * @return
     */
    public static XSSFWorkbook exportExcel_2007(List<List> dataList,BiConsumer<Cell,Object> cellBiConsumer){
        try(XSSFWorkbook workBook = new XSSFWorkbook()) {
            Sheet sheet = workBook.createSheet();
            writeSheet(sheet, 1, 1, cellBiConsumer, dataList);
            return workBook;
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 导出excel(.xls)
     * @param dataList 数据集合
     * @return
     */
    public static HSSFWorkbook exportExcel_2003(List<List> dataList,BiConsumer<Cell,Object> cellBiConsumer){
        try(HSSFWorkbook workBook = new HSSFWorkbook()) {
            Sheet sheet = workBook.createSheet();
            writeSheet(sheet, 1, 1, cellBiConsumer, dataList);
            return workBook;
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 将源excel的数据经过加工写入到目标excel中
     * @param sourcePath 源excel
     * @param targetPath 目标excel
     * @param sheetIndex path的sheet编号,从1开始
     * @param beginRowIndex sheet开始的行号,从1开始
     * @param beginColIndex sheet开始的列号,从1开始
     * @param cellBiConsumer 读取cell值的方法
     * @param dataList 数据集合
     */
    public static void writeExcel(final Path sourcePath,final Path targetPath,final int sheetIndex, final int beginRowIndex, final int beginColIndex,BiConsumer<Cell,Object> cellBiConsumer, List<List> dataList){
        try (InputStream is = Files.newInputStream(sourcePath);
             OutputStream os = Files.newOutputStream(targetPath);
             Workbook workbook=WorkbookFactory.create(is)) {
            if(sheetIndex>workbook.getNumberOfSheets()){
                return;
            }
            Sheet sheet= workbook.getSheetAt(sheetIndex-1);
            writeSheet(sheet,beginRowIndex,beginColIndex,cellBiConsumer,dataList);
            workbook.write(os);
        }catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 生成(.xlsx)
     * @param path 文件路径
     * @param dataList 数据集合
     * @param cellBiConsumer 单元格插入方法
     */
    public static void writeExcel_2007(Path path,List<List> dataList,BiConsumer<Cell,Object> cellBiConsumer){
        XSSFWorkbook workbook= exportExcel_2007(dataList, cellBiConsumer);
        FileUtil.createFileIfNotExists(path);
        try(OutputStream os=Files.newOutputStream(path)){
            workbook.write(os);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 生成(.xls)
     * @param path 文件路径
     * @param dataList 数据集合
     * @param cellBiConsumer 单元格插入方法
     */
    public static void writeExcel_2003(Path path,List<List> dataList,BiConsumer<Cell,Object> cellBiConsumer){
        HSSFWorkbook workbook= exportExcel_2003(dataList, cellBiConsumer);
        FileUtil.createFileIfNotExists(path);
        try(OutputStream os=Files.newOutputStream(path)){
            workbook.write(os);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }


    /**
     * 写入数据到sheet
     * 会进行覆盖操作
     * @param sheet 操作的sheet
     * @param beginRowIndex sheet开始的行号,从1开始
     * @param beginColIndex sheet开始的列号,从1开始
     * @param cellBiConsumer 读取cell值的方法
     * @param dataList 数据集合
     */
    public static void writeSheet(Sheet sheet, final int beginRowIndex, final int beginColIndex, BiConsumer<Cell,Object> cellBiConsumer, List<List> dataList){
        if(dataList==null||dataList.isEmpty()){
           return;
        }
        for(int i=0;i<=dataList.size()-1;i++){
            List data= dataList.get(i);
            int rowIndex=i+beginRowIndex-1;
            Row row= sheet.getRow(rowIndex);
            if(row==null){
                row= sheet.createRow(rowIndex);
            }
            for(int j=0;j<=data.size()-1;j++){
                int colIndex=j+beginColIndex-1;
                Cell cell=row.getCell(colIndex);
                if(cell==null){
                    cell=row.createCell(colIndex);
                }
                Object val=data.get(j);
                if(cellBiConsumer==null){
                    inputValue(cell,val);
                }else{
                    cellBiConsumer.accept(cell,val);
                }
            }
        }
    }



    /**
     * 根绝开始行，开始列，结束列。获取excel中的数据
     * 数据格式为clazzArr指定
     * 结束行 为指定的列 为空
     * @param sheet 操作的sheet
     * @param beginRowIndex sheet开始的行号,从1开始
     * @param beginColIndex sheet开始的列号,从1开始
     * @param endColIndex sheet结束的列号,从1开始
     * @param rowFunction 判断结束行 方法
     * @param cellFunction 读取单元格数据 方法
     * @return
     */
    public static List<List> readExcel(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                       final Function<Row,Boolean> rowFunction,final Function<Cell,Object> cellFunction){
        List<List> returnList=new ArrayList<>();
        int startRow=beginRowIndex-1;
        int endRow=sheet.getLastRowNum();
        for(int i=startRow;i<=endRow;i++){
            Row row= sheet.getRow(i);
            boolean isContinue=rowFunction.apply(row);
            if(!isContinue){
                break;
            }
            List<Object> objList=new ArrayList<>();
            for(int j=beginColIndex-1;j<=endColIndex-1;j++){
                Cell cell= row.getCell(j);
                Object val;
                if(cellFunction==null){
                    val=readCell(cell);
                }else{
                    val=cellFunction.apply(cell);
                }
                objList.add(val);
            }
            returnList.add(objList);
        }
        return returnList;
    }

    public static List<List> readExcel(final InputStream is,final int sheetIndex,final int beginRowIndex,final int beginColIndex,final int endColIndex,
                                       final Function<Row,Boolean> rowFunction,final Function<Cell,Object> cellFunction){
        try (Workbook workbook=WorkbookFactory.create(is)){
            if(sheetIndex>workbook.getNumberOfSheets()){
                return new ArrayList<>();
            }
            Sheet sheet= workbook.getSheetAt(sheetIndex-1);
            return readExcel(sheet,beginRowIndex,beginColIndex,endColIndex,rowFunction,cellFunction);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static List<Map<String,Object>> readExcel(final Sheet sheet, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                      final String[] fieldNameArr, final Function<Row,Boolean> rowFunction,final Function<Cell,Object> cellFunction){
        List<List> dataList=readExcel(sheet, beginRowIndex, beginColIndex, endColIndex, rowFunction,cellFunction);
        return parseToJsonArrayData(dataList,fieldNameArr);
    }

    public static List<Map<String,Object>> readExcel(final InputStream is, final int sheetIndex, final int beginRowIndex, final int beginColIndex, final int endColIndex,
                                      final String[] fieldNameArr, final Function<Row,Boolean> rowFunction,final Function<Cell,Object> cellFunction){
        List<List> dataList=readExcel(is,sheetIndex, beginRowIndex, beginColIndex, endColIndex, rowFunction,cellFunction);
        return parseToJsonArrayData(dataList,fieldNameArr);
    }

    private static List<Map<String,Object>> parseToJsonArrayData(List<List> dataList,final String[] fieldNameArr){
        return dataList.stream().map(data->{
            Map<String,Object> jsonObject=new LinkedHashMap<>();
            for (int i=0;i<=data.size()-1;i++) {
                jsonObject.put(fieldNameArr[i],data.get(i));
            }
            return jsonObject;
        }).collect(Collectors.toList());
    }



    public static void main(String [] args) throws IOException {
//        List<List> dataList= Arrays.asList(
//                Arrays.asList("a","b","c","d","a","b","c","d"),
//                Arrays.asList("a","b","c","d","a","b","c","d","1","3"),
//                Arrays.asList("a","b","c","d","a","b","c","d","1","3"),
//                Arrays.asList("a","b","c","d","a","b","c","d","1","3")
//        );
//        overWriteExcel(Paths.get("/Users/baichangda/test.xlsx"),1,1,1,null,dataList);
    }
}
