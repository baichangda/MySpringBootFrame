package com.bcd.rdb.dbinfo.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import io.swagger.annotations.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/tables")
public class TablesController extends BaseController{

    @Autowired
    private TablesService tablesService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportDBDesigner",method = RequestMethod.GET)
    @ApiOperation(value = "导出数据库设计",notes = "导出数据库设计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "dbName",value = "数据库名称",dataType = "String")
    })
    @ApiResponse(code = 200,message = "导出结果")
    public JsonMessage<Object> exportDBDesigner(
            @RequestParam(value="dbName",required = false) String dbName,
            HttpServletResponse response){
        Workbook workbook= tablesService.exportDBDesigner(dbName);
        String fileName=I18nUtil.getMessage("TablesController.exportDBDesigner.fileName",new Object[]{dbName})+".xlsx";
        response(workbook,toDateFileName(fileName),response);
        return JsonMessage.success();
    }
}
