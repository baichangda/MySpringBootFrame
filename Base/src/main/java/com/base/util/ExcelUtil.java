package com.base.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.RichTextString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExcelUtil {

    /**
     * 将值填充到单元格中
     * @param cell
     * @param val
     */
    private static void inputValue(HSSFCell cell,Object val){
        Class clazz= val.getClass();
        if(String.class.isAssignableFrom(clazz)){
            cell.setCellValue((String)val);
        }else if(Double.class.isAssignableFrom(clazz)){
            cell.setCellValue((Double)val);
        }else if(Date.class.isAssignableFrom(clazz)){
            cell.setCellValue((Date)val);
        }else if(Boolean.class.isAssignableFrom(clazz)){
            cell.setCellValue((Boolean)val);
        }else if(Calendar.class.isAssignableFrom(clazz)){
            cell.setCellValue((Calendar)val);
        }else if(RichTextString.class.isAssignableFrom(clazz)){
            cell.setCellValue((RichTextString)val);
        }

    }

    /**
     * 导出excel
     * @param dataList 数据集合
     * @return
     */
    public static HSSFWorkbook exportExcel(List<List<Object>> dataList){
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet();
        exportExcel(sheet,dataList);
        return workBook;
    }

    /**
     * 导出excel到对应sheet中
     * @param sheet
     * @param dataList
     */
    public static void exportExcel(HSSFSheet sheet,List<List<Object>> dataList){
        for(int i=0;i<=dataList.size()-1;i++){
            HSSFRow curRow = sheet.createRow(i);
            List<Object> innerDataList= dataList.get(i);
            for(int j=0;j<=innerDataList.size()-1;j++){
                HSSFCell curCell= curRow.createCell(j);
                inputValue(curCell,innerDataList.get(j));
            }
        }
    }


    /**
     * 根据 表头，方法数组，数据集合 生成excel
     * @param headArr
     * @param methodArr
     * @param dataList
     * @param <T>
     * @return
     */
    public static <T>HSSFWorkbook exportExcel(String[] headArr, String[] methodArr, List<T> dataList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet();
        exportExcel(sheet,headArr,methodArr,dataList);
        return workBook;
    }

    /**
     *
     * @param sheet
     * @param headArr
     * @param methodArr
     * @param dataList
     * @param <T>
     */
    public static <T>void exportExcel(HSSFSheet sheet,String[] headArr, String[] methodArr, List<T> dataList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //生成表格表头
        HSSFRow rowTitle = sheet.createRow(0);
        for (int i = 0; i < headArr.length; i++) {
            rowTitle.createCell(i).setCellValue(headArr[i]);
        }

        for (int i = 0; i <= dataList.size()-1; i++) {
            HSSFRow row = sheet.createRow(i + 1);
            T t=  dataList.get(i);
            for (int j = 0; j <= methodArr.length-1; j++) {
                Method method = t.getClass().getDeclaredMethod(methodArr[j]);
                Object val=method.invoke(t);
                HSSFCell curCell= row.createCell(j);
                inputValue(curCell,val);
            }
        }
    }
}
