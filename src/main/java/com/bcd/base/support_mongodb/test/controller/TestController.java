package com.bcd.base.support_mongodb.test.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/test/test")
@Tag(name = "测试-TestController")
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
    public Result<List<TestBean>> list(
        @Parameter(description = "vin") @RequestParam(required = false) String vin,
        @Parameter(description = "时间开始",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeBegin,
        @Parameter(description = "时间结束",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeEnd,
        @Parameter(description = "主键(唯一标识符,自动生成)") @RequestParam(required = false) String id
    ){
        Condition condition= Condition.and(
           StringCondition.EQUAL("vin",vin),
           DateCondition.BETWEEN("time",timeBegin,timeEnd),
           StringCondition.EQUAL("id",id)
        );
        return Result.success(testService.list(condition));
    }

    /**
     * 查询测试分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(summary="查询测试分页")
    @ApiResponse(responseCode = "200",description = "测试分页结果集")
    public Result<Page<TestBean>> page(
        @Parameter(description = "vin") @RequestParam(required = false) String vin,
        @Parameter(description = "时间开始",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeBegin,
        @Parameter(description = "时间结束",schema = @Schema(type = "integer")) @RequestParam(required = false) Date timeEnd,
        @Parameter(description = "主键(唯一标识符,自动生成)") @RequestParam(required = false) String id,
        @Parameter(description = "分页参数(页数)")  @RequestParam(required = false,defaultValue = "1")Integer pageNum,
        @Parameter(description = "分页参数(页大小)") @RequestParam(required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
           StringCondition.EQUAL("vin",vin),
           DateCondition.BETWEEN("time",timeBegin,timeEnd),
           StringCondition.EQUAL("id",id)
        );
        return Result.success(testService.page(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存测试
     * @param test
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @Operation(summary = "保存测试")
    @ApiResponse(responseCode = "200",description = "保存结果")
    public Result<?> save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "测试实体") @Validated @RequestBody TestBean test){
        testService.save(test);
        return Result.success();
    }


    /**
     * 删除测试
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @Operation(summary = "删除测试")
    @ApiResponse(responseCode = "200",description = "删除结果")
    public Result<?> delete(@Parameter(description = "id数组",example = "100,101,102") @RequestParam String[] ids){
        testService.delete(ids);
        return Result.success();
    }

}
