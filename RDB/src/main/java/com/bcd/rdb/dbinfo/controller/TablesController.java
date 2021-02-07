package com.bcd.rdb.dbinfo.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
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

    Logger logger = LoggerFactory.getLogger(TablesController.class);

    @Autowired
    private TablesService tablesService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportSpringDBDesignerExcel", method = RequestMethod.GET)
    @ApiOperation(value = "导出spring数据库设计", notes = "导出spring数据库设计")
    @ApiResponse(code = 200, message = "导出结果")
    public void exportSpringDBDesignerExcel(
            @ApiParam(value = "数据库名称") @RequestParam(value = "dbName", required = true) String dbName,
            HttpServletResponse response) {
        try {
            tablesService.exportSpringDBDesignerExcel(dbName, response.getOutputStream(), () -> {
                String fileName = I18nUtil.getMessage("TablesController.exportDBDesignerExcel.fileName", new Object[]{dbName}) + ".xlsx";
                doBeforeResponseFile(fileName, response);
            });
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportDBDesignerExcel", method = RequestMethod.GET)
    @ApiOperation(value = "导出数据库设计", notes = "导出数据库设计")
    @ApiResponse(code = 200, message = "导出结果")
    public void exportDBDesignerExcel(
            @ApiParam(value = "数据库url(例如:127.0.0.1:3306)") @RequestParam(value = "url", required = true) String url,
            @ApiParam(value = "数据库用户名") @RequestParam(value = "username", required = true) String username,
            @ApiParam(value = "数据库密码") @RequestParam(value = "password", required = true) String password,
            @ApiParam(value = "数据库名称") @RequestParam(value = "dbName", required = true) String dbName,
            HttpServletResponse response) {
        try {
            tablesService.exportDBDesignerExcel(url, username, password, dbName, response.getOutputStream(), () -> {
                String fileName = I18nUtil.getMessage("TablesController.exportDBDesignerExcel.fileName", new Object[]{dbName}) + ".xlsx";
                doBeforeResponseFile(fileName, response);
            });
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
