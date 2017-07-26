package com.base.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ExportExcelUtil {


    /**
     * 导出excel
     *
     * @param dataList 数据集合
     * @return
     */
    public static HSSFWorkbook exportExcel(List<List<String>> dataList) {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet();
        for (int i = 0; i <= dataList.size() - 1; i++) {
            HSSFRow curRow = sheet.createRow(i);
            List<String> innerDataList = dataList.get(i);
            for (int j = 0; j <= innerDataList.size() - 1; j++) {
                HSSFCell curCell = curRow.createCell(j);
                String val = innerDataList.get(j);
                curCell.setCellValue(val);
            }
        }
        return workBook;
    }


    /**
     * 根据 表头，方法数组，数据集合 生成excel
     *
     * @param headArr
     * @param methodArr
     * @param dataList
     * @param <T>
     * @return
     */
    public static <T> HSSFWorkbook exportExcel(String[] headArr, String[] methodArr, List<T> dataList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet();
        //生成表格表头
        HSSFRow rowTitle = sheet.createRow(0);
        for (int i = 0; i < headArr.length; i++) {
            rowTitle.createCell(i).setCellValue(headArr[i]);
        }

        for (int i = 0; i <= dataList.size() - 1; i++) {
            HSSFRow row = sheet.createRow(i + 1);
            T t = dataList.get(i);
            for (int j = 0; j <= methodArr.length - 1; j++) {
                Method method = t.getClass().getDeclaredMethod(methodArr[j]);
                Object val = method.invoke(t);
                row.createCell(j).setCellValue(val == null ? "" : val.toString());
            }
        }
        return workBook;
    }
}
