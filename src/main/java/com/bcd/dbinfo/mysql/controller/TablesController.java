package com.bcd.dbinfo.mysql.controller;

import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.dbinfo.mysql.service.TablesService;
import io.swagger.annotations.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/tables")
public class TablesController {

    @Autowired
    private TablesService tablesService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportDBDesigner",method = RequestMethod.GET)
    @ApiOperation(value = "导出数据库设计",notes = "导出数据库设计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dbName",value = "数据库名称",dataType = "String",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "是否导出成功")})
    public JsonMessage<Object> exportDBDesigner(HttpServletRequest request,
                                           @RequestParam(value="dbName",required = false) String dbName,
                                                HttpServletResponse response){
        HSSFWorkbook workbook= tablesService.exportDBDesigner(dbName);
        response.setHeader("conent-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=alarmReport" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xls");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return JsonMessage.successed(null,"导出成功");
    }
}
