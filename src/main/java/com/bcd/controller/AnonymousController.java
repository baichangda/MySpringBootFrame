package com.bcd.controller;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.base.util.I18nUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.service.ApiService;
import com.bcd.sys.keys.KeysConst;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.incar.base.Dispatcher;
import com.incar.base.exception.NoHandlerException;
import com.incar.business.MapTrackingStarter;
import io.swagger.annotations.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;


/**
 * Created by Administrator on 2017/6/15.
 */
@RestController
@RequestMapping("/api/anonymous")
public class AnonymousController extends BaseController{

    Dispatcher dispatcher;

    @Autowired
    ApiService apiService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPublicKey",method = RequestMethod.GET)
    @ApiOperation(value = "获取公钥",notes = "获取公钥")
    @ApiResponse(code = 200,message = "公钥信息",response = JsonMessage.class)
    public JsonMessage<String> getPublicKey(){
        return JsonMessage.success(KeysConst.PUBLIC_KEY_BASE64);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getCookie",method = RequestMethod.GET)
    @ApiOperation(value = "获取cookie",notes = "获取cookie")
    @ApiResponse(code = 200,message = "当前浏览器的cookie")
    public JsonMessage<String> getCookie(){
        Subject subject=SecurityUtils.getSubject();
        String cookie=Optional.ofNullable(subject).map(e->e.getSession()).map(e->e.getId()).orElse("").toString();
        return JsonMessage.success(cookie);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/exportApi",method = RequestMethod.GET)
    @ApiOperation(value = "导出所有Api",notes = "导出所有Api")
    @ApiResponse(code = 200,message = "导入的Excel")
    public JsonMessage<String> exportApi(HttpServletResponse response){
        XSSFWorkbook workbook=apiService.exportApi();
        String fileName=I18nUtil.getMessage("AnonymousController.exportApi.fileName")+".xlsx";
        response(workbook,toDateFileName(fileName),response);
        return JsonMessage.success();
    }

    public AnonymousController(){
        dispatcher= MapTrackingStarter.getDispatcher();
        dispatcher.getConfig().withRequestMappingPre("/api/anonymous/ics");
        dispatcher.getDynamicRequestHandler().withJsonReader(obj->{
            try {
                return JsonUtil.GLOBAL_OBJECT_MAPPER.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw BaseRuntimeException.getException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/ics/**",method = RequestMethod.GET)
    public void icsTest(HttpServletRequest request,HttpServletResponse response){

        try {
            dispatcher.dispatch(request, response);
        } catch (NoHandlerException e) {
           throw BaseRuntimeException.getException(e);
        }
    }
}
