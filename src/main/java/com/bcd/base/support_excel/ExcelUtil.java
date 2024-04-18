package com.bcd.base.support_excel;

import com.bcd.base.exception.BaseRuntimeException;
import io.netty.buffer.ByteBufUtil;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static boolean isExcel(InputStream is) {
        try {
            FileMagic fileMagic = FileMagic.valueOf(FileMagic.prepareToCheckMagic(is));
            switch (fileMagic) {
                case OOXML, OLE2 -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isXlsx(InputStream is) {
        try {
            FileMagic fileMagic = FileMagic.valueOf(FileMagic.prepareToCheckMagic(is));
            return fileMagic == FileMagic.OOXML;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isXls(InputStream is) {
        try {
            FileMagic fileMagic = FileMagic.valueOf(FileMagic.prepareToCheckMagic(is));
            return fileMagic == FileMagic.OLE2;
        } catch (IOException ex) {
            return false;
        }
    }

    private static int[] checkRule(List<List<Td>> table) {
        final int size = table.size();
        int colNum = 0;
        int all = 0;
        for (int i = 0; i < size; i++) {
            for (Td td : table.get(i)) {
                if (i == 0) {
                    colNum += td.colSpan;
                }
                all += td.rowSpan * td.colSpan;
            }
        }
        if (colNum * size != all) {
            throw BaseRuntimeException.get("data error");
        }
        return new int[]{size, colNum};
    }


    static class WriteExcelContext {
        final XSSFWorkbook workbook;

        final XSSFDataFormat dataFormat;
        final Map<String, XSSFCellStyle> cellStyleMap = new HashMap<>();
        final Map<String, XSSFFont> fontMap = new HashMap<>();

        public WriteExcelContext(XSSFWorkbook workbook) {
            this.workbook = workbook;
            this.dataFormat = workbook.createDataFormat();
        }
    }

    /**
     * 模拟html table、tr、td形式写入成xlsx
     *
     * @param table
     * @param os
     */
    public static void writeExcel_xlsx(List<List<Td>> table, OutputStream os) {
        final int[] ints = checkRule(table);
        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            WriteExcelContext writeExcelContext = new WriteExcelContext(workbook);
            final XSSFSheet sheet = workbook.createSheet();
            final int rowNum = ints[0];
            final int colNum = ints[1];
            final Td[][] arr = new Td[rowNum][colNum];
            for (int i = 0; i < rowNum; i++) {
                final List<Td> tr = table.get(i);
                final XSSFRow row = sheet.createRow(i);
                int tdIndex = 0;
                final Td[] cols = arr[i];
                for (int j = 0; j < colNum; j++) {
                    final XSSFCell cell = row.createCell(j);
                    if (cols[j] == null) {
                        final Td td = tr.get(tdIndex++);
                        if (td.rowSpan != 1 || td.colSpan != 1) {
                            for (int x = 0; x < td.rowSpan; x++) {
                                for (int y = 0; y < td.colSpan; y++) {
                                    if (x == 0 && y == 0) {
                                        continue;
                                    }
                                    arr[i + x][j + y] = td;
                                }
                            }
                            sheet.addMergedRegion(new CellRangeAddress(i, i + td.rowSpan - 1, j, j + td.colSpan - 1));
                        }
                        setCell(cell, td, writeExcelContext);
                    } else {
                        setCell(cell, cols[j], writeExcelContext);
                    }
                }
            }
            workbook.write(os);
        } catch (IOException ex) {
            throw BaseRuntimeException.get(ex);
        }
    }

    private static void setCell(XSSFCell cell, Td td, WriteExcelContext writeExcelContext) {
        cell.setCellValue(td.content);
        final CellStyle cellStyle = writeExcelContext.cellStyleMap.computeIfAbsent(td.fontColor.index + "," + td.bgColor.index, k -> {
            final XSSFCellStyle xssfCellStyle = writeExcelContext.workbook.createCellStyle();
            xssfCellStyle.setFillForegroundColor(td.bgColor.index);
            xssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            final XSSFFont xssfFont = writeExcelContext.fontMap.computeIfAbsent(td.fontColor.index + "", i -> {
                final XSSFFont font = writeExcelContext.workbook.createFont();
                font.setColor(td.fontColor.index);
                font.setBold(td.fontBold);
                return font;
            });
            xssfCellStyle.setFont(xssfFont);
            xssfCellStyle.setBorderTop(td.borderStyle);
            xssfCellStyle.setTopBorderColor(td.borderColor.index);
            xssfCellStyle.setBorderRight(td.borderStyle);
            xssfCellStyle.setRightBorderColor(td.borderColor.index);
            xssfCellStyle.setBorderBottom(td.borderStyle);
            xssfCellStyle.setBottomBorderColor(td.borderColor.index);
            xssfCellStyle.setBorderLeft(td.borderStyle);
            xssfCellStyle.setLeftBorderColor(td.borderColor.index);
            xssfCellStyle.setAlignment(td.horizontalAlignment);
            xssfCellStyle.setVerticalAlignment(td.verticalAlignment);
            xssfCellStyle.setDataFormat(writeExcelContext.dataFormat.getFormat(td.dataFormat));
            return xssfCellStyle;
        });
        cell.setCellStyle(cellStyle);
    }

    public static void main(String[] args) {
        System.out.println(ByteBufUtil.hexDump("%PDF".getBytes(StandardCharsets.UTF_8)));
//        List<List<Td>> table = new ArrayList<>();
//        final List<Td> tr1 = new ArrayList<>();
//        final List<Td> tr2 = new ArrayList<>();
//        final List<Td> tr3 = new ArrayList<>();
//        tr1.add(Td.newTd("1-1"));
//        tr1.add(Td.newTd("1-2", 1, 2));
//        tr1.add(Td.newTd("1-3"));
//
//        tr1.add(Td.newTd("1-3"));
//        tr1.add(Td.newTd("1-3", 1, 3));
//        tr1.add(Td.newTd("1-3"));
//
//        tr2.add(Td.newTd("2-1", 2, 2));
//        tr2.add(Td.newTd("2-2"));
//        tr2.add(Td.newTd("2-2", 2, 1));
//
//        tr2.add(Td.newTd("1-3"));
//        tr2.add(Td.newTd("1-3"));
//        tr2.add(Td.newTd("1-3", 2, 1));
//        tr2.add(Td.newTd("1-3"));
//        tr2.add(Td.newTd("1-3"));
//
//        tr3.add(Td.newTd("3-1"));
//
//        tr3.add(Td.newTd("1-3"));
//        tr3.add(Td.newTd("1-3"));
//        tr3.add(Td.newTd("1-3", 1, 2));
//        table.add(tr1);
//        table.add(tr2);
//        table.add(tr3);
//        try (final OutputStream os = Files.newOutputStream(Paths.get("test55.xlsx"))) {
//            writeExcel_xlsx(table, os);
//        } catch (IOException ex) {
//            throw BaseRuntimeException.get(ex);
//        }
    }
}
