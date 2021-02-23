package com.bcd.rdb.dbinfo.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "TablesController",description = "数据库设计")
public class TablesController extends BaseController {

    Logger logger = LoggerFactory.getLogger(TablesController.class);

    @Autowired
    private TablesService tablesService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportSpringDBDesignerExcel", method = RequestMethod.GET)
    @Operation(description = "导出spring数据库设计")
    @ApiResponse(responseCode = "200",description = "导出结果")
    public void exportSpringDBDesignerExcel(
            @Parameter(name = "数据库名称") @RequestParam(value = "dbName", required = true) String dbName,
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
    @Operation(description = "导出数据库设计")
    @ApiResponse(responseCode = "200", description = "导出结果")
    public void exportDBDesignerExcel(
            @Parameter(name = "数据库url(例如:127.0.0.1:3306)") @RequestParam(value = "url", required = true) String url,
            @Parameter(name = "数据库用户名") @RequestParam(value = "username", required = true) String username,
            @Parameter(name = "数据库密码") @RequestParam(value = "password", required = true) String password,
            @Parameter(name = "数据库名称") @RequestParam(value = "dbName", required = true) String dbName,
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
