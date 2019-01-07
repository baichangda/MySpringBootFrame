package com.bcd.service;

import com.bcd.base.util.ExcelUtil;
import com.bcd.base.util.ProxyUtil;
import com.bcd.base.util.SpringUtil;
import io.swagger.annotations.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class ApiService {
    /**
     * 获取方法下面所有方法名称和注释的map
     * RequestParam 参数名称取值顺序:
     * 1、
     * @see RequestParam#value()
     * 2、
     * @see Parameter#getName()
     *
     * RequestBody 参数取值:
     * @see Parameter#getName()
     *
     * 参数注释取值:
     * @see ApiParam#value()
     *
     * @param method
     * @return
     */
    public LinkedHashMap<String,String> getApiParamsMap(Method method){
        LinkedHashMap<String,String> resultMap=new LinkedHashMap<>();
        //1、使用spring工具类获取所有参数真实名称
        LocalVariableTableParameterNameDiscoverer discoverer=new LocalVariableTableParameterNameDiscoverer();
        String [] paramNames=discoverer.getParameterNames(method);
        //2、获取所有参数
        Parameter[] parameters= method.getParameters();
        for (int i=0;i<=parameters.length-1;i++) {
            //2.1、获取 RequestParam 注解
            Parameter parameter=parameters[i];
            RequestParam requestParam=parameter.getAnnotation(RequestParam.class);
            String name;
            if(requestParam==null){
                //2.2、如果 RequestParam 为空,获取 RequestBody 注解
                RequestBody requestBody=parameter.getAnnotation(RequestBody.class);
                if(requestBody==null){
                    //2.2.1、如果 RequestBody 也为空,跳过此参数
                    continue;
                }else{
                    //2.2.2、如果是 RequestBody 参数,直接过去 参数名
                    name=paramNames[i];
                }
            }else{
                //2.3、如果 RequestParam 不为空,判断RequestParam有没有设置别名,没有则设置默认参数名
                name=requestParam.value();
                if(StringUtils.isEmpty(name)){
                    name=paramNames[i];
                }
            }
            //2.4、获取参数的swagger注解来获取其注释
            ApiParam apiParam=parameter.getAnnotation(ApiParam.class);
            String comment=apiParam==null?"":apiParam.value();
            resultMap.put(name,comment);
        }
        return resultMap;
    }

    /**
     * 取值顺序如下:
     * 1、
     * @see ApiOperation#notes()
     * 2、
     * @see ApiOperation#value()
     * @param method
     * @return
     */
    public String getApiComment(Method method){
        String comment="";
        ApiOperation apiOperation=method.getAnnotation(ApiOperation.class);
        if(apiOperation!=null){
            comment=apiOperation.notes();
            if(StringUtils.isEmpty(comment)){
                comment=apiOperation.value();
            }
        }
        return comment;
    }

    /**
     * 取值如下:
     * @see ApiResponse#message()
     * @return
     */
    public String getApiResponse(Method method){
        ApiResponse apiResponse= method.getAnnotation(ApiResponse.class);
        return apiResponse==null?"":apiResponse.message();
    }

    /**
     * 取值如下:
     * @see RequestMapping#method()
     * @return
     */
    public String[] getApiMethods(Method method){
        RequestMapping methodRequestMapping=method.getAnnotation(RequestMapping.class);
        return Arrays.stream(methodRequestMapping.method()).map(RequestMethod::toString).toArray(len->new String[len]);
    }

    /**
     * 取值如下:
     * controller的
     * @see RequestMapping#value()
     * +
     * method的
     * @see RequestMapping#value()
     * @param controllerRequestMapping
     * @param methodRequestMapping
     * @return
     */
    public String[] getApiPaths(RequestMapping controllerRequestMapping,RequestMapping methodRequestMapping){
       String[] controllerPaths= controllerRequestMapping.value();
       String[] methodPaths= methodRequestMapping.value();
       List<String> pathList=new ArrayList<>();
        for (String controllerPath : controllerPaths) {
            for (String methodPath : methodPaths) {
                pathList.add(controllerPath+methodPath);
            }
        }
        return pathList.stream().toArray(len->new String[len]);
    }


    /**
     * 导出系统中所有的Api成excel
     * @return
     */
    public XSSFWorkbook exportApi(){
        //1、获取所有controller
        Map<String,Object> controllerMap= SpringUtil.applicationContext.getBeansWithAnnotation(RestController.class);
        //2、循环controller
        List<Map<String,Object>> dataList=new ArrayList<>();
        controllerMap.values().forEach(controller->{
            //2.1、获取每个controller的原始类
            Class controllerClass= ProxyUtil.getSource(controller).getClass();
            //2.2、获取RequestMapping注解
            RequestMapping controllerRequestMapping=(RequestMapping) controllerClass.getAnnotation(RequestMapping.class);
            if(controllerRequestMapping==null||controllerRequestMapping.value().length==0){
                return;
            }
            //2.3、获取controller类下面所有方法
            List<Method> methodList= org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation(controllerClass,RequestMapping.class);
            methodList.forEach(method->{
                //2.3.1、获取方法的 RequestMapping 注解
                RequestMapping methodRequestMapping=method.getAnnotation(RequestMapping.class);
                //2.3.2、如果没有注解,跳过此方法
                if(methodRequestMapping==null||methodRequestMapping.value().length==0){
                    return;
                }
                //2.3.3、获取api备注
                String comment=getApiComment(method);
                //2.3.4、获取api调用方式
                String [] methods=getApiMethods(method);
                String methodStr= Arrays.stream(methods).reduce((e1,e2)->e1+","+e2).orElse("");
                //2.3.5、获取api路径
                String [] paths=getApiPaths(controllerRequestMapping,methodRequestMapping);
                String pathStr= Arrays.stream(paths).reduce((e1,e2)->e1+"\n"+e2).orElse("");
                //2.3.6、获取api参数
                LinkedHashMap<String,String> paramMap=getApiParamsMap(method);
                String params=paramMap.entrySet().stream().map(e->e.getKey()+" : "+e.getValue()).reduce((e1,e2)->e1+"\n"+e2).orElse("");
                //2.3.7、获取返回结果备注
                String response=getApiResponse(method);
                //2.3.7、组装数据
                Map<String,Object> data=new HashMap<>();
                data.put("comment",comment);
                data.put("method",methodStr);
                data.put("path",pathStr);
                data.put("params",params);
                data.put("response",response);
                dataList.add(data);
            });
        });
        //3、准备导入excel的数据
        List<List> excelList=new ArrayList<>();
        dataList.forEach(e->{
            String comment=e.get("comment").toString();
            String path=e.get("path").toString();
            String method=e.get("method").toString();
            String params=e.get("params").toString();
            String response=e.get("response").toString();

            excelList.add(Arrays.asList("接口说明",comment));
            excelList.add(Arrays.asList("接口调用方式",method));
            excelList.add(Arrays.asList("接口路径",path));
            excelList.add(Arrays.asList("接口输入",params));
            excelList.add(Arrays.asList("接口返回",response));
            excelList.add(Arrays.asList());
        });

        //4、生成excel
        //4.1、准备样式
        CellStyle[] cellStyle1=new CellStyle[]{null};
        CellStyle[] cellStyle2=new CellStyle[]{null};
        XSSFWorkbook workbook= ExcelUtil.exportExcel_2007(excelList,(cell, val)->{
            int y=cell.getColumnIndex();
            if(y==0){
                //4.2、设置标头列样式
                if(cellStyle1[0]==null){
                    cellStyle1[0]=cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle1[0].setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    cellStyle1[0].setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    cellStyle1[0].setBorderLeft(BorderStyle.THIN);
                    cellStyle1[0].setBorderBottom(BorderStyle.THIN);
                    cellStyle1[0].setBorderTop(BorderStyle.THIN);
                    cellStyle1[0].setBorderRight(BorderStyle.THIN);
                    cellStyle1[0].setWrapText(true);
                    cellStyle1[0].setVerticalAlignment(VerticalAlignment.CENTER);
                }
                cell.setCellStyle(cellStyle1[0]);
            }else{
                //4.3、设置内容列样式
                if(cellStyle2[0]==null){
                    cellStyle2[0]=cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle2[0].setBorderLeft(BorderStyle.THIN);
                    cellStyle2[0].setBorderBottom(BorderStyle.THIN);
                    cellStyle2[0].setBorderTop(BorderStyle.THIN);
                    cellStyle2[0].setBorderRight(BorderStyle.THIN);
                    cellStyle2[0].setWrapText(true);

                }
                cell.setCellStyle(cellStyle2[0]);
            }
            ExcelUtil.inputValue(cell,val);
        });
        //4.4、设置列宽
        workbook.getSheetAt(0).setColumnWidth(0,256*15+184);
        workbook.getSheetAt(0).setColumnWidth(1,256*100+184);
        return workbook;
    }
}
