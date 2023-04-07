package com.bcd.base.support_mongodb.test.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.base.support_mongodb.test.bean.TestBean;
import com.bcd.base.support_mongodb.test.service.TestService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/test/test")
public class TestController extends BaseController {

    @Autowired
    private TestService testService;

    /**
     * 查询测试列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(summary="查询测试列表")
    @ApiResponse(responseCode = "200",description = "测试列表")
    public JsonMessage<List<TestBean>> list(
        @Parameter(description = "vin") @RequestParam(required = false) String vin,
        @Parameter(description = "时间开始",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeBegin,
        @Parameter(description = "时间结束",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeEnd
    ){
        Condition condition= Condition.and(
           StringCondition.EQUAL("vin",vin),
           DateCondition.BETWEEN("time",timeBegin,timeEnd)
        );
        return JsonMessage.success(testService.list(condition));
    }

    /**
     * 查询测试分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(summary="查询测试分页")
    @ApiResponse(responseCode = "200",description = "测试分页结果集")
    public JsonMessage<Page<TestBean>> page(
        @Parameter(description = "vin") @RequestParam(required = false) String vin,
        @Parameter(description = "时间开始",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeBegin,
        @Parameter(description = "时间结束",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeEnd,
        @Parameter(description = "分页参数(页数)")  @RequestParam(required = false,defaultValue = "1")Integer pageNum,
        @Parameter(description = "分页参数(页大小)") @RequestParam(required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
           StringCondition.EQUAL("vin",vin),
           DateCondition.BETWEEN("time",timeBegin,timeEnd)
        );
        return JsonMessage.success(testService.page(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存测试
     * @param test
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @Operation(summary = "保存测试")
    @ApiResponse(responseCode = "200",description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "测试实体") @Validated @RequestBody TestBean test){
        testService.save(test);
        return JsonMessage.success();
    }


    /**
     * 删除测试
     * @param vins
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @Operation(summary = "删除测试")
    @ApiResponse(responseCode = "200",description = "删除结果")
    public JsonMessage delete(@Parameter(description = "测试vin数组") @RequestParam String[] vins){
        testService.delete(vins);
        return JsonMessage.success();
    }

}
