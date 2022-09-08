package com.bcd.base.support_excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;

public class Td {
    public int colSpan = 1;
    public int rowSpan = 1;

    //字体
    public IndexedColors fontColor = IndexedColors.BLACK;
    public boolean fontBold = false;
    //背景颜色
    public IndexedColors bgColor = IndexedColors.WHITE;

    //边框样式
    public BorderStyle borderStyle = BorderStyle.THIN;
    public IndexedColors borderColor = IndexedColors.BLACK;

    //水平对齐
    public HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    //垂直对齐
    public VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;


    //数据内容格式
    public String dataFormat = "@";

    public String content;

    private Td() {
    }

    public static Td newTd(String content) {
        Td td = new Td();
        td.content = content;
        return td;
    }

    public static Td newTd(String content,int rowSpan,int colSpan) {
        Td td = new Td();
        td.content = content;
        td.rowSpan = rowSpan;
        td.colSpan = colSpan;
        return td;
    }


}
