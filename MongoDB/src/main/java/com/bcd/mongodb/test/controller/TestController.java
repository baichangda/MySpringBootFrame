package com.bcd.mongodb.test.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.mongodb.test.bean.TestBean;
import com.bcd.mongodb.test.service.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings(value = "unchecked")
@Api(tags = "测试/Test")
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
    @ApiOperation(value="查询测试列表",notes = "查询测试列表")
    @ApiResponse(code = 200,message = "测试列表")
    public JsonMessage<List<TestBean>> list(
        @ApiParam(value = "班线code(长度20)") @RequestParam(required = false) String postlinecode,
        @ApiParam(value = "班线名称(长度30)") @RequestParam(required = false) String postlinename,
        @ApiParam(value = "主键(唯一标识符,自动生成)(不需要赋值)") @RequestParam(required = false) String id
    ){
        Condition condition= Condition.and(
           new StringCondition("postlinecode",postlinecode),
           new StringCondition("postlinename",postlinename),
           new StringCondition("id",id)
        );
        return JsonMessage.success().withData(testService.findAll(condition));
    }

    /**
     * 查询测试分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询测试分页",notes = "查询测试分页")
    @ApiResponse(code = 200,message = "测试分页结果集")
    public JsonMessage<Page<TestBean>> page(
        @ApiParam(value = "班线code(长度20)") @RequestParam(required = false) String postlinecode,
        @ApiParam(value = "班线名称(长度30)") @RequestParam(required = false) String postlinename,
        @ApiParam(value = "主键(唯一标识符,自动生成)(不需要赋值)") @RequestParam(required = false) String id,
        @ApiParam(value = "分页参数(页数)",defaultValue = "1")  @RequestParam(required = false,defaultValue = "1")Integer pageNum,
        @ApiParam(value = "分页参数(页大小)",defaultValue = "20") @RequestParam(required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
           new StringCondition("postlinecode",postlinecode),
           new StringCondition("postlinename",postlinename),
           new StringCondition("id",id)
        );
        return JsonMessage.success().withData(testService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存测试
     * @param test
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存测试",notes = "保存测试")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "测试实体") @Validated @RequestBody TestBean test){
        testService.save(test);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除测试
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除测试",notes = "删除测试")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "测试id数组") @RequestParam String[] ids){
        testService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
