package com.bcd.api.controller;

import com.bcd.api.service.ApiService;
import com.bcd.base.controller.BaseController;
import com.bcd.base.util.I18nUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Tag(name = "test",description = "接口/ApiController")
public class ApiController extends BaseController {

    @Autowired
    ApiService apiService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportApi", method = RequestMethod.GET)
    @Operation(description = "导出所有Api")
    @ApiResponse(responseCode = "200", description = "导入的Excel")
    public void exportApi(HttpServletResponse response) {
        try {
            apiService.exportApi(response.getOutputStream(), () -> {
                String fileName = I18nUtil.getMessage("AnonymousController.exportApi.fileName") + ".xlsx";
                doBeforeResponseFile(toDateFileName(fileName), response);
            });
        } catch (IOException e) {
            logger.error("export error", e);
        }
    }
}
