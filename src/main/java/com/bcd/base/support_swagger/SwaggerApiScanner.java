package com.bcd.base.support_swagger;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.bcd.base.util.ClassUtil;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class SwaggerApiScanner {



    static MyWorkbookWriteHandler workbookWriteHandler = new MyWorkbookWriteHandler();

    /**
     * 获取方法下面所有方法名称和注释的map
     * <p>
     * 规则:
     * 1、先获取所有 {@link RequestParam}和{@link RequestBody}注解的参数名称
     * 2、根据参数名称从 {@link io.swagger.v3.oas.annotations.Parameter} 获取参数对应注释
     *
     * @param method
     * @return
     */
    private static LinkedHashMap<String, ApiParamData> getApiParamsMap(Method method) {
        LinkedHashMap<String, ApiParamData> resultMap = new LinkedHashMap<>();
        //使用spring工具类获取所有参数真实名称
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = discoverer.getParameterNames(method);
        //获取所有swagger注解参数对应注释
        //获取所有参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i <= parameters.length - 1; i++) {
            //获取 RequestParam 注解
            Parameter parameter = parameters[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            String name;
            boolean required;
            if (requestParam == null) {
                //如果 RequestParam 为空,获取 RequestBody 注解
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                if (requestBody == null) {
                    //如果 RequestBody 也为空,跳过此参数
                    continue;
                } else {
                    //如果是 RequestBody 参数,直接取 参数名
                    name = paramNames[i];
                    required = true;
                }
            } else {
                //如果 RequestParam 不为空,判断RequestParam有没有设置别名,没有则设置默认参数名
                name = requestParam.value();
                if (Strings.isNullOrEmpty(name)) {
                    name = paramNames[i];
                }
                required = requestParam.required();
            }
            //获取参数的swagger注解来获取其注释
            io.swagger.v3.oas.annotations.Parameter parameterAnnotation = parameter.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
            String comment = parameterAnnotation == null ? "" : parameterAnnotation.description();
            ApiParamData paramData = new ApiParamData(name, comment, required);
            resultMap.put(name, paramData);
        }
        return resultMap;
    }

    /**
     * 取值顺序如下:
     * 1、
     *
     * @param method
     * @return
     * @see Operation#description()
     */
    private static String getApiComment(Method method) {
        String comment = "";
        Operation operation = method.getAnnotation(Operation.class);
        if (operation != null) {
            comment = operation.description();
        }
        return comment;
    }

    /**
     * 取值如下:
     *
     * @return
     * @see ApiResponse#description()
     */
    private static String getApiResponse(Method method) {
        //1、获取ApiResponse注解值
        ApiResponse apiResponse = method.getAnnotation(ApiResponse.class);
        return apiResponse == null ? "" : apiResponse.description();
    }

    /**
     * 取值如下:
     *
     * @return
     * @see RequestMapping#method()
     */
    private static String[] getApiMethods(Method method) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        return Arrays.stream(methodRequestMapping.method()).map(RequestMethod::toString).toArray(String[]::new);
    }

    /**
     * 取值如下:
     * controller的
     *
     * @param controllerRequestMapping
     * @param methodRequestMapping
     * @return
     * @see RequestMapping#value()
     * +
     * method的
     * @see RequestMapping#value()
     */
    private static String[] getApiPaths(RequestMapping controllerRequestMapping, RequestMapping methodRequestMapping) {
        String[] controllerPaths = controllerRequestMapping.value();
        String[] methodPaths = methodRequestMapping.value();
        List<String> pathList = new ArrayList<>();
        for (String controllerPath : controllerPaths) {
            for (String methodPath : methodPaths) {
                pathList.add(controllerPath + methodPath);
            }
        }
        return pathList.toArray(new String[0]);
    }


    /**
     * 导出系统中所有的Api成excel
     *
     * @return
     */
    public static void scanApiAndExport(OutputStream os, Consumer<List<List>> doBeforeWrite, String... packageNames) throws IOException, ClassNotFoundException {
        //1、获取所有controller
        final List<Class> classesWithAnno = ClassUtil.getClassesWithAnno(RestController.class,packageNames);
        //2、循环controller
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Class controllerClass : classesWithAnno) {
            //获取RequestMapping注解
            RequestMapping controllerRequestMapping = (RequestMapping) controllerClass.getAnnotation(RequestMapping.class);
            if (controllerRequestMapping == null || controllerRequestMapping.value().length == 0) {
                return;
            }
            //获取controller类下面所有方法
            List<Method> methodList = org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation(controllerClass, RequestMapping.class);
            methodList.forEach(method -> {
                //获取方法的 RequestMapping 注解
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                //如果没有注解,跳过此方法
                if (methodRequestMapping == null || methodRequestMapping.value().length == 0) {
                    return;
                }
                //获取api备注
                String comment = getApiComment(method);
                //获取api调用方式
                String[] methods = getApiMethods(method);
                String methodStr = Arrays.stream(methods).reduce((e1, e2) -> e1 + "," + e2).orElse("");
                //获取api路径
                String[] paths = getApiPaths(controllerRequestMapping, methodRequestMapping);
                String pathStr = Arrays.stream(paths).reduce((e1, e2) -> e1 + "\n" + e2).orElse("");
                //获取api参数
                LinkedHashMap<String, ApiParamData> paramMap = getApiParamsMap(method);
                String params = paramMap.values().stream().map(ApiParamData::toString).reduce((e1, e2) -> e1 + "\n" + e2).orElse("");
                //获取返回结果备注
                String response = getApiResponse(method);
                //组装数据
                Map<String, Object> data = new HashMap<>();
                data.put("comment", comment);
                data.put("method", methodStr);
                data.put("path", pathStr);
                data.put("params", params);
                data.put("response", response);
                dataList.add(data);
            });
        }
        //准备导入excel的数据
        List<List> excelList = new ArrayList<>();
        dataList.forEach(e -> {
            String comment = e.get("comment").toString();
            String path = e.get("path").toString();
            String method = e.get("method").toString();
            String params = e.get("params").toString();
            String response = e.get("response").toString();

            excelList.add(Arrays.asList("接口说明", comment));
            excelList.add(Arrays.asList("接口调用方式", method));
            excelList.add(Arrays.asList("接口路径", path));
            excelList.add(Arrays.asList("接口输入", params));
            excelList.add(Arrays.asList("接口返回", response));
            excelList.add(Arrays.asList());
        });

        if (doBeforeWrite != null) {
            doBeforeWrite.accept(excelList);
        }

        //生成excel
        //准备样式
        MyCellWriteHandler cellWriteHandler = new MyCellWriteHandler();
        EasyExcel.write(os).sheet("接口设计")
                .registerWriteHandler(workbookWriteHandler)
                .registerWriteHandler(cellWriteHandler)
                .doWrite(excelList);
    }

    static class MyWorkbookWriteHandler implements WorkbookWriteHandler {
        @Override
        public void beforeWorkbookCreate() {

        }

        @Override
        public void afterWorkbookCreate(WriteWorkbookHolder writeWorkbookHolder) {

        }

        @Override
        public void afterWorkbookDispose(WriteWorkbookHolder writeWorkbookHolder) {
            int sheetNum = writeWorkbookHolder.getCachedWorkbook().getNumberOfSheets();
            for (int i = 0; i < sheetNum; i++) {
                Sheet sheet = writeWorkbookHolder.getCachedWorkbook().getSheetAt(i);
                sheet.setColumnWidth(0, 256 * 15 + 184);
                sheet.setColumnWidth(1, 256 * 100 + 184);
            }
        }
    }

    static class MyCellWriteHandler implements CellWriteHandler {
        CellStyle cellStyle1;
        CellStyle cellStyle2;

        @Override
        public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
            int y = cell.getColumnIndex();
            if (y == 0) {
                //设置标头列样式
                if (cellStyle1 == null) {
                    cellStyle1 = cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    cellStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    cellStyle1.setBorderLeft(BorderStyle.THIN);
                    cellStyle1.setBorderBottom(BorderStyle.THIN);
                    cellStyle1.setBorderTop(BorderStyle.THIN);
                    cellStyle1.setBorderRight(BorderStyle.THIN);
                    cellStyle1.setWrapText(true);
                    cellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
                }
                cell.setCellStyle(cellStyle1);
            } else {
                //设置内容列样式
                if (cellStyle2 == null) {
                    cellStyle2 = cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle2.setBorderLeft(BorderStyle.THIN);
                    cellStyle2.setBorderBottom(BorderStyle.THIN);
                    cellStyle2.setBorderTop(BorderStyle.THIN);
                    cellStyle2.setBorderRight(BorderStyle.THIN);
                    cellStyle2.setWrapText(true);

                }
                cell.setCellStyle(cellStyle2);
            }
        }
    }

    @Setter
    @Getter
    static class ApiParamData {
        private String name;
        private String desc;
        private boolean required;

        public ApiParamData(String name, String desc, boolean required) {
            this.name = name;
            this.desc = desc;
            this.required = required;
        }

        public String toString() {
            return name + " : " + desc + (required ? "(必填)" : "(非必填)");
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try(final OutputStream os = Files.newOutputStream(Paths.get("/Users/baichangda/msbf.xlsx"))){
            scanApiAndExport(os,null,"com.bcd");
        }
    }
}