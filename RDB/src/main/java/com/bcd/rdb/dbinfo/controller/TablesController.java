package com.bcd.rdb.dbinfo.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/tables")
@Api(tags = "数据库设计/TablesController")
public class TablesController extends BaseController {

    Logger logger= LoggerFactory.getLogger(TablesController.class);

    @Autowired
    private TablesService tablesService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportDBDesignerExcel", method = RequestMethod.GET)
    @ApiOperation(value = "导出数据库设计", notes = "导出数据库设计")
    @ApiResponse(code = 200, message = "导出结果")
    public void exportDBDesignerExcel(
            @ApiParam(value = "数据库名称") @RequestParam(value = "dbName", required = true) String dbName,
            HttpServletResponse response) {
        try {
            String fileName = I18nUtil.getMessage("TablesController.exportDBDesignerExcel.fileName", new Object[]{dbName}) + ".xlsx";
            doBeforeResponseFile(fileName,response);
            tablesService.exportDBDesignerExcel(dbName,response.getOutputStream());
        } catch (IOException e) {
            logger.error("export error",e);
        }

    }
}
