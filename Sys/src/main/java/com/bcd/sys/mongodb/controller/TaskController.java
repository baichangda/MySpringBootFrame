package com.bcd.sys.mongodb.controller;

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
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.mongodb.bean.TaskBean;
import com.bcd.sys.mongodb.service.TaskService;

@SuppressWarnings(value = "unchecked")
//@RestController
@RequestMapping("/api/sys/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;


    /**
     * 查询系统任务列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询系统任务列表",notes = "查询系统任务列表")
    @ApiResponse(code = 200,message = "系统任务列表")
    public JsonMessage<List<TaskBean>> list(
            @ApiParam(value = "任务完成时间开始")
            @RequestParam(value = "finishTimeBegin",required = false) Date finishTimeBegin,
            @ApiParam(value = "任务完成时间截止")
            @RequestParam(value = "finishTimeEnd",required = false) Date finishTimeEnd,
            @ApiParam(value = "失败堆栈信息")
            @RequestParam(value = "stackMessage",required = false) String stackMessage,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务开始时间开始")
            @RequestParam(value = "startTimeBegin",required = false) Date startTimeBegin,
            @ApiParam(value = "任务开始时间截止")
            @RequestParam(value = "startTimeEnd",required = false) Date startTimeEnd,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id,
            @ApiParam(value = "任务信息")
            @RequestParam(value = "message",required = false) String message,
            @ApiParam(value = "任务类型",example="1")
            @RequestParam(value = "type",required = false) Integer type,
            @ApiParam(value = "文件路径")
            @RequestParam(value = "filePaths",required = false) String filePaths,
            @ApiParam(value = "任务状态",example="1")
            @RequestParam(value = "status",required = false) Integer status
            ){
        Condition condition= Condition.and(
            new DateCondition("finishTime",finishTimeBegin, DateCondition.Handler.GE),
            new DateCondition("finishTime",finishTimeEnd, DateCondition.Handler.LE),
            new StringCondition("stackMessage",stackMessage, StringCondition.Handler.ALL_LIKE),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new DateCondition("startTime",startTimeBegin, DateCondition.Handler.GE),
            new DateCondition("startTime",startTimeEnd, DateCondition.Handler.LE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL),
            new StringCondition("message",message, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("type",type, NumberCondition.Handler.EQUAL),
            new StringCondition("filePaths",filePaths, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(taskService.findAll(condition));
    }


    /**
     * 查询系统任务列表
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询系统任务分页",notes = "查询系统任务分页")
    @ApiResponse(code = 200,message = "系统任务分页结果集")
    public JsonMessage<Page<TaskBean>> page(
            @ApiParam(value = "任务完成时间开始")
            @RequestParam(value = "finishTimeBegin",required = false) Date finishTimeBegin,
            @ApiParam(value = "任务完成时间截止")
            @RequestParam(value = "finishTimeEnd",required = false) Date finishTimeEnd,
            @ApiParam(value = "失败堆栈信息")
            @RequestParam(value = "stackMessage",required = false) String stackMessage,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务开始时间开始")
            @RequestParam(value = "startTimeBegin",required = false) Date startTimeBegin,
            @ApiParam(value = "任务开始时间截止")
            @RequestParam(value = "startTimeEnd",required = false) Date startTimeEnd,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id,
            @ApiParam(value = "任务信息")
            @RequestParam(value = "message",required = false) String message,
            @ApiParam(value = "任务类型",example="1")
            @RequestParam(value = "type",required = false) Integer type,
            @ApiParam(value = "文件路径")
            @RequestParam(value = "filePaths",required = false) String filePaths,
            @ApiParam(value = "任务状态",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
            ){
        Condition condition= Condition.and(
            new DateCondition("finishTime",finishTimeBegin, DateCondition.Handler.GE),
            new DateCondition("finishTime",finishTimeEnd, DateCondition.Handler.LE),
            new StringCondition("stackMessage",stackMessage, StringCondition.Handler.ALL_LIKE),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new DateCondition("startTime",startTimeBegin, DateCondition.Handler.GE),
            new DateCondition("startTime",startTimeEnd, DateCondition.Handler.LE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL),
            new StringCondition("message",message, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("type",type, NumberCondition.Handler.EQUAL),
            new StringCondition("filePaths",filePaths, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(taskService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));

    }

    /**
     * 保存系统任务
     * @param task
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存系统任务",notes = "保存系统任务")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "系统任务实体")  @RequestBody TaskBean task){
        taskService.save(task);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除系统任务
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除系统任务",notes = "删除系统任务")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "系统任务id数组") @RequestParam String[] ids){
        taskService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }
}
