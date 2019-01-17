package com.bcd.mongodb.test.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.mongodb.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.bcd.mongodb.test.bean.TestBean;
import com.bcd.mongodb.test.service.TestService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/test/test")
public class TestController extends BaseController {

    @Autowired
    private TestService testService;


    /**
     * 查询测试类列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询测试类列表",notes = "查询测试类列表")
    @ApiResponse(code = 200,message = "测试类列表")
    public JsonMessage<List<TestBean>> list(
            @ApiParam(value = "班线code")
            @RequestParam(value = "postlinecode",required = false) String postlinecode,
            @ApiParam(value = "班线名称")
            @RequestParam(value = "postlinename",required = false) String postlinename,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id
            ){
        Condition condition= Condition.and(
            new StringCondition("postlinecode",postlinecode, StringCondition.Handler.ALL_LIKE),
            new StringCondition("postlinename",postlinename, StringCondition.Handler.ALL_LIKE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL)
        );
        return JsonMessage.success(testService.findAll(condition));
    }


    /**
     * 查询测试类列表
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询测试类分页",notes = "查询测试类分页")
    @ApiResponse(code = 200,message = "测试类分页结果集")
    public JsonMessage<Page<TestBean>> page(
            @ApiParam(value = "班线code")
            @RequestParam(value = "postlinecode",required = false) String postlinecode,
            @ApiParam(value = "班线名称")
            @RequestParam(value = "postlinename",required = false) String postlinename,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
            ){
        Condition condition= Condition.and(
            new StringCondition("postlinecode",postlinecode, StringCondition.Handler.ALL_LIKE),
            new StringCondition("postlinename",postlinename, StringCondition.Handler.ALL_LIKE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL)
        );
        return JsonMessage.success(testService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));

    }

    /**
     * 保存测试类
     * @param test
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存测试类",notes = "保存测试类")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "测试类实体")  @RequestBody TestBean test){
        testService.save(test);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除测试类
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除测试类",notes = "删除测试类")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "测试类id数组") @RequestParam String[] ids){
        testService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }
}
